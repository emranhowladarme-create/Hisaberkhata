package com.example.support

import java.util.Locale

const val SUPPORT_HELPLINE_NUMBER = "+8809638517411"
const val SUPPORT_HELPLINE_DIAL_URI = "tel:$SUPPORT_HELPLINE_NUMBER"
const val SUPPORT_BRAND_NAME_BN = "হিসাবের খাতা"
const val SUPPORT_WELCOME_GREETING_BN =
    "আসসালামু আলাইকুম! হিসাবের খাতার স্মার্ট AI কাস্টমার সাপোর্টে স্বাগতম। অ্যাপের ফিচার, হিসাব-নিকাশ বা কারিগরি যেকোনো প্রশ্ন লিখুন।"

object SupportTextPolicy {
    const val TEXT_ONLY_MODE_ENABLED = true
    const val VOICE_FEATURES_ENABLED = false
    const val AUDIO_FEATURES_ENABLED = false
    const val VOICE_CALL_ENABLED = false
    const val MICROPHONE_ENABLED = false

    const val HUMAN_ESCALATION_MARKER = "[HUMAN_SUPPORT]"

    fun computeInitialGreeting(userMessage: String): String {
        val trimmed = userMessage.trim()
        val lower = trimmed.lowercase(Locale.ENGLISH)
        return when {
            trimmed.contains("আসসালামু") ||
                trimmed.contains("সালাম") ||
                lower.contains("assalamu") ||
                lower.contains("salam") ->
                "ওয়ালাইকুম আসসালাম। কেমন আছেন? বলুন, কীভাবে আপনাকে সাহায্য করতে পারি?"

            trimmed.contains("নমস্কার") ||
                lower.contains("nomoskar") ||
                lower.contains("namaskar") ->
                "নমস্কার! বলুন, কীভাবে আপনাকে সাহায্য করতে পারি?"

            trimmed.contains("আদাব") || lower.contains("adab") ->
                "আদাব! বলুন, কীভাবে আপনাকে সাহায্য করতে পারি?"

            trimmed.contains("ধন্যবাদ") ||
                lower.contains("thank") ->
                "আপনাকে স্বাগতম। যেকোনো প্রয়োজনে আমাদের জানাবেন।"

            isLikelyEnglish(trimmed) ->
                "Hello! Welcome to Hishaber Khata Support. How can I help you today?"

            else ->
                SUPPORT_WELCOME_GREETING_BN
        }
    }

    fun isLikelyEnglish(text: String): Boolean {
        if (text.isBlank()) return false
        val hasBengali = text.any { it in '\u0980'..'\u09FF' }
        if (hasBengali) return false
        return text.any { it in 'a'..'z' || it in 'A'..'Z' }
    }

    fun buildHumanEscalationNotice(isEnglish: Boolean): String = if (isEnglish) {
        "If this issue requires manual verification or remains unresolved, please contact our official helpline at $SUPPORT_HELPLINE_NUMBER."
    } else {
        "সমস্যাটি সমাধান না হলে অথবা সরাসরি যাচাইয়ের প্রয়োজনে আমাদের অফিশিয়াল হেল্পলাইন $SUPPORT_HELPLINE_NUMBER নম্বরে যোগাযোগ করুন।"
    }
}
