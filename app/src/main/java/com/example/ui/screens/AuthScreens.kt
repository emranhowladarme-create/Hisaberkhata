package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.auth.BangladeshPhoneValidator
import com.example.auth.OtpChallengeSession
import com.example.auth.OtpErrorMessages
import com.example.auth.OtpSendOutcome
import com.example.auth.OtpVerifyOutcome
import com.example.auth.PhoneOtpAuthGateway
import com.example.auth.RealPhoneOtpAuthGateway
import com.example.auth.SmsAutofillMode
import com.example.auth.SmsAutofillStatus
import com.example.auth.SmsOtpAutofillEffect
import com.example.auth.normalizeDigitsToAscii
import com.example.auth.onGoogleSignInClicked
import com.example.ui.components.GoogleGLogo
import com.example.ui.components.HISABER_PROMO_ADS
import com.example.ui.components.HisaberPromoAdCarouselBanner
import com.example.ui.components.HisaberPromoAdItem
import com.example.ui.components.ReferenceTopStatusAndHeaderSection
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DenaRed
import com.example.ui.theme.HisabGreen
import com.example.ui.theme.HisabGreenBorder
import com.example.ui.theme.HisabGreenDark
import com.example.ui.theme.HisabGreenSoft
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.auth.computeSmsRetrieverAppHashes
import com.example.ui.theme.PaonaGreen
import com.example.ui.theme.PureWhite
import com.example.util.Formatters
import com.example.util.HishabiPrefsStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val AuthLightMintBg = Color(0xFFE3F7DA)
private val AuthPrimaryGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF0A8E15), Color(0xFF1BB726), Color(0xFF0A8E15)),
)

private tailrec fun Context.findActivityOrNull(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivityOrNull()
    else -> null
}

