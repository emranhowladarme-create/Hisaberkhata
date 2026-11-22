package com.example.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

data class HishabiLocalPrefs(
    val decimalEnabled: Boolean = true,
    val notificationSoundEnabled: Boolean = true,
    val voiceAlertEnabled: Boolean = true,
    val pinlessEntryEnabled: Boolean = false,
    val nidNumber: String = "4186402238",
    val businessAgeYears: String = "২ বছর",
    val nomineeName: String = "ইমরান স্টোর",
    val nomineeRelation: String = "ব্যবসায়িক অংশীদার / পরিবার",
    val nomineeVerified: Boolean = true,
    val personalUseMode: Boolean = false,
    val goldMembershipActive: Boolean = false,
    val goldAutoRenew: Boolean = true,
    val preferredPaymentProvider: String = "bkash",
    val goldPackageId: String = "",
    val goldPackageTitle: String = "",
    val goldPackageDurationDays: Int = 0,
    val goldPackagePriceTaka: Int = 0,
    val goldStartTimeMillis: Long = 0L,
    val goldExpiryTimeMillis: Long = 0L,
    val goldLastVerifiedTxId: String = "",
    val goldProcessedTxIds: Set<String> = emptySet(),
    val businessLogoPath: String = "",
    val selectedVoiceProfileId: String = "imran",
    val voiceAutoSpeakEnabled: Boolean = false,
    val preferSmsFallback: Boolean = true,
    val verifiedPhoneNumber: String = "",
    val isPhoneSessionVerified: Boolean = false,
)

object HishabiPrefsStore {
    private const val PREFS_NAME = "hishabi_khata_prefs"

    private const val KEY_DECIMAL = "decimal_enabled"
    private const val KEY_NOTIF_SOUND = "notif_sound_enabled"
    private const val KEY_VOICE_ALERT = "voice_alert_enabled"
    private const val KEY_PINLESS_ENTRY = "pinless_entry_enabled"
    private const val KEY_NID = "nid_number"
    private const val KEY_BIZ_AGE = "business_age_years"
    private const val KEY_NOMINEE_NAME = "nominee_name"
    private const val KEY_NOMINEE_REL = "nominee_relation"
    private const val KEY_NOMINEE_VERIFIED = "nominee_verified"
    private const val KEY_PERSONAL_USE = "personal_use_mode"
    private const val KEY_GOLD_ACTIVE = "gold_membership_active"
    private const val KEY_GOLD_AUTO_RENEW = "gold_auto_renew"
    private const val KEY_GOLD_PROVIDER = "preferred_payment_provider"
    private const val KEY_GOLD_PKG_ID = "gold_package_id"
    private const val KEY_GOLD_PKG_TITLE = "gold_package_title"
    private const val KEY_GOLD_PKG_DAYS = "gold_package_duration_days"
    private const val KEY_GOLD_PKG_PRICE = "gold_package_price_taka"
    private const val KEY_GOLD_START_MS = "gold_start_time_millis"
    private const val KEY_GOLD_EXPIRY_MS = "gold_expiry_time_millis"
    private const val KEY_GOLD_LAST_TX = "gold_last_verified_tx_id"
    private const val KEY_GOLD_PROCESSED_TXS = "gold_processed_tx_ids"
    private const val KEY_BIZ_LOGO_PATH = "business_logo_path"
    private const val KEY_VOICE_PROFILE_ID = "ai_voice_profile_id"
    private const val KEY_VOICE_AUTO_SPEAK = "ai_voice_auto_speak"
    private const val KEY_PREFER_SMS_FALLBACK = "ai_prefer_sms_fallback"
    private const val KEY_VERIFIED_PHONE = "verified_phone_number"
    private const val KEY_PHONE_SESSION_VERIFIED = "phone_session_verified"

    private val _prefsFlow = MutableStateFlow(HishabiLocalPrefs())
    val prefsFlow: StateFlow<HishabiLocalPrefs> = _prefsFlow.asStateFlow()

    @Volatile
    private var initialized = false

