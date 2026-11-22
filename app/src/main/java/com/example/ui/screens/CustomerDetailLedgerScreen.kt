package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.Customer
import com.example.data.model.LedgerTransaction
import com.example.ui.components.EmptyStateBox
import com.example.ui.components.GoldCrownIllustration
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DenaRed
import com.example.ui.theme.DenaRedBg
import com.example.ui.theme.HisabGreen
import com.example.ui.theme.HisabGreenSoft
import com.example.ui.theme.HisabGreenTint
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PureWhite
import com.example.util.CustomerPhotoStore
import com.example.util.Formatters
import com.example.util.GoldPackageManager
import com.example.util.HishabiPrefsStore
import kotlin.math.abs

internal val EmeraldLedger = Color(0xFF057A4B)
internal val EmeraldLedgerDark = Color(0xFF04633D)
internal val DuePinkCardBg = Color(0xFFFDE8E8)
internal val DuePinkCardBorder = Color(0xFFF5B8B8)
internal val DuePinkText = Color(0xFFB42318)
private val CustomerGoldRing = Color(0xFFF5B400)
private val SoftActionBlueBg = Color(0xFFEAF2FE)
private val SoftActionBlueBorder = Color(0xFFC8DDFB)
private val SoftActionBlueText = Color(0xFF1D4ED8)
private val ConfirmIdlePinkBg = Color(0xFFFDE8EA)
private val ConfirmIdlePinkText = Color(0xFFE11D48)

private enum class CustomerLedgerSubView {
    PROFILE,
    TAGADA_PREVIEW,
    REPORT,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customer: Customer?,
    businessName: String = "হিসাবি খাতা",
    transactions: List<LedgerTransaction>,
    onBack: () -> Unit,
    onSaveTransaction: (
        type: String,
        amount: Double,
        description: String,
        dateMillis: Long,
        attachmentUri: String,
    ) -> Unit,
    onUpdateTransaction: (
        oldTx: LedgerTransaction,
        newAmount: Double,
        newDescription: String,
        newDateMillis: Long,
    ) -> Unit,
    onSendSms: () -> Unit,
    onOpenQr: () -> Unit,
    onUpdateCustomer: (name: String, phone: String, address: String, notes: String) -> Unit,
    onDeleteCustomer: () -> Unit,
    onDeleteTransaction: (LedgerTransaction) -> Unit,
    onShowMessage: (String) -> Unit = {},
    onOpenEditCustomer: (() -> Unit)? = null,
    onOpenHisabiGold: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    var activeSubView by rememberSaveable { mutableStateOf(CustomerLedgerSubView.PROFILE) }
    var editingTx by remember { mutableStateOf<LedgerTransaction?>(null) }

    if (activeSubView != CustomerLedgerSubView.PROFILE) {
        BackHandler {
            activeSubView = CustomerLedgerSubView.PROFILE
        }
    }

    if (customer == null) {
        Column(modifier = Modifier.fillMaxSize().background(PureWhite)) {
            HishabiShapedTopHeader(title = "কাস্টমার প্রোফাইল", onBack = onBack)
            EmptyStateBox(
                icon = Icons.Default.Groups,
                title = "কাস্টমার খুঁজে পাওয়া যায়নি",
                subtitle = "তালিকায় ফিরে যান।",
                actionLabel = "ফিরে যান",
                onAction = onBack,
            )
        }
        return
    }

