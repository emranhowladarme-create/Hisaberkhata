package com.example.util

import android.content.Context
import android.net.Uri
import java.io.File

object CustomerPhotoStore {
    private const val PREFS_NAME = "hisaber_customer_photos"

    fun persistPhotoForCustomer(context: Context, customerId: String, sourceUri: Uri): String? {
        if (customerId.isBlank()) return null
        return runCatching {
            val dir = File(context.filesDir, "customer_photos").apply { mkdirs() }
            val dest = File(dir, "cust_$customerId.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                dest.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            if (dest.exists() && dest.length() > 0L) {
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(customerId, dest.absolutePath)
                    .apply()
                dest.absolutePath
            } else {
                null
            }
        }.getOrNull()
    }

    fun getPhotoPath(context: Context, customerId: String): String {
        if (customerId.isBlank()) return ""
        val path = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(customerId, "")
            .orEmpty()
        return if (path.isNotBlank() && File(path).exists()) path else ""
    }
}
