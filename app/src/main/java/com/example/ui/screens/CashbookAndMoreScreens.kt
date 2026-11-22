package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LedgerTransaction
import com.example.ui.components.AuthenticProviderLogo
import com.example.ui.components.EmptyStateBox
import com.example.ui.components.HisaberTopBar
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DenaRed
import com.example.ui.theme.HisabGreen
import com.example.ui.theme.HisabGreenBorder
import com.example.ui.theme.HisabGreenSoft
import com.example.ui.theme.HisabGreenTint
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PaonaGreen
import com.example.ui.theme.PaonaGreenBg
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftCanvas
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberBg
import com.example.ui.viewmodel.HisaberAppData
import android.content.Intent
import android.net.Uri
import com.example.util.BackendGatewayClient
import com.example.util.Formatters
import com.example.util.GatewayExecutionMode
import com.example.util.GoldActivationOutcome
import com.example.util.GoldPackageManager
import com.example.util.GoldPackagePlan
import com.example.util.PaymentGatewayConfig
import com.example.util.PaymentGatewayResult
import com.example.util.PaymentProvider
import kotlinx.coroutines.launch

@Composable
fun CashbookScreen(
    data: HisaberAppData,
    onOpenDrawer: () -> Unit,
    onAddCashbookEntry: () -> Unit,
    onDeleteTransaction: (LedgerTransaction) -> Unit,
) {
    var filter by rememberSaveable { mutableStateOf("all") } // all, income, expense

    val cashbookEntries = remember(data.transactions, filter) {
        data.transactions.filter { tx ->
            val scopeMatch = tx.entryScope == "cashbook" || tx.type == "income" || tx.type == "expense" || tx.type == "received" || tx.type == "gave"
            val typeMatch = when (filter) {
                "income" -> tx.type == "income" || tx.type == "received"
                "expense" -> tx.type == "expense" || tx.type == "gave"
                else -> true
            }
            scopeMatch && typeMatch
        }
    }

    val totalIn = remember(data.transactions) {
        data.transactions.filter { it.type == "income" || it.type == "received" }.sumOf { it.amount }
    }
    val totalOut = remember(data.transactions) {
        data.transactions.filter { it.type == "expense" || it.type == "gave" }.sumOf { it.amount }
    }
    val netCash = totalIn - totalOut

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCanvas),
    ) {
        HisaberTopBar(
            title = "ক্যাশবুক",
            showMenu = true,
            onMenuClick = onOpenDrawer,
            actions = {
                IconButton(
                    onClick = onAddCashbookEntry,
                    modifier = Modifier.testTag("cashbook_add_button"),
                ) {
                    Icon(Icons.Default.Add, contentDescription = "ক্যাশবুক এন্ট্রি যোগ করুন", tint = HisabGreen)
                }
            },
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = HisabGreenTint),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, HisabGreenBorder),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("মোট জমা / আয়", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                                Text(
                                    text = Formatters.formatTaka(totalIn),
                                    style = MaterialTheme.typography.titleLarge.copy(color = PaonaGreen, fontWeight = FontWeight.Bold),
                                )
                            }
                            VerticalDivider(modifier = Modifier.height(38.dp), color = CardBorder)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("মোট খরচ / ব্যয়", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                                Text(
                                    text = Formatters.formatTaka(totalOut),
                                    style = MaterialTheme.typography.titleLarge.copy(color = DenaRed, fontWeight = FontWeight.Bold),
                                )
                            }
                        }
                        HorizontalDivider(color = CardBorder)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(HisabGreenSoft)
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("নগদ স্থিতি (Net Balance)", style = MaterialTheme.typography.bodyMedium, color = InkPrimary)
                            Text(
                                text = Formatters.formatTaka(netCash),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = if (netCash >= 0) PaonaGreen else DenaRed,
                                    fontWeight = FontWeight.Bold,
                                ),
                            )
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("all" to "সব লেনদেন", "income" to "আয় / জমা", "expense" to "ব্যয় / খরচ").forEach { (key, label) ->
                        FilterChip(
                            selected = filter == key,
                            onClick = { filter = key },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HisabGreenSoft,
                                selectedLabelColor = HisabGreen,
                            ),
                        )
                    }
                }
            }

            if (cashbookEntries.isEmpty()) {
                item {
                    EmptyStateBox(
                        icon = Icons.Default.Book,
                        title = "ক্যাশবুকে কোনো এন্ট্রি নেই",
                        subtitle = "আজকের আয় বা খরচ যোগ করতে '+' বাটনে ট্যাপ করুন।",
                        actionLabel = "+ নতুন এন্ট্রি যোগ করুন",
                        onAction = onAddCashbookEntry,
                    )
                }
            } else {
                items(cashbookEntries, key = { it.id }) { tx ->
                    val isPositive = tx.type == "income" || tx.type == "received"
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tx.customerName.ifBlank {
                                        if (isPositive) "নগদ আয় / জমা" else "ব্যবসায়িক খরচ"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = InkPrimary,
                                )
                                if (tx.description.isNotBlank()) {
                                    Text(
                                        text = tx.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = InkSecondary,
                                    )
                                }
                                Text(
                                    text = "${Formatters.formatBanglaDate(tx.dateMillis)} • ${tx.paymentMethod.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = InkMuted,
                                )
                            }
                            Text(
                                text = (if (isPositive) "+ " else "- ") + Formatters.formatTaka(tx.amount),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPositive) PaonaGreen else DenaRed,
                                ),
                            )
                            IconButton(onClick = { onDeleteTransaction(tx) }) {
                                Icon(Icons.Default.Delete, contentDescription = "মুছুন", tint = InkMuted, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MoreMenuScreen(
    unreadNotifications: Int,
    onNotificationClick: () -> Unit,
    onOpenPaymentGateway: (providerId: String, title: String) -> Unit,
    onOpenMyQr: () -> Unit,
    onOpenScanQr: () -> Unit,
    onOpenReports: (ownerMode: Boolean) -> Unit,
    onOpenHelpCenter: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPermissionCenter: () -> Unit = {},
    onOpenBusinessInfo: () -> Unit = {},
    onOpenGoldMembership: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCanvas),
    ) {
        HisaberTopBar(
            title = "মেনু",
            unreadNotifications = unreadNotifications,
            onNotificationClick = onNotificationClick,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Row 1 & 2: Financial Utility Services matching reference
            item {
                val topServices = listOf(
                    MenuTileSpec("বিল পে", Icons.AutoMirrored.Filled.ReceiptLong) { onOpenPaymentGateway("bkash", "বিল পে") },
                    MenuTileSpec("মোবাইল রিচার্জ", Icons.Default.PhoneAndroid) { onOpenPaymentGateway("bkash", "মোবাইল রিচার্জ") },
                    MenuTileSpec("অ্যাড মানি", Icons.Default.AddCard) { onOpenPaymentGateway("bkash", "অ্যাড মানি") },
                    MenuTileSpec("ওয়ালেট ট্রান্সফার", Icons.Default.SwapHoriz) { onOpenPaymentGateway("bkash", "ওয়ালেট ট্রান্সফার") },
                    MenuTileSpec("ব্যাংক ট্রান্সফার", Icons.Default.AccountBalance) { onOpenPaymentGateway("bkash", "ব্যাংক ট্রান্সফার") },
                    MenuTileSpec("ক্যাশ কালেকশন", Icons.Default.AttachMoney) { onOpenPaymentGateway("bkash", "ক্যাশ কালেকশন") },
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    topServices.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            row.forEach { item ->
                                MenuIconTile(item, Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Row 3: Authentic bKash, Nagad, Rocket Logos matching reference
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    listOf(
                        Triple("bkash", "বিকাশ", "menu_bkash"),
                        Triple("nagad", "নগদ", "menu_nagad"),
                        Triple("rocket", "রকেট", "menu_rocket"),
                    ).forEach { (id, label, tag) ->
                        Card(
                            onClick = { onOpenPaymentGateway(id, "$label পেমেন্ট গেটওয়ে") },
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, CardBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(92.dp)
                                .testTag(tag),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                AuthenticProviderLogo(providerId = id, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = InkPrimary,
                                    ),
                                )
                            }
                        }
                    }
                }
            }

            // Row 4 & 5 & 6: QR, Reports, Business Profile, Gold, Help Center, Settings
            item {
                val bottomServices = listOf(
                    MenuTileSpec("আমার QR", Icons.Default.QrCode2, onOpenMyQr),
                    MenuTileSpec("QR স্ক্যান", Icons.Default.QrCodeScanner, onOpenScanQr),
                    MenuTileSpec("রিপোর্ট", Icons.Default.Description) { onOpenReports(false) },
                    MenuTileSpec("মালিকের রিপোর্ট", Icons.Default.Assessment) { onOpenReports(true) },
                    MenuTileSpec("ব্যবসার তথ্য", Icons.Default.Storefront, onOpenBusinessInfo),
                    MenuTileSpec("গোল্ড মেম্বার", Icons.Default.WorkspacePremium, onOpenGoldMembership),
                    MenuTileSpec("সাপোর্ট সেন্টার", Icons.Default.HeadsetMic, onOpenHelpCenter),
                    MenuTileSpec("সেটিংস", Icons.Default.Settings, onOpenSettings),
                    MenuTileSpec("পারমিশন", Icons.Default.VerifiedUser, onOpenPermissionCenter),
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    bottomServices.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            row.forEach { item ->
                                MenuIconTile(item, Modifier.weight(1f))
                            }
                            repeat(3 - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

private data class MenuTileSpec(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

@Composable
private fun MenuIconTile(
    spec: MenuTileSpec,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = spec.onClick,
        colors = CardDefaults.cardColors(containerColor = HisabGreenTint),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, HisabGreenBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.height(92.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = spec.icon,
                contentDescription = spec.title,
                tint = HisabGreen,
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = spec.title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = InkPrimary,
                ),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun PaymentGatewayScreen(
    providerId: String,
    title: String,
    amount: Double,
    smsPackageCount: Int = 0,
    activateGold: Boolean = false,
    goldPackageId: String = "",
    userId: String = "",
    onBack: () -> Unit,
    onGoldActivated: () -> Unit = {},
    onRecordPaymentComplete: (
        provider: PaymentProvider,
        paidAmount: Double,
        txType: String,
        gatewayTxId: String,
        isDemo: Boolean,
        accountNumber: String,
    ) -> Unit = { _, _, _, _, _, _ -> },
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val goldPlan = remember(activateGold, goldPackageId, amount) {
        if (activateGold) GoldPackagePlan.fromIdOrDays(goldPackageId, amount.toInt()) else null
    }

    var selectedProvider by rememberSaveable {
        mutableStateOf(
            PaymentProvider.entries.find { it.id == providerId } ?: PaymentProvider.BKASH
        )
    }
    var amountInput by rememberSaveable {
        mutableStateOf(
            when {
                goldPlan != null -> goldPlan.priceTaka.toString()
                amount > 0.0 -> amount.toLong().toString()
                else -> ""
            }
        )
    }
    var walletNumber by rememberSaveable { mutableStateOf("") }
    val defaultTxType = remember(title) {
        if (title.contains("অ্যাড মানি") || title.contains("ক্যাশ কালেকশন")) "income" else "expense"
    }
    var txType by rememberSaveable { mutableStateOf(defaultTxType) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var completedResult by remember { mutableStateOf<PaymentGatewayResult?>(null) }
    var pendingCheckoutTxId by rememberSaveable { mutableStateOf<String?>(null) }
    var showBackendDialog by remember { mutableStateOf(false) }

    var savedBackendUrl by remember { mutableStateOf(PaymentGatewayConfig.getBackendServerUrl(context)) }
    var forceDemoMode by remember { mutableStateOf(PaymentGatewayConfig.isForceDemoMode(context)) }
    val currentMode = if (!forceDemoMode && (savedBackendUrl.startsWith("https://") || savedBackendUrl.startsWith("http://"))) {
        GatewayExecutionMode.BACKEND_SERVER
    } else {
        GatewayExecutionMode.DEMO
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCanvas),
    ) {
        HisaberTopBar(
            title = title,
            showBack = true,
            onBackClick = onBack,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "পেমেন্ট মাধ্যম নির্বাচন করুন",
                style = MaterialTheme.typography.titleMedium,
                color = InkPrimary,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PaymentProvider.entries.forEach { provider ->
                    val selected = selectedProvider == provider
                    Card(
                        onClick = { selectedProvider = provider },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) HisabGreenTint else PureWhite,
                        ),
                        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) HisabGreen else CardBorder),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).testTag("gateway_provider_${provider.id}"),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            AuthenticProviderLogo(providerId = provider.id, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = provider.displayNameBn,
                                fontWeight = FontWeight.Bold,
                                color = InkPrimary,
                            )
                        }
                    }
                }
            }

            // Amount & Wallet Input Card
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (goldPlan != null) {
                        Surface(
                            color = Color(0xFFFEF7DC),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFF3D478)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "${goldPlan.bannerTitleBn} • মূল্য: ${goldPlan.priceDisplayBn} (নিরাপদ গেটওয়ে যাচাইয়ের পর প্যাকেজ চালু হবে; এখানে কোনো PIN বা পেমেন্ট OTP লাগবে না)",
                                color = Color(0xFF78350F),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            )
                        }
                    } else if (smsPackageCount > 0) {
                        Surface(
                            color = PaonaGreenBg,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "প্যাকেজ সুবিধা: +$smsPackageCount SMS ব্যালেন্সে যোগ হবে",
                                color = PaonaGreen,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            )
                        }
                    }

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { raw ->
                            amountInput = raw.filter { it.isDigit() || it == '.' }
                            errorMessage = null
                        },
                        enabled = smsPackageCount == 0 && !activateGold,
                        label = { Text("টাকার পরিমাণ (৳) *") },
                        prefix = { Text("৳ ", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HisabGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("gateway_amount_input"),
                    )

                    if (!activateGold) {
                        OutlinedTextField(
                            value = walletNumber,
                            onValueChange = { raw ->
                                walletNumber = raw.filter { it.isDigit() }.take(11)
                                errorMessage = null
                            },
                            label = { Text("${selectedProvider.displayNameBn} নম্বর (ঐচ্ছিক)") },
                            placeholder = { Text("01XXXXXXXXX") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HisabGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("gateway_wallet_input"),
                        )
                    }

                    if (smsPackageCount == 0 && !activateGold) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = txType == "expense",
                                onClick = { txType = "expense" },
                                label = { Text("পেমেন্ট / খরচ") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HisabGreenSoft,
                                    selectedLabelColor = HisabGreen,
                                ),
                            )
                            FilterChip(
                                selected = txType == "income",
                                onClick = { txType = "income" },
                                label = { Text("জমা / অ্যাড মানি") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HisabGreenSoft,
                                    selectedLabelColor = HisabGreen,
                                ),
                            )
                        }
                    }
                }
            }

            // Mode Banner: Demo Mode (Default, no secrets needed) vs Backend Server Mode
            if (currentMode == GatewayExecutionMode.DEMO) {
                Surface(
                    color = WarningAmberBg,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = WarningAmber)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ডেমো মোড চালু আছে (টেস্টিং)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "বর্তমানে কোনো আসল ${selectedProvider.displayNameBn} API সিক্রেট ছাড়াই ডেমো মোডে সব ফিচার পরীক্ষা করা যাচ্ছে। পরে একটি নিরাপদ ব্যাকএন্ড সার্ভারের মাধ্যমে আসল মার্চেন্ট ক্রেডেনশিয়াল যুক্ত করতে পারবেন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkPrimary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { showBackendDialog = true },
                            border = BorderStroke(1.dp, HisabGreen),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Icon(Icons.Default.CloudQueue, contentDescription = null, tint = HisabGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ব্যাকএন্ড সার্ভার কনফিগার করুন (ঐচ্ছিক)", color = HisabGreen, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                Surface(
                    color = PaonaGreenBg,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, PaonaGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = PaonaGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ব্যাকএন্ড সার্ভার মোড সক্রিয়",
                                style = MaterialTheme.typography.titleMedium.copy(color = PaonaGreen, fontWeight = FontWeight.Bold),
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "সার্ভার: $savedBackendUrl\nসকল মার্চেন্ট API সিক্রেট নিরাপদে আপনার ব্যাকএন্ড সার্ভারে সংরক্ষিত থাকবে।",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkPrimary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { showBackendDialog = true }) {
                            Text("সার্ভার সেটিংস / ডেমো মোডে ফিরুন", color = HisabGreen, fontSize = 12.sp)
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = DenaRed,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Surface(color = PureWhite, shadowElevation = 6.dp, modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    val parsedAmt = goldPlan?.priceTaka?.toDouble() ?: (amountInput.toDoubleOrNull() ?: 0.0)
                    if (parsedAmt <= 0.0) {
                        errorMessage = "সঠিক টাকার পরিমাণ লিখুন (শূন্যের বেশি)"
                        return@Button
                    }
                    if (isProcessing) return@Button
                    errorMessage = null
                    isProcessing = true
                    scope.launch {
                        val existingTxId = pendingCheckoutTxId
                        if (existingTxId != null) {
                            val verifyRes = BackendGatewayClient.verifyPaymentStatus(
                                context = context,
                                provider = selectedProvider,
                                transactionId = existingTxId,
                                expectedAmount = parsedAmt,
                            )
                            isProcessing = false
                            verifyRes.onSuccess { verifiedResult ->
                                if (goldPlan != null) {
                                    when (
                                        val activation = GoldPackageManager.activateVerifiedGoldPackage(
                                            context = context,
                                            plan = goldPlan,
                                            providerId = selectedProvider.id,
                                            verifiedTxId = verifiedResult.transactionId,
                                            paidAmountTaka = verifiedResult.amount,
                                        )
                                    ) {
                                        is GoldActivationOutcome.Activated -> {
                                            pendingCheckoutTxId = null
                                            onRecordPaymentComplete(
                                                selectedProvider,
                                                verifiedResult.amount,
                                                txType,
                                                verifiedResult.transactionId,
                                                verifiedResult.isDemo,
                                                walletNumber,
                                            )
                                            onGoldActivated()
                                        }
                                        is GoldActivationOutcome.DuplicateTransaction -> {
                                            errorMessage = "এই পেমেন্ট ইতিমধ্যে ব্যবহার করা হয়েছে।"
                                        }
                                        is GoldActivationOutcome.InvalidPayment -> {
                                            errorMessage = activation.reasonBn
                                        }
                                    }
                                } else {
                                    pendingCheckoutTxId = null
                                    onRecordPaymentComplete(
                                        selectedProvider,
                                        verifiedResult.amount,
                                        txType,
                                        verifiedResult.transactionId,
                                        verifiedResult.isDemo,
                                        walletNumber,
                                    )
                                    completedResult = verifiedResult
                                }
                            }.onFailure { err ->
                                errorMessage = err.localizedMessage ?: "পেমেন্ট যাচাই সম্পন্ন হয়নি।"
                            }
                            return@launch
                        }

                        val res = BackendGatewayClient.initiatePayment(
                            context = context,
                            provider = selectedProvider,
                            amount = parsedAmt,
                            accountNumber = walletNumber,
                            reference = goldPlan?.id ?: title,
                            userId = userId,
                        )
                        res.onSuccess { gwResult ->
                            if (gwResult.checkoutUrl.isNotBlank()) {
                                isProcessing = false
                                pendingCheckoutTxId = gwResult.transactionId
                                runCatching {
                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(gwResult.checkoutUrl))
                                    context.startActivity(browserIntent)
                                }.onFailure {
                                    errorMessage = "পেমেন্ট ব্রাউজার খোলা যায়নি।"
                                }
                                return@onSuccess
                            }

                            val verifiedRes = BackendGatewayClient.verifyPaymentStatus(
                                context = context,
                                provider = selectedProvider,
                                transactionId = gwResult.transactionId,
                                expectedAmount = parsedAmt,
                            )
                            isProcessing = false
                            verifiedRes.onSuccess { verified ->
                                if (goldPlan != null) {
                                    when (
                                        val activation = GoldPackageManager.activateVerifiedGoldPackage(
                                            context = context,
                                            plan = goldPlan,
                                            providerId = selectedProvider.id,
                                            verifiedTxId = verified.transactionId,
                                            paidAmountTaka = verified.amount,
                                        )
                                    ) {
                                        is GoldActivationOutcome.Activated -> {
                                            onRecordPaymentComplete(
                                                selectedProvider,
                                                verified.amount,
                                                txType,
                                                verified.transactionId,
                                                verified.isDemo,
                                                walletNumber,
                                            )
                                            onGoldActivated()
                                        }
                                        is GoldActivationOutcome.DuplicateTransaction -> {
                                            errorMessage = "এই পেমেন্ট ইতিমধ্যে ব্যবহার করা হয়েছে।"
                                        }
                                        is GoldActivationOutcome.InvalidPayment -> {
                                            errorMessage = activation.reasonBn
                                        }
                                    }
                                } else {
                                    onRecordPaymentComplete(
                                        selectedProvider,
                                        verified.amount,
                                        txType,
                                        verified.transactionId,
                                        verified.isDemo,
                                        walletNumber,
                                    )
                                    completedResult = verified
                                }
                            }.onFailure { err ->
                                errorMessage = err.localizedMessage ?: "পেমেন্ট যাচাই ব্যর্থ হয়েছে।"
                            }
                        }.onFailure { err ->
                            isProcessing = false
                            errorMessage = err.localizedMessage ?: "পেমেন্ট অনুরোধ ব্যর্থ হয়েছে"
                        }
                    }
                },
                enabled = !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp)
                    .testTag("gateway_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = HisabGreen),
                shape = RoundedCornerShape(12.dp),
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = PureWhite, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                } else {
                    Text(
                        text = when {
                            pendingCheckoutTxId != null -> "পেমেন্ট যাচাই ও চালু করুন"
                            currentMode == GatewayExecutionMode.DEMO -> "${selectedProvider.displayNameBn} ডেমো পেমেন্ট সম্পন্ন করুন"
                            else -> "${selectedProvider.displayNameBn} নিরাপদ পেমেন্ট শুরু করুন"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    )
                }
            }
        }
    }

    if (showBackendDialog) {
        BackendServerConfigDialog(
            initialUrl = savedBackendUrl,
            initialForceDemo = forceDemoMode,
            onDismiss = { showBackendDialog = false },
            onSave = { url, demo ->
                PaymentGatewayConfig.saveBackendConfig(context, url, demo)
                savedBackendUrl = PaymentGatewayConfig.getBackendServerUrl(context)
                forceDemoMode = PaymentGatewayConfig.isForceDemoMode(context)
                showBackendDialog = false
            },
        )
    }

    completedResult?.let { result ->
        AlertDialog(
            onDismissRequest = { completedResult = null; onBack() },
            icon = {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PaonaGreen, modifier = Modifier.size(44.dp))
            },
            title = {
                Text(
                    text = if (result.isDemo) "ডেমো পেমেন্ট সফল (টেস্টিং)" else "পেমেন্ট অনুরোধ সফল",
                    textAlign = TextAlign.Center,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AuthenticProviderLogo(providerId = result.provider.id, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${result.provider.displayNameBn} • ${Formatters.formatTaka(result.amount)}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                    Text("রেফারেন্স আইডি: ${result.transactionId}", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                    if (smsPackageCount > 0) {
                        Text(
                            text = "+$smsPackageCount SMS আপনার ব্যালেন্সে যোগ হয়েছে।",
                            style = MaterialTheme.typography.bodyMedium.copy(color = PaonaGreen, fontWeight = FontWeight.Bold),
                        )
                    }
                    Text(result.message, style = MaterialTheme.typography.bodySmall, color = InkPrimary)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        completedResult = null
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HisabGreen),
                ) {
                    Text("ঠিক আছে")
                }
            },
        )
    }
}