    when (activeSubView) {
        CustomerLedgerSubView.TAGADA_PREVIEW -> {
            CustomerTagadaPreviewScreen(
                customer = customer,
                businessName = businessName,
                onBack = { activeSubView = CustomerLedgerSubView.PROFILE },
                onOpenSmsFallback = onSendSms,
                onShowMessage = onShowMessage,
            )
        }

        CustomerLedgerSubView.REPORT -> {
            CustomerLedgerReportScreen(
                customer = customer,
                businessName = businessName,
                transactions = transactions,
                onBack = { activeSubView = CustomerLedgerSubView.PROFILE },
                onTransactionClick = { tx -> editingTx = tx },
                onShowMessage = onShowMessage,
            )
        }

        CustomerLedgerSubView.PROFILE -> {
            CustomerProfileEntryContent(
                customer = customer,
                businessName = businessName,
                photoPath = CustomerPhotoStore.getPhotoPath(context, customer.id),
                transactions = transactions,
                onBack = onBack,
                onOpenTagada = { activeSubView = CustomerLedgerSubView.TAGADA_PREVIEW },
                onOpenReport = { activeSubView = CustomerLedgerSubView.REPORT },
                onSaveTransaction = onSaveTransaction,
                onTransactionClick = { tx -> editingTx = tx },
                onUpdateCustomer = onUpdateCustomer,
                onDeleteCustomer = onDeleteCustomer,
                onShowMessage = onShowMessage,
                onOpenEditCustomer = onOpenEditCustomer,
                onOpenHisabiGold = onOpenHisabiGold,
            )
        }
    }

    if (editingTx != null) {
        EditDeleteTransactionDialog(
            customerName = customer.name,
            tx = editingTx!!,
            currentCustomerBalance = customer.balance,
            onDismiss = { editingTx = null },
            onDelete = {
                val target = editingTx!!
                editingTx = null
                onDeleteTransaction(target)
            },
            onUpdate = { newAmount, newDesc, newDateMillis ->
                val target = editingTx!!
                editingTx = null
                onUpdateTransaction(target, newAmount, newDesc, newDateMillis)
            },
        )
    }
}

