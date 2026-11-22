package com.example.util

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory

private const val TAG = "AppCheckInit"

private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
private const val META_PREFS_NAME = "hishabi_appcheck_meta"
private const val META_HAS_REGISTERED_DEBUG_TOKEN = "has_intent_debug_token"
private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"

fun configureAppCheck(context: Context, intent: Intent?) {
    try {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }

        val persistenceKey = FirebaseApp.getInstance().persistenceKey
        val debugPrefs = context.getSharedPreferences(
            "$DEBUG_STORE_PREFIX$persistenceKey",
            Context.MODE_PRIVATE
        )
        val metaPrefs = context.getSharedPreferences(META_PREFS_NAME, Context.MODE_PRIVATE)
        val intentDebugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)?.trim()

        if (BuildConfig.DEBUG) {
            if (!intentDebugToken.isNullOrEmpty()) {
                // Synchronous commit required before DebugAppCheckProviderFactory reads it.
                debugPrefs.edit()
                    .putString(DEBUG_SECRET_KEY, intentDebugToken)
                    .commit()
                metaPrefs.edit()
                    .putBoolean(META_HAS_REGISTERED_DEBUG_TOKEN, true)
                    .commit()

                val appCheck = FirebaseAppCheck.getInstance()
                appCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
                appCheck.setTokenAutoRefreshEnabled(true)
                Log.d(TAG, "DebugAppCheckProviderFactory installed with Intent debug secret.")
                return
            }

            val hasPriorIntentToken = metaPrefs.getBoolean(META_HAS_REGISTERED_DEBUG_TOKEN, false)
            val storedDebugSecret = debugPrefs.getString(DEBUG_SECRET_KEY, null)
            if (hasPriorIntentToken && !storedDebugSecret.isNullOrBlank()) {
                val appCheck = FirebaseAppCheck.getInstance()
                appCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
                appCheck.setTokenAutoRefreshEnabled(true)
                Log.d(TAG, "DebugAppCheckProviderFactory installed using persisted Intent debug secret.")
                return
            }

            // No registered debug secret from harness (e.g. direct APK install on device):
            // Clear any unregistered self-generated UUID and use provisioned reCAPTCHA Enterprise.
            if (!storedDebugSecret.isNullOrBlank()) {
                debugPrefs.edit().remove(DEBUG_SECRET_KEY).commit()
            }
        }

        val resId = context.resources.getIdentifier(
            RECAPTCHA_SITE_KEY_RES,
            "string",
            context.packageName
        )
        if (resId != 0) {
            val siteKey = context.getString(resId)
            if (siteKey.isNotBlank()) {
                val appCheck = FirebaseAppCheck.getInstance()
                appCheck.installAppCheckProviderFactory(
                    RecaptchaAppCheckProviderFactory.getInstance(siteKey)
                )
                appCheck.setTokenAutoRefreshEnabled(true)
                warmUpAppCheck(appCheck)
                Log.d(TAG, "RecaptchaAppCheckProviderFactory installed.")
                return
            } else {
                Log.e(TAG, "recaptcha_site_key resource string is blank.")
            }
        } else {
            Log.e(TAG, "recaptcha_site_key string resource not found in package.")
        }

        if (BuildConfig.DEBUG) {
            Log.w(TAG, "Falling back to DebugAppCheckProviderFactory without Intent token.")
            val appCheck = FirebaseAppCheck.getInstance()
            appCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            warmUpAppCheck(appCheck)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize Firebase App Check", e)
    }
}

private fun warmUpAppCheck(appCheck: FirebaseAppCheck) {
    runCatching {
        appCheck.getAppCheckToken(false)
            .addOnSuccessListener {
                Log.d(TAG, "App Check token cached at startup.")
            }
            .addOnFailureListener { err ->
                Log.w(TAG, "App Check startup warm-up failed: ${err.message}")
            }
    }
}