@Composable
fun BackendServerConfigDialog(
    initialUrl: String,
    initialForceDemo: Boolean,
    onDismiss: () -> Unit,
    onSave: (serverUrl: String, forceDemo: Boolean) -> Unit,
) {
    var serverUrl by remember { mutableStateOf(initialUrl) }
    var forceDemo by remember { mutableStateOf(initialForceDemo) }
    var err by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ব্যাকএন্ড সার্ভার ও ডেমো মোড") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "নিরাপত্তার জন্য bKash, Nagad, Rocket এবং SMS Gateway-এর আসল API সিক্রেট অ্যাপের ভেতর না রেখে আপনার নিজস্ব ব্যাকএন্ড সার্ভারে (যেমন Cloud Functions) রাখুন।",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("ডেমো মোড চালু রাখুন", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = forceDemo,
                        onCheckedChange = { forceDemo = it; err = null },
                        colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = HisabGreen),
                    )
                }
                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it; err = null },
                    label = { Text("ব্যাকএন্ড সার্ভার URL (ঐচ্ছিক)") },
                    placeholder = { Text("https://api.yourserver.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (err != null) {
                    Text(err!!, color = DenaRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = serverUrl.trim()
                    if (!forceDemo && !(trimmed.startsWith("https://") || trimmed.startsWith("http://"))) {
                        err = "লাইভ মোডের জন্য সঠিক https:// সার্ভার URL দিন অথবা ডেমো মোড চালু রাখুন"
                    } else {
                        onSave(trimmed, forceDemo)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = HisabGreen),
            ) { Text("সংরক্ষণ") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        },
    )
}