@Composable
internal fun HishabiShapedTopHeader(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Surface(
        color = EmeraldLedger,
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp).testTag("ledger_header_back"),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "পিছনে যান",
                    tint = PureWhite,
                )
            }
            Text(
                text = title,
                color = PureWhite,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            actions()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerProfileEntryContent(
    customer: Customer,
    businessName: String,
    photoPath: String,
    transactions: List<LedgerTransaction>,
    onBack: () -> Unit,
    onOpenTagada: () -> Unit,
    onOpenReport: () -> Unit,
    onSaveTransaction: (type: String, amount: Double, description: String, dateMillis: Long, attachmentUri: String) -> Unit,
    onTransactionClick: (LedgerTransaction) -> Unit,
    onUpdateCustomer: (name: String, phone: String, address: String, notes: String) -> Unit,
    onDeleteCustomer: () -> Unit,
    onShowMessage: (String) -> Unit,
    onOpenEditCustomer: (() -> Unit)?,
    onOpenHisabiGold: (() -> Unit)?,
) {
    val context = LocalContext.current
    val prefsState by HishabiPrefsStore.prefsFlow.collectAsState()
    val effectivePrefs = remember(prefsState) { HishabiPrefsStore.load(context) }
    val goldSnapshot = remember(effectivePrefs) { GoldPackageManager.computeSnapshot(effectivePrefs) }

    var gaveAmountText by rememberSaveable { mutableStateOf("") }
    var receivedAmountText by rememberSaveable { mutableStateOf("") }
    var activeType by rememberSaveable { mutableStateOf("gave") }
    var description by rememberSaveable { mutableStateOf("") }
    var selectedDateMillis by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    var hishabiMessageEnabled by rememberSaveable { mutableStateOf(false) }
    var attachmentUri by rememberSaveable { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showEditCustomerDialog by remember { mutableStateOf(false) }
    var showDeleteCustomerConfirm by remember { mutableStateOf(false) }

    val triggerCustomerEdit: () -> Unit = {
        if (onOpenEditCustomer != null) {
            onOpenEditCustomer()
        } else {
            showEditCustomerDialog = true
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) attachmentUri = uri.toString()
    }

    val filteredTx = remember(transactions, searchQuery) {
        if (searchQuery.isBlank()) {
            transactions
        } else {
            transactions.filter {
                it.description.contains(searchQuery, ignoreCase = true) ||
                    Formatters.formatTaka(it.amount).contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val hasEnteredAmount = (gaveAmountText.toDoubleOrNull() ?: 0.0) > 0.0 ||
        (receivedAmountText.toDoubleOrNull() ?: 0.0) > 0.0

    val latestActivityMillis = remember(customer, transactions) {
        val custTime = (customer.updatedAt ?: customer.createdAt)?.toDate()?.time ?: 0L
        val txTime = transactions.maxOfOrNull {
            maxOf(it.dateMillis, (it.updatedAt ?: it.createdAt)?.toDate()?.time ?: 0L)
        } ?: 0L
        maxOf(custTime, txTime).takeIf { it > 0L } ?: System.currentTimeMillis()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite)
            .imePadding()
            .navigationBarsPadding(),
    ) {
        // Panel 2 Clean White Top Bar
        Surface(color = PureWhite, shadowElevation = 1.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(48.dp).testTag("customer_profile_back"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "পিছনে যান",
                        tint = InkPrimary,
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = triggerCustomerEdit),
                ) {
                    Text(
                        text = customer.name,
                        color = InkPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (customer.phone.isNotBlank()) {
                        Text(
                            text = customer.phone,
                            color = InkSecondary,
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                IconButton(
                    onClick = { showSearchBar = !showSearchBar },
                    modifier = Modifier.size(44.dp).testTag("customer_tx_search_toggle"),
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "লেনদেন খুঁজুন",
                        tint = InkPrimary,
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(44.dp).testTag("customer_profile_menu"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "আরও অপশন",
                            tint = InkPrimary,
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("গ্রাহকের তথ্য এডিট করুন") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                triggerCustomerEdit()
                            },
                        )
                        if (customer.phone.isNotBlank()) {
                            DropdownMenuItem(
                                text = { Text("সরাসরি কল করুন") },
                                leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}")))
                                },
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("গ্রাহক মুছে ফেলুন", color = DenaRed) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = DenaRed) },
                            onClick = {
                                showMenu = false
                                showDeleteCustomerConfirm = true
                            },
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (showSearchBar) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("বিবরণ বা টাকা দিয়ে খুঁজুন...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EmeraldLedger) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "মুছুন")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // 1. Centered Circular Customer Photo with Golden Ring & Black Camera Badge
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(98.dp)
                            .clip(CircleShape)
                            .clickable(onClick = triggerCustomerEdit)
                            .testTag("customer_profile_avatar_edit"),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(92.dp)
                                .clip(CircleShape)
                                .background(HisabGreenSoft)
                                .border(2.5.dp, CustomerGoldRing, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (photoPath.isNotBlank()) {
                                AsyncImage(
                                    model = photoPath,
                                    contentDescription = customer.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            } else {
                                Text(
                                    text = extractInitials(customer.name),
                                    color = EmeraldLedgerDark,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 3.dp, bottom = 3.dp)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(PureWhite)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1F2937)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "ছবি পরিবর্তন",
                                tint = PureWhite,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }
            }

            // 2. Gold Expiry Banner Pill ("হিসাবি খাতা গোল্ড-এর মেয়াদ অবশিষ্ট X দিন >")
            item {
                val goldBannerText = when {
                    goldSnapshot.isActive -> "হিসাবি খাতা গোল্ড-এর মেয়াদ অবশিষ্ট ${goldSnapshot.remainingDaysBangla}"
                    goldSnapshot.isExpired -> "হিসাবি খাতা গোল্ড-এর মেয়াদ শেষ • নবায়ন করুন"
                    else -> "হিসাবি খাতা গোল্ড-এর মেয়াদ অবশিষ্ট ০ দিন"
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, Color(0xFFE5B82E)),
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .clickable { onOpenHisabiGold?.invoke() }
                        .testTag("customer_gold_expiry_banner"),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFFF7CC), Color(0xFFFDE68A), Color(0xFFFCE077))
                                )
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        GoldCrownIllustration(modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = goldBannerText,
                            color = Color(0xFF2B1600),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "গোল্ড বিবরণ",
                            tint = Color(0xFF2B1600),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            // 3. Balance Display + Relative Time on Left & Soft-Blue 'তাগাদা' + 'রিপোর্ট' Pills on Right
            item {
                val isPaona = customer.balance >= 0.0
                val statusLabel = when {
                    customer.balance > 0.0 -> "পাবো"
                    customer.balance < 0.0 -> "দেবো"
                    else -> "পরিশোধিত"
                }
                val balanceColor = when {
                    customer.balance > 0.0 -> DuePinkText
                    customer.balance < 0.0 -> EmeraldLedger
                    else -> InkPrimary
                }
                val amountFormatted = Formatters.formatDecimalAmount(abs(customer.balance), useBanglaDigits = true)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$statusLabel ৳ $amountFormatted",
                            color = balanceColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "(${Formatters.formatRelativeBanglaTime(latestActivityMillis)})",
                            color = InkMuted,
                            fontSize = 12.sp,
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            onClick = onOpenTagada,
                            color = SoftActionBlueBg,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, SoftActionBlueBorder),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("customer_tagada_button"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = SoftActionBlueText,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "তাগাদা",
                                    color = InkPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        Surface(
                            onClick = onOpenReport,
                            color = SoftActionBlueBg,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, SoftActionBlueBorder),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("customer_report_button"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = SoftActionBlueText,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "রিপোর্ট",
                                    color = InkPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }

            // 4. Side-by-Side Dual Amount Inputs: '৳ দিলাম/বাকি' & '৳ পেমেন্ট'
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedTextField(
                        value = gaveAmountText,
                        onValueChange = { raw ->
                            val clean = raw.filter { it.isDigit() || it == '.' }
                            if (clean.count { it == '.' } <= 1) {
                                gaveAmountText = clean.take(12)
                                if (clean.isNotEmpty()) {
                                    receivedAmountText = ""
                                    activeType = "gave"
                                }
                                validationError = null
                            }
                        },
                        placeholder = { Text("দিলাম/বাকি", color = InkMuted, fontSize = 14.5.sp) },
                        prefix = {
                            Text(
                                text = "৳ ",
                                color = InkPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = DenaRed,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (activeType == "gave") DenaRed else EmeraldLedger,
                            unfocusedBorderColor = CardBorder,
                            focusedContainerColor = PureWhite,
                            unfocusedContainerColor = PureWhite,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_gave_input"),
                    )

                    OutlinedTextField(
                        value = receivedAmountText,
                        onValueChange = { raw ->
                            val clean = raw.filter { it.isDigit() || it == '.' }
                            if (clean.count { it == '.' } <= 1) {
                                receivedAmountText = clean.take(12)
                                if (clean.isNotEmpty()) {
                                    gaveAmountText = ""
                                    activeType = "received"
                                }
                                validationError = null
                            }
                        },
                        placeholder = { Text("পেমেন্ট", color = InkMuted, fontSize = 14.5.sp) },
                        prefix = {
                            Text(
                                text = "৳ ",
                                color = InkPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldLedger,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldLedger,
                            unfocusedBorderColor = CardBorder,
                            focusedContainerColor = PureWhite,
                            unfocusedContainerColor = PureWhite,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_received_input"),
                    )
                }
            }

            // 5. Full-Width 'বিবরণ' Input with Edit Icon
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("বিবরণ", color = InkMuted) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.EditNote,
                            contentDescription = null,
                            tint = InkPrimary,
                            modifier = Modifier.size(22.dp),
                        )
                    },
                    minLines = 1,
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldLedger,
                        unfocusedBorderColor = CardBorder,
                        focusedContainerColor = PureWhite,
                        unfocusedContainerColor = PureWhite,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_description_input"),
                )
            }

            // 6. 3-Item Pill Row: Date Pill + 'টালি-মেসেজ' Switch Pill + 'ছবি' Camera Pill
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(
                        onClick = { showDatePicker = true },
                        color = SoftActionBlueBg,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, SoftActionBlueBorder),
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("profile_date_button"),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "তারিখ নির্বাচন",
                                tint = SoftActionBlueText,
                                modifier = Modifier.size(17.dp),
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = Formatters.formatBanglaDayMonth(selectedDateMillis),
                                color = InkPrimary,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFFF3F6FA),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 10.dp, end = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "টালি-মেসেজ",
                                color = InkPrimary,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            Switch(
                                checked = hishabiMessageEnabled,
                                onCheckedChange = { hishabiMessageEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PureWhite,
                                    checkedTrackColor = EmeraldLedger,
                                    uncheckedThumbColor = PureWhite,
                                    uncheckedTrackColor = Color(0xFFCBD5E1),
                                ),
                                modifier = Modifier.testTag("profile_hishabi_message_switch"),
                            )
                        }
                    }

                    Surface(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        color = if (attachmentUri.isNotBlank()) HisabGreenSoft else SoftActionBlueBg,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            1.dp,
                            if (attachmentUri.isNotBlank()) EmeraldLedger else SoftActionBlueBorder,
                        ),
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("profile_camera_button"),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "ছবি যুক্ত করুন",
                                tint = SoftActionBlueText,
                                modifier = Modifier.size(17.dp),
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "ছবি",
                                color = InkPrimary,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }

            if (attachmentUri.isNotBlank()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(HisabGreenTint)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AsyncImage(
                            model = Uri.parse(attachmentUri),
                            contentDescription = "রসিদ ছবি",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "রসিদ/ভাউচার ছবি যুক্ত হয়েছে",
                            color = EmeraldLedgerDark,
                            fontSize = 12.5.sp,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { attachmentUri = "" }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "ছবি সরান", tint = DenaRed, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            if (validationError != null) {
                item {
                    Text(
                        text = validationError!!,
                        color = DenaRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            // 7. Panel 2 'নিশ্চিত' Action CTA Button (Soft Rose when idle, Vibrant Green when amount entered)
            item {
                Button(
                    onClick = {
                        val gaveVal = gaveAmountText.toDoubleOrNull() ?: 0.0
                        val receivedVal = receivedAmountText.toDoubleOrNull() ?: 0.0
                        val (txType, txAmount) = when {
                            gaveVal > 0.0 -> "gave" to gaveVal
                            receivedVal > 0.0 -> "received" to receivedVal
                            else -> "" to 0.0
                        }
                        if (txAmount <= 0.0) {
                            validationError = "অনুগ্রহ করে 'দিলাম/বাকি' অথবা 'পেমেন্ট' ঘরে সঠিক পরিমাণ লিখুন"
                        } else {
                            val savedDesc = description.trim()
                            onSaveTransaction(txType, txAmount, savedDesc, selectedDateMillis, attachmentUri)
                            if (hishabiMessageEnabled) {
                                val projectedDue = if (txType == "gave") customer.balance + txAmount else customer.balance - txAmount
                                val msg = buildTagadaSmsText(
                                    customerName = customer.name,
                                    businessName = businessName,
                                    dueAmount = abs(projectedDue),
                                    customerId = customer.id,
                                )
                                val smsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${customer.phone}")).apply {
                                    putExtra("sms_body", msg)
                                }
                                runCatching { context.startActivity(smsIntent) }
                                    .onFailure { onShowMessage("লেনদেন সংরক্ষিত হয়েছে (টালি-মেসেজ প্রস্তুত)") }
                            }
                            gaveAmountText = ""
                            receivedAmountText = ""
                            description = ""
                            attachmentUri = ""
                            validationError = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasEnteredAmount) EmeraldLedger else ConfirmIdlePinkBg,
                        contentColor = if (hasEnteredAmount) PureWhite else ConfirmIdlePinkText,
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("profile_confirm_button"),
                ) {
                    Text(
                        text = "নিশ্চিত",
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // Transaction History Section Header
            item {
                HorizontalDivider(color = CardBorder, modifier = Modifier.padding(vertical = 2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "লেনদেনসমূহ (${Formatters.toBanglaDigits(filteredTx.size.toString())})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary,
                    )
                    Text(
                        text = "এডিট করতে ট্যাপ করুন",
                        fontSize = 11.5.sp,
                        color = EmeraldLedger,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            if (filteredTx.isEmpty()) {
                item {
                    EmptyStateBox(
                        icon = Icons.Default.Description,
                        title = "কোনো লেনদেন পাওয়া যায়নি",
                        subtitle = "উপরে দিলাম/বাকি বা পেমেন্ট ঘরে টাকা লিখে নিশ্চিত করুন।",
                    )
                }
            } else {
                items(filteredTx, key = { it.id }) { tx ->
                    val isGave = tx.type == "gave"
                    Surface(
                        onClick = { onTransactionClick(tx) },
                        color = PureWhite,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_tx_item_${tx.id}"),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isGave) "দিলাম / বাকি" else "পেমেন্ট",
                                    color = if (isGave) DenaRed else EmeraldLedger,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                if (tx.description.isNotBlank()) {
                                    Text(
                                        text = tx.description,
                                        color = InkPrimary,
                                        fontSize = 13.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Text(
                                    text = Formatters.formatBanglaDateTime(tx.dateMillis),
                                    color = InkMuted,
                                    fontSize = 11.5.sp,
                                )
                            }
                            Text(
                                text = Formatters.formatTakaDecimal(tx.amount, useBanglaDigits = true),
                                color = if (isGave) DenaRed else EmeraldLedger,
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "লেনদেন এডিট",
                                tint = InkMuted,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "হিসাবের খাতা",
                        color = EmeraldLedgerDark.copy(alpha = 0.78f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dateState.selectedDateMillis?.let { selectedDateMillis = it }
                        showDatePicker = false
                    }
                ) { Text("ঠিক আছে", color = EmeraldLedger, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("বাতিল") }
            },
        ) {
            DatePicker(state = dateState)
        }
    }

    if (showEditCustomerDialog) {
        CustomerFormDialog(
            title = "গ্রাহকের তথ্য এডিট করুন",
            confirmLabel = "আপডেট করুন",
            showInitialBalance = false,
            initialName = customer.name,
            initialPhone = customer.phone,
            initialAddress = customer.address,
            initialNotes = customer.notes,
            onDismiss = { showEditCustomerDialog = false },
            onConfirm = { name, phone, address, _, notes ->
                onUpdateCustomer(name, phone, address, notes)
                showEditCustomerDialog = false
            },
        )
    }

    if (showDeleteCustomerConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteCustomerConfirm = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = DenaRed,
                    modifier = Modifier.size(30.dp),
                )
            },
            title = { Text("মুছে ফেলার কনফার্মেশন", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("আপনি কি নিশ্চিতভাবে \"${customer.name}\"-কে এবং তাঁর সকল হিসাব স্থায়ীভাবে মুছে ফেলতে চান?")
                    Text(
                        text = "হিসাবের খাতা",
                        color = EmeraldLedgerDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteCustomerConfirm = false
                        onDeleteCustomer()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DenaRed),
                ) { Text("হ্যাঁ, মুছে ফেলুন") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCustomerConfirm = false }) { Text("বাতিল করুন") }
            },
        )
    }
}

// --- Screen 3: Edit or Delete Transaction Popup Module ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDeleteTransactionDialog(
    customerName: String,
    tx: LedgerTransaction,
    currentCustomerBalance: Double,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: (newAmount: Double, newDescription: String, newDateMillis: Long) -> Unit,
) {
    var amountText by remember(tx.id) { mutableStateOf(tx.amount.toBigDecimal().stripTrailingZeros().toPlainString()) }
    var description by remember(tx.id) { mutableStateOf(tx.description) }
    var dateMillis by remember(tx.id) { mutableLongStateOf(tx.dateMillis) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirmPopup by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val isGave = tx.type == "gave"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = PureWhite,
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 10.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "লেনদেন এডিট বা মুছে ফেলুন",
                    color = InkPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )

                Surface(
                    color = HisabGreenTint,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = customerName,
                            color = InkPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp,
                        )
                        Text(
                            text = if (isGave) "দিলাম/বেচা" else "পেলাম",
                            color = if (isGave) DenaRed else EmeraldLedger,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                        )
                    }
                }

                // Date Selector Row ('১৭ নভেম্বর' format)
                Surface(
                    onClick = { showDatePicker = true },
                    color = PureWhite,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_tx_date_picker"),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "তারিখ: ${Formatters.formatBanglaDate(dateMillis)}",
                            color = InkPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "তারিখ পরিবর্তন",
                            tint = EmeraldLedger,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { raw ->
                        val clean = raw.filter { it.isDigit() || it == '.' }
                        if (clean.count { it == '.' } <= 1) {
                            amountText = clean.take(12)
                            errorMsg = null
                        }
                    },
                    label = { Text("টাকার পরিমাণ") },
                    prefix = { Text("৳ ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldLedger),
                    modifier = Modifier.fillMaxWidth().testTag("edit_tx_amount_input"),
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("বিবরণ") },
                    singleLine = false,
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldLedger),
                    modifier = Modifier.fillMaxWidth().testTag("edit_tx_description_input"),
                )

                Text(
                    text = "বর্তমান মোট ব্যালেন্স: ${Formatters.formatTakaDecimal(abs(currentCustomerBalance), useBanglaDigits = true)}",
                    color = InkSecondary,
                    fontSize = 12.5.sp,
                )

                if (errorMsg != null) {
                    Text(text = errorMsg!!, color = DenaRed, fontSize = 12.5.sp)
                }

                // Soft-red 'লেনদেন মুছে ফেলুন' Button -> Opens Delete Confirmation Popup
                OutlinedButton(
                    onClick = { showDeleteConfirmPopup = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = DenaRedBg,
                        contentColor = DuePinkText,
                    ),
                    border = BorderStroke(1.dp, DuePinkCardBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("edit_tx_delete_button"),
                ) {
                    Text("লেনদেন মুছে ফেলুন", fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                }

                // Bottom Row: 'বাতিল করুন' + 'আপডেট করুন'
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, EmeraldLedger),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("edit_tx_cancel_button"),
                    ) {
                        Text("বাতিল করুন", color = EmeraldLedger, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val parsed = amountText.toDoubleOrNull()
                            if (parsed == null || parsed <= 0.0) {
                                errorMsg = "সঠিক টাকার পরিমাণ লিখুন"
                            } else {
                                onUpdate(parsed, description.trim(), dateMillis)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldLedger),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("edit_tx_update_button"),
                    ) {
                        Text("আপডেট করুন", color = PureWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDeleteConfirmPopup) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmPopup = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = DenaRed,
                    modifier = Modifier.size(28.dp),
                )
            },
            title = { Text("মুছে ফেলার কনফার্মেশন", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "আপনি কি নিশ্চিতভাবে ${Formatters.formatTakaDecimal(tx.amount, useBanglaDigits = true)} টাকার এই লেনদেনটি মুছে ফেলতে চান?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmPopup = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DenaRed),
                ) { Text("হ্যাঁ, মুছে ফেলুন") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmPopup = false }) { Text("বাতিল") }
            },
        )
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { dateMillis = it }
                        showDatePicker = false
                    }
                ) { Text("ঠিক আছে", color = EmeraldLedger, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("বাতিল") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

private fun extractInitials(name: String): String {
    val words = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        words.isEmpty() -> "হ"
        words.size == 1 -> words[0].take(2).uppercase()
        else -> "${words[0].first()}${words[1].first()}".uppercase()
    }
}
