package com.example.support

enum class SupportIssueCategory(val labelBn: String) {
    AUTH_LOGIN("লগইন ও OTP সহায়তা"),
    FIREBASE_TOKEN_APPCHECK("Firebase Auth / Token ত্রুটি"),
    BACKUP_SYNC("ব্যাকআপ ও সিঙ্ক সহায়তা"),
    PAYMENT_QR("বাকি আদায় ও QR পেমেন্ট"),
    ACCOUNTING_LEDGER("হিসাব, আয়-ব্যয় ও রিপোর্ট"),
    SETTINGS_PERMISSIONS("সেটিংস ও পারমিশন"),
    ACCOUNT_PRIVACY_RISK("সুরক্ষা ও গোপনীয়তা নীতি"),
    GENERAL_APP_INFO("অ্যাপ তথ্য ও নির্দেশিকা"),
    UNKNOWN_OR_UNVERIFIED("যাচাইকৃত তথ্য প্রয়োজন"),
}

data class SupportProblemClassification(
    val category: SupportIssueCategory,
    val isSecurityOrPrivacyViolation: Boolean = false,
    val needsAdditionalDetails: Boolean = false,
    val missingDetailsQuestionBn: String? = null,
    val immediateSafetyWarningBn: String? = null,
    val suggestHumanEscalation: Boolean = false,
)

object SupportProblemDetector {

    private val secretOrUnauthorizedTerms = listOf(
        "অন্য কাস্টমারের", "অন্যের হিসাব", "অন্য দোকানের ডেটা", "other user", "other customer",
        "show password", "পাসওয়ার্ড দিন", "পাসওয়ার্ড দিন", "আমার পাসওয়ার্ড বলুন",
        "পিন নম্বর দিন", "otp দিন", "ফায়ারবেস টোকেন দিন", "api key দিন", "firebase token",
        "হিসাব পরিবর্তন করে দিন", "ব্যালেন্স এডিট করে দিন", "delete customer data",
    )

    private val tokenErrorTerms = listOf(
        "invalid token", "token expired", "invalid_id_token", "user_token_expired",
        "403", "app check", "appcheck", "permission_denied", "permission denied",
        "unauthenticated", "firebase auth", "টোকেন", "ফায়ারবেস", "ফায়ারবেস",
        "অথেনটিকেশন", "অবৈধ টোকেন",
    )

    private val loginOtpTerms = listOf(
        "লগইন", "সাইন ইন", "লগ ইন", "ওটিপি", "otp", "রেজিস্ট্রেশন", "অ্যাকাউন্ট খুল",
        "login", "sign in", "signin", "register", "verify", "verification",
    )

    private val backupSyncTerms = listOf(
        "ব্যাকআপ", "সিঙ্ক", "রিস্টোর", "ডেটা হার", "ডাটা হার", "backup", "sync", "restore", "json",
    )

    private val paymentQrTerms = listOf(
        "তাগাদা", "qr", "কিউআর", "হিসাব পে", "পেমেন্ট", "sms প্যাক", "মেসেজ ব্যালেন্স", "payment",
    )

    private val settingsTerms = listOf(
        "সেটিংস", "পারমিশন", "অনুমতি", "গোল্ড", "প্রিমিয়াম", "প্রিমিয়াম", "পিন", "ভাষা", "দশমিক",
        "settings", "permission", "gold", "premium", "membership", "language", "pin",
    )

    private val accountingTerms = listOf(
        "হিসাব", "আয়", "ব্যয়", "খরচ", "কাস্টমার", "সাপ্লায়ার", "ক্যাশবক্স", "ক্যাশবাক্স",
        "রিপোর্ট", "pdf", "স্টক", "নোট", "মাল্টি", "বাকি", "পাওনা", "দেবো", "পাবো",
        "ledger", "customer", "supplier", "income", "expense", "cashbook", "report", "stock",
    )

    private val genericBrokenTerms = listOf(
        "কাজ করছে না", "সমস্যা", "ত্রুটি", "এরর", "error", "crash", "ক্র্যাশ", "বন্ধ হয়ে",
        "fail", "failed", "bug", "not working", "issue", "সমাধান", "হচ্ছে না",
    )

    private val explicitHumanHelpTerms = listOf(
        "হেল্পলাইন", "কল করব", "মানুষের সাথে", "কাস্টমার কেয়ার", "সাপোর্ট টিম",
        "helpline", "human", "representative", "call", "agent",
    )

