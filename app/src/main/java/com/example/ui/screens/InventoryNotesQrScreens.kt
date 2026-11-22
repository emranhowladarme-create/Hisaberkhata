package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.Business
import com.example.data.model.BusinessNote
import com.example.data.model.Product
import com.example.ui.components.BanglaQrBrandStrip
import com.example.ui.components.EmptyStateBox
import com.example.ui.components.HisaberTopBar
import com.example.ui.theme.BanglaQrGreen
import com.example.ui.theme.BanglaQrRed
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DenaRed
import com.example.ui.theme.DenaRedBg
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
import com.example.util.Formatters
import com.example.util.QrHelper
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors

// --- Inventory Management ---

@Composable
fun InventoryScreen(
    data: HisaberAppData,
    onBack: () -> Unit,
    onAddProduct: (name: String, unit: String, buyPrice: Double, sellPrice: Double, qty: Long, lowThreshold: Long) -> Unit,
    onAdjustStock: (product: Product, delta: Long, reason: String) -> Unit,
    onDeleteProduct: (String) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) } // 0 = স্টক তালিকা, 1 = ইতিহাস
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCanvas),
    ) {
        HisaberTopBar(
            title = "স্টক হিসাব",
            showBack = true,
            onBackClick = onBack,
            actions = {
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("add_product_button"),
                ) {
                    Icon(Icons.Default.Add, contentDescription = "পণ্য যোগ করুন", tint = HisabGreen)
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
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("পণ্য (${data.products.size})") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("স্টক ইতিহাস (${data.inventoryLogs.size})") })
        }

        if (selectedTab == 0) {
            if (data.products.isEmpty()) {
                EmptyStateBox(
                    icon = Icons.Default.Inventory2,
                    title = "কোনো পণ্য যোগ করা হয়নি",
                    subtitle = "আপনার দোকানের পণ্যের ক্রয়মূল্য, বিক্রয়মূল্য ও স্টক সংখ্যা যোগ করুন।",
                    actionLabel = "+ নতুন পণ্য যোগ করুন",
                    onAction = { showAddDialog = true },
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (data.summary.lowStockCount > 0) {
                        item {
                            Surface(
                                color = WarningAmberBg,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = WarningAmber)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${data.summary.lowStockCount}টি পণ্যের স্টক কম!",
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                }
                            }
                        }
                    }

                    items(data.products, key = { it.id }) { prod ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = HisabGreenTint),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (prod.isLowStock) WarningAmber else HisabGreenBorder),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(prod.name, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            text = "ক্রয়: ${Formatters.formatTaka(prod.purchasePrice)}  |  বিক্রয়: ${Formatters.formatTaka(prod.sellingPrice)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = InkSecondary,
                                        )
                                    }
                                    if (prod.isLowStock) {
                                        Surface(color = DenaRedBg, shape = RoundedCornerShape(6.dp)) {
                                            Text(
                                                text = "লো-স্টক",
                                                color = DenaRed,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            )
                                        }
                                    }
                                    IconButton(onClick = { onDeleteProduct(prod.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "মুছুন", tint = InkMuted, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "বর্তমান স্টক: ${prod.quantity} ${prod.unit}",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = if (prod.isLowStock) DenaRed else PaonaGreen,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Surface(
                                            color = DenaRedBg,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.clickable(enabled = prod.quantity > 0) {
                                                onAdjustStock(prod, -1L, "বিক্রি / স্টক কমানো")
                                            },
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "স্টক কমান", tint = DenaRed, modifier = Modifier.padding(8.dp).size(18.dp))
                                        }
                                        Surface(
                                            color = PaonaGreenBg,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.clickable {
                                                onAdjustStock(prod, 1L, "নতুন স্টক যোগ")
                                            },
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "স্টক বাড়ান", tint = PaonaGreen, modifier = Modifier.padding(8.dp).size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (data.inventoryLogs.isEmpty()) {
                EmptyStateBox(
                    icon = Icons.Default.Inventory2,
                    title = "কোনো স্টক ইতিহাস নেই",
                    subtitle = "পণ্য যোগ বা স্টক পরিবর্তন করলে এখানে রেকর্ড থাকবে।",
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(data.inventoryLogs, key = { it.id }) { log ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
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
                                    Text(log.productName, style = MaterialTheme.typography.titleSmall)
                                    Text(log.reason, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    val deltaStr = if (log.changeQty >= 0) "+${log.changeQty}" else "${log.changeQty}"
                                    Text(
                                        text = deltaStr,
                                        color = if (log.changeQty >= 0) PaonaGreen else DenaRed,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text("মোট: ${log.resultingQty}", style = MaterialTheme.typography.labelSmall, color = InkMuted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var unit by remember { mutableStateOf("পিস") }
        var buy by remember { mutableStateOf("") }
        var sell by remember { mutableStateOf("") }
        var qty by remember { mutableStateOf("") }
        var low by remember { mutableStateOf("5") }
        var err by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("নতুন পণ্য যোগ করুন") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(value = name, onValueChange = { name = it; err = null }, label = { Text("পণ্যের নাম *") }, singleLine = true)
                    OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("একক (পিস / কেজি / লিটার)") }, singleLine = true)
                    OutlinedTextField(value = buy, onValueChange = { buy = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("ক্রয়মূল্য (৳)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                    OutlinedTextField(value = sell, onValueChange = { sell = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("বিক্রয়মূল্য (৳)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                    OutlinedTextField(value = qty, onValueChange = { qty = it.filter { c -> c.isDigit() } }, label = { Text("পরিমাণ *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                    OutlinedTextField(value = low, onValueChange = { low = it.filter { c -> c.isDigit() } }, label = { Text("লো-স্টক সতর্কতা সীমা") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                    if (err != null) Text(err!!, color = DenaRed, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val q = qty.toLongOrNull()
                        if (name.isBlank() || q == null) {
                            err = "পণ্যের নাম ও পরিমাণ দিন"
                        } else {
                            onAddProduct(
                                name,
                                unit.ifBlank { "পিস" },
                                buy.toDoubleOrNull() ?: 0.0,
                                sell.toDoubleOrNull() ?: 0.0,
                                q,
                                low.toLongOrNull() ?: 5L,
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HisabGreen),
                ) { Text("সংরক্ষণ") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("বাতিল") } },
        )
    }
}

// --- Business Notes ---

@Composable
fun BusinessNotesScreen(
    data: HisaberAppData,
    onBack: () -> Unit,
    onSaveNote: (title: String, content: String, pinned: Boolean, existing: BusinessNote?) -> Unit,
    onDeleteNote: (String) -> Unit,
) {
    var editingNote by remember { mutableStateOf<BusinessNote?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(SoftCanvas)) {
        HisaberTopBar(
            title = "ব্যবসার নোট",
            showBack = true,
            onBackClick = onBack,
            actions = {
                IconButton(onClick = { editingNote = null; showEditor = true }) {
                    Icon(Icons.Default.Add, contentDescription = "নতুন নোট", tint = HisabGreen)
                }
            },
        )

        if (data.notes.isEmpty()) {
            EmptyStateBox(
                icon = Icons.Default.EditNote,
                title = "কোনো ব্যবসার নোট নেই",
                subtitle = "জরুরি হিসাব, পণ্যের অর্ডার বা মনে রাখার তথ্য নোট করে রাখুন।",
                actionLabel = "+ নতুন নোট লিখুন",
                onAction = { editingNote = null; showEditor = true },
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(data.notes, key = { it.id }) { note ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = HisabGreenTint),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (note.pinned) HisabGreen else HisabGreenBorder),
                        modifier = Modifier.fillMaxWidth().clickable {
                            editingNote = note
                            showEditor = true
                        },
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (note.pinned) {
                                        Icon(Icons.Default.PushPin, contentDescription = "পিন করা", tint = HisabGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(note.title, style = MaterialTheme.typography.titleMedium)
                                }
                                if (note.content.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(note.content, style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                                }
                            }
                            IconButton(onClick = { onDeleteNote(note.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "মুছুন", tint = InkMuted, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        var title by remember(editingNote) { mutableStateOf(editingNote?.title ?: "") }
        var content by remember(editingNote) { mutableStateOf(editingNote?.content ?: "") }
        var pinned by remember(editingNote) { mutableStateOf(editingNote?.pinned ?: false) }

        AlertDialog(
            onDismissRequest = { showEditor = false },
            title = { Text(if (editingNote == null) "নতুন নোট" else "নোট সম্পাদনা") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("শিরোনাম *") }, singleLine = true)
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("বিস্তারিত") }, minLines = 3)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = pinned, onCheckedChange = { pinned = it })
                        Text("উপরে পিন করে রাখুন")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onSaveNote(title, content, pinned, editingNote)
                            showEditor = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HisabGreen),
                ) { Text("সংরক্ষণ") }
            },
            dismissButton = { TextButton(onClick = { showEditor = false }) { Text("বাতিল") } },
        )
    }
}

// --- Multi-Business Manager ---

@Composable
fun MultiBusinessScreen(
    data: HisaberAppData,
    onBack: () -> Unit,
    onSwitchBusiness: (Business) -> Unit,
    onAddBusiness: (name: String, category: String, phone: String, address: String) -> Unit,
) {
    var showAdd by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(SoftCanvas)) {
        HisaberTopBar(
            title = "মাল্টি ব্যবসা",
            showBack = true,
            onBackClick = onBack,
            actions = {
                IconButton(onClick = { showAdd = true }) {
                    Icon(Icons.Default.Add, contentDescription = "নতুন ব্যবসা", tint = HisabGreen)
                }
            },
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(data.businesses, key = { it.id }) { biz ->
                val isActive = biz.id == data.profile.activeBusinessId
                Card(
                    onClick = { onSwitchBusiness(biz) },
                    colors = CardDefaults.cardColors(containerColor = if (isActive) HisabGreenTint else PureWhite),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(if (isActive) 2.dp else 1.dp, if (isActive) HisabGreen else CardBorder),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = HisabGreen, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(biz.name, style = MaterialTheme.typography.titleMedium)
                            Text(biz.category.ifBlank { "সাধারণ ব্যবসা" }, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                        }
                        if (isActive) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "সক্রিয়", tint = PaonaGreen)
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        var name by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("মুদি / রিটেইল") }
        var phone by remember { mutableStateOf("") }
        var address by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("নতুন ব্যবসা যোগ করুন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("ব্যবসার নাম *") }, singleLine = true)
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("ব্যবসার ধরন") }, singleLine = true)
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("ফোন") }, singleLine = true)
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("ঠিকানা") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onAddBusiness(name, category, phone, address)
                            showAdd = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HisabGreen),
                ) { Text("তৈরি করুন") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("বাতিল") } },
        )
    }
}

