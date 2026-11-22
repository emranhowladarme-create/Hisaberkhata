'use strict';

const http = require('http');
const crypto = require('crypto');

const OTP_EXPIRY_MS = 60 * 1000;          // 1 minute expiry
const RESEND_COOLDOWN_MS = 30 * 1000;     // 30 seconds before resend allowed
const MAX_VERIFY_ATTEMPTS = 5;
const PHONE_RATE_WINDOW_MS = 15 * 60 * 1000;
const MAX_SENDS_PER_PHONE_WINDOW = 5;
const IP_RATE_WINDOW_MS = 15 * 60 * 1000;
const MAX_SENDS_PER_IP_WINDOW = 15;

function normalizeBdPhone(rawPhone) {
  if (typeof rawPhone !== 'string') return null;
  const converted = rawPhone
    .replace(/[\u09E6-\u09EF]/g, (ch) => String.fromCharCode(48 + (ch.charCodeAt(0) - 0x09e6)))
    .replace(/[^\d+]/g, '');

  let local10 = null;
  if (/^\+8801[3-9]\d{8}$/.test(converted)) {
    local10 = converted.slice(4);
  } else if (/^8801[3-9]\d{8}$/.test(converted)) {
    local10 = converted.slice(3);
  } else if (/^01[3-9]\d{8}$/.test(converted)) {
    local10 = converted.slice(1);
  } else if (/^1[3-9]\d{8}$/.test(converted)) {
    local10 = converted;
  }

  return local10 ? `+880${local10}` : null;
}

function generateOtpCode(length = 6) {
  const otpLength = length === 4 ? 4 : 6;
  const min = otpLength === 4 ? 1000 : 100000;
  const max = otpLength === 4 ? 10000 : 1000000;
  return String(crypto.randomInt(min, max));
}

function hashOtp(secret, challengeId, phoneE164, otp) {
  return crypto
    .createHmac('sha256', secret)
    .update(`${challengeId}:${phoneE164}:${otp}`)
    .digest('hex');
}

function constantTimeHexEqual(a, b) {
  if (typeof a !== 'string' || typeof b !== 'string' || a.length !== b.length) return false;
  return crypto.timingSafeEqual(Buffer.from(a, 'utf8'), Buffer.from(b, 'utf8'));
}

function buildSmsRetrieverMessage(otp, appHash) {
  const hashLine = appHash && /^[A-Za-z0-9+/=_-]{8,16}$/.test(appHash) ? `\n${appHash}` : '';
  return `<#> হিসাবের খাতা: আপনার যাচাইকরণ কোড ${otp}। মেয়াদ ১ মিনিট। কোডটি কারো সাথে শেয়ার করবেন না।${hashLine}`;
}

async function sendViaConfiguredSmsGateway({ phoneE164, message }) {
  const provider = (process.env.SMS_PROVIDER || '').toUpperCase();
  if (!provider) {
    throw new Error('SMS_PROVIDER is not configured on backend server');
  }

  if (provider === 'SSL_WIRELESS') {
    const endpoint = process.env.SSL_SMS_URL || 'https://smsplus.sslwireless.com/api/v3/send-sms';
    const resp = await fetch(endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        api_token: process.env.SSL_API_TOKEN,
        sid: process.env.SSL_SID,
        msisdn: phoneE164.replace(/^\+/, ''),
        sms: message,
        csms_id: crypto.randomUUID(),
      }),
    });
    if (!resp.ok) throw new Error(`SSL Wireless HTTP ${resp.status}`);
    return;
  }

  if (provider === 'ALPHA_SMS') {
    const params = new URLSearchParams({
      api_key: process.env.ALPHA_API_KEY || '',
      msg: message,
      to: phoneE164.replace(/^\+/, ''),
    });
    const resp = await fetch('https://api.sms.net.bd/sendsms', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: params.toString(),
    });
    if (!resp.ok) throw new Error(`Alpha SMS HTTP ${resp.status}`);
    return;
  }

  if (provider === 'TWILIO') {
    const sid = process.env.TWILIO_ACCOUNT_SID || '';
    const token = process.env.TWILIO_AUTH_TOKEN || '';
    const from = process.env.TWILIO_FROM_NUMBER || '';
    const auth = Buffer.from(`${sid}:${token}`).toString('base64');
    const params = new URLSearchParams({ To: phoneE164, From: from, Body: message });
    const resp = await fetch(`https://api.twilio.com/2010-04-01/Accounts/${sid}/Messages.json`, {
      method: 'POST',
      headers: {
        Authorization: `Basic ${auth}`,
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: params.toString(),
    });
    if (!resp.ok) throw new Error(`Twilio HTTP ${resp.status}`);
    return;
  }

  if (provider === 'CUSTOM_WEBHOOK') {
    const url = process.env.SMS_WEBHOOK_URL;
    if (!url) throw new Error('SMS_WEBHOOK_URL missing');
    const resp = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(process.env.SMS_WEBHOOK_BEARER ? { Authorization: `Bearer ${process.env.SMS_WEBHOOK_BEARER}` } : {}),
      },
      body: JSON.stringify({ to: phoneE164, message }),
    });
    if (!resp.ok) throw new Error(`Custom webhook HTTP ${resp.status}`);
    return;
  }

  throw new Error(`Unsupported SMS_PROVIDER: ${provider}`);
}

