package com.example.ui.screens

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.LedgerTransaction
import com.example.ui.components.CustomerAvatarCircle
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
import com.example.ui.viewmodel.HisaberAppData
import com.example.util.BackendGatewayClient
import com.example.util.Formatters
import com.example.util.PaymentGatewayConfig
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsSenderScreen(
    data: HisaberAppData,
    initialCustomerId: String,
    initialGroupMode: Boolean,
    onBack: () -> Unit,
    onOpenPackages: () -> Unit,
    onLogSms: (
        customerId: String,
        customerName: String,
        phone: String,
        message: String,
        messageType: String,
        status: String,
        scheduledAtMillis: Long,
        decrementBalance: Boolean,
    ) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isBackendSms = PaymentGatewayConfig.isBackendConfigured(context)
    var selectedTab by rememberSaveable { mutableIntStateOf(0) } // 0 = পাঠান, 1 = ইতিহাস
    var groupMode by rememberSaveable { mutableStateOf(initialGroupMode) }
    var selectedCustomerId by rememberSaveable {
        mutableStateOf(
            initialCustomerId.ifBlank {
                data.customers.firstOrNull { it.balance > 0 }?.id ?: data.customers.firstOrNull()?.id ?: ""
            }
        )
    }
    val selectedCustomer: Customer? = remember(data.customers, selectedCustomerId) {
        data.customers.find { it.id == selectedCustomerId } ?: data.customers.firstOrNull()
    }

    var messageType by rememberSaveable { mutableStateOf(if (initialGroupMode) "group" else "tagada") }
    var messageBody by rememberSaveable { mutableStateOf("") }
    var showCustomerPicker by remember { mutableStateOf(false) }
    var showTypePicker by remember { mutableStateOf(false) }
    var showSchedulePicker by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var demoDispatchSummary by remember { mutableStateOf<Pair<String, String>?>(null) } // phones to statusMsg

    val dueCustomers = remember(data.customers) {
        data.customers.filter { it.balance > 0 && it.phone.isNotBlank() }
    }

    LaunchedEffect(selectedCustomer?.id, messageType, groupMode) {
        if (groupMode) {
            messageBody = "প্রিয় গ্রাহক, ${data.profile.businessName.ifBlank { "আমাদের দোকান" }} থেকে বকেয়া পরিশোধের অনুরোধ জানানো হচ্ছে। ধন্যবাদ - হিসাবের খাতা।"
        } else if (selectedCustomer != null) {
            val dueText = Formatters.formatTaka(kotlin.math.abs(selectedCustomer.balance))
            messageBody = when (messageType) {
                "tagada" -> "প্রিয় ${selectedCustomer.name}, আপনার কাছে $dueText বকেয়া রয়েছে। অনুগ্রহ করে দ্রুত পরিশোধ করুন। ধন্যবাদ - হিসাবের খাতা।"
                "order" -> "প্রিয় ${selectedCustomer.name}, আপনার অর্ডারটি প্রস্তুত হয়েছে। অনুগ্রহ করে সংগ্রহ করুন। ধন্যবাদ - ${data.profile.businessName}।"
                else -> messageBody
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite)
            .imePadding(),
    ) {
        HisaberTopBar(
            title = if (groupMode) "গ্রুপ তাগাদা SMS" else "SMS তাগাদা পাঠান",
            showBack = true,
            onBackClick = onBack,
            actions = {
                TextButton(onClick = onOpenPackages) {
                    Text("প্যাকেজ", color = HisabGreen, fontWeight = FontWeight.Bold)
                }
            },
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = PureWhite,
            contentColor = HisabGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = HisabGreen,
                )
            },
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("তাগাদা পাঠান") },
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("SMS ইতিহাস (${data.smsLogs.size})") },
            )
        }

        if (selectedTab == 0) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !groupMode,
                        onClick = { groupMode = false; messageType = "tagada" },
                        label = { Text("একক কাস্টমার") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HisabGreenSoft,
                            selectedLabelColor = HisabGreen,
                        ),
                    )
                    FilterChip(
                        selected = groupMode,
                        onClick = { groupMode = true; messageType = "group" },
                        label = { Text("গ্রুপ তাগাদা (${dueCustomers.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HisabGreenSoft,
                            selectedLabelColor = HisabGreen,
                        ),
                    )
                }

                Text("প্রাপক নির্বাচন করুন", style = MaterialTheme.typography.bodyMedium, color = InkSecondary)

                if (!groupMode) {
                    Surface(
                        color = SoftCanvas,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCustomerPicker = true },
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CustomerAvatarCircle(name = selectedCustomer?.name ?: "প্রাপক", size = 44.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedCustomer?.name ?: "কাস্টমার নির্বাচন করুন",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    text = selectedCustomer?.phone?.ifBlank { "ফোন নম্বর নেই" } ?: "কোনো কাস্টমার নেই",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkSecondary,
                                )
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                        }
                    }
                } else {
                    Surface(
                        color = HisabGreenTint,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, HisabGreenBorder),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "মোট ${dueCustomers.size} জন বকেয়া কাস্টমারের কাছে গ্রুপ তাগাদা পাঠানো হবে।",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }

                // Remaining SMS Balance + Demo/Backend Badge matching reference
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        color = PaonaGreenBg,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.clickable(onClick = onOpenPackages),
                    ) {
                        Text(
                            text = "ব্যালেন্স SMS: ${data.profile.smsBalance} টি  •  প্যাকেজ কিনুন",
                            color = PaonaGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                    Surface(
                        color = HisabGreenSoft,
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(
                            text = if (isBackendSms) "সার্ভার গেটওয়ে" else "ডেমো মোড",
                            color = HisabGreen,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }

                // Message Editor with char counter matching reference
                Text("বার্তা লিখুন", style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                OutlinedTextField(
                    value = messageBody,
                    onValueChange = {
                        if (it.length <= 320) messageBody = it
                        validationError = null
                    },
                    minLines = 4,
                    maxLines = 6,
                    supportingText = {
                        Text(
                            text = "${messageBody.length}/160",
                            modifier = Modifier.fillMaxWidth(),
                            color = InkSecondary,
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = SoftCanvas,
                        focusedContainerColor = PureWhite,
                        focusedBorderColor = HisabGreen,
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("sms_message_input"),
                )

                // Message Type Dropdown Row matching reference
                Text("বার্তা টাইপ", style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                Surface(
                    color = SoftCanvas,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTypePicker = true },
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val typeLabel = when (messageType) {
                            "tagada" -> "তাগাদা বার্তা"
                            "order" -> "অর্ডার বার্তা"
                            "group" -> "গ্রুপ তাগাদা"
                            else -> "কাস্টম বার্তা"
                        }
                        Text(typeLabel, style = MaterialTheme.typography.bodyLarge)
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                }

                if (validationError != null) {
                    Text(validationError!!, color = DenaRed, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Send Now Button matching reference (works in Demo Mode, Optional Backend, or Phone SMS App)
                Button(
                    onClick = {
                        if (messageBody.isBlank()) {
                            validationError = "অনুগ্রহ করে বার্তা লিখুন"
                            return@Button
                        }
                        if (groupMode) {
                            if (dueCustomers.isEmpty()) {
                                validationError = "মোবাইল নম্বরসহ কোনো বকেয়া কাস্টমার পাওয়া যায়নি"
                                return@Button
                            }
                            val phonesList = dueCustomers.map { it.phone }
                            val phonesStr = phonesList.joinToString(";")
                            scope.launch {
                                val res = BackendGatewayClient.sendSmsViaBackendOrDemo(
                                    context = context,
                                    recipients = phonesList,
                                    message = messageBody,
                                    messageType = "group",
                                    userId = data.profile.userId,
                                )
                                res.onSuccess { (_, statusMsg) ->
                                    dueCustomers.forEach { c ->
                                        val personalMsg = "প্রিয় ${c.name}, আপনার কাছে ${Formatters.formatTaka(c.balance)} বকেয়া রয়েছে। অনুগ্রহ করে দ্রুত পরিশোধ করুন। ধন্যবাদ - হিসাবের খাতা।"
                                        onLogSms(c.id, c.name, c.phone, personalMsg, "group", "sent", 0L, true)
                                    }
                                    demoDispatchSummary = phonesStr to statusMsg
                                }.onFailure { err ->
                                    validationError = err.localizedMessage ?: "SMS সার্ভার ত্রুটি"
                                }
                            }
                        } else {
                            if (selectedCustomer == null) {
                                validationError = "অনুগ্রহ করে কাস্টমার নির্বাচন করুন"
                                return@Button
                            }
                            val targetPhone = selectedCustomer.phone.ifBlank { "ডেমো-নম্বর" }
                            scope.launch {
                                val res = BackendGatewayClient.sendSmsViaBackendOrDemo(
                                    context = context,
                                    recipients = listOf(targetPhone),
                                    message = messageBody,
                                    messageType = messageType,
                                    userId = data.profile.userId,
                                )
                                res.onSuccess { (_, statusMsg) ->
                                    onLogSms(
                                        selectedCustomer.id,
                                        selectedCustomer.name,
                                        targetPhone,
                                        messageBody,
                                        messageType,
                                        "sent",
                                        0L,
                                        true,
                                    )
                                    demoDispatchSummary = selectedCustomer.phone to statusMsg
                                }.onFailure { err ->
                                    validationError = err.localizedMessage ?: "SMS সার্ভার ত্রুটি"
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("sms_send_now_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = HisabGreen),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SMS পাঠান এখনই", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                // Schedule Button matching reference
                OutlinedButton(
                    onClick = {
                        if (messageBody.isBlank() || (!groupMode && selectedCustomer == null)) {
                            validationError = "প্রাপক এবং বার্তা সঠিকভাবে নির্বাচন করুন"
                        } else {
                            showSchedulePicker = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    border = BorderStroke(1.dp, HisabGreen),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = HisabGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("সময় নির্ধারণ করুন", color = HisabGreen, fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            // SMS History Tab
            if (data.smsLogs.isEmpty()) {
                EmptyStateBox(
                    icon = Icons.Default.History,
                    title = "কোনো SMS ইতিহাস নেই",
                    subtitle = "কাস্টমারকে পাঠানো বা নির্ধারিত তাগাদা মেসেজ এখানে দেখা যাবে।",
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(data.smsLogs, key = { it.id }) { log ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(log.customerName, style = MaterialTheme.typography.titleSmall)
                                    Surface(
                                        color = if (log.status == "sent") PaonaGreenBg else HisabGreenSoft,
                                        shape = RoundedCornerShape(6.dp),
                                    ) {
                                        Text(
                                            text = if (log.status == "sent") "পাঠানো হয়েছে" else "নির্ধারিত",
                                            color = if (log.status == "sent") PaonaGreen else HisabGreen,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        )
                                    }
                                }
                                Text(log.phone, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(log.message, style = MaterialTheme.typography.bodyMedium, color = InkPrimary)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCustomerPicker) {
        ModalBottomSheet(onDismissRequest = { showCustomerPicker = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("কাস্টমার নির্বাচন করুন", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(modifier = Modifier.height(260.dp)) {
                    items(data.customers, key = { it.id }) { c ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCustomerId = c.id
                                    showCustomerPicker = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CustomerAvatarCircle(name = c.name, size = 38.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(c.name, style = MaterialTheme.typography.titleSmall)
                                Text(c.phone, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                            }
                        }
                        HorizontalDivider(color = CardBorder)
                    }
                }
            }
        }
    }

    if (showTypePicker) {
        ModalBottomSheet(onDismissRequest = { showTypePicker = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("বার্তার ধরন", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                listOf(
                    "tagada" to "তাগাদা বার্তা",
                    "order" to "অর্ডার বার্তা",
                    "custom" to "কাস্টম বার্তা",
                ).forEach { (key, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                messageType = key
                                showTypePicker = false
                            }
                            .padding(vertical = 14.dp),
                    ) {
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                    }
                    HorizontalDivider(color = CardBorder)
                }
            }
        }
    }

    if (showSchedulePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis() + 86_400_000L)
        DatePickerDialog(
            onDismissRequest = { showSchedulePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val schedMillis = dateState.selectedDateMillis ?: System.currentTimeMillis()
                        val cust = selectedCustomer
                        if (cust != null) {
                            onLogSms(
                                cust.id,
                                cust.name,
                                cust.phone.ifBlank { "N/A" },
                                messageBody,
                                messageType,
                                "scheduled",
                                schedMillis,
                                false,
                            )
                        }
                        showSchedulePicker = false
                    }
                ) { Text("নির্ধারণ করুন", color = HisabGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showSchedulePicker = false }) { Text("বাতিল") }
            },
        ) {
            DatePicker(state = dateState)
        }
    }

    if (demoDispatchSummary != null) {
        val (phones, statusMsg) = demoDispatchSummary!!
        AlertDialog(
            onDismissRequest = { demoDispatchSummary = null },
            icon = {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PaonaGreen, modifier = Modifier.size(40.dp))
            },
            title = { Text(if (isBackendSms) "SMS পাঠানো হয়েছে" else "ডেমো SMS সম্পন্ন (টেস্টিং)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(statusMsg, style = MaterialTheme.typography.bodyMedium)
                    Surface(
                        color = SoftCanvas,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CardBorder),
                    ) {
                        Text(
                            text = messageBody,
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary,
                            modifier = Modifier.padding(10.dp),
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { demoDispatchSummary = null },
                    colors = ButtonDefaults.buttonColors(containerColor = HisabGreen),
                ) { Text("ঠিক আছে") }
            },
            dismissButton = {
                if (phones.isNotBlank()) {
                    TextButton(
                        onClick = {
                            demoDispatchSummary = null
                            val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phones")).apply {
                                putExtra("sms_body", messageBody)
                            }
                            runCatching { context.startActivity(smsIntent) }
                        }
                    ) {
                        Text("ফোনের SMS অ্যাপ খুলুন", color = HisabGreen)
                    }
                }
            },
        )
    }
}

private data class SmsPackageSpec(
    val title: String,
    val count: Int,
    val validity: String,
    val priceTaka: Double,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsPackagesScreen(
    currentSmsBalance: Long,
    packageHistory: List<LedgerTransaction> = emptyList(),
    onBack: () -> Unit,
    onBuyPackage: (title: String, price: Double, smsCount: Int) -> Unit,
) {
    var showHistorySheet by remember { mutableStateOf(false) }

    val packages = remember {
        listOf(
            SmsPackageSpec("স্টার্টার প্যাকেজ", 100, "মেয়াদ ৩০ দিন", 99.0),
            SmsPackageSpec("বেসিক প্যাকেজ", 250, "মেয়াদ ৩০ দিন", 199.0),
            SmsPackageSpec("বিজনেস প্যাকেজ", 500, "মেয়াদ ৩০ দিন", 349.0),
            SmsPackageSpec("প্রিমিয়াম প্যাকেজ", 1000, "মেয়াদ ৩০ দিন", 599.0),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCanvas),
    ) {
        HisaberTopBar(
            title = "SMS প্যাকেজ",
            showBack = true,
            onBackClick = onBack,
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(packages) { pkg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = HisabGreenTint),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, HisabGreenBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pkg.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = InkPrimary,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${pkg.count} SMS",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = HisabGreen,
                                    fontWeight = FontWeight.Bold,
                                ),
                            )
                            Text(
                                text = pkg.validity,
                                style = MaterialTheme.typography.bodySmall,
                                color = InkSecondary,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = Formatters.formatTaka(pkg.priceTaka),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = HisabGreen,
                                    fontWeight = FontWeight.Bold,
                                ),
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = { onBuyPackage("${pkg.title} (${pkg.count} SMS)", pkg.priceTaka, pkg.count) },
                                colors = ButtonDefaults.buttonColors(containerColor = HisabGreen),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("buy_sms_${pkg.count}"),
                            ) {
                                Text("কিনুন", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        Surface(
            color = PureWhite,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    color = HisabGreenSoft,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "বর্তমান ব্যালেন্স: $currentSmsBalance SMS",
                        color = HisabGreen,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(12.dp),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { showHistorySheet = true }) {
                    Text("প্যাকেজ ইতিহাস দেখুন", color = HisabGreen, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showHistorySheet) {
        val smsHistory = remember(packageHistory) {
            packageHistory.filter { it.description.contains("SMS") || it.description.contains("প্যাকেজ") }
        }
        ModalBottomSheet(onDismissRequest = { showHistorySheet = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("SMS প্যাকেজ ক্রয়ের ইতিহাস", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                if (smsHistory.isEmpty()) {
                    EmptyStateBox(
                        icon = Icons.Default.Sms,
                        title = "এখনও কোনো প্যাকেজ কেনা হয়নি",
                        subtitle = "উপরের যেকোনো প্যাকেজ ডেমো মোডে কিনে সাথে সাথে ব্যালেন্স পরীক্ষা করুন।",
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(smsHistory, key = { it.id }) { tx ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SoftCanvas),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CardBorder),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(tx.description, style = MaterialTheme.typography.bodyMedium, color = InkPrimary)
                                        Text(
                                            text = Formatters.formatBanglaDate(tx.dateMillis),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = InkMuted,
                                        )
                                    }
                                    Text(
                                        text = Formatters.formatTaka(tx.amount),
                                        style = MaterialTheme.typography.titleSmall.copy(color = HisabGreen, fontWeight = FontWeight.Bold),
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
