package com.example.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.content.PackageManagerCompat
import androidx.core.content.UnusedAppRestrictionsConstants
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume

enum class AppPermissionType(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
) {
    CAMERA(
        id = "camera",
        title = "Camera",
        subtitle = "Access to camera",
        description = "Used to capture customer receipts, invoice documents, and scan Hisabi QR codes directly from the app.",
    ),
    CONTACTS(
        id = "contacts",
        title = "Contacts",
        subtitle = "Access to contacts",
        description = "Used to quickly select customers and suppliers from your phone address book when creating ledger entries.",
    ),
    LOCATION(
        id = "location",
        title = "Location",
        subtitle = "Access to location",
        description = "Used for optional shop/branch verification and regional business context when you explicitly trigger a location check.",
    ),
    NOTIFICATIONS(
        id = "notifications",
        title = "Notifications",
        subtitle = "Show notifications",
        description = "Used to deliver payment reminders, backup status updates, and due-collection alerts.",
    ),
    PHONE(
        id = "phone",
        title = "Phone",
        subtitle = "Access to phone state",
        description = "Used to verify device phone state during SIM/number verification and dual-SIM helpline calling readiness.",
    ),
}

enum class PermissionGrantStatus {
    ALLOWED,
    DENIED,
    PERMANENTLY_DENIED,
}

data class AppPermissionState(
    val type: AppPermissionType,
    val status: PermissionGrantStatus,
    val statusLabel: String,
    val detailNote: String,
    val requiresSystemSettingsOnly: Boolean = false,
) {
    val isAllowed: Boolean
        get() = status == PermissionGrantStatus.ALLOWED
}

data class LocationAccessRecord(
    val timestampMillis: Long,
    val label: String,
    val provider: String,
    val accuracyMeters: Float? = null,
)

enum class UnusedAppRestrictionState(
    val badgeLabel: String,
    val detailText: String,
) {
    CHECKING(
        badgeLabel = "Checking",
        detailText = "Checking Android unused-app restriction status...",
    ),
    ACTIVE(
        badgeLabel = "Enabled",
        detailText = "Android will remove permissions and archive if unused. Tap to manage in App Info.",
    ),
    EXEMPT(
        badgeLabel = "Off",
        detailText = "Unused-app restrictions are currently turned off in Android settings. Tap to manage.",
    ),
    NOT_AVAILABLE(
        badgeLabel = "System",
        detailText = "Tap to review App Info and permissions in Android system settings.",
    ),
    ERROR(
        badgeLabel = "Settings",
        detailText = "Tap to open Android App Info and review unused-app settings.",
    ),
}

object PermissionCenterManager {
    private const val PREFS_NAME = "hisaber_permission_center_prefs"
    private const val KEY_LOCATION_HISTORY = "location_access_history_json"
    private const val KEY_REQUESTED_PREFIX = "requested_perm_"
    private const val MAX_LOCATION_HISTORY = 25

    tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    fun hasCameraPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED

    fun hasContactsPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED

    fun hasFineLocationPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    fun hasCoarseLocationPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    fun hasLocationPermission(context: Context): Boolean =
        hasFineLocationPermission(context) || hasCoarseLocationPermission(context)

