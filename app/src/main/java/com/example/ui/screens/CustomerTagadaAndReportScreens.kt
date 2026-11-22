package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.LedgerTransaction
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DenaRed
import com.example.ui.theme.HisabGreenBorder
import com.example.ui.theme.HisabGreenTint
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PureWhite
import com.example.util.Formatters
import com.example.util.PdfReportGenerator
import java.util.Calendar
import kotlin.math.abs

private val WhatsAppGreen = Color(0xFF16A34A)
private val MessengerBlue = Color(0xFF1D74F5)

internal fun buildPaymentLink(customerId: String): String {
    val slug = customerId.ifBlank { "pay" }.take(12)
    return "https://hisabikhata.app/pay/$slug"
}

internal fun buildTagadaSmsText(
    customerName: String,
    businessName: String,
    dueAmount: Double,
    customerId: String,
): String {
    val shop = businessName.ifBlank { "হিসাবের খাতা" }
    val amountStr = Formatters.formatTakaDecimal(abs(dueAmount), useBanglaDigits = true)
    val link = buildPaymentLink(customerId)
    return "প্রিয় $customerName, $shop-এ আপনার মোট বাকি $amountStr। অনুগ্রহ করে নিচের লিংকে পেমেন্ট করুন অথবা দোকানে যোগাযোগ করুন:\n$link\n— ধন্যবাদ, হিসাবের খাতা।"
}

// --- Screen 2: তাগাদা প্রিভিউ (Tagada Preview Screen) ---

