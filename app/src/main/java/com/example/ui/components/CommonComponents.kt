package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.AvatarMint
import com.example.ui.theme.AvatarMintText
import com.example.ui.theme.AvatarPeach
import com.example.ui.theme.AvatarPeachText
import com.example.ui.theme.AvatarSage
import com.example.ui.theme.AvatarSageText
import com.example.ui.theme.AvatarSand
import com.example.ui.theme.AvatarSandText
import com.example.ui.theme.AvatarSky
import com.example.ui.theme.AvatarSkyText
import com.example.ui.theme.BrandBkashPink
import com.example.ui.theme.BrandCellfinAccent
import com.example.ui.theme.BrandCellfinGreen
import com.example.ui.theme.BrandNagadRedOrange
import com.example.ui.theme.BrandNagadYellow
import com.example.ui.theme.BrandRocketPurple
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DenaRed
import com.example.ui.theme.HisabGreen
import com.example.ui.theme.HisabGreenSoft
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PureWhite
import com.example.util.Formatters
import kotlin.math.abs

@Composable
fun HisaberTopBar(
    title: String,
    showBack: Boolean = false,
    showMenu: Boolean = false,
    centerTitle: Boolean = false,
    unreadNotifications: Int = 0,
    onBackClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onNotificationClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Surface(
        color = PureWhite,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when {
                showBack -> {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("topbar_back_button"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "পিছনে যান",
                            tint = InkPrimary,
                        )
                    }
                }
                showMenu -> {
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier.testTag("topbar_menu_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "মেনু খুলুন",
                            tint = InkPrimary,
                        )
                    }
                }
                else -> Spacer(modifier = Modifier.width(48.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = HisabGreen,
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                actions()
                if (onNotificationClick != null) {
                    Box {
                        IconButton(
                            onClick = onNotificationClick,
                            modifier = Modifier.testTag("topbar_notification_button"),
                        ) {
                            Icon(
                                imageVector = if (unreadNotifications > 0) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                                contentDescription = "ইনবক্স নোটিফিকেশন",
                                tint = InkPrimary,
                            )
                        }
                        if (unreadNotifications > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-4).dp, y = 4.dp)
                                    .background(DenaRed, CircleShape)
                                    .padding(horizontal = 5.dp, vertical = 1.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = if (unreadNotifications > 99) "99+" else unreadNotifications.toString(),
                                    color = PureWhite,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                } else if (centerTitle) {
                    Spacer(modifier = Modifier.width(48.dp))
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}

enum class BottomNavTab { TALI, CASHBOX, QR_CODE, TALIPAY, MENU }

@Composable
fun HisaberBottomBar(
    selectedTab: BottomNavTab,
    onSelectTali: () -> Unit,
    onSelectCashbox: () -> Unit,
    onSelectQrCode: () -> Unit,
    onSelectTalipay: () -> Unit,
    onSelectMenu: () -> Unit,
) {
    Surface(
        color = PureWhite,
        shadowElevation = 8.dp,
        border = BorderStroke(0.5.dp, CardBorder),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(66.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomNavItem(
                label = "হোম",
                selected = selectedTab == BottomNavTab.TALI,
                activeIcon = Icons.Filled.Home,
                inactiveIcon = Icons.Outlined.Home,
                testTag = "nav_tali",
                onClick = onSelectTali,
                modifier = Modifier.weight(1f),
            )
            BottomNavItem(
                label = "ক্যাশবাক্স",
                selected = selectedTab == BottomNavTab.CASHBOX,
                activeIcon = Icons.Filled.AccountBalanceWallet,
                inactiveIcon = Icons.Outlined.AccountBalanceWallet,
                testTag = "nav_cashbox",
                onClick = onSelectCashbox,
                modifier = Modifier.weight(1f),
            )
            BottomNavItem(
                label = "কিউআর কোড",
                selected = selectedTab == BottomNavTab.QR_CODE,
                activeIcon = Icons.Filled.QrCode2,
                inactiveIcon = Icons.Outlined.QrCode2,
                testTag = "nav_qr_code",
                onClick = onSelectQrCode,
                modifier = Modifier.weight(1f),
            )
            BottomNavItem(
                label = "হিসাব পে",
                selected = selectedTab == BottomNavTab.TALIPAY,
                activeIcon = Icons.Filled.Payments,
                inactiveIcon = Icons.Outlined.Payments,
                testTag = "nav_hisabi_pay",
                onClick = onSelectTalipay,
                modifier = Modifier.weight(1f),
            )
            BottomNavItem(
                label = "মেনু",
                selected = selectedTab == BottomNavTab.MENU,
                activeIcon = Icons.Filled.Menu,
                inactiveIcon = Icons.Outlined.Menu,
                testTag = "nav_menu",
                onClick = onSelectMenu,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    selected: Boolean,
    activeIcon: ImageVector,
    inactiveIcon: ImageVector,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint = if (selected) HisabGreen else InkSecondary
    Column(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(if (selected) HisabGreenSoft else Color.Transparent)
                .padding(horizontal = 14.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (selected) activeIcon else inactiveIcon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun CustomerAvatarCircle(
    name: String,
    size: Dp = 44.dp,
    photoPath: String = "",
) {
    if (photoPath.isNotBlank()) {
        AsyncImage(
            model = photoPath,
            contentDescription = "$name ছবি",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .border(1.dp, CardBorder, CircleShape),
        )
        return
    }
    val pair = when (abs(name.hashCode()) % 5) {
        0 -> AvatarMint to AvatarMintText
        1 -> AvatarSand to AvatarSandText
        2 -> AvatarSky to AvatarSkyText
        3 -> AvatarSage to AvatarSageText
        else -> AvatarPeach to AvatarPeachText
    }
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(pair.first),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = Formatters.firstLetterAvatar(name),
            color = pair.second,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.42f).sp,
        )
    }
}

/**
 * Authentic brand marks in their original brand colors for bKash, Nagad, and Rocket.
 */
@Composable
fun AuthenticProviderLogo(
    providerId: String,
    modifier: Modifier = Modifier.size(44.dp),
) {
    when (providerId) {
        "bkash" -> {
            // bKash origami bird emblem in authentic #E2136E pink
            Canvas(modifier = modifier) {
                val w = size.width
                val h = size.height
                val wingTop = Path().apply {
                    moveTo(w * 0.14f, h * 0.22f)
                    lineTo(w * 0.52f, h * 0.44f)
                    lineTo(w * 0.36f, h * 0.60f)
                    close()
                }
                val wingBody = Path().apply {
                    moveTo(w * 0.38f, h * 0.62f)
                    lineTo(w * 0.64f, h * 0.26f)
                    lineTo(w * 0.86f, h * 0.36f)
                    lineTo(w * 0.56f, h * 0.78f)
                    close()
                }
                val beak = Path().apply {
                    moveTo(w * 0.64f, h * 0.26f)
                    lineTo(w * 0.90f, h * 0.24f)
                    lineTo(w * 0.86f, h * 0.36f)
                    close()
                }
                drawPath(wingTop, color = BrandBkashPink)
                drawPath(wingBody, color = Color(0xFFC00E5B))
                drawPath(beak, color = BrandBkashPink)
            }
        }
        "nagad" -> {
            // Nagad circular flame emblem in authentic red-orange & amber-gold
            Canvas(modifier = modifier) {
                val w = size.width
                val h = size.height
                val outerFlame = Path().apply {
                    moveTo(w * 0.52f, h * 0.10f)
                    cubicTo(w * 0.84f, h * 0.28f, w * 0.88f, h * 0.74f, w * 0.50f, h * 0.90f)
                    cubicTo(w * 0.16f, h * 0.78f, w * 0.16f, h * 0.34f, w * 0.44f, h * 0.20f)
                    cubicTo(w * 0.36f, h * 0.42f, w * 0.46f, h * 0.52f, w * 0.52f, h * 0.10f)
                    close()
                }
                drawPath(outerFlame, color = BrandNagadRedOrange)
                drawCircle(
                    color = BrandNagadYellow,
                    radius = w * 0.16f,
                    center = Offset(w * 0.52f, h * 0.60f),
                )
            }
        }
        "rocket" -> {
            // Rocket DBBL purple emblem in authentic #8C3494 purple
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(BrandRocketPurple),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "রকেট",
                    color = PureWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                )
            }
        }
        "cellfin" -> {
            // Islami Bank CellFin authentic deep green emblem with bright green arc & bold C mark
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(BrandCellfinGreen),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val accentCorner = Path().apply {
                        moveTo(w * 0.62f, 0f)
                        lineTo(w, 0f)
                        lineTo(w, h * 0.38f)
                        cubicTo(w * 0.80f, h * 0.32f, w * 0.68f, h * 0.20f, w * 0.62f, 0f)
                        close()
                    }
                    drawPath(accentCorner, color = BrandCellfinAccent)
                }
                Text(
                    text = "CF",
                    color = PureWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun BanglaQrBrandStrip(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BanglaQrBrandBadge(
            providerId = "nagad",
            brandText = "Nagad",
            brandColor = BrandNagadRedOrange,
            isItalic = false,
        )
        BanglaQrBrandBadge(
            providerId = "bkash",
            brandText = "bKash",
            brandColor = BrandBkashPink,
            isItalic = false,
        )
        BanglaQrBrandBadge(
            providerId = "rocket",
            brandText = "Rocket",
            brandColor = BrandRocketPurple,
            isItalic = true,
        )
        BanglaQrBrandBadge(
            providerId = "cellfin",
            brandText = "Cellfin",
            brandColor = BrandCellfinGreen,
            isItalic = false,
        )
    }
}

@Composable
private fun BanglaQrBrandBadge(
    providerId: String,
    brandText: String,
    brandColor: Color,
    isItalic: Boolean,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Surface(
            color = PureWhite,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardBorder),
            shadowElevation = 1.dp,
        ) {
            Box(
                modifier = Modifier.padding(6.dp),
                contentAlignment = Alignment.Center,
            ) {
                AuthenticProviderLogo(
                    providerId = providerId,
                    modifier = Modifier.size(30.dp),
                )
            }
        }
        Text(
            text = brandText,
            color = brandColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
            letterSpacing = 0.2.sp,
            maxLines = 1,
        )
    }
}

@Composable
fun LoadingStateBox(message: String = "ডাটা লোড হচ্ছে...") {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = HisabGreen)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = message, style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
        }
    }
}

@Composable
fun EmptyStateBox(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(HisabGreenSoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = HisabGreen,
                modifier = Modifier.size(34.dp),
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = InkPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = InkMuted,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = HisabGreen),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(actionLabel)
            }
        }
    }
}