// --- QR Code System (Generate Business/Customer QR, CameraX Live Scan, QR History) ---

@Composable
fun QrCodeScreen(
    data: HisaberAppData,
    initialTab: Int,
    onBack: () -> Unit,
    onRecordQrEvent: (customerName: String, amount: Double, payload: String, direction: String) -> Unit,
    onNavigateToAddTxFromQr: (amount: Double, note: String) -> Unit,
) {
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab.coerceIn(0, 2)) }
    var customAmountText by rememberSaveable { mutableStateOf("") }
    var selectedCustomerName by rememberSaveable { mutableStateOf("") }
    var lastScannedRaw by rememberSaveable { mutableStateOf<String?>(null) }

    val amountDouble = customAmountText.toDoubleOrNull() ?: 0.0
    val qrPayload = remember(data.profile.businessName, data.profile.phone, amountDouble, selectedCustomerName) {
        QrHelper.buildPaymentPayload(
            businessName = data.profile.businessName.ifBlank { "হিসাবের খাতা" },
            phone = data.profile.phone,
            amount = amountDouble,
            customerName = selectedCustomerName,
        )
    }
    val qrBitmap = remember(qrPayload) { QrHelper.generateQrBitmap(qrPayload, 520) }

    val galleryQrLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bmp = BitmapFactory.decodeStream(stream)
                    val decoded = bmp?.let { QrHelper.decodeFromBitmap(it) }
                    if (decoded != null) {
                        lastScannedRaw = decoded
                        val parsed = QrHelper.parsePayload(decoded)
                        onRecordQrEvent(
                            parsed.businessName.ifBlank { "QR স্ক্যান" },
                            parsed.amount,
                            decoded,
                            "scanned",
                        )
                    } else {
                        lastScannedRaw = "ছবিতে কোনো বৈধ QR কোড পাওয়া যায়নি।"
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(SoftCanvas)) {
        HisaberTopBar(
            title = "QR Code",
            showBack = true,
            centerTitle = true,
            onBackClick = onBack,
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
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("আমার QR") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("QR স্ক্যান") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("ইতিহাস") })
        }

        when (selectedTab) {
            0 -> {
                val storeName = data.profile.businessName.ifBlank { "ইমরান টি স্টল" }
                val merchantPhone = data.profile.phone.ifBlank { "ফোন নম্বর যুক্ত নেই" }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, HisabGreenBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bangla_qr_card"),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            // Instruction above central Bangla QR code
                            Text(
                                text = "বিকাশ, রকেট বা যেকোনো ব্যাংক অ্যাপ থেকে স্ক্যান করে কাস্টমারকে পেমেন্ট করতে বলুন",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = InkPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 20.sp,
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("bangla_qr_instruction"),
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Prominent Central Bangla QR Box
                            Surface(
                                color = PureWhite,
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(1.5.dp, HisabGreenBorder),
                                shadowElevation = 1.dp,
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    // Bangla QR official header badge
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(BanglaQrGreen),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(BanglaQrRed),
                                            )
                                        }
                                        Text(
                                            text = "BANGLA ",
                                            color = BanglaQrRed,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.6.sp,
                                        )
                                        Text(
                                            text = "QR",
                                            color = BanglaQrGreen,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.6.sp,
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    if (qrBitmap != null) {
                                        Image(
                                            bitmap = qrBitmap.asImageBitmap(),
                                            contentDescription = "বাংলা QR কোড",
                                            modifier = Modifier
                                                .size(226.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .testTag("bangla_qr_image"),
                                        )
                                    }

                                    if (amountDouble > 0) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            color = PaonaGreenBg,
                                            shape = RoundedCornerShape(8.dp),
                                        ) {
                                            Text(
                                                text = "নির্ধারিত পরিমাণ: ${Formatters.formatTaka(amountDouble)}",
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    color = PaonaGreen,
                                                    fontWeight = FontWeight.Bold,
                                                ),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Real logos and branded text for Nagad, bKash, Rocket, and Cellfin below QR code
                            BanglaQrBrandStrip(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("bangla_qr_brand_strip"),
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = CardBorder)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Merchant Phone Number and Store Name directly below QR code
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "মার্চেন্ট ফোন নম্বর",
                                    tint = HisabGreen,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = merchantPhone,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = InkPrimary,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    modifier = Modifier.testTag("bangla_qr_merchant_phone"),
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = "দোকানের নাম",
                                    tint = HisabGreen,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = storeName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = HisabGreen,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.testTag("bangla_qr_store_name"),
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = selectedCustomerName,
                        onValueChange = { selectedCustomerName = it },
                        label = { Text("কাস্টমারের নাম (ঐচ্ছিক পেমেন্ট QR)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = customAmountText,
                        onValueChange = { customAmountText = it.filter { c -> c.isDigit() } },
                        label = { Text("টাকার পরিমাণ (৳ ঐচ্ছিক)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = {
                                if (qrBitmap != null) {
                                    onRecordQrEvent(
                                        selectedCustomerName.ifBlank { data.profile.businessName },
                                        amountDouble,
                                        qrPayload,
                                        "generated",
                                    )
                                    val file = File(context.cacheDir, "hisab_qr_${System.currentTimeMillis()}.png")
                                    FileOutputStream(file).use { out ->
                                        qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                                    }
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/png"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        putExtra(Intent.EXTRA_TEXT, "${data.profile.businessName} পেমেন্ট QR")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "QR শেয়ার করুন"))
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HisabGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(48.dp),
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("QR শেয়ার করুন")
                        }
                    }
                }
            }
            1 -> {
                var hasCameraPerm by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_GRANTED
                    )
                }
                val permLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { hasCameraPerm = it }

                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (hasCameraPerm) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(290.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(2.dp, HisabGreen, RoundedCornerShape(16.dp)),
                        ) {
                            CameraQrPreview { decoded ->
                                if (lastScannedRaw != decoded) {
                                    lastScannedRaw = decoded
                                    val parsed = QrHelper.parsePayload(decoded)
                                    onRecordQrEvent(
                                        parsed.businessName.ifBlank { "QR স্ক্যান" },
                                        parsed.amount,
                                        decoded,
                                        "scanned",
                                    )
                                }
                            }
                        }
                    } else {
                        EmptyStateBox(
                            icon = Icons.Default.QrCodeScanner,
                            title = "ক্যামেরা অনুমতি প্রয়োজন",
                            subtitle = "লাইভ QR কোড স্ক্যান করতে ক্যামেরার অনুমতি দিন অথবা গ্যালারি থেকে ছবি নির্বাচন করুন।",
                            actionLabel = "ক্যামেরা চালু করুন",
                            onAction = { permLauncher.launch(Manifest.permission.CAMERA) },
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            galleryQrLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        border = BorderStroke(1.dp, HisabGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = HisabGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("গ্যালারি থেকে QR ছবি স্ক্যান করুন", color = HisabGreen)
                    }

                    if (lastScannedRaw != null) {
                        val parsed = QrHelper.parsePayload(lastScannedRaw!!)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            border = BorderStroke(1.dp, PaonaGreen),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("QR শনাক্ত হয়েছে", style = MaterialTheme.typography.titleMedium.copy(color = PaonaGreen))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (parsed.businessName.isNotBlank()) {
                                        "প্রতিষ্ঠান: ${parsed.businessName}\nপরিমাণ: ${Formatters.formatTaka(parsed.amount)}"
                                    } else {
                                        lastScannedRaw!!
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                if (parsed.amount > 0) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            onNavigateToAddTxFromQr(parsed.amount, "QR: ${parsed.businessName}")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = HisabGreen),
                                    ) {
                                        Text("ক্যাশবুকে যোগ করুন")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                if (data.qrHistory.isEmpty()) {
                    EmptyStateBox(
                        icon = Icons.Default.QrCode2,
                        title = "কোনো QR ইতিহাস নেই",
                        subtitle = "QR তৈরি বা স্ক্যান করলে এখানে সংরক্ষিত থাকবে।",
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(data.qrHistory, key = { it.id }) { item ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CardBorder),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.customerName, style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            text = if (item.direction == "generated") "তৈরি করা QR" else "স্ক্যান করা QR",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = InkSecondary,
                                        )
                                    }
                                    Text(
                                        text = if (item.amount > 0) Formatters.formatTaka(item.amount) else "সাধারণ QR",
                                        style = MaterialTheme.typography.titleSmall.copy(color = HisabGreen),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraQrPreview(
    onDecoded: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose { executor.shutdown() }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                runCatching {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    val reader = MultiFormatReader()
                    analysis.setAnalyzer(executor) { imageProxy ->
                        try {
                            val buffer = imageProxy.planes[0].buffer
                            val bytes = ByteArray(buffer.remaining())
                            buffer.get(bytes)
                            val source = PlanarYUVLuminanceSource(
                                bytes,
                                imageProxy.width,
                                imageProxy.height,
                                0,
                                0,
                                imageProxy.width,
                                imageProxy.height,
                                false,
                            )
                            val bitmap = BinaryBitmap(HybridBinarizer(source))
                            val result = reader.decode(bitmap)
                            if (result.text.isNotBlank()) {
                                ContextCompat.getMainExecutor(ctx).execute {
                                    onDecoded(result.text)
                                }
                            }
                        } catch (_: Exception) {
                            // No QR in frame
                        } finally {
                            imageProxy.close()
                        }
                    }
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis,
                    )
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = Modifier.fillMaxSize(),
    )
}