@Composable
fun CustomerTagadaPreviewScreen(
    customer: Customer,
    businessName: String,
    onBack: () -> Unit,
    onOpenSmsFallback: () -> Unit,
    onShowMessage: (String) -> Unit,
) {
    val context = LocalContext.current
    val paymentLink = remember(customer.id) { buildPaymentLink(customer.id) }
    val dueAmount = abs(customer.balance)
    val shopName = businessName.ifBlank { "হিসাবি খাতা" }
    val shareMessage = remember(customer, shopName, paymentLink) {
        buildTagadaSmsText(
            customerName = customer.name,
            businessName = shopName,
            dueAmount = dueAmount,
            customerId = customer.id,
        )
    }

    fun copyToClipboard(label: String, content: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        cm?.setPrimaryClip(ClipData.newPlainText(label, content))
        onShowMessage("পেমেন্ট লিংক ও তাগাদা বার্তা কপি হয়েছে")
    }

    fun launchShareIntent(packageName: String? = null) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            if (!packageName.isNullOrBlank()) {
                setPackage(packageName)
            }
        }
        runCatching {
            context.startActivity(
                if (packageName == null) Intent.createChooser(intent, "তাগাদা শেয়ার করুন") else intent
            )
        }.onFailure {
            val fallback = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareMessage)
            }
            context.startActivity(Intent.createChooser(fallback, "তাগাদা শেয়ার করুন"))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite)
            .navigationBarsPadding(),
    ) {
        HishabiShapedTopHeader(
            title = "তাগাদা",
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Tagada Preview Copy Card
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "তাগাদা",
                    color = InkPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Surface(
                    color = PureWhite,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "প্রিয় ${customer.name},",
                            color = InkPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "$shopName-এ আপনার মোট বকেয়া রয়েছে ${Formatters.formatTakaDecimal(dueAmount, useBanglaDigits = true)}। অনুগ্রহ করে সময়মতো বকেয়া পরিশোধ করে হিসাব হালনাগাদ করুন।",
                            color = InkPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                        )
                        Text(
                            text = "দোকান: $shopName  •  অ্যাপ: হিসাবের খাতা",
                            color = EmeraldLedger,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            // Mint Green 'পেমেন্ট লিংক' Card with Copy Action
            Surface(
                color = HisabGreenTint,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, HisabGreenBorder),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "পেমেন্ট লিংক",
                                color = InkPrimary,
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = paymentLink,
                                color = EmeraldLedger,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        IconButton(
                            onClick = { copyToClipboard("Payment Link", paymentLink) },
                            modifier = Modifier.size(48.dp).testTag("tagada_copy_link_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "লিংক কপি করুন",
                                tint = EmeraldLedger,
                            )
                        }
                    }
                    HorizontalDivider(
                        color = HisabGreenBorder,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    Text(
                        text = "লিংক কপি করে বা নিচের অপশন থেকে শেয়ার করুন",
                        color = InkSecondary,
                        fontSize = 12.sp,
                    )
                }
            }

            // Share Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HorizontalDivider(color = CardBorder, modifier = Modifier.weight(1f))
                Text(
                    text = "শেয়ার",
                    color = InkSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                HorizontalDivider(color = CardBorder, modifier = Modifier.weight(1f))
            }

            // Brand Share Buttons matching reference layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = { launchShareIntent("com.whatsapp") },
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("tagada_whatsapp_filled"),
                ) {
                    Text("হোয়াটসঅ্যাপ", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Button(
                    onClick = { launchShareIntent("com.facebook.orca") },
                    colors = ButtonDefaults.buttonColors(containerColor = MessengerBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("tagada_messenger_filled"),
                ) {
                    Text("মেসেঞ্জার", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        if (customer.phone.isNotBlank()) {
                            val smsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${customer.phone}")).apply {
                                putExtra("sms_body", shareMessage)
                            }
                            runCatching { context.startActivity(smsIntent) }.onFailure { onOpenSmsFallback() }
                        } else {
                            onOpenSmsFallback()
                        }
                    },
                    border = BorderStroke(1.dp, EmeraldLedger),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("tagada_sms_button"),
                ) {
                    Icon(Icons.Default.Sms, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("এসএমএস তাগাদা", color = InkPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                }

                OutlinedButton(
                    onClick = { launchShareIntent("com.facebook.orca") },
                    border = BorderStroke(1.dp, MessengerBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("tagada_messenger_outlined"),
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, tint = MessengerBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("মেসেঞ্জার", color = InkPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                }
            }

            OutlinedButton(
                onClick = { launchShareIntent(null) },
                border = BorderStroke(1.2.dp, EmeraldLedger),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("tagada_general_share"),
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = EmeraldLedger, modifier = Modifier.size(19.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("শেয়ার", color = EmeraldLedger, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
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

// --- Screen 4: রিপোর্ট (Customer Detailed Ledger Report Screen) ---

private enum class ReportDateFilter(val label: String) {
    ALL("তারিখ - সব সময়"),
    TODAY("তারিখ - আজ"),
    LAST_7_DAYS("তারিখ - শেষ ৭ দিন"),
    THIS_MONTH("তারিখ - এই মাস"),
    CUSTOM("নির্দিষ্ট তারিখ থেকে"),
}

private data class LedgerReportRow(
    val tx: LedgerTransaction,
    val runningBalance: Double,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerLedgerReportScreen(
    customer: Customer,
    businessName: String,
    transactions: List<LedgerTransaction>,
    onBack: () -> Unit,
    onTransactionClick: (LedgerTransaction) -> Unit,
    onShowMessage: (String) -> Unit,
) {
    val context = LocalContext.current
    var selectedFilter by rememberSaveable { mutableStateOf(ReportDateFilter.ALL) }
    var customStartMillis by rememberSaveable { mutableLongStateOf(System.currentTimeMillis() - 30L * 86_400_000L) }
    var showFilterDropdown by remember { mutableStateOf(false) }
    var showCustomDatePicker by remember { mutableStateOf(false) }

    val startCutoffMillis = remember(selectedFilter, customStartMillis) {
        val cal = Calendar.getInstance()
        when (selectedFilter) {
            ReportDateFilter.ALL -> 0L
            ReportDateFilter.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            ReportDateFilter.LAST_7_DAYS -> {
                cal.add(Calendar.DAY_OF_YEAR, -7)
                cal.timeInMillis
            }
            ReportDateFilter.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.timeInMillis
            }
            ReportDateFilter.CUSTOM -> customStartMillis
        }
    }

    val sortedAllAsc = remember(transactions) { transactions.sortedBy { it.dateMillis } }

    // Infer implicit initial/previous balance prior to recorded transactions
    val allTxNetDelta = remember(sortedAllAsc) {
        sortedAllAsc.sumOf { if (it.type == "gave") it.amount else -it.amount }
    }
    val baseInitialBalance = customer.balance - allTxNetDelta

    val beforePeriodDelta = remember(sortedAllAsc, startCutoffMillis) {
        sortedAllAsc
            .filter { it.dateMillis < startCutoffMillis }
            .sumOf { if (it.type == "gave") it.amount else -it.amount }
    }
    val openingBalance = baseInitialBalance + beforePeriodDelta

    val periodTransactions = remember(sortedAllAsc, startCutoffMillis) {
        sortedAllAsc.filter { it.dateMillis >= startCutoffMillis }
    }
    val cashDue = remember(periodTransactions) {
        periodTransactions.filter { it.type == "gave" }.sumOf { it.amount }
    }
    val totalDeposit = remember(periodTransactions) {
        periodTransactions.filter { it.type == "received" }.sumOf { it.amount }
    }
    val closingBalance = openingBalance + cashDue - totalDeposit

    val reportRows = remember(periodTransactions, openingBalance) {
        var running = openingBalance
        periodTransactions.map { tx ->
            running += if (tx.type == "gave") tx.amount else -tx.amount
            LedgerReportRow(tx = tx, runningBalance = running)
        }
    }

    val activeFilterLabel = if (selectedFilter == ReportDateFilter.CUSTOM) {
        "তারিখ - ${Formatters.formatShortDate(customStartMillis)} থেকে"
    } else {
        selectedFilter.label
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite),
    ) {
        HishabiShapedTopHeader(
            title = "বিস্তারিত রিপোর্ট",
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Date Filter Selector Dropdown
            Box {
                Surface(
                    onClick = { showFilterDropdown = true },
                    color = PureWhite,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("report_date_filter_dropdown"),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = EmeraldLedger,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = activeFilterLabel,
                            color = InkPrimary,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "ফিল্টার পরিবর্তন",
                            tint = InkSecondary,
                        )
                    }
                }

                DropdownMenu(
                    expanded = showFilterDropdown,
                    onDismissRequest = { showFilterDropdown = false },
                ) {
                    ReportDateFilter.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                showFilterDropdown = false
                                selectedFilter = option
                                if (option == ReportDateFilter.CUSTOM) {
                                    showCustomDatePicker = true
                                }
                            },
                        )
                    }
                }
            }

            // Summary Card: ওপেনিং ব্যালেন্স, ক্যাশ ডিউ, মোট জমা
            Surface(
                color = HisabGreenTint,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, HisabGreenBorder),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ReportSummaryLineRow("ওপেনিং ব্যালেন্স", Formatters.formatTakaDecimal(openingBalance, true), InkPrimary)
                    ReportSummaryLineRow("বাকি বিক্রি (দিলাম/বেচা)", Formatters.formatTakaDecimal(cashDue, true), DenaRed)
                    ReportSummaryLineRow("মোট জমা (পেলাম)", Formatters.formatTakaDecimal(totalDeposit, true), EmeraldLedger)
                    HorizontalDivider(color = HisabGreenBorder)
                    ReportSummaryLineRow(
                        label = if (closingBalance >= 0) "মোট বাকি ব্যালেন্স" else "মোট জমা ব্যালেন্স",
                        value = Formatters.formatTakaDecimal(abs(closingBalance), true),
                        valueColor = if (closingBalance >= 0) DenaRed else EmeraldLedger,
                        boldLabel = true,
                    )
                }
            }

            // Date-Wise Transaction Table Header
            Surface(
                color = HisabGreenTint,
                shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TableHeaderText("তারিখ", Modifier.weight(0.95f))
                    TableHeaderText("বিবরণ", Modifier.weight(1.35f))
                    TableHeaderText("দিলাম", Modifier.weight(0.85f), TextAlign.End)
                    TableHeaderText("পেলাম", Modifier.weight(0.85f), TextAlign.End)
                    TableHeaderText("ব্যালেন্স", Modifier.weight(0.95f), TextAlign.End)
                }
            }

            // Scrollable Table Rows
            if (reportRows.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "এই তারিখের মধ্যে কোনো লেনদেন নেই",
                        color = InkMuted,
                        fontSize = 14.sp,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 8.dp),
                ) {
                    items(reportRows, key = { it.tx.id }) { row ->
                        val isGave = row.tx.type == "gave"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTransactionClick(row.tx) }
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = Formatters.formatBanglaDayMonth(row.tx.dateMillis),
                                color = InkPrimary,
                                fontSize = 11.5.sp,
                                modifier = Modifier.weight(0.95f),
                            )
                            Text(
                                text = row.tx.description.ifBlank { if (isGave) "বাকি/বেচা" else "জমা/পেলাম" },
                                color = InkPrimary,
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1.35f).padding(end = 4.dp),
                            )
                            Text(
                                text = if (isGave) Formatters.formatDecimalAmount(row.tx.amount, true) else "—",
                                color = if (isGave) DenaRed else InkMuted,
                                fontSize = 11.5.sp,
                                fontWeight = if (isGave) FontWeight.SemiBold else FontWeight.Normal,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(0.85f),
                            )
                            Text(
                                text = if (!isGave) Formatters.formatDecimalAmount(row.tx.amount, true) else "—",
                                color = if (!isGave) EmeraldLedger else InkMuted,
                                fontSize = 11.5.sp,
                                fontWeight = if (!isGave) FontWeight.SemiBold else FontWeight.Normal,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(0.85f),
                            )
                            Text(
                                text = Formatters.formatDecimalAmount(abs(row.runningBalance), true),
                                color = InkPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(0.95f),
                            )
                        }
                        HorizontalDivider(color = CardBorder)
                    }
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "হিসাবের খাতা",
                                color = EmeraldLedgerDark.copy(alpha = 0.78f),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }

        // Bottom Full-Width 'রিপোর্ট ডাউনলোড করুন' Button
        Surface(
            color = PureWhite,
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            Button(
                onClick = {
                    PdfReportGenerator.generateCustomerLedgerPdf(
                        context = context,
                        businessName = businessName.ifBlank { "হিসাবি খাতা" },
                        customerName = customer.name,
                        customerPhone = customer.phone,
                        filterLabel = activeFilterLabel,
                        openingBalance = openingBalance,
                        cashDue = cashDue,
                        totalDeposit = totalDeposit,
                        closingBalance = closingBalance,
                        transactions = periodTransactions,
                    ).onSuccess { pdfUri ->
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, pdfUri)
                            putExtra(Intent.EXTRA_SUBJECT, "হিসাবি খাতা - ${customer.name} রিপোর্ট")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "রিপোর্ট ডাউনলোড করুন"))
                    }.onFailure {
                        onShowMessage("রিপোর্ট PDF তৈরি করতে সমস্যা হয়েছে")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldLedger),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(52.dp)
                    .testTag("download_customer_report_button"),
            ) {
                Icon(Icons.Default.Download, contentDescription = null, tint = PureWhite, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "বিস্তারিত রিপোর্ট ডাউনলোড করুন",
                    color = PureWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }

    if (showCustomDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = customStartMillis)
        DatePickerDialog(
            onDismissRequest = { showCustomDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dateState.selectedDateMillis?.let { customStartMillis = it }
                        showCustomDatePicker = false
                    }
                ) { Text("ঠিক আছে", color = EmeraldLedger, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDatePicker = false }) { Text("বাতিল") }
            },
        ) {
            DatePicker(state = dateState)
        }
    }
}

@Composable
private fun ReportSummaryLineRow(
    label: String,
    value: String,
    valueColor: Color,
    boldLabel: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = InkPrimary,
            fontSize = 13.5.sp,
            fontWeight = if (boldLabel) FontWeight.Bold else FontWeight.Medium,
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun TableHeaderText(
    label: String,
    modifier: Modifier,
    align: TextAlign = TextAlign.Start,
) {
    Text(
        text = label,
        color = InkPrimary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        textAlign = align,
        modifier = modifier,
    )
}