    fun hasNotificationPermission(context: Context): Boolean {
        val systemNotificationsEnabled = runCatching {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }.getOrDefault(true)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED && systemNotificationsEnabled
        } else {
            systemNotificationsEnabled
        }
    }

    fun hasPhonePermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) ==
            PackageManager.PERMISSION_GRANTED

    fun markPermissionRequested(context: Context, type: AppPermissionType) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_REQUESTED_PREFIX + type.id, true)
            .apply()
    }

    private fun wasPermissionRequested(context: Context, type: AppPermissionType): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_REQUESTED_PREFIX + type.id, false)

    fun runtimePermissionsFor(type: AppPermissionType): Array<String> = when (type) {
        AppPermissionType.CAMERA -> arrayOf(Manifest.permission.CAMERA)
        AppPermissionType.CONTACTS -> arrayOf(Manifest.permission.READ_CONTACTS)
        AppPermissionType.LOCATION -> arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
        AppPermissionType.NOTIFICATIONS -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyArray()
        }
        AppPermissionType.PHONE -> arrayOf(Manifest.permission.READ_PHONE_STATE)
    }

    fun evaluatePermission(
        context: Context,
        type: AppPermissionType,
        activity: Activity? = context.findActivity(),
    ): AppPermissionState {
        val granted = when (type) {
            AppPermissionType.CAMERA -> hasCameraPermission(context)
            AppPermissionType.CONTACTS -> hasContactsPermission(context)
            AppPermissionType.LOCATION -> hasLocationPermission(context)
            AppPermissionType.NOTIFICATIONS -> hasNotificationPermission(context)
            AppPermissionType.PHONE -> hasPhonePermission(context)
        }

        if (granted) {
            val detail = when (type) {
                AppPermissionType.LOCATION -> if (hasFineLocationPermission(context)) {
                    "Precise location is allowed while using the app."
                } else {
                    "Approximate location is allowed while using the app."
                }
                AppPermissionType.NOTIFICATIONS -> if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    "App notifications are enabled on Android ${Build.VERSION.RELEASE}."
                } else {
                    "POST_NOTIFICATIONS permission is granted."
                }
                else -> "Permission is granted by the system."
            }
            return AppPermissionState(
                type = type,
                status = PermissionGrantStatus.ALLOWED,
                statusLabel = "Allowed",
                detailNote = detail,
                requiresSystemSettingsOnly = false,
            )
        }

        if (type == AppPermissionType.NOTIFICATIONS && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return AppPermissionState(
                type = type,
                status = PermissionGrantStatus.PERMANENTLY_DENIED,
                statusLabel = "Denied",
                detailNote = "Notifications are disabled in system settings for this app.",
                requiresSystemSettingsOnly = true,
            )
        }

        val perms = runtimePermissionsFor(type)
        val shouldShowRationale = activity != null && perms.any { perm ->
            ActivityCompat.shouldShowRequestPermissionRationale(activity, perm)
        }
        val wasRequested = wasPermissionRequested(context, type)
        val isPermanentlyDenied = wasRequested && !shouldShowRationale

        return if (isPermanentlyDenied) {
            AppPermissionState(
                type = type,
                status = PermissionGrantStatus.PERMANENTLY_DENIED,
                statusLabel = "Denied",
                detailNote = "Permission was denied. Enable it from Android App Settings.",
                requiresSystemSettingsOnly = true,
            )
        } else {
            AppPermissionState(
                type = type,
                status = PermissionGrantStatus.DENIED,
                statusLabel = "Denied",
                detailNote = "Permission has not been granted yet.",
                requiresSystemSettingsOnly = false,
            )
        }
    }

    fun evaluateAllPermissions(
        context: Context,
        activity: Activity? = context.findActivity(),
    ): List<AppPermissionState> = AppPermissionType.entries.map { type ->
        evaluatePermission(context, type, activity)
    }

    fun loadLocationHistory(context: Context): List<LocationAccessRecord> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LOCATION_HISTORY, null)
            .orEmpty()
        if (raw.isBlank()) return emptyList()

        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val ts = obj.optLong("timestampMillis", 0L)
                    val label = obj.optString("label", "").trim()
                    val provider = obj.optString("provider", "").trim()
                    val acc = if (obj.has("accuracyMeters") && !obj.isNull("accuracyMeters")) {
                        obj.optDouble("accuracyMeters").toFloat()
                    } else {
                        null
                    }
                    if (ts > 0L && label.isNotBlank()) {
                        add(
                            LocationAccessRecord(
                                timestampMillis = ts,
                                label = label,
                                provider = provider,
                                accuracyMeters = acc,
                            )
                        )
                    }
                }
            }.sortedByDescending { it.timestampMillis }
        }.getOrDefault(emptyList())
    }

    fun recordLocationAccess(context: Context, record: LocationAccessRecord): List<LocationAccessRecord> {
        val updated = (listOf(record) + loadLocationHistory(context))
            .sortedByDescending { it.timestampMillis }
            .take(MAX_LOCATION_HISTORY)

        val arr = JSONArray().apply {
            updated.forEach { item ->
                put(
                    JSONObject().apply {
                        put("timestampMillis", item.timestampMillis)
                        put("label", item.label)
                        put("provider", item.provider)
                        if (item.accuracyMeters != null) {
                            put("accuracyMeters", item.accuracyMeters.toDouble())
                        }
                    }
                )
            }
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LOCATION_HISTORY, arr.toString())
            .apply()

        return updated
    }

    fun clearLocationHistory(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_LOCATION_HISTORY)
            .apply()
    }

    fun isLocationServiceEnabled(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return runCatching { LocationManagerCompat.isLocationEnabled(lm) }.getOrDefault(false)
    }

    /**
     * Fetches the actual current or last-known device location from Android [LocationManager].
     * Returns failure if Location permission is denied or no real location fix is available.
     * Never fabricates or hardcodes fake location coordinates or place names.
     */
    @SuppressLint("MissingPermission")
    suspend fun fetchAndRecordRealLocation(context: Context): Result<LocationAccessRecord> {
        if (!hasLocationPermission(context)) {
            return Result.failure(SecurityException("Location permission is not granted."))
        }
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return Result.failure(IllegalStateException("LocationManager is unavailable on this device."))

        val location = acquireRealLocation(context, lm)
            ?: return Result.failure(
                IllegalStateException(
                    if (isLocationServiceEnabled(context)) {
                        "No GPS or network location fix available right now."
                    } else {
                        "Device Location services are turned off."
                    }
                )
            )

        val label = resolveRealLocationLabel(context, location)
        val record = LocationAccessRecord(
            timestampMillis = System.currentTimeMillis(),
            label = label,
            provider = location.provider ?: "location",
            accuracyMeters = if (location.hasAccuracy()) location.accuracy else null,
        )
        recordLocationAccess(context, record)
        return Result.success(record)
    }

    @SuppressLint("MissingPermission")
    private suspend fun acquireRealLocation(context: Context, lm: LocationManager): Location? {
        val candidates = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(LocationManager.FUSED_PROVIDER)
            }
            if (hasFineLocationPermission(context)) {
                add(LocationManager.GPS_PROVIDER)
            }
            add(LocationManager.NETWORK_PROVIDER)
            add(LocationManager.PASSIVE_PROVIDER)
        }.distinct()

        // Prefer a fresh active current-location lookup if a provider is enabled.
        val activeProvider = candidates.firstOrNull { provider ->
            provider != LocationManager.PASSIVE_PROVIDER &&
                runCatching { lm.isProviderEnabled(provider) }.getOrDefault(false)
        }

        if (activeProvider != null) {
            val fresh = withTimeoutOrNull(5500L) {
                suspendCancellableCoroutine<Location?> { cont ->
                    val signal = CancellationSignal()
                    cont.invokeOnCancellation { signal.cancel() }
                    runCatching {
                        LocationManagerCompat.getCurrentLocation(
                            lm,
                            activeProvider,
                            signal,
                            ContextCompat.getMainExecutor(context),
                        ) { loc ->
                            if (cont.isActive) cont.resume(loc)
                        }
                    }.onFailure {
                        if (cont.isActive) cont.resume(null)
                    }
                }
            }
            if (fresh != null) return fresh
        }

        // Fall back to the freshest real lastKnownLocation recorded by the OS.
        return candidates
            .mapNotNull { provider -> runCatching { lm.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull { it.time }
    }

    @Suppress("DEPRECATION")
    private suspend fun resolveRealLocationLabel(context: Context, location: Location): String =
        withContext(Dispatchers.IO) {
            val coordFallback = String.format(
                Locale.US,
                "Lat %.4f, Lng %.4f",
                location.latitude,
                location.longitude,
            )
            if (!Geocoder.isPresent()) return@withContext coordFallback

            runCatching {
                val geocoder = Geocoder(context, Locale.getDefault())
                val address = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocation(location.latitude, location.longitude, 1) { list ->
                            if (cont.isActive) cont.resume(list.firstOrNull())
                        }
                    }
                } else {
                    geocoder.getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()
                }
                val parts = listOfNotNull(
                    address?.locality?.takeIf { it.isNotBlank() }
                        ?: address?.subAdminArea?.takeIf { it.isNotBlank() },
                    address?.countryName?.takeIf { it.isNotBlank() },
                ).distinct()
                if (parts.isNotEmpty()) parts.joinToString(", ") else coordFallback
            }.getOrDefault(coordFallback)
        }

    fun fetchUnusedAppRestrictionsState(
        context: Context,
        onResult: (UnusedAppRestrictionState) -> Unit,
    ) {
        runCatching {
            val future = PackageManagerCompat.getUnusedAppRestrictionsStatus(context)
            future.addListener(
                {
                    val code = runCatching { future.get() }
                        .getOrDefault(UnusedAppRestrictionsConstants.ERROR)
                    val mapped = when (code) {
                        UnusedAppRestrictionsConstants.DISABLED -> UnusedAppRestrictionState.EXEMPT
                        UnusedAppRestrictionsConstants.API_30_BACKPORT,
                        UnusedAppRestrictionsConstants.API_30,
                        UnusedAppRestrictionsConstants.API_31 -> UnusedAppRestrictionState.ACTIVE
                        UnusedAppRestrictionsConstants.FEATURE_NOT_AVAILABLE -> UnusedAppRestrictionState.NOT_AVAILABLE
                        else -> fallbackAutoRevokeState(context)
                    }
                    onResult(mapped)
                },
                ContextCompat.getMainExecutor(context),
            )
        }.onFailure {
            onResult(fallbackAutoRevokeState(context))
        }
    }

    private fun fallbackAutoRevokeState(context: Context): UnusedAppRestrictionState {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val whitelisted = runCatching { context.packageManager.isAutoRevokeWhitelisted }.getOrNull()
            if (whitelisted == true) return UnusedAppRestrictionState.EXEMPT
            if (whitelisted == false) return UnusedAppRestrictionState.ACTIVE
        }
        return UnusedAppRestrictionState.NOT_AVAILABLE
    }

    fun createAppSettingsIntent(context: Context): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

    fun openAppSettings(context: Context) {
        runCatching { context.startActivity(createAppSettingsIntent(context)) }
    }

    fun openNotificationSettings(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            createAppSettingsIntent(context)
        }
        runCatching { context.startActivity(intent) }
            .onFailure { openAppSettings(context) }
    }

    fun openLocationSourceSettings(context: Context) {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }.onFailure { openAppSettings(context) }
    }

    fun createManageUnusedAppIntent(context: Context): Intent =
        runCatching {
            IntentCompat.createManageUnusedAppRestrictionsIntent(context, context.packageName)
        }.getOrElse {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }

    fun formatDateOnly(timestampMillis: Long): String =
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestampMillis))

    fun formatTimeOnly(timestampMillis: Long): String =
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestampMillis))

    fun formatDateTimeBanner(timestampMillis: Long): String =
        "${formatDateOnly(timestampMillis)} • ${formatTimeOnly(timestampMillis)}"
}
