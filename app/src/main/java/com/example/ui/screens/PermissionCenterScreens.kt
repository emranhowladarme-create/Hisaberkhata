package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.permissions.AppPermissionState
import com.example.permissions.AppPermissionType
import com.example.permissions.LocationAccessRecord
import com.example.permissions.PermissionCenterManager
import com.example.permissions.PermissionCenterManager.findActivity
import com.example.permissions.PermissionGrantStatus
import com.example.permissions.UnusedAppRestrictionState
import com.example.ui.theme.PureWhite
import kotlinx.coroutines.launch

private val PermGreenDeep = Color(0xFF0A7D37)
private val PermGreenPrimary = Color(0xFF109B4B)
private val PermGreenBright = Color(0xFF18AD54)
private val PermGreenWaveMint = Color(0xFF9EE8AE)
private val PermGreenLogoBg = Color(0xFF006B2E)
private val PermHeadingGreen = Color(0xFF0D4C28)
private val PermBodySlate = Color(0xFF475B4E)
private val PermCardBg = Color(0xFFFDFEFD)
private val PermCardBorder = Color(0xFFD8EFE0)
private val PermBannerMintBg = Color(0xFFE7F6EB)
private val PermBannerMintBorder = Color(0xFFC2E7CE)
private val PermDeniedRed = Color(0xFFDC2626)
private val PermDeniedBg = Color(0xFFFEF2F2)
private val PermDeniedBorder = Color(0xFFFECACA)
private val PermWarnBg = Color(0xFFFEF7DC)
private val PermWarnBorder = Color(0xFFF3D478)
private val PermWarnInk = Color(0xFF92400E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionCenterScreen(
    onBack: () -> Unit,
    onOpenLocationDetails: () -> Unit,
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val lifecycleOwner = LocalLifecycleOwner.current

    var permissionStates by remember {
        mutableStateOf(PermissionCenterManager.evaluateAllPermissions(context, activity))
    }
    var locationHistory by remember {
        mutableStateOf(PermissionCenterManager.loadLocationHistory(context))
    }
    var unusedRestrictionState by remember {
        mutableStateOf(UnusedAppRestrictionState.CHECKING)
    }
    var selectedDetailType by remember { mutableStateOf<AppPermissionType?>(null) }

    val refreshAll: () -> Unit = remember(context, activity) {
        {
            permissionStates = PermissionCenterManager.evaluateAllPermissions(context, activity)
            locationHistory = PermissionCenterManager.loadLocationHistory(context)
            PermissionCenterManager.fetchUnusedAppRestrictionsState(context) { state ->
                unusedRestrictionState = state
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshAll()
    }

    DisposableEffect(lifecycleOwner, refreshAll) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshAll()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val singlePermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        refreshAll()
    }

    val multiPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        refreshAll()
    }

    val unusedAppSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        refreshAll()
    }

    val requestPermissionFor: (AppPermissionState) -> Unit = { permState ->
        val perms = PermissionCenterManager.runtimePermissionsFor(permState.type)
        when {
            permState.requiresSystemSettingsOnly -> {
                if (permState.type == AppPermissionType.NOTIFICATIONS) {
                    PermissionCenterManager.openNotificationSettings(context)
                } else {
                    PermissionCenterManager.openAppSettings(context)
                }
            }
            perms.size > 1 -> {
                PermissionCenterManager.markPermissionRequested(context, permState.type)
                multiPermLauncher.launch(perms)
            }
            perms.size == 1 -> {
                PermissionCenterManager.markPermissionRequested(context, permState.type)
                singlePermLauncher.launch(perms.first())
            }
            else -> {
                PermissionCenterManager.openNotificationSettings(context)
            }
        }
    }

    val grantedCount = permissionStates.count { it.isAllowed }
    val allGranted = grantedCount == permissionStates.size
    val latestLocation = locationHistory.firstOrNull()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6FBF7)),
    ) {
        // Top and Bottom Green Wave Canvas Artwork
        PermissionWaveBackdrop(
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            // Top Bar over Green Wave
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    onClick = onBack,
                    shape = CircleShape,
                    color = PureWhite.copy(alpha = 0.22f),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("permission_center_back_button"),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PureWhite,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Permission Center",
                    color = PureWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .align(Alignment.CenterHorizontally)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Custom Hisaber Khata Logo matching reference design
                HisaberPermissionBrandBadge(modifier = Modifier.size(78.dp))

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Hisaber Khata",
                    color = PermHeadingGreen,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Manage your app permissions",
                    color = PermBodySlate,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 5 Rounded Permission Cards
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    permissionStates.forEach { state ->
                        val locationSubtitle = if (state.type == AppPermissionType.LOCATION && latestLocation != null) {
                            "Last accessed: ${PermissionCenterManager.formatDateTimeBanner(latestLocation.timestampMillis)}"
                        } else {
                            null
                        }

                        PermissionItemCard(
                            state = state,
                            extraLine = locationSubtitle,
                            onClick = {
                                if (state.type == AppPermissionType.LOCATION) {
                                    onOpenLocationDetails()
                                } else {
                                    selectedDetailType = state.type
                                }
                            },
                        )
                    }

                    // Manage app if unused Card (real OS API + System Settings launch, NO fake toggle)
                    ManageUnusedAppCard(
                        restrictionState = unusedRestrictionState,
                        onClick = {
                            val intent = PermissionCenterManager.createManageUnusedAppIntent(context)
                            runCatching { unusedAppSettingsLauncher.launch(intent) }
                                .onFailure { PermissionCenterManager.openAppSettings(context) }
                        },
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Status Banner
                Surface(
                    color = if (allGranted) PermBannerMintBg else PermWarnBg,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, if (allGranted) PermBannerMintBorder else PermWarnBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("permission_center_summary_banner"),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = if (allGranted) Icons.Default.Info else Icons.Default.Warning,
                            contentDescription = if (allGranted) "All permissions granted" else "Permissions notice",
                            tint = if (allGranted) PermGreenPrimary else PermWarnInk,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (allGranted) {
                                "All permissions are granted"
                            } else {
                                "$grantedCount of ${permissionStates.size} permissions are allowed • Tap any card to review"
                            },
                            color = if (allGranted) PermGreenDeep else PermWarnInk,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    val activeDetailState = selectedDetailType?.let { type ->
        permissionStates.find { it.type == type }
    }
    if (activeDetailState != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedDetailType = null },
            containerColor = PureWhite,
        ) {
            PermissionDetailBottomSheetContent(
                state = activeDetailState,
                onRequestOrEnable = {
                    selectedDetailType = null
                    requestPermissionFor(activeDetailState)
                },
                onOpenSystemSettings = {
                    selectedDetailType = null
                    if (activeDetailState.type == AppPermissionType.NOTIFICATIONS) {
                        PermissionCenterManager.openNotificationSettings(context)
                    } else {
                        PermissionCenterManager.openAppSettings(context)
                    }
                },
                onClose = { selectedDetailType = null },
            )
        }
    }
}

@Composable
fun LocationPermissionScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var locationState by remember {
        mutableStateOf(
            PermissionCenterManager.evaluatePermission(context, AppPermissionType.LOCATION, activity)
        )
    }
    var hasFineLocation by remember {
        mutableStateOf(PermissionCenterManager.hasFineLocationPermission(context))
    }
    var locationServicesEnabled by remember {
        mutableStateOf(PermissionCenterManager.isLocationServiceEnabled(context))
    }
    var history by remember {
        mutableStateOf(PermissionCenterManager.loadLocationHistory(context))
    }
    var isFetchingLocation by remember { mutableStateOf(false) }
    var locationFeedback by remember { mutableStateOf<String?>(null) }

    val refreshLocationState: () -> Unit = remember(context, activity) {
        {
            locationState = PermissionCenterManager.evaluatePermission(
                context = context,
                type = AppPermissionType.LOCATION,
                activity = activity,
            )
            hasFineLocation = PermissionCenterManager.hasFineLocationPermission(context)
            locationServicesEnabled = PermissionCenterManager.isLocationServiceEnabled(context)
            history = PermissionCenterManager.loadLocationHistory(context)
        }
    }

    LaunchedEffect(Unit) {
        refreshLocationState()
    }

    DisposableEffect(lifecycleOwner, refreshLocationState) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshLocationState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        refreshLocationState()
    }

    val latestRecord = history.firstOrNull()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6FBF7)),
    ) {
        PermissionWaveBackdrop(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            // Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    onClick = onBack,
                    shape = CircleShape,
                    color = PureWhite.copy(alpha = 0.22f),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("location_permission_back_button"),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Permission Center",
                            tint = PureWhite,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Location Permission",
                    color = PureWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .align(Alignment.CenterHorizontally)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Location Circular Halo Badge
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD7F3DF)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(PermGreenPrimary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = PureWhite,
                            modifier = Modifier.size(30.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Location",
                    color = PermHeadingGreen,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Access to location",
                    color = PermBodySlate,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Card 1: Real Permission Status Banner
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (locationState.isAllowed) PermBannerMintBg else PermDeniedBg,
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        if (locationState.isAllowed) PermBannerMintBorder else PermDeniedBorder,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("location_status_banner_card"),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (locationState.isAllowed) PermGreenPrimary else PermDeniedRed),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = if (locationState.isAllowed) {
                                        Icons.Default.CheckCircle
                                    } else {
                                        Icons.Default.Cancel
                                    },
                                    contentDescription = if (locationState.isAllowed) "Granted" else "Denied",
                                    tint = PureWhite,
                                    modifier = Modifier.size(26.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (locationState.isAllowed) {
                                        "Permission Granted"
                                    } else {
                                        "Permission Denied"
                                    },
                                    color = if (locationState.isAllowed) PermHeadingGreen else PermDeniedRed,
                                    fontSize = 16.5.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = if (locationState.isAllowed) {
                                        val precision = if (hasFineLocation) "Precise GPS" else "Approximate"
                                        "Your location permission is allowed ($precision)."
                                    } else {
                                        locationState.detailNote
                                    },
                                    color = PermBodySlate,
                                    fontSize = 13.sp,
                                )
                            }
                        }

                        if (!locationState.isAllowed) {
                            Button(
                                onClick = {
                                    if (locationState.requiresSystemSettingsOnly) {
                                        PermissionCenterManager.openAppSettings(context)
                                    } else {
                                        PermissionCenterManager.markPermissionRequested(
                                            context,
                                            AppPermissionType.LOCATION,
                                        )
                                        locationPermissionLauncher.launch(
                                            PermissionCenterManager.runtimePermissionsFor(AppPermissionType.LOCATION)
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PermGreenPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("grant_location_permission_button"),
                            ) {
                                Text(
                                    text = if (locationState.requiresSystemSettingsOnly) {
                                        "Enable in App Settings"
                                    } else {
                                        "Allow Location Permission"
                                    },
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Card 2: Real Last Accessed Timestamp & Live Device Check
                Card(
                    colors = CardDefaults.cardColors(containerColor = PermCardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, PermCardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("location_last_accessed_card"),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = PermGreenPrimary,
                                modifier = Modifier.size(22.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Last accessed",
                                color = PermHeadingGreen,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        if (latestRecord != null) {
                            LocationMetaRow(
                                icon = Icons.Default.CalendarMonth,
                                label = "Date",
                                value = PermissionCenterManager.formatDateOnly(latestRecord.timestampMillis),
                            )
                            LocationMetaRow(
                                icon = Icons.Default.Schedule,
                                label = "Time",
                                value = PermissionCenterManager.formatTimeOnly(latestRecord.timestampMillis),
                            )
                            LocationMetaRow(
                                icon = Icons.Default.LocationOn,
                                label = "Location",
                                value = latestRecord.label,
                            )
                        } else {
                            Text(
                                text = "No location access recorded yet.",
                                color = PermBodySlate,
                                fontSize = 13.5.sp,
                            )
                        }

                        if (locationState.isAllowed) {
                            OutlinedButton(
                                onClick = {
                                    if (!isFetchingLocation) {
                                        isFetchingLocation = true
                                        locationFeedback = null
                                        scope.launch {
                                            val res = PermissionCenterManager.fetchAndRecordRealLocation(context)
                                            isFetchingLocation = false
                                            refreshLocationState()
                                            res.onSuccess { rec ->
                                                locationFeedback = "Location recorded: ${rec.label}"
                                            }.onFailure { err ->
                                                locationFeedback = err.localizedMessage
                                                    ?: "Unable to retrieve device location."
                                            }
                                        }
                                    }
                                },
                                border = BorderStroke(1.dp, PermGreenPrimary),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = PermGreenDeep),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("location_fetch_now_button"),
                            ) {
                                if (isFetchingLocation) {
                                    CircularProgressIndicator(
                                        color = PermGreenPrimary,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Checking real device location...", fontWeight = FontWeight.SemiBold)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Check & Record Current Location", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        if (!locationServicesEnabled) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "Device GPS / Location services are OFF",
                                    color = PermWarnInk,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f),
                                )
                                TextButton(
                                    onClick = { PermissionCenterManager.openLocationSourceSettings(context) },
                                ) {
                                    Text("Turn On", color = PermGreenDeep, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (locationFeedback != null) {
                            Text(
                                text = locationFeedback!!,
                                color = PermBodySlate,
                                fontSize = 12.sp,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Card 3: Real Location Access History
                Card(
                    colors = CardDefaults.cardColors(containerColor = PermCardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, PermCardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("location_history_card"),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(PermGreenPrimary),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = PureWhite,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Location access history",
                                    color = PermHeadingGreen,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            if (history.isNotEmpty()) {
                                TextButton(
                                    onClick = {
                                        PermissionCenterManager.clearLocationHistory(context)
                                        refreshLocationState()
                                        locationFeedback = null
                                    },
                                    modifier = Modifier.testTag("location_history_clear_button"),
                                ) {
                                    Text("Clear", color = PermDeniedRed, fontSize = 12.5.sp)
                                }
                            }
                        }

                        if (history.isEmpty()) {
                            Text(
                                text = "No real location accesses have been recorded yet. History only stores actual timestamps when location is accessed by the device.",
                                color = PermBodySlate,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                            )
                        } else {
                            history.forEachIndexed { index, record ->
                                LocationHistoryRow(record = record)
                                if (index < history.lastIndex) {
                                    HorizontalDivider(color = PermCardBorder.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Card 4: Open Android System Settings
                Card(
                    onClick = { PermissionCenterManager.openAppSettings(context) },
                    colors = CardDefaults.cardColors(containerColor = PermCardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, PermCardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("location_open_settings_card"),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 64.dp)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(PermGreenPrimary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Open settings",
                                tint = PureWhite,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Open settings",
                                color = PermHeadingGreen,
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Manage location permission",
                                color = PermBodySlate,
                                fontSize = 13.sp,
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Open settings",
                            tint = PermBodySlate,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PermissionItemCard(
    state: AppPermissionState,
    extraLine: String?,
    onClick: () -> Unit,
) {
    val icon = permissionIconFor(state.type)
    val statusColor = if (state.isAllowed) PermGreenPrimary else PermDeniedRed

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = PermCardBg),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, PermCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("permission_card_${state.type.id}"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 68.dp)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(PermGreenPrimary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = state.type.title,
                    tint = PureWhite,
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.type.title,
                    color = Color(0xFF102A1D),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = state.type.subtitle,
                    color = PermBodySlate,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!extraLine.isNullOrBlank()) {
                    Text(
                        text = extraLine,
                        color = PermGreenDeep,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("permission_status_${state.type.id}"),
            ) {
                Icon(
                    imageVector = if (state.isAllowed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = "${state.type.title} ${state.statusLabel}",
                    tint = statusColor,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = state.statusLabel,
                    color = statusColor,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Details",
                    tint = PermBodySlate,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun ManageUnusedAppCard(
    restrictionState: UnusedAppRestrictionState,
    onClick: () -> Unit,
) {
    val pillBg = when (restrictionState) {
        UnusedAppRestrictionState.ACTIVE -> PermBannerMintBg
        UnusedAppRestrictionState.EXEMPT -> PermWarnBg
        else -> Color(0xFFF1F5F9)
    }
    val pillInk = when (restrictionState) {
        UnusedAppRestrictionState.ACTIVE -> PermGreenDeep
        UnusedAppRestrictionState.EXEMPT -> PermWarnInk
        else -> PermBodySlate
    }

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = PermCardBg),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, PermCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("manage_unused_app_card"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 72.dp)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Manage app if unused",
                    color = Color(0xFF102A1D),
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Remove permissions, delete temporary files, stop notifications, and archive the app",
                    color = PermBodySlate,
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = restrictionState.detailText,
                    color = PermGreenDeep,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Surface(
                    color = pillBg,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, pillInk.copy(alpha = 0.25f)),
                ) {
                    Text(
                        text = restrictionState.badgeLabel,
                        color = pillInk,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Open Android unused app settings",
                    tint = PermBodySlate,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun PermissionDetailBottomSheetContent(
    state: AppPermissionState,
    onRequestOrEnable: () -> Unit,
    onOpenSystemSettings: () -> Unit,
    onClose: () -> Unit,
) {
    val icon = permissionIconFor(state.type)
    val isAllowed = state.isAllowed
    val statusColor = if (isAllowed) PermGreenPrimary else PermDeniedRed

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(PermGreenPrimary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = state.type.title,
                    tint = PureWhite,
                    modifier = Modifier.size(26.dp),
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.type.title,
                    color = PermHeadingGreen,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isAllowed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = when (state.status) {
                            PermissionGrantStatus.ALLOWED -> "Allowed"
                            PermissionGrantStatus.DENIED -> "Denied"
                            PermissionGrantStatus.PERMANENTLY_DENIED -> "Denied (Enable in Settings)"
                        },
                        color = statusColor,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        Surface(
            color = if (isAllowed) PermBannerMintBg else PermDeniedBg,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, if (isAllowed) PermBannerMintBorder else PermDeniedBorder),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = state.type.description,
                    color = Color(0xFF1E293B),
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                )
                Text(
                    text = state.detailNote,
                    color = PermBodySlate,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        if (!isAllowed) {
            Button(
                onClick = onRequestOrEnable,
                colors = ButtonDefaults.buttonColors(containerColor = PermGreenPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("permission_sheet_request_button"),
            ) {
                Text(
                    text = if (state.requiresSystemSettingsOnly) {
                        "Open System Settings"
                    } else {
                        "Request ${state.type.title} Permission"
                    },
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        OutlinedButton(
            onClick = onOpenSystemSettings,
            border = BorderStroke(1.dp, PermGreenPrimary),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PermGreenDeep),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .testTag("permission_sheet_settings_button"),
        ) {
            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Manage in Android Settings", fontWeight = FontWeight.SemiBold)
        }

        TextButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text("Close", color = PermBodySlate)
        }
    }
}

@Composable
private fun LocationMetaRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = PermBodySlate,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = PermBodySlate,
            fontSize = 13.5.sp,
            modifier = Modifier.width(72.dp),
        )
        Text(
            text = ": $value",
            color = Color(0xFF102A1D),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun LocationHistoryRow(record: LocationAccessRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(PermBannerMintBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Recorded location",
                tint = PermGreenPrimary,
                modifier = Modifier.size(19.dp),
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.label,
                color = Color(0xFF102A1D),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val meta = buildString {
                append(PermissionCenterManager.formatDateTimeBanner(record.timestampMillis))
                if (record.provider.isNotBlank()) {
                    append(" • ${record.provider.uppercase()}")
                }
                if (record.accuracyMeters != null) {
                    append(" (±${record.accuracyMeters.toInt()}m)")
                }
            }
            Text(
                text = meta,
                color = PermBodySlate,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PermissionWaveBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Top soft mint layer
        val topMint = Path().apply {
            moveTo(0f, 0f)
            lineTo(w, 0f)
            lineTo(w, 165.dp.toPx())
            cubicTo(
                w * 0.72f, 195.dp.toPx(),
                w * 0.34f, 118.dp.toPx(),
                0f, 158.dp.toPx(),
            )
            close()
        }
        drawPath(topMint, color = PermGreenWaveMint.copy(alpha = 0.55f))

        // Top bright green wave
        val topBright = Path().apply {
            moveTo(0f, 0f)
            lineTo(w, 0f)
            lineTo(w, 134.dp.toPx())
            cubicTo(
                w * 0.72f, 178.dp.toPx(),
                w * 0.30f, 102.dp.toPx(),
                0f, 140.dp.toPx(),
            )
            close()
        }
        drawPath(topBright, color = PermGreenBright)

        // Top deep green primary wave
        val topDeep = Path().apply {
            moveTo(0f, 0f)
            lineTo(w, 0f)
            lineTo(w, 112.dp.toPx())
            cubicTo(
                w * 0.65f, 152.dp.toPx(),
                w * 0.25f, 92.dp.toPx(),
                0f, 124.dp.toPx(),
            )
            close()
        }
        drawPath(topDeep, color = PermGreenDeep)

        // Bottom soft mint layer
        val bottomMint = Path().apply {
            moveTo(0f, h - 84.dp.toPx())
            cubicTo(
                w * 0.32f, h - 122.dp.toPx(),
                w * 0.68f, h - 54.dp.toPx(),
                w, h - 92.dp.toPx(),
            )
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(bottomMint, color = PermGreenWaveMint.copy(alpha = 0.55f))

        // Bottom bright green wave
        val bottomBright = Path().apply {
            moveTo(0f, h - 62.dp.toPx())
            cubicTo(
                w * 0.30f, h - 98.dp.toPx(),
                w * 0.72f, h - 38.dp.toPx(),
                w, h - 72.dp.toPx(),
            )
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(bottomBright, color = PermGreenBright)

        // Bottom deep green wave
        val bottomDeep = Path().apply {
            moveTo(0f, h - 42.dp.toPx())
            cubicTo(
                w * 0.35f, h - 76.dp.toPx(),
                w * 0.70f, h - 22.dp.toPx(),
                w, h - 50.dp.toPx(),
            )
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(bottomDeep, color = PermGreenDeep)
    }
}

@Composable
private fun HisaberPermissionBrandBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(PermGreenLogoBg),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
            val w = size.width
            val h = size.height

            // Notebook cover outline
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(w * 0.14f, h * 0.06f),
                size = Size(w * 0.74f, h * 0.88f),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                style = Stroke(width = 2.6.dp.toPx()),
            )
            // Inner page accent
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(w * 0.30f, h * 0.22f),
                size = Size(w * 0.44f, h * 0.54f),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            )
            // Spiral rings along the left spine
            for (i in 0..3) {
                val y = h * (0.20f + i * 0.20f)
                drawLine(
                    color = Color.White,
                    start = Offset(w * 0.05f, y),
                    end = Offset(w * 0.22f, y),
                    strokeWidth = 2.6.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
            // Stylized Taka symbol inside white center plate
            val cx = w * 0.52f
            val cy = h * 0.49f
            drawLine(
                color = PermGreenLogoBg,
                start = Offset(cx - w * 0.10f, cy - h * 0.03f),
                end = Offset(cx + w * 0.10f, cy - h * 0.03f),
                strokeWidth = 2.4.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawArc(
                color = PermGreenLogoBg,
                startAngle = 210f,
                sweepAngle = 295f,
                useCenter = false,
                topLeft = Offset(cx - w * 0.09f, cy - h * 0.13f),
                size = Size(w * 0.18f, h * 0.26f),
                style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round),
            )
        }
    }
}

private fun permissionIconFor(type: AppPermissionType): ImageVector = when (type) {
    AppPermissionType.CAMERA -> Icons.Default.PhotoCamera
    AppPermissionType.CONTACTS -> Icons.Default.Group
    AppPermissionType.LOCATION -> Icons.Default.LocationOn
    AppPermissionType.NOTIFICATIONS -> Icons.Default.Notifications
    AppPermissionType.PHONE -> Icons.Default.Call
}