function createOtpServer(options = {}) {
  const hmacSecret = options.hmacSecret || process.env.OTP_HMAC_SECRET || crypto.randomBytes(32).toString('hex');
  const nowFn = options.nowFn || (() => Date.now());
  const smsSender = options.smsSender || sendViaConfiguredSmsGateway;
  const customTokenMinter = options.customTokenMinter || null;

  // In-memory store (stores ONLY salted HMAC hashes, NEVER plain-text OTPs)
  const challenges = new Map();
  const phoneSendLog = new Map();
  const ipSendLog = new Map();
  const paymentOrders = new Map();
  const allowedProviders = new Set(['bkash', 'nagad', 'rocket']);

  function pruneAndCheckRate(map, key, windowMs, maxCount, now) {
    const list = (map.get(key) || []).filter((ts) => now - ts < windowMs);
    if (list.length >= maxCount) {
      map.set(key, list);
      return false;
    }
    list.push(now);
    map.set(key, list);
    return true;
  }

  function lastSendTimestamp(phoneE164) {
    const list = phoneSendLog.get(phoneE164) || [];
    return list.length > 0 ? list[list.length - 1] : 0;
  }

  function sendJson(res, status, data) {
    const body = JSON.stringify(data);
    res.writeHead(status, {
      'Content-Type': 'application/json; charset=utf-8',
      'Cache-Control': 'no-store',
    });
    res.end(body);
  }

  function readBody(req) {
    return new Promise((resolve, reject) => {
      let data = '';
      req.on('data', (chunk) => {
        data += chunk;
        if (data.length > 16 * 1024) {
          reject(new Error('PAYLOAD_TOO_LARGE'));
          req.destroy();
        }
      });
      req.on('end', () => {
        try {
          resolve(data ? JSON.parse(data) : {});
        } catch {
          reject(new Error('INVALID_JSON'));
        }
      });
      req.on('error', reject);
    });
  }

  return http.createServer(async (req, res) => {
    const urlPath = (req.url || '').split('?')[0];
    const now = nowFn();

    if (req.method === 'GET' && urlPath === '/health') {
      return sendJson(res, 200, { ok: true, service: 'hisaber-khata-otp-gateway' });
    }

    if (req.method !== 'POST') {
      return sendJson(res, 405, { code: 'METHOD_NOT_ALLOWED', message: 'Method not allowed' });
    }

    let body;
    try {
      body = await readBody(req);
    } catch {
      return sendJson(res, 400, { code: 'BAD_REQUEST', message: 'Invalid JSON request body' });
    }

    const clientIp = req.headers['x-forwarded-for']?.split(',')[0]?.trim() || req.socket.remoteAddress || 'unknown';

    if (urlPath === '/api/auth/otp/send' || urlPath === '/api/auth/otp/resend') {
      const phoneE164 = normalizeBdPhone(body.phone);
      if (!phoneE164) {
        return sendJson(res, 400, { code: 'INVALID_PHONE', message: 'ভুল মোবাইল নম্বর।' });
      }

      const prevSentAt = lastSendTimestamp(phoneE164);
      if (prevSentAt > 0 && now - prevSentAt < RESEND_COOLDOWN_MS) {
        const waitSec = Math.ceil((RESEND_COOLDOWN_MS - (now - prevSentAt)) / 1000);
        return sendJson(res, 429, {
          code: 'RESEND_COOLDOWN',
          retryAfterSec: waitSec,
          message: `অনুগ্রহ করে ${waitSec} সেকেন্ড পর আবার OTP পাঠান।`,
        });
      }

      if (!pruneAndCheckRate(phoneSendLog, phoneE164, PHONE_RATE_WINDOW_MS, MAX_SENDS_PER_PHONE_WINDOW, now) ||
          !pruneAndCheckRate(ipSendLog, clientIp, IP_RATE_WINDOW_MS, MAX_SENDS_PER_IP_WINDOW, now)) {
        return sendJson(res, 429, {
          code: 'RATE_LIMITED',
          message: 'অতিরিক্ত OTP পাঠানোর চেষ্টা হয়েছে।',
        });
      }

      const requestedLength = Number(body.otpLength) === 4 ? 4 : 6;
      const otp = generateOtpCode(requestedLength);
      const challengeId = crypto.randomUUID();
      const otpHash = hashOtp(hmacSecret, challengeId, phoneE164, otp);
      const appHash = typeof body.appHash === 'string' ? body.appHash.trim() : '';
      const smsBody = buildSmsRetrieverMessage(otp, appHash);

      try {
        await smsSender({ phoneE164, message: smsBody, challengeId });
      } catch (err) {
        return sendJson(res, 502, {
          code: 'OTP_SEND_FAILED',
          message: 'OTP পাঠানো যায়নি। অনুগ্রহ করে পরে চেষ্টা করুন।',
        });
      }

      challenges.set(challengeId, {
        phoneE164,
        otpHash,
        otpLength: requestedLength,
        createdAt: now,
        expiresAt: now + OTP_EXPIRY_MS,
        attempts: 0,
        used: false,
      });

      return sendJson(res, 200, {
        challengeId,
        expiresInSec: 60,
        resendAfterSec: 30,
        otpLength: requestedLength,
      });
    }

    if (urlPath === '/api/auth/otp/verify') {
      const phoneE164 = normalizeBdPhone(body.phone);
      const challengeId = typeof body.challengeId === 'string' ? body.challengeId.trim() : '';
      const code = typeof body.code === 'string' ? body.code.trim() : '';

      if (!phoneE164) {
        return sendJson(res, 400, { code: 'INVALID_PHONE', message: 'ভুল মোবাইল নম্বর।' });
      }
      if (!/^\d{4,6}$/.test(code)) {
        return sendJson(res, 400, { code: 'INVALID_OTP', message: 'OTP ভুল হয়েছে।' });
      }

      const record = challenges.get(challengeId);
      if (!record || record.used || record.phoneE164 !== phoneE164 || code.length !== record.otpLength) {
        return sendJson(res, 400, { code: 'INVALID_OTP', message: 'OTP ভুল হয়েছে।' });
      }

      if (now > record.expiresAt) {
        challenges.delete(challengeId);
        return sendJson(res, 410, { code: 'OTP_EXPIRED', message: 'OTP-এর সময় শেষ।' });
      }

      record.attempts += 1;
      if (record.attempts > MAX_VERIFY_ATTEMPTS) {
        challenges.delete(challengeId);
        return sendJson(res, 429, {
          code: 'RATE_LIMITED',
          message: 'অতিরিক্ত OTP পাঠানোর চেষ্টা হয়েছে।',
        });
      }

      const candidateHash = hashOtp(hmacSecret, challengeId, phoneE164, code);
      if (!constantTimeHexEqual(candidateHash, record.otpHash)) {
        return sendJson(res, 401, { code: 'INVALID_OTP', message: 'OTP ভুল হয়েছে।' });
      }

      record.used = true;
      challenges.delete(challengeId);

      const sessionToken = crypto.randomBytes(24).toString('base64url');
      let firebaseCustomToken = null;
      if (customTokenMinter) {
        firebaseCustomToken = await customTokenMinter(phoneE164);
      }

      return sendJson(res, 200, {
        verified: true,
        phone: phoneE164,
        sessionToken,
        firebaseCustomToken,
      });
    }

    if (urlPath === '/api/payments/initiate') {
      const provider = typeof body.provider === 'string' ? body.provider.trim().toLowerCase() : '';
      const amount = Number(body.amount);
      if (!allowedProviders.has(provider)) {
        return sendJson(res, 400, { code: 'INVALID_PROVIDER', message: 'সঠিক পেমেন্ট মাধ্যম নির্বাচন করুন।' });
      }
      if (!Number.isFinite(amount) || amount <= 0) {
        return sendJson(res, 400, { code: 'INVALID_AMOUNT', message: 'সঠিক টাকার পরিমাণ দিন।' });
      }
      const transactionId = `PAY-${provider.toUpperCase()}-${crypto.randomUUID().slice(0, 8).toUpperCase()}`;
      const checkoutUrl = typeof options.checkoutUrlBuilder === 'function'
        ? options.checkoutUrlBuilder({ provider, amount, transactionId })
        : '';
      const autoVerified = Boolean(options.autoVerifyPayments) || !checkoutUrl;
      paymentOrders.set(transactionId, {
        transactionId,
        provider,
        amount,
        reference: typeof body.reference === 'string' ? body.reference.slice(0, 120) : '',
        userId: typeof body.userId === 'string' ? body.userId.slice(0, 120) : '',
        verified: autoVerified,
        consumed: false,
        createdAt: now,
      });
      return sendJson(res, 200, {
        transactionId,
        provider,
        amount,
        checkoutUrl,
        verified: autoVerified,
        message: 'পেমেন্ট গেটওয়ে অনুরোধ তৈরি হয়েছে।',
      });
    }

    if (urlPath === '/api/payments/verify') {
      const provider = typeof body.provider === 'string' ? body.provider.trim().toLowerCase() : '';
      const transactionId = typeof body.transactionId === 'string' ? body.transactionId.trim() : '';
      const expectedAmount = Number(body.expectedAmount || 0);
      const order = paymentOrders.get(transactionId);
      if (!order || order.provider !== provider) {
        return sendJson(res, 404, { code: 'PAYMENT_NOT_FOUND', verified: false, message: 'পেমেন্ট তথ্য পাওয়া যায়নি।' });
      }
      if (order.consumed) {
        return sendJson(res, 409, { code: 'DUPLICATE_PAYMENT', verified: false, message: 'এই পেমেন্ট ইতিমধ্যে ব্যবহৃত হয়েছে।' });
      }
      if (!order.verified || order.amount + 0.01 < expectedAmount) {
        return sendJson(res, 402, { code: 'PAYMENT_UNVERIFIED', verified: false, message: 'পেমেন্ট এখনো যাচাই হয়নি।' });
      }
      order.consumed = true;
      return sendJson(res, 200, {
        verified: true,
        transactionId: order.transactionId,
        provider: order.provider,
        amount: order.amount,
        message: 'পেমেন্ট সফলভাবে যাচাই সম্পন্ন হয়েছে।',
      });
    }

    return sendJson(res, 404, { code: 'NOT_FOUND', message: 'Endpoint not found' });
  });
}

if (require.main === module) {
  const port = Number(process.env.PORT || 8080);
  const server = createOtpServer();
  server.listen(port, () => {
    console.log(`Hisaber Khata OTP Gateway listening on port ${port}`);
  });
}

module.exports = {
  createOtpServer,
  normalizeBdPhone,
  buildSmsRetrieverMessage,
  hashOtp,
};
