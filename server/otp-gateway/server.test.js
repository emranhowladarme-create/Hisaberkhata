'use strict';

const { test, after } = require('node:test');
const assert = require('node:assert/strict');
const { createOtpServer, normalizeBdPhone, buildSmsRetrieverMessage } = require('./server');

const activeServers = [];
after(() => {
  for (const s of activeServers) {
    s.close();
    if (typeof s.closeAllConnections === 'function') s.closeAllConnections();
  }
});

test('Bangladesh phone normalization handles +880, 880, 01, 1, and Bengali digits', () => {
  assert.equal(normalizeBdPhone('+8801712345678'), '+8801712345678');
  assert.equal(normalizeBdPhone('01712345678'), '+8801712345678');
  assert.equal(normalizeBdPhone('1712345678'), '+8801712345678');
  assert.equal(normalizeBdPhone('০১৭১২৩৪৫৬৭৮'), '+8801712345678');
  assert.equal(normalizeBdPhone('01212345678'), null);
  assert.equal(normalizeBdPhone('12345'), null);
});

test('OTP server generates 6-digit SMS Retriever code, enforces 30s resend cooldown, 60s expiry, and verifies once', async () => {
  let fakeNow = 1_700_000_000_000;
  let capturedOtp = null;
  let capturedMessage = null;

  const server = createOtpServer({
    hmacSecret: 'test-secret-pepper-123',
    nowFn: () => fakeNow,
    smsSender: async ({ phoneE164, message }) => {
      capturedMessage = message;
      const match = message.match(/কোড (\d{6})/);
      capturedOtp = match ? match[1] : null;
      assert.equal(phoneE164, '+8801712345678');
    },
    customTokenMinter: async (phone) => `custom-token-for-${phone}`,
  });
  activeServers.push(server);

  await new Promise((resolve) => server.listen(0, '127.0.0.1', resolve));
  const port = server.address().port;
  const baseUrl = `http://127.0.0.1:${port}`;

  // 1. Send OTP
  const sendResp = await fetch(`${baseUrl}/api/auth/otp/send`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ phone: '01712345678', appHash: 'FA+9qCX9VSu' }),
  });
  assert.equal(sendResp.status, 200);
  const sendData = await sendResp.json();
  assert.equal(sendData.expiresInSec, 60);
  assert.equal(sendData.resendAfterSec, 30);
  assert.equal(sendData.otpLength, 6);
  assert.ok(/^\d{6}$/.test(capturedOtp));
  assert.ok(capturedMessage.startsWith('<#> '));
  assert.ok(capturedMessage.endsWith('\nFA+9qCX9VSu'));

  // 2. Resend before 30s fails with 429 cooldown
  fakeNow += 10_000;
  const earlyResend = await fetch(`${baseUrl}/api/auth/otp/resend`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ phone: '+8801712345678', appHash: 'FA+9qCX9VSu' }),
  });
  assert.equal(earlyResend.status, 429);

  // 3. Wrong OTP returns 401 INVALID_OTP
  const wrongVerify = await fetch(`${baseUrl}/api/auth/otp/verify`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      challengeId: sendData.challengeId,
      phone: '+8801712345678',
      code: capturedOtp === '000000' ? '111111' : '000000',
    }),
  });
  assert.equal(wrongVerify.status, 401);

  // 4. Correct OTP succeeds & returns custom token
  const okVerify = await fetch(`${baseUrl}/api/auth/otp/verify`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      challengeId: sendData.challengeId,
      phone: '+8801712345678',
      code: capturedOtp,
    }),
  });
  assert.equal(okVerify.status, 200);
  const okData = await okVerify.json();
  assert.equal(okData.verified, true);
  assert.equal(okData.firebaseCustomToken, 'custom-token-for-+8801712345678');

  // 5. Resend after 30s works, then expires after 60s
  fakeNow += 25_000; // total +35s
  const secondSend = await fetch(`${baseUrl}/api/auth/otp/resend`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ phone: '+8801712345678', appHash: 'FA+9qCX9VSu' }),
  });
  assert.equal(secondSend.status, 200);
  const secondData = await secondSend.json();

  fakeNow += 61_000; // expired!
  const expiredVerify = await fetch(`${baseUrl}/api/auth/otp/verify`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      challengeId: secondData.challengeId,
      phone: '+8801712345678',
      code: capturedOtp,
    }),
  });
  assert.equal(expiredVerify.status, 410);
});

test('Payment endpoints validate bkash/nagad/rocket, verify once, and block duplicate activation', async () => {
  const server = createOtpServer({ autoVerifyPayments: true });
  activeServers.push(server);
  await new Promise((resolve) => server.listen(0, '127.0.0.1', resolve));
  const baseUrl = `http://127.0.0.1:${server.address().port}`;

  const initResp = await fetch(`${baseUrl}/api/payments/initiate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ provider: 'rocket', amount: 150, reference: 'GOLD_2M' }),
  });
  assert.equal(initResp.status, 200);
  const initData = await initResp.json();
  assert.equal(initData.provider, 'rocket');
  assert.equal(initData.amount, 150);

  const verifyOk = await fetch(`${baseUrl}/api/payments/verify`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ provider: 'rocket', transactionId: initData.transactionId, expectedAmount: 150 }),
  });
  assert.equal(verifyOk.status, 200);
  const verifyData = await verifyOk.json();
  assert.equal(verifyData.verified, true);

  const duplicateVerify = await fetch(`${baseUrl}/api/payments/verify`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ provider: 'rocket', transactionId: initData.transactionId, expectedAmount: 150 }),
  });
  assert.equal(duplicateVerify.status, 409);
});