    fun analyze(
        rawQuery: String,
        priorExchangeCount: Int = 0,
        unresolvedHistoryCount: Int = 0,
    ): SupportProblemClassification {
        val norm = VerifiedKnowledgeBase.normalizeBengaliAndEnglish(rawQuery)

        val hasPrivacyRisk = secretOrUnauthorizedTerms.any { norm.contains(it) } ||
            Regex("""AIza[0-9A-Za-z\-_]{10,}""").containsMatchIn(rawQuery) ||
            Regex("""eyJ[A-Za-z0-9\-_]+\.[A-Za-z0-9\-_]+""").containsMatchIn(rawQuery)

        if (hasPrivacyRisk) {
            return SupportProblemClassification(
                category = SupportIssueCategory.ACCOUNT_PRIVACY_RISK,
                isSecurityOrPrivacyViolation = true,
                immediateSafetyWarningBn =
                    "নিরাপত্তা নীতির কারণে পাসওয়ার্ড, OTP, PIN, Firebase টোকেন বা অন্য কোনো ব্যবহারকারীর আর্থিক তথ্য শেয়ার কিংবা পরিবর্তন করা সম্পূর্ণ নিষিদ্ধ।",
                suggestHumanEscalation = false,
            )
        }

        val isRepeatedFailure = unresolvedHistoryCount >= 1 && genericBrokenTerms.any { norm.contains(it) }
        val asksForHuman = explicitHumanHelpTerms.any { norm.contains(it) }

        if (tokenErrorTerms.any { norm.contains(it) }) {
            val hasErrorDetail = norm.contains("403") || norm.contains("expired") ||
                norm.contains("denied") || norm.contains("invalid") || norm.contains("app check")
            return SupportProblemClassification(
                category = SupportIssueCategory.FIREBASE_TOKEN_APPCHECK,
                needsAdditionalDetails = !hasErrorDetail,
                missingDetailsQuestionBn = if (!hasErrorDetail) {
                    "সমস্যাটি সঠিকভাবে নির্ণয়ের জন্য দয়া করে জানান: স্ক্রিনে ঠিক কোন এরর মেসেজটি দেখাচ্ছে এবং কোন পদ্ধতিতে (Google না OTP) লগইন করছেন?"
                } else {
                    null
                },
                suggestHumanEscalation = isRepeatedFailure || norm.contains("403") || norm.contains("permission"),
            )
        }

        if (loginOtpTerms.any { norm.contains(it) }) {
            val hasContext = norm.contains("google") || norm.contains("otp") || norm.contains("sms") || norm.contains("অটো")
            return SupportProblemClassification(
                category = SupportIssueCategory.AUTH_LOGIN,
                needsAdditionalDetails = !hasContext,
                missingDetailsQuestionBn = if (!hasContext) {
                    "আরও নির্ভুল সাহায্যের জন্য অনুগ্রহ করে জানান: আপনি কি মোবাইল OTP দিয়ে নাকি Google Sign-In দিয়ে লগইন করতে সমস্যা পাচ্ছেন?"
                } else {
                    null
                },
                suggestHumanEscalation = isRepeatedFailure,
            )
        }

        if (backupSyncTerms.any { norm.contains(it) }) {
            return SupportProblemClassification(
                category = SupportIssueCategory.BACKUP_SYNC,
                needsAdditionalDetails = norm.contains("হার") || norm.contains("lost") || norm.contains("restore"),
                missingDetailsQuestionBn = if (norm.contains("হার") || norm.contains("lost") || norm.contains("restore")) {
                    "অনুগ্রহ করে জানান: আপনি কি আগে একই Google অ্যাকাউন্ট দিয়ে ক্লাউড সিঙ্ক করেছিলেন অথবা JSON ব্যাকআপ ফাইল সংরক্ষণ আছে কি?"
                } else {
                    null
                },
                suggestHumanEscalation = isRepeatedFailure || norm.contains("হার") || norm.contains("lost"),
            )
        }

        if (paymentQrTerms.any { norm.contains(it) }) {
            return SupportProblemClassification(
                category = SupportIssueCategory.PAYMENT_QR,
                suggestHumanEscalation = isRepeatedFailure || norm.contains("কেটে") || norm.contains("আটকে"),
            )
        }

        if (settingsTerms.any { norm.contains(it) }) {
            return SupportProblemClassification(
                category = SupportIssueCategory.SETTINGS_PERMISSIONS,
                suggestHumanEscalation = asksForHuman,
            )
        }

        if (accountingTerms.any { norm.contains(it) }) {
            return SupportProblemClassification(
                category = SupportIssueCategory.ACCOUNTING_LEDGER,
                suggestHumanEscalation = asksForHuman,
            )
        }

        if (genericBrokenTerms.any { norm.contains(it) }) {
            return SupportProblemClassification(
                category = SupportIssueCategory.UNKNOWN_OR_UNVERIFIED,
                needsAdditionalDetails = true,
                missingDetailsQuestionBn = "দয়া করে সমস্যাটি কোন স্ক্রিনে হচ্ছে এবং কোনো এরর মেসেজ দেখাচ্ছে কিনা বিস্তারিত লিখুন।",
                suggestHumanEscalation = isRepeatedFailure || priorExchangeCount >= 2 || asksForHuman,
            )
        }

        return SupportProblemClassification(
            category = if (asksForHuman) SupportIssueCategory.GENERAL_APP_INFO else SupportIssueCategory.UNKNOWN_OR_UNVERIFIED,
            suggestHumanEscalation = asksForHuman,
        )
    }

    fun redactSensitiveTokens(text: String): String {
        return text
            .replace(Regex("""AIza[0-9A-Za-z\-_]{10,}"""), "[API_KEY_REDACTED]")
            .replace(Regex("""eyJ[A-Za-z0-9\-_]+\.[A-Za-z0-9\-_]+\.[A-Za-z0-9\-_]+"""), "[TOKEN_REDACTED]")
            .replace(Regex("""\b\d{6}\b(?=\s*(?:otp|ওটিপি|pin|পিন))""", RegexOption.IGNORE_CASE), "[CODE_REDACTED]")
    }
}