@Composable
fun SplashScreen() {
    val scale = remember { Animatable(0.75f) }
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(HisabGreen, HisabGreenDark)
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .padding(24.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(PureWhite),
                contentAlignment = Alignment.Center,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = "হিসাবের খাতা লোগো",
                        tint = HisabGreen,
                        modifier = Modifier.size(54.dp),
                    )
                    Text(
                        text = "৳",
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Hishaber Khata",
                style = MaterialTheme.typography.displaySmall.copy(
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "আপনার ব্যবসার বিশ্বস্ত হিসাব সঙ্গী",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = PureWhite.copy(alpha = 0.9f),
                ),
            )
            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator(
                color = PureWhite,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

private enum class AuthStep {
    LOGIN, OTP
}

@Composable
fun AuthFlowScreen(
    onOtpVerifiedOnly: (ownerName: String, businessName: String, phone: String) -> Unit,
    onGoogleSignInSuccess: (ownerName: String, businessName: String, phone: String) -> Unit,
    otpGateway: PhoneOtpAuthGateway = remember { RealPhoneOtpAuthGateway() },
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var step by rememberSaveable { mutableStateOf(AuthStep.LOGIN) }
    var phoneDigits by rememberSaveable { mutableStateOf("") }
    var otpCode by rememberSaveable { mutableStateOf("") }
    var otpSessionKey by rememberSaveable { mutableIntStateOf(0) }
    var countdownSec by rememberSaveable { mutableIntStateOf(60) }
    var resendCooldownSec by rememberSaveable { mutableIntStateOf(30) }
    var activeChallenge by remember { mutableStateOf<OtpChallengeSession?>(null) }

    var isSendingOtp by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var isGoogleSigningIn by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    var autofillMode by rememberSaveable { mutableStateOf(SmsAutofillMode.SMS_RETRIEVER) }
    var autofillStatus by remember { mutableStateOf(SmsAutofillStatus.IDLE) }
    var autofillDetail by remember { mutableStateOf<String?>(null) }
    val smsAppHashes = remember(context) { computeSmsRetrieverAppHashes(context) }

    var currentAdIndex by rememberSaveable { mutableIntStateOf(0) }
    var activeAdDialogItem by remember { mutableStateOf<HisaberPromoAdItem?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                delay(5_000L)
                currentAdIndex = (currentAdIndex + 1) % HISABER_PROMO_ADS.size
            }
        }
    }

    val expectedOtpLength = activeChallenge?.otpLength ?: 6

    if (step != AuthStep.LOGIN) {
        BackHandler {
            errorMessage = null
            infoMessage = null
            otpCode = ""
            step = AuthStep.LOGIN
        }
    }

    // Zero-permission Google Play Services SMS Retriever / User Consent Auto-fill (never requests READ_SMS)
    SmsOtpAutofillEffect(
        enabled = step == AuthStep.OTP,
        mode = autofillMode,
        sessionKey = otpSessionKey,
        expectedOtpLength = expectedOtpLength,
        onStatusChanged = { st, detail ->
            autofillStatus = st
            autofillDetail = detail
        },
        onOtpExtracted = { code ->
            otpCode = code.take(expectedOtpLength)
            errorMessage = null
            infoMessage = "SMS থেকে OTP স্বয়ংক্রিয়ভাবে পূরণ হয়েছে!"
        },
    )

    // 1-minute (60s) OTP expiration countdown & 30-second Resend cooldown
    LaunchedEffect(step, otpSessionKey) {
        if (step == AuthStep.OTP) {
            val totalSec = activeChallenge?.expiresInSec ?: 60
            val resendWait = activeChallenge?.resendAfterSec ?: 30
            countdownSec = totalSec
            resendCooldownSec = resendWait
            while (countdownSec > 0 || resendCooldownSec > 0) {
                delay(1000L)
                if (countdownSec > 0) countdownSec--
                if (resendCooldownSec > 0) resendCooldownSec--
            }
        }
    }

    fun requestSendOtp(isResend: Boolean) {
        val normalized = BangladeshPhoneValidator.normalize(phoneDigits)
        if (normalized == null) {
            errorMessage = OtpErrorMessages.INVALID_PHONE
            return
        }
        if (isResend && resendCooldownSec > 0) {
            errorMessage = "অনুগ্রহ করে $resendCooldownSec সেকেন্ড পর আবার OTP পাঠান।"
            return
        }
        if (isSendingOtp) return

        phoneDigits = normalized.localTenDigits
        errorMessage = null
        infoMessage = null
        isSendingOtp = true

        scope.launch {
            val activity = context.findActivityOrNull()
            val prev = activeChallenge
            val outcome = if (isResend && prev != null) {
                otpGateway.resendOtp(activity, context, prev, smsAppHashes)
            } else {
                otpGateway.sendOtp(activity, context, normalized.e164, smsAppHashes)
            }
            isSendingOtp = false

            when (outcome) {
                is OtpSendOutcome.CodeSent -> {
                    activeChallenge = outcome.session
                    otpCode = ""
                    otpSessionKey++
                    infoMessage = if (isResend) {
                        "নতুন OTP পাঠানো হয়েছে (${normalized.displayFormatted})"
                    } else {
                        null
                    }
                    step = AuthStep.OTP
                }
                is OtpSendOutcome.AutoVerified -> {
                    HishabiPrefsStore.update(context) {
                        it.copy(
                            verifiedPhoneNumber = outcome.phoneE164,
                            isPhoneSessionVerified = true,
                        )
                    }
                    onOtpVerifiedOnly("Mohammad Emran Hawladar", "ইমরান টি স্টল", outcome.phoneE164)
                }
                is OtpSendOutcome.Error -> {
                    errorMessage = outcome.message
                }
            }
        }
    }

    fun verifyOtpWithBackend() {
        val session = activeChallenge
        if (session == null) {
            errorMessage = OtpErrorMessages.OTP_EXPIRED
            return
        }
        if (countdownSec <= 0) {
            errorMessage = OtpErrorMessages.OTP_EXPIRED
            return
        }
        val cleanedOtp = normalizeDigitsToAscii(otpCode).filter { it.isDigit() }
        if (cleanedOtp.length != expectedOtpLength) {
            errorMessage = "অনুগ্রহ করে $expectedOtpLength ডিজিটের সম্পূর্ণ OTP কোড দিন"
            return
        }
        if (isVerifying) return

        errorMessage = null
        infoMessage = null
        isVerifying = true

        scope.launch {
            val outcome = otpGateway.verifyOtp(context, session, cleanedOtp)
            isVerifying = false
            when (outcome) {
                is OtpVerifyOutcome.Verified -> {
                    HishabiPrefsStore.update(context) {
                        it.copy(
                            verifiedPhoneNumber = outcome.phoneE164,
                            isPhoneSessionVerified = true,
                        )
                    }
                    onOtpVerifiedOnly("Mohammad Emran Hawladar", "ইমরান টি স্টল", outcome.phoneE164)
                }
                is OtpVerifyOutcome.Error -> {
                    errorMessage = outcome.message
                }
            }
        }
    }

    fun startExplicitGoogleSignIn() {
        errorMessage = null
        infoMessage = null
        isGoogleSigningIn = true
        onGoogleSignInClicked(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                isGoogleSigningIn = false
                val fullPhone = BangladeshPhoneValidator.normalize(phoneDigits)?.e164.orEmpty()
                onGoogleSignInSuccess("Mohammad Emran Hawladar", "ইমরান টি স্টল", fullPhone)
            },
            onAuthError = { msg ->
                isGoogleSigningIn = false
                errorMessage = msg
            },
            scope = scope,
            onAuthCancelled = {
                isGoogleSigningIn = false
            },
        )
    }

    when (step) {
        AuthStep.LOGIN -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PureWhite)
                    .imePadding(),
            ) {
                ReferenceTopStatusAndHeaderSection(
                    isOtpStep = false,
                    onTopBannerClick = { activeAdDialogItem = HISABER_PROMO_ADS.first() },
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Column(
                        modifier = Modifier.widthIn(max = 460.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Spacer(modifier = Modifier.height(2.dp))

                        // 1. NID Registration Instruction Text
                        Text(
                            text = "NID দিয়ে নিবন্ধিত মোবাইল নম্বর লিখুন",
                            color = InkPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )

                        // 2. +880 Bangladeshi Mobile Number Pill Input
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(25.dp))
                                .background(PureWhite)
                                .border(1.2.dp, Color(0xFFA7B7AC), RoundedCornerShape(25.dp))
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "মোবাইল নম্বর",
                                tint = Color(0xFF0A6B16),
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "+880",
                                color = InkPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            BasicTextField(
                                value = phoneDigits,
                                onValueChange = { raw ->
                                    phoneDigits = BangladeshPhoneValidator.sanitizeInputToTenDigits(raw)
                                    errorMessage = null
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                cursorBrush = SolidColor(HisabGreen),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = InkPrimary,
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("login_phone_input"),
                                decorationBox = { innerTextField ->
                                    if (phoneDigits.isEmpty()) {
                                        Text(
                                            text = "মোবাইল নম্বর লিখুন",
                                            color = InkMuted,
                                            fontSize = 14.5.sp,
                                        )
                                    }
                                    innerTextField()
                                },
                            )
                        }

                        // 3. Large 5-Rotating Promo Ad Banner IN PLACE OF bKash/Nagad/Rocket/Bank
                        HisaberPromoAdCarouselBanner(
                            currentAdIndex = currentAdIndex,
                            onDownloadClick = { activeAdDialogItem = it },
                            modifier = Modifier.testTag("login_ad_banner"),
                        )

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = DenaRed,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_error_text"),
                            )
                        }

                        // 4. Primary "OTP পাঠান" Emerald Button
                        Surface(
                            onClick = { requestSendOtp(isResend = false) },
                            enabled = !isSendingOtp,
                            shape = RoundedCornerShape(26.dp),
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("send_otp_button"),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(AuthPrimaryGradient)
                                    .padding(horizontal = 18.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isSendingOtp) {
                                    CircularProgressIndicator(
                                        color = PureWhite,
                                        strokeWidth = 2.5.dp,
                                        modifier = Modifier.size(22.dp),
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Send,
                                            contentDescription = null,
                                            tint = PureWhite,
                                            modifier = Modifier.size(20.dp),
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "OTP পাঠান",
                                            color = PureWhite,
                                            fontSize = 16.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                        )
                                    }
                                }
                            }
                        }

                        // 5. "অথবা" Divider + Google Sign-In Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = CardBorder)
                            Text(
                                text = "  অথবা  ",
                                color = InkPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = CardBorder)
                        }

                        OutlinedButton(
                            onClick = { startExplicitGoogleSignIn() },
                            enabled = !isGoogleSigningIn,
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.2.dp, Color(0xFFB5C4BA)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = PureWhite,
                                contentColor = InkPrimary,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("google_signin_button"),
                        ) {
                            if (isGoogleSigningIn) {
                                CircularProgressIndicator(
                                    color = HisabGreen,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp),
                                )
                            } else {
                                GoogleGLogo(modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Google দিয়ে লগইন করুন",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InkPrimary,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        AuthStep.OTP -> {
            val focusManager = LocalFocusManager.current
            val keyboardController = LocalSoftwareKeyboardController.current
            val focusRequesters = remember(expectedOtpLength) { List(expectedOtpLength) { FocusRequester() } }
            val otpDigits = remember(expectedOtpLength, otpSessionKey) {
                mutableStateListOf<String>().apply {
                    repeat(expectedOtpLength) { idx ->
                        add(otpCode.getOrNull(idx)?.toString() ?: "")
                    }
                }
            }
            var focusedBoxIndex by remember { mutableIntStateOf(0) }

            LaunchedEffect(otpCode, expectedOtpLength) {
                val clean = normalizeDigitsToAscii(otpCode).filter { it.isDigit() }.take(expectedOtpLength)
                val joined = otpDigits.joinToString("")
                if (clean != joined) {
                    for (i in 0 until expectedOtpLength) {
                        otpDigits[i] = clean.getOrNull(i)?.toString() ?: ""
                    }
                }
            }

            fun syncOtpStringFromSlots() {
                otpCode = otpDigits.joinToString("")
            }

            val digitsLabelBn = if (expectedOtpLength == 4) "৪" else "৬"

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PureWhite)
                    .imePadding(),
            ) {
                ReferenceTopStatusAndHeaderSection(
                    isOtpStep = true,
                    onTopBannerClick = { activeAdDialogItem = HISABER_PROMO_ADS.first() },
                    onBackClick = {
                        errorMessage = null
                        infoMessage = null
                        otpCode = ""
                        step = AuthStep.LOGIN
                    },
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Column(
                        modifier = Modifier.widthIn(max = 460.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Spacer(modifier = Modifier.height(2.dp))

                        // 1. Mint Pill Card with Green Verified Shield + OTP Sent Confirmation
                        Surface(
                            color = Color(0xFFDFF7E3),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "OTP সফলভাবে পাঠানো হয়েছে",
                                    tint = Color(0xFF0A6B16),
                                    modifier = Modifier.size(28.dp),
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(
                                        text = "আপনার মোবাইলে $digitsLabelBn সংখ্যার\nOTP পাঠানো হয়েছে",
                                        color = InkPrimary,
                                        fontSize = 13.5.sp,
                                        lineHeight = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = "+880 $phoneDigits",
                                        color = Color(0xFF0A6B16),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.testTag("otp_sent_number_label"),
                                    )
                                }
                            }
                        }

                        // 2. Six Separate Interactive Rounded OTP Digit Boxes
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("otp_boxes_row"),
                            horizontalArrangement = Arrangement.spacedBy(
                                if (expectedOtpLength >= 6) 7.dp else 10.dp
                            ),
                        ) {
                            repeat(expectedOtpLength) { idx ->
                                val digit = otpDigits.getOrElse(idx) { "" }
                                val isFocused = focusedBoxIndex == idx
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(PureWhite)
                                        .border(
                                            width = if (isFocused) 2.dp else 1.2.dp,
                                            color = if (isFocused || digit.isNotEmpty()) {
                                                Color(0xFF0A8E15)
                                            } else {
                                                Color(0xFF9EA8A0)
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                        )
                                        .clickable {
                                            focusRequesters[idx].requestFocus()
                                            keyboardController?.show()
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicTextField(
                                        value = digit,
                                        onValueChange = { raw ->
                                            val incoming = normalizeDigitsToAscii(raw).filter { it.isDigit() }
                                            errorMessage = null
                                            when {
                                                incoming.isEmpty() -> {
                                                    otpDigits[idx] = ""
                                                    syncOtpStringFromSlots()
                                                }
                                                incoming.length == 1 -> {
                                                    otpDigits[idx] = incoming
                                                    syncOtpStringFromSlots()
                                                    if (idx < expectedOtpLength - 1) {
                                                        focusRequesters[idx + 1].requestFocus()
                                                    } else {
                                                        focusManager.clearFocus()
                                                    }
                                                }
                                                incoming.length == 2 && digit.isNotEmpty() -> {
                                                    val replacement = when {
                                                        incoming.startsWith(digit) -> incoming.drop(1)
                                                        incoming.endsWith(digit) -> incoming.dropLast(1)
                                                        else -> incoming.takeLast(1)
                                                    }
                                                    otpDigits[idx] = replacement
                                                    syncOtpStringFromSlots()
                                                    if (idx < expectedOtpLength - 1) {
                                                        focusRequesters[idx + 1].requestFocus()
                                                    } else {
                                                        focusManager.clearFocus()
                                                    }
                                                }
                                                else -> {
                                                    val start = if (incoming.length >= expectedOtpLength) 0 else idx
                                                    incoming.take(expectedOtpLength - start).forEachIndexed { offset, ch ->
                                                        otpDigits[start + offset] = ch.toString()
                                                    }
                                                    syncOtpStringFromSlots()
                                                    val nextIndex = start + incoming.length
                                                    if (nextIndex < expectedOtpLength) {
                                                        focusRequesters[nextIndex].requestFocus()
                                                    } else {
                                                        focusManager.clearFocus()
                                                    }
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        cursorBrush = SolidColor(Color(0xFF0A8E15)),
                                        textStyle = MaterialTheme.typography.titleLarge.copy(
                                            color = InkPrimary,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(focusRequesters[idx])
                                            .onFocusChanged { state ->
                                                if (state.isFocused) focusedBoxIndex = idx
                                            }
                                            .onPreviewKeyEvent { keyEvent ->
                                                if (keyEvent.type == KeyEventType.KeyDown &&
                                                    keyEvent.key == Key.Backspace &&
                                                    digit.isEmpty() &&
                                                    idx > 0
                                                ) {
                                                    otpDigits[idx - 1] = ""
                                                    syncOtpStringFromSlots()
                                                    focusRequesters[idx - 1].requestFocus()
                                                    true
                                                } else {
                                                    false
                                                }
                                            }
                                            .testTag("otp_box_$idx"),
                                    )
                                }
                            }
                        }

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = DenaRed,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.testTag("otp_error_text"),
                            )
                        }

                        if (infoMessage != null) {
                            Text(
                                text = infoMessage!!,
                                color = PaonaGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                            )
                        }

                        // 3. Primary Emerald "✓ যাচাই করুন" Button right below the 6 boxes
                        Surface(
                            onClick = { verifyOtpWithBackend() },
                            enabled = !isVerifying,
                            shape = RoundedCornerShape(26.dp),
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("verify_otp_button"),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(AuthPrimaryGradient),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isVerifying) {
                                    CircularProgressIndicator(
                                        color = PureWhite,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(22.dp),
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = PureWhite,
                                            modifier = Modifier.size(20.dp),
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "যাচাই করুন",
                                            color = PureWhite,
                                            fontSize = 16.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                        )
                                    }
                                }
                            }
                        }

                        // 4. 60-Second Red Clock Countdown Row matching exact screenshot copy
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = if (countdownSec > 0) Color(0xFFD92D20) else Color(0xFF0A8E15),
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (countdownSec > 0) {
                                    "$countdownSec সেকেন্ড পর আবার\nOTP পাঠাতে পারবেন"
                                } else {
                                    "এখন পুনরায় OTP পাঠাতে পারবেন"
                                },
                                color = if (countdownSec > 0) Color(0xFFD92D20) else Color(0xFF0A8E15),
                                fontSize = 13.sp,
                                lineHeight = 16.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("otp_countdown_text"),
                            )
                        }

                        // Delayed SMS / Autofill hint cleanly below timer if triggered
                        val smsDelayMessage = when {
                            autofillStatus == SmsAutofillStatus.AUTO_FILLED -> "SMS থেকে OTP স্বয়ংক্রিয়ভাবে পূরণ হয়েছে"
                            countdownSec in 1..40 && otpCode.isEmpty() -> OtpErrorMessages.SMS_DELAYED
                            else -> null
                        }
                        if (!smsDelayMessage.isNullOrBlank()) {
                            Text(
                                text = smsDelayMessage,
                                color = if (autofillStatus == SmsAutofillStatus.AUTO_FILLED) HisabGreenDark else Color(0xFFB45309),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.testTag("otp_autofill_status_text"),
                            )
                        }

                        // 5. Light Mint Outlined Pill "পুনরায় OTP পাঠান" (auto-enables when 60s countdown finishes)
                        val canResend = countdownSec == 0 && resendCooldownSec == 0 && !isSendingOtp
                        Surface(
                            onClick = { requestSendOtp(isResend = true) },
                            enabled = canResend,
                            color = if (canResend) Color(0xFFCCF5D3) else Color(0xFFE7F6EA),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(
                                1.1.dp,
                                if (canResend) Color(0xFF0A8E15) else Color(0xFF9ACFA3),
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("resend_otp_button"),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = if (canResend) Color(0xFF0A6B16) else InkMuted,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "পুনরায় OTP পাঠান",
                                    color = if (canResend) InkPrimary else InkSecondary,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        // 6. Same Auto-Rotating 5-Scenario Ad Banner on OTP Page
                        HisaberPromoAdCarouselBanner(
                            currentAdIndex = currentAdIndex,
                            onDownloadClick = { activeAdDialogItem = it },
                            modifier = Modifier.testTag("otp_ad_banner"),
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }

    activeAdDialogItem?.let { ad ->
        val storeUrl = "https://play.google.com/store/apps/details?id=${context.packageName}"
        AlertDialog(
            onDismissRequest = { activeAdDialogItem = null },
            title = {
                Text(
                    text = "${ad.headline} — ${ad.subtitle}",
                    fontWeight = FontWeight.Bold,
                    color = HisabGreenDark,
                )
            },
            text = {
                Text(
                    text = "${ad.bullets.joinToString("\n") { "• $it" }}\n\nআপনার ব্যবসার দৈনিক আয়-ব্যয়, বাকি ও কাস্টমারের নিখুঁত হিসাব রাখতে হিসাবের খাতা অ্যাপটি শেয়ার বা ডাউনলোড করুন।",
                    color = InkPrimary,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        activeAdDialogItem = null
                        val playIntent = Intent(Intent.ACTION_VIEW, Uri.parse(storeUrl))
                        runCatching { context.startActivity(playIntent) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HisabGreen),
                ) {
                    Text("Play Store", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            activeAdDialogItem = null
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "${ad.headline} - ${ad.subtitle} ডাউনলোড করুন: $storeUrl"
                                )
                            }
                            runCatching {
                                context.startActivity(Intent.createChooser(shareIntent, "হিসাবের খাতা শেয়ার করুন"))
                            }
                        },
                    ) {
                        Text("শেয়ার করুন", color = HisabGreenDark, fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(onClick = { activeAdDialogItem = null }) {
                        Text("বন্ধ করুন", color = InkSecondary)
                    }
                }
            },
        )
    }
}