    fun load(context: Context): HishabiLocalPrefs {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rawLogoPath = sp.getString(KEY_BIZ_LOGO_PATH, "").orEmpty()
        val validLogoPath = if (rawLogoPath.isNotBlank() && File(rawLogoPath).exists()) rawLogoPath else ""
        val rawTxSet = sp.getStringSet(KEY_GOLD_PROCESSED_TXS, emptySet()).orEmpty()
            .filter { it.isNotBlank() }
            .toSet()
        val loaded = HishabiLocalPrefs(
            decimalEnabled = sp.getBoolean(KEY_DECIMAL, true),
            notificationSoundEnabled = sp.getBoolean(KEY_NOTIF_SOUND, true),
            voiceAlertEnabled = sp.getBoolean(KEY_VOICE_ALERT, true),
            pinlessEntryEnabled = sp.getBoolean(KEY_PINLESS_ENTRY, false),
            nidNumber = sp.getString(KEY_NID, "4186402238").orEmpty(),
            businessAgeYears = sp.getString(KEY_BIZ_AGE, "২ বছর").orEmpty(),
            nomineeName = sp.getString(KEY_NOMINEE_NAME, "ইমরান স্টোর").orEmpty(),
            nomineeRelation = sp.getString(KEY_NOMINEE_REL, "পরিবার ও নমিনি").orEmpty(),
            nomineeVerified = sp.getBoolean(KEY_NOMINEE_VERIFIED, true),
            personalUseMode = sp.getBoolean(KEY_PERSONAL_USE, false),
            goldMembershipActive = sp.getBoolean(KEY_GOLD_ACTIVE, false),
            goldAutoRenew = sp.getBoolean(KEY_GOLD_AUTO_RENEW, true),
            preferredPaymentProvider = sp.getString(KEY_GOLD_PROVIDER, "bkash").orEmpty(),
            goldPackageId = sp.getString(KEY_GOLD_PKG_ID, "").orEmpty(),
            goldPackageTitle = sp.getString(KEY_GOLD_PKG_TITLE, "").orEmpty(),
            goldPackageDurationDays = sp.getInt(KEY_GOLD_PKG_DAYS, 0),
            goldPackagePriceTaka = sp.getInt(KEY_GOLD_PKG_PRICE, 0),
            goldStartTimeMillis = sp.getLong(KEY_GOLD_START_MS, 0L),
            goldExpiryTimeMillis = sp.getLong(KEY_GOLD_EXPIRY_MS, 0L),
            goldLastVerifiedTxId = sp.getString(KEY_GOLD_LAST_TX, "").orEmpty(),
            goldProcessedTxIds = rawTxSet,
            businessLogoPath = validLogoPath,
            selectedVoiceProfileId = sp.getString(KEY_VOICE_PROFILE_ID, "imran").orEmpty().ifBlank { "imran" },
            voiceAutoSpeakEnabled = sp.getBoolean(KEY_VOICE_AUTO_SPEAK, false),
            preferSmsFallback = sp.getBoolean(KEY_PREFER_SMS_FALLBACK, true),
            verifiedPhoneNumber = sp.getString(KEY_VERIFIED_PHONE, "").orEmpty(),
            isPhoneSessionVerified = sp.getBoolean(KEY_PHONE_SESSION_VERIFIED, false),
        )
        _prefsFlow.value = loaded
        initialized = true
        return loaded
    }

    fun update(context: Context, transform: (HishabiLocalPrefs) -> HishabiLocalPrefs): HishabiLocalPrefs {
        val current = if (initialized) _prefsFlow.value else load(context)
        val next = transform(current)
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit()
            .putBoolean(KEY_DECIMAL, next.decimalEnabled)
            .putBoolean(KEY_NOTIF_SOUND, next.notificationSoundEnabled)
            .putBoolean(KEY_VOICE_ALERT, next.voiceAlertEnabled)
            .putBoolean(KEY_PINLESS_ENTRY, next.pinlessEntryEnabled)
            .putString(KEY_NID, next.nidNumber)
            .putString(KEY_BIZ_AGE, next.businessAgeYears)
            .putString(KEY_NOMINEE_NAME, next.nomineeName)
            .putString(KEY_NOMINEE_REL, next.nomineeRelation)
            .putBoolean(KEY_NOMINEE_VERIFIED, next.nomineeVerified)
            .putBoolean(KEY_PERSONAL_USE, next.personalUseMode)
            .putBoolean(KEY_GOLD_ACTIVE, next.goldMembershipActive)
            .putBoolean(KEY_GOLD_AUTO_RENEW, next.goldAutoRenew)
            .putString(KEY_GOLD_PROVIDER, next.preferredPaymentProvider)
            .putString(KEY_GOLD_PKG_ID, next.goldPackageId)
            .putString(KEY_GOLD_PKG_TITLE, next.goldPackageTitle)
            .putInt(KEY_GOLD_PKG_DAYS, next.goldPackageDurationDays)
            .putInt(KEY_GOLD_PKG_PRICE, next.goldPackagePriceTaka)
            .putLong(KEY_GOLD_START_MS, next.goldStartTimeMillis)
            .putLong(KEY_GOLD_EXPIRY_MS, next.goldExpiryTimeMillis)
            .putString(KEY_GOLD_LAST_TX, next.goldLastVerifiedTxId)
            .putStringSet(KEY_GOLD_PROCESSED_TXS, next.goldProcessedTxIds)
            .putString(KEY_BIZ_LOGO_PATH, next.businessLogoPath)
            .putString(KEY_VOICE_PROFILE_ID, next.selectedVoiceProfileId)
            .putBoolean(KEY_VOICE_AUTO_SPEAK, next.voiceAutoSpeakEnabled)
            .putBoolean(KEY_PREFER_SMS_FALLBACK, next.preferSmsFallback)
            .putString(KEY_VERIFIED_PHONE, next.verifiedPhoneNumber)
            .putBoolean(KEY_PHONE_SESSION_VERIFIED, next.isPhoneSessionVerified)
            .apply()
        _prefsFlow.value = next
        initialized = true
        return next
    }

    fun persistBusinessLogo(context: Context, sourceUri: Uri): String? {
        return runCatching {
            val dir = File(context.filesDir, "business_assets").apply { mkdirs() }
            val dest = File(dir, "business_logo_${System.currentTimeMillis()}.jpg")
            dir.listFiles()?.forEach { old ->
                if (old.name.startsWith("business_logo_")) {
                    old.delete()
                }
            }
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                dest.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            if (dest.exists() && dest.length() > 0L) {
                update(context) { it.copy(businessLogoPath = dest.absolutePath) }
                dest.absolutePath
            } else {
                null
            }
        }.getOrNull()
    }
}
