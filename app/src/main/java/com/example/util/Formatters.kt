package com.example.util

import java.security.MessageDigest
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

object Formatters {
    private val bdNumberFormat = NumberFormat.getNumberInstance(Locale.Builder().setLanguage("en").setRegion("BD").build())

    private val banglaMonths = arrayOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )

    fun toBanglaDigits(text: String): String {
        val bangla = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        val sb = StringBuilder(text.length)
        for (ch in text) {
            if (ch in '0'..'9') {
                sb.append(bangla[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatTaka(amount: Double, useBanglaDigits: Boolean = false): String {
        val rounded = abs(amount).roundToLong()
        val formatted = bdNumberFormat.format(rounded)
        val sign = if (amount < 0) "-" else ""
        val value = "${sign}৳ $formatted"
        return if (useBanglaDigits) toBanglaDigits(value) else value
    }

    fun formatDecimalAmount(amount: Double, useBanglaDigits: Boolean = true): String {
        val raw = String.format(Locale.US, "%.2f", abs(amount))
        val parts = raw.split('.')
        val whole = parts.getOrElse(0) { "0" }.toLongOrNull() ?: 0L
        val wholeFormatted = bdNumberFormat.format(whole)
        val frac = parts.getOrElse(1) { "00" }
        val result = "$wholeFormatted.$frac"
        return if (useBanglaDigits) toBanglaDigits(result) else result
    }

    fun formatTakaDecimal(amount: Double, useBanglaDigits: Boolean = true): String {
        val sign = if (amount < 0) "-" else ""
        return "${sign}৳ ${formatDecimalAmount(amount, useBanglaDigits)}"
    }

    fun formatBanglaDate(millis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        val day = toBanglaDigits(cal.get(Calendar.DAY_OF_MONTH).toString())
        val month = banglaMonths[cal.get(Calendar.MONTH).coerceIn(0, 11)]
        val year = toBanglaDigits(cal.get(Calendar.YEAR).toString())
        return "$day $month $year"
    }

    fun formatBanglaDayMonth(millis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        val day = toBanglaDigits(cal.get(Calendar.DAY_OF_MONTH).toString())
        val month = banglaMonths[cal.get(Calendar.MONTH).coerceIn(0, 11)]
        return "$day $month"
    }

    fun formatShortDate(millis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        val d = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        val m = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val y = cal.get(Calendar.YEAR).toString()
        return toBanglaDigits("$d/$m/$y")
    }

    fun formatBanglaClockTime(millis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        val hour24 = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE).toString().padStart(2, '0')
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        val period = when (hour24) {
            in 4..5 -> "ভোর"
            in 6..11 -> "সকাল"
            in 12..15 -> "দুপুর"
            in 16..17 -> "বিকাল"
            in 18..19 -> "সন্ধ্যা"
            else -> "রাত"
        }
        return "$period ${toBanglaDigits(hour12.toString())}:${toBanglaDigits(minute)}"
    }

    fun formatBanglaDateTime(millis: Long): String {
        if (millis <= 0L) return "নতুন এন্ট্রি"
        return "${formatBanglaDayMonth(millis)}, ${formatBanglaClockTime(millis)}"
    }

    fun formatRelativeBanglaTime(millis: Long, nowMillis: Long = System.currentTimeMillis()): String {
        if (millis <= 0L) return "নতুন এন্ট্রি"
        val diff = (nowMillis - millis).coerceAtLeast(0L)
        val seconds = (diff / 1000L).coerceAtLeast(1L)
        val minutes = diff / 60_000L
        val hours = diff / 3_600_000L
        val days = diff / 86_400_000L
        return when {
            diff < 60_000L -> "${toBanglaDigits(seconds.toString())} সেকেন্ড আগে"
            minutes < 60L -> "${toBanglaDigits(minutes.toString())} মিনিট আগে"
            hours < 24L -> "${toBanglaDigits(hours.toString())} ঘণ্টা আগে"
            days == 1L -> "১ দিন আগে"
            else -> "${toBanglaDigits(days.toString())} দিন আগে"
        }
    }

    fun formatCustomerEntryTimestamp(millis: Long, nowMillis: Long = System.currentTimeMillis()): String {
        if (millis <= 0L) return "নতুন এন্ট্রি"
        val relative = formatRelativeBanglaTime(millis, nowMillis)
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        val hour24 = cal.get(Calendar.HOUR_OF_DAY)
        val amPm = if (hour24 < 12) "AM" else "PM"
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        val minute = cal.get(Calendar.MINUTE).toString().padStart(2, '0')
        val dateStr = formatBanglaDate(millis)
        val clockStr = "${toBanglaDigits(hour12.toString())}:${toBanglaDigits(minute)} $amPm"
        return "$relative • $dateStr, $clockStr"
    }

    fun isSameDay(millisA: Long, millisB: Long): Boolean {
        val a = Calendar.getInstance().apply { timeInMillis = millisA }
        val b = Calendar.getInstance().apply { timeInMillis = millisB }
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }

    fun startOfTodayMillis(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun firstLetterAvatar(name: String): String {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return "ক"
        return trimmed.substring(0, 1)
    }

    fun sha256Hex(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
