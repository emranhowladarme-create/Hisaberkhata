package com.example.util

import android.content.Context

enum class GoldPackagePlan(
    val id: String,
    val durationTitleBn: String,
    val durationSubtitleBn: String,
    val durationDetailBn: String,
    val bannerTitleBn: String,
    val durationDays: Int,
    val priceTaka: Int,
    val priceDisplayBn: String,
) {
    GOLD_1M(
        id = "gold_1m",
        durationTitleBn = "এক মাস",
        durationSubtitleBn = "(৩০ দিন)",
        durationDetailBn = "১ মাস (৩০ দিন)",
        bannerTitleBn = "১ মাস (৩০ দিন) প্যাকেজ",
        durationDays = 30,
        priceTaka = 79,
        priceDisplayBn = "৳৭৯ টাকা",
    ),
    GOLD_2M(
        id = "gold_2m",
        durationTitleBn = "দুই মাস",
        durationSubtitleBn = "(৬০ দিন)",
        durationDetailBn = "২ মাস (৬০ দিন)",
        bannerTitleBn = "২ মাস (৬০ দিন) প্যাকেজ",
        durationDays = 60,
        priceTaka = 150,
        priceDisplayBn = "৳১৫০ টাকা",
    ),
    GOLD_3M(
        id = "gold_3m",
        durationTitleBn = "তিন মাস",
        durationSubtitleBn = "(৯০ দিন)",
        durationDetailBn = "৩ মাস (৯০ দিন)",
        bannerTitleBn = "৩ মাস (৯০ দিন) প্যাকেজ",
        durationDays = 90,
        priceTaka = 210,
        priceDisplayBn = "৳২১০ টাকা",
    );

    companion object {
        fun fromIdOrDays(id: String?, days: Int = 0): GoldPackagePlan {
            return entries.find { it.id == id }
                ?: entries.find { it.durationDays == days }
                ?: GOLD_1M
        }
    }
}

data class GoldSubscriptionSnapshot(
    val plan: GoldPackagePlan,
    val hasEverPurchased: Boolean,
    val isActive: Boolean,
    val isExpired: Boolean,
    val remainingDays: Int,
    val startDateMillis: Long,
    val expiryDateMillis: Long,
    val paymentProviderId: String,
    val lastVerifiedTxId: String,
) {
    val remainingDaysBangla: String
        get() = "${Formatters.toBanglaDigits(remainingDays.coerceAtLeast(0).toString())} দিন"

    val statusTextBn: String
        get() = when {
            isActive -> "সক্রিয়"
            isExpired -> "মেয়াদ শেষ"
            else -> "নিষ্ক্রিয়"
        }
}

sealed interface GoldActivationOutcome {
    data class Activated(val snapshot: GoldSubscriptionSnapshot) : GoldActivationOutcome
    data class DuplicateTransaction(val transactionId: String) : GoldActivationOutcome
    data class InvalidPayment(val reasonBn: String) : GoldActivationOutcome
}

object GoldPackageManager {
    const val DAY_MILLIS: Long = 86_400_000L

    fun computeSnapshot(
        prefs: HishabiLocalPrefs,
        nowMillis: Long = System.currentTimeMillis(),
    ): GoldSubscriptionSnapshot {
        val plan = GoldPackagePlan.fromIdOrDays(prefs.goldPackageId, prefs.goldPackageDurationDays)
        val startMs = prefs.goldStartTimeMillis
        val expiryMs = prefs.goldExpiryTimeMillis
        val hasPurchased = startMs > 0L && expiryMs > 0L

        if (!hasPurchased) {
            return GoldSubscriptionSnapshot(
                plan = plan,
                hasEverPurchased = false,
                isActive = prefs.goldMembershipActive,
                isExpired = false,
                remainingDays = if (prefs.goldMembershipActive) plan.durationDays else 0,
                startDateMillis = nowMillis,
                expiryDateMillis = nowMillis + plan.durationDays * DAY_MILLIS,
                paymentProviderId = prefs.preferredPaymentProvider,
                lastVerifiedTxId = prefs.goldLastVerifiedTxId,
            )
        }

        val remainingMs = expiryMs - nowMillis
        val activeNow = prefs.goldMembershipActive && remainingMs > 0L
        val remainingDays = if (activeNow) {
            ((remainingMs + DAY_MILLIS - 1L) / DAY_MILLIS)
                .toInt()
                .coerceIn(1, plan.durationDays.coerceAtLeast(1))
        } else {
            0
        }

        return GoldSubscriptionSnapshot(
            plan = plan,
            hasEverPurchased = true,
            isActive = activeNow,
            isExpired = !activeNow,
            remainingDays = remainingDays,
            startDateMillis = startMs,
            expiryDateMillis = expiryMs,
            paymentProviderId = prefs.preferredPaymentProvider,
            lastVerifiedTxId = prefs.goldLastVerifiedTxId,
        )
    }

    fun syncExpiryIfNeeded(
        context: Context,
        nowMillis: Long = System.currentTimeMillis(),
    ): GoldSubscriptionSnapshot {
        val prefs = HishabiPrefsStore.load(context)
        val hasExpiry = prefs.goldExpiryTimeMillis > 0L
        if (prefs.goldMembershipActive && hasExpiry && nowMillis >= prefs.goldExpiryTimeMillis) {
            val updated = HishabiPrefsStore.update(context) { it.copy(goldMembershipActive = false) }
            return computeSnapshot(updated, nowMillis)
        }
        return computeSnapshot(prefs, nowMillis)
    }

    fun activateVerifiedGoldPackage(
        context: Context,
        plan: GoldPackagePlan,
        providerId: String,
        verifiedTxId: String,
        paidAmountTaka: Double,
        nowMillis: Long = System.currentTimeMillis(),
    ): GoldActivationOutcome {
        val cleanTxId = verifiedTxId.trim()
        if (cleanTxId.isBlank()) {
            return GoldActivationOutcome.InvalidPayment("পেমেন্ট ট্রানজেকশন যাচাই সম্পন্ন হয়নি।")
        }
        if (paidAmountTaka + 0.01 < plan.priceTaka.toDouble()) {
            return GoldActivationOutcome.InvalidPayment("পর্যাপ্ত পেমেন্ট পাওয়া যায়নি (${plan.priceDisplayBn} প্রয়োজন)।")
        }

        val current = HishabiPrefsStore.load(context)
        if (cleanTxId == current.goldLastVerifiedTxId || cleanTxId in current.goldProcessedTxIds) {
            return GoldActivationOutcome.DuplicateTransaction(cleanTxId)
        }

        val startMs = nowMillis
        val expiryMs = nowMillis + plan.durationDays * DAY_MILLIS
        val retainedTxIds = (current.goldProcessedTxIds.toList() + cleanTxId).takeLast(50).toSet()

        val updated = HishabiPrefsStore.update(context) {
            it.copy(
                goldMembershipActive = true,
                preferredPaymentProvider = providerId,
                goldPackageId = plan.id,
                goldPackageTitle = plan.bannerTitleBn,
                goldPackageDurationDays = plan.durationDays,
                goldPackagePriceTaka = plan.priceTaka,
                goldStartTimeMillis = startMs,
                goldExpiryTimeMillis = expiryMs,
                goldLastVerifiedTxId = cleanTxId,
                goldProcessedTxIds = retainedTxIds,
            )
        }
        return GoldActivationOutcome.Activated(computeSnapshot(updated, nowMillis))
    }
}