@Composable
fun PinLockScreen(
    expectedPinHash: String,
    onUnlocked: () -> Unit,
    onForgotPinLogout: () -> Unit,
) {
    var enteredPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.widthIn(max = 360.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(HisabGreenSoft),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "অ্যাপ লক",
                    tint = HisabGreen,
                    modifier = Modifier.size(36.dp),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "হিসাবের খাতা সুরক্ষিত",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "প্রবেশ করতে আপনার ৪ ডিজিটের পিন দিন",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSecondary,
            )
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                value = enteredPin,
                onValueChange = {
                    enteredPin = it.filter { c -> c.isDigit() }.take(4)
                    error = null
                },
                label = { Text("৪ ডিজিট পিন") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("pin_lock_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HisabGreen),
                shape = RoundedCornerShape(12.dp),
            )
            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = error!!, color = DenaRed, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (Formatters.sha256Hex(enteredPin) == expectedPinHash) {
                        onUnlocked()
                    } else {
                        error = "ভুল পিন! আবার চেষ্টা করুন।"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("pin_unlock_button"),
                colors = ButtonDefaults.buttonColors(containerColor = HisabGreen),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("আনলক করুন", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onForgotPinLogout) {
                Text("পিন ভুলে গেছেন? লগআউট করুন", color = HisabGreen)
            }
        }
    }
}
