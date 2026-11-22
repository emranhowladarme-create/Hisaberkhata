package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.HeadsetMic
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.MoveToInbox
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.model.Customer
import com.example.ui.components.EmptyStateBox
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DenaRed
import com.example.ui.theme.HisabGreen
import com.example.ui.theme.HisabGreenDark
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PaonaGreen
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TallyCrownGold
import com.example.ui.theme.TallyFeatureGreen
import com.example.ui.theme.TallyFeatureOrange
import com.example.ui.theme.TallyFeatureRed
import com.example.ui.viewmodel.HisaberAppData
import com.example.util.CustomerPhotoStore
import com.example.util.Formatters
import com.example.util.PdfReportGenerator
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.math.abs

private val HomeSoftSkyCardBg = Color(0xFFEDF6FC)
private val HomeSoftSkyBorder = Color(0xFFD5E6F3)
private val HomeLabelBlue = Color(0xFF1E3A8A)
private val HomeAvatarSkyBg = Color(0xFFDBEAFE)
private val HomeAvatarSkyText = Color(0xFF1D4ED8)
private val HomeRecentPillLime = Color(0xFFCEF36E)

internal fun isSupplierParty(customer: Customer): Boolean {
    val notes = customer.notes.trim()
    return notes.contains("supplier", ignoreCase = true) ||
        notes.contains("সাপ্লায়ার") ||
        (notes.isBlank() && customer.balance < 0.0)
}

@Composable
fun HomeScreen(
    data: HisaberAppData,
    isCloudConnected: Boolean = false,
    onConnectGoogleCloud: () -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    onOpenNotifications: () -> Unit,
    onOpenHelpCenter: () -> Unit,
    onOpenMultiBusiness: () -> Unit,
    onOpenGoldMembership: () -> Unit = {},
    onOpenInventory: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenGroupTagada: () -> Unit,
    onOpenBackupSettings: () -> Unit,
    onOpenTallySms: () -> Unit,
    onOpenCashbook: () -> Unit,
    onOpenQr: () -> Unit,
    onAddCustomerClick: () -> Unit,
    onCustomerClick: (String) -> Unit,
    onShowMessage: (String) -> Unit,
) {
    val context = LocalContext.current
    var searchQuery by rememberSaveable { mutableStateOf("") }
    // 0 = সব, 1 = পাবো, 2 = দেবো
    var balanceFilter by rememberSaveable { mutableIntStateOf(0) }
    // 0 = সব, 1 = শুধু কাস্টমার, 2 = শুধু সাপ্লায়ার
    var partyFilter by rememberSaveable { mutableIntStateOf(0) }
    var showClearBackupDialog by remember { mutableStateOf(false) }

    // Periodic clock tick so relative seconds / minutes / hours stay live
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5_000L)
            nowMillis = System.currentTimeMillis()
        }
    }

    val businessName = data.profile.businessName
        .takeIf { it.isNotBlank() && it != "আমার ব্যবসা" }
        ?: "ইমরান টি স্টল"

    val customerCount = remember(data.customers) {
        data.customers.count { !isSupplierParty(it) }
    }
    val supplierCount = remember(data.customers) {
        data.customers.count { isSupplierParty(it) }
    }

    // Latest activity timestamp per customer (considering both Customer & latest Transaction)
    val latestActivityMap = remember(data.customers, data.transactions) {
        val txLatestByCustomer = data.transactions
            .groupBy { it.customerId }
            .mapValues { (_, list) ->
                list.maxOfOrNull { tx ->
                    maxOf(tx.dateMillis, (tx.updatedAt ?: tx.createdAt)?.toDate()?.time ?: 0L)
                } ?: 0L
            }
        data.customers.associate { c ->
            val custTime = (c.updatedAt ?: c.createdAt)?.toDate()?.time ?: 0L
            val txTime = txLatestByCustomer[c.id] ?: 0L
            c.id to maxOf(custTime, txTime)
        }
    }

    val filteredCustomers = remember(data.customers, latestActivityMap, searchQuery, balanceFilter, partyFilter) {
        data.customers
            .filter { customer ->
                val matchesSearch = searchQuery.isBlank() ||
                    customer.name.contains(searchQuery, ignoreCase = true) ||
                    customer.phone.contains(searchQuery, ignoreCase = true)
                val matchesBalance = when (balanceFilter) {
                    1 -> customer.balance > 0.0
                    2 -> customer.balance < 0.0
                    else -> true
                }
                val isSup = isSupplierParty(customer)
                val matchesParty = when (partyFilter) {
                    1 -> !isSup
                    2 -> isSup
                    else -> true
                }
                matchesSearch && matchesBalance && matchesParty
            }
            .sortedByDescending { latestActivityMap[it.id] ?: 0L }
    }

    fun exportCloudBackupJson() {
        runCatching {
            val backup = JSONObject().apply {
                put("businessName", businessName)
                put("ownerName", data.profile.ownerName)
                put("exportedAt", System.currentTimeMillis())
                put("customers", JSONArray(data.customers.map { c ->
                    JSONObject()
                        .put("name", c.name)
                        .put("phone", c.phone)
                        .put("balance", c.balance)
                        .put("type", if (isSupplierParty(c)) "Supplier" else "Customer")
                }))
                put("transactionsCount", data.transactions.size)
            }
            val file = File(context.cacheDir, "hisaber_khata_backup.json").apply {
                writeText(backup.toString(2))
            }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "ক্লাউড ব্যাকআপ শেয়ার / সংরক্ষণ করুন"))
        }.onSuccess {
            onShowMessage("ক্লাউড ব্যাকআপ ফাইল প্রস্তুত হয়েছে")
        }.onFailure {
            onOpenBackupSettings()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFECFCE9),
                        Color(0xFFEAF7FA),
                        Color(0xFFECFCEB),
                    )
                )
            ),
    ) {
        // Top & Bottom vibrant green-lime wave layers matching the reference screenshot
        HomeBackdropWaves(modifier = Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Top Curved Header Bar
            TallyHomeHeader(
                businessName = businessName,
                unreadNotifications = data.summary.unreadNotifications,
                onOpenDrawer = onOpenDrawer,
                onCrownClick = onOpenGoldMembership,
                onBusinessClick = onOpenMultiBusiness,
                onInboxClick = onOpenNotifications,
                onSupportClick = onOpenHelpCenter,
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 12.dp,
                    end = 12.dp,
                    top = 4.dp,
                    bottom = 94.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // 2. Top Feature Grid White Card (7 exact icons in 4-column grid)
                item {
                    TallyFeatureGridCard(
                        lowStockCount = data.summary.lowStockCount,
                        onOpenMultiBusiness = onOpenMultiBusiness,
                        onOpenInventory = onOpenInventory,
                        onOpenNotes = onOpenNotes,
                        onOpenGroupTagada = onOpenGroupTagada,
                        onOpenBackupSettings = onOpenBackupSettings,
                        onOpenTallySms = onOpenTallySms,
                        onOpenCashbook = onOpenCashbook,
                    )
                }

                // 3. Balance Summary White Card (মোট পাবো + Green ৳ Bag | মোট দেবো + Hand Cash)
                item {
                    TallyBalanceSummaryCard(
                        totalReceivable = data.summary.totalPaona,
                        totalPayable = data.summary.totalDena,
                        activeFilter = balanceFilter,
                        onReceivableClick = {
                            balanceFilter = if (balanceFilter == 1) 0 else 1
                        },
                        onPayableClick = {
                            balanceFilter = if (balanceFilter == 2) 0 else 2
                        },
                    )
                }

                // 4. Search Box ('খুঁজন') + Filter & Download Circular Buttons
                item {
                    TallySearchAndFilterRow(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        activeFilter = balanceFilter,
                        onCycleFilter = {
                            balanceFilter = (balanceFilter + 1) % 3
                        },
                        onDownloadPdf = {
                            PdfReportGenerator.generateAccountingPdf(
                                context = context,
                                businessName = businessName,
                                periodLabel = "সর্বমোট হিসাব",
                                totalIncome = data.summary.totalIncomeAll,
                                totalExpense = data.summary.totalExpenseAll,
                                totalPaona = data.summary.totalPaona,
                                totalDena = data.summary.totalDena,
                                customers = data.customers,
                                transactions = data.transactions,
                            ).onSuccess { pdfUri ->
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, pdfUri)
                                    putExtra(Intent.EXTRA_SUBJECT, "$businessName - কাস্টমার ও সাপ্লায়ার হিসাব")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(
                                    Intent.createChooser(shareIntent, "PDF রিপোর্ট ডাউনলোড / শেয়ার করুন")
                                )
                            }.onFailure {
                                onShowMessage("PDF তৈরি করতে সমস্যা হয়েছে")
                            }
                        },
                    )
                }

                // 5. Three Small Backup Options directly below Search Box
                item {
                    HomeBackupOptionsBar(
                        onClearBackupClick = { showClearBackupDialog = true },
                        onCloudBackupClick = { exportCloudBackupJson() },
                        onGoogleBackupClick = {
                            if (isCloudConnected) {
                                onShowMessage("গুগল ব্যাকআপ ক্লাউড সিঙ্ক সক্রিয় আছে")
                            } else {
                                onConnectGoogleCloud()
                            }
                        },
                    )
                }

                // 6. Customer (3-head icon) & Supplier (simple person icon) Dynamic Counts Bar
                item {
                    HomeCustomerSupplierCountsBar(
                        customerCount = customerCount,
                        supplierCount = supplierCount,
                        activePartyFilter = partyFilter,
                        onCustomerClick = {
                            partyFilter = if (partyFilter == 1) 0 else 1
                        },
                        onSupplierClick = {
                            partyFilter = if (partyFilter == 2) 0 else 2
                        },
                    )
                }

                // 7. 'সাম্প্রতিক এন্ট্রি' Lime Pill Badge Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            color = HomeRecentPillLime,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("home_recent_entry_pill"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = Color(0xFF065F46),
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "সাম্প্রতিক এন্ট্রি",
                                    color = InkPrimary,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        if (balanceFilter != 0 || partyFilter != 0) {
                            val activeLabel = buildString {
                                if (partyFilter == 1) append("শুধু কাস্টমার")
                                if (partyFilter == 2) append("শুধু সাপ্লায়ার")
                                if (balanceFilter == 1) {
                                    if (isNotEmpty()) append(" • ")
                                    append("শুধু পাবো")
                                } else if (balanceFilter == 2) {
                                    if (isNotEmpty()) append(" • ")
                                    append("শুধু দেবো")
                                }
                            }
                            Surface(
                                color = PureWhite,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, HisabGreenDark.copy(alpha = 0.35f)),
                                modifier = Modifier.clickable {
                                    balanceFilter = 0
                                    partyFilter = 0
                                },
                            ) {
                                Text(
                                    text = "$activeLabel ✕",
                                    color = HisabGreenDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }
                }

                // 8. Individual Rounded White Customer List Cards
                if (data.customers.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(0.8.dp, CardBorder),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            EmptyStateBox(
                                icon = Icons.Default.Groups,
                                title = "কোনো কাস্টমার বা সাপ্লায়ার নেই",
                                subtitle = "নিচের সবুজ + বাটনে ট্যাপ করে নতুন কাস্টমার বা সাপ্লায়ার যোগ করুন।",
                                actionLabel = "নতুন যোগ করুন",
                                onAction = onAddCustomerClick,
                            )
                        }
                    }
                } else if (filteredCustomers.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(0.8.dp, CardBorder),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            EmptyStateBox(
                                icon = Icons.Default.Search,
                                title = "খুঁজে পাওয়া যায়নি",
                                subtitle = "অন্য নাম বা মোবাইল নম্বর দিয়ে চেষ্টা করুন।",
                            )
                        }
                    }
                } else {
                    items(
                        items = filteredCustomers,
                        key = { customer -> customer.id },
                    ) { customer ->
                        val photoPath = remember(customer.id) {
                            CustomerPhotoStore.getPhotoPath(context, customer.id)
                        }
                        val activityMillis = latestActivityMap[customer.id] ?: 0L
                        TallyCustomerCardRow(
                            customer = customer,
                            photoPath = photoPath,
                            activityMillis = activityMillis,
                            nowMillis = nowMillis,
                            onClick = { onCustomerClick(customer.id) },
                        )
                    }
                }
            }
        }

        // 9. Green-and-White Circular Floating Action Button (Person + Plus Icon, exact size & position)
        FloatingActionButton(
            onClick = onAddCustomerClick,
            shape = CircleShape,
            containerColor = HisabGreen,
            contentColor = PureWhite,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
                .size(58.dp)
                .border(2.dp, PureWhite, CircleShape)
                .testTag("home_add_customer_fab"),
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = "নতুন কাস্টমার বা সাপ্লায়ার যোগ করুন",
                tint = PureWhite,
                modifier = Modifier.size(28.dp),
            )
        }
    }

    if (showClearBackupDialog) {
        AlertDialog(
            onDismissRequest = { showClearBackupDialog = false },
            containerColor = PureWhite,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = PaonaGreen,
                    modifier = Modifier.size(28.dp),
                )
            },
            title = { Text("ক্লিয়ার ব্যাকআপ", fontWeight = FontWeight.Bold) },
            text = { Text("আপনি কি ডিভাইসে সংরক্ষিত অস্থায়ী ব্যাকআপ ফাইল ও ক্যাশ পরিষ্কার করতে চান?") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearBackupDialog = false
                        runCatching {
                            File(context.cacheDir, "hisaber_khata_backup.json").delete()
                        }
                        onShowMessage("ব্যাকআপ ক্যাশ পরিষ্কার করা হয়েছে")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HisabGreen),
                ) {
                    Text("পরিষ্কার করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearBackupDialog = false }) {
                    Text("বাতিল", color = InkSecondary)
                }
            },
        )
    }
}

@Composable
private fun HomeBackdropWaves(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Top soft lime-yellow wave backdrop behind header
        val topOuterWave = Path().apply {
            moveTo(0f, 0f)
            lineTo(w, 0f)
            lineTo(w, h * 0.135f)
            cubicTo(
                w * 0.65f, h * 0.10f,
                w * 0.30f, h * 0.16f,
                0f, h * 0.11f,
            )
            close()
        }
        drawPath(
            path = topOuterWave,
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFFD5FA7A), Color(0xFFC2F55B), Color(0xFFBDF456))
            ),
        )

        val topInnerAccent = Path().apply {
            moveTo(0f, 0f)
            lineTo(w * 0.58f, 0f)
            cubicTo(
                w * 0.42f, h * 0.08f,
                w * 0.18f, h * 0.09f,
                0f, h * 0.06f,
            )
            close()
        }
        drawPath(
            path = topInnerAccent,
            color = Color(0xFFECFCC4).copy(alpha = 0.68f),
        )

        // Bottom lush green-lime sweeping curves matching the reference screenshot
        val bottomLimeWave = Path().apply {
            moveTo(0f, h * 0.84f)
            cubicTo(
                w * 0.28f, h * 0.78f,
                w * 0.65f, h * 0.90f,
                w, h * 0.78f,
            )
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = bottomLimeWave,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFB3F34E), Color(0xFF6CD91F), Color(0xFF34BD33)),
                startY = h * 0.76f,
                endY = h,
            ),
        )

        val bottomEmeraldWave = Path().apply {
            moveTo(0f, h * 0.90f)
            cubicTo(
                w * 0.36f, h * 0.85f,
                w * 0.72f, h * 0.94f,
                w, h * 0.84f,
            )
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = bottomEmeraldWave,
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF42C424), Color(0xFF1CAE43), Color(0xFF0A8F4A))
            ),
        )
    }
}

@Composable
private fun TallyHomeHeader(
    businessName: String,
    unreadNotifications: Int,
    onOpenDrawer: () -> Unit,
    onCrownClick: () -> Unit,
    onBusinessClick: () -> Unit,
    onInboxClick: () -> Unit,
    onSupportClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(54.dp)
            .padding(start = 6.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onOpenDrawer,
            modifier = Modifier
                .size(46.dp)
                .testTag("home_drawer_menu_button"),
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "মেনু ড্রয়ার",
                tint = InkPrimary,
                modifier = Modifier.size(26.dp),
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onBusinessClick)
                .padding(horizontal = 4.dp, vertical = 6.dp)
                .testTag("home_business_selector"),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onCrownClick)
                    .padding(2.dp)
                    .testTag("home_gold_crown_button"),
            ) {
                CrownBadgeIcon(modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = businessName,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 18.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "ব্যবসা পরিবর্তন",
                tint = InkPrimary,
                modifier = Modifier.size(24.dp),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            HeaderActionIcon(
                icon = Icons.Outlined.MoveToInbox,
                label = "ইনবক্স",
                badgeCount = unreadNotifications,
                testTag = "home_inbox_button",
                onClick = onInboxClick,
            )
            HeaderActionIcon(
                icon = Icons.Outlined.HeadsetMic,
                label = "সাপোর্ট",
                badgeCount = 0,
                testTag = "home_support_button",
                onClick = onSupportClick,
            )
        }
    }
}

@Composable
private fun HeaderActionIcon(
    icon: ImageVector,
    label: String,
    badgeCount: Int,
    testTag: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 3.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = InkPrimary,
                modifier = Modifier.size(21.dp),
            )
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 5.dp, y = (-3).dp)
                        .background(DenaRed, CircleShape)
                        .padding(horizontal = 4.dp, vertical = 0.5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                        color = PureWhite,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = InkPrimary,
            maxLines = 1,
        )
    }
}

@Composable
private fun CrownBadgeIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val crownPath = Path().apply {
            moveTo(w * 0.12f, h * 0.72f)
            lineTo(w * 0.08f, h * 0.28f)
            lineTo(w * 0.32f, h * 0.50f)
            lineTo(w * 0.50f, h * 0.16f)
            lineTo(w * 0.68f, h * 0.50f)
            lineTo(w * 0.92f, h * 0.28f)
            lineTo(w * 0.88f, h * 0.72f)
            close()
        }
        drawPath(crownPath, color = Color(0xFFF59E0B))
        drawRoundRect(
            color = TallyCrownGold,
            topLeft = Offset(w * 0.12f, h * 0.75f),
            size = Size(w * 0.76f, h * 0.12f),
            cornerRadius = CornerRadius(w * 0.06f, w * 0.06f),
        )
        drawCircle(color = Color(0xFFFEF3C7), radius = w * 0.055f, center = Offset(w * 0.50f, h * 0.52f))
    }
}

private data class HomeFeatureSpec(
    val title: String,
    val icon: ImageVector,
    val iconTint: Color,
    val circleBg: Color,
    val testTag: String,
    val badgeCount: Int = 0,
    val onClick: () -> Unit,
)

@Composable
private fun TallyFeatureGridCard(
    lowStockCount: Int,
    onOpenMultiBusiness: () -> Unit,
    onOpenInventory: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenGroupTagada: () -> Unit,
    onOpenBackupSettings: () -> Unit,
    onOpenTallySms: () -> Unit,
    onOpenCashbook: () -> Unit,
) {
    val mintCircle = Color(0xFFDFF7E8)
    val peachCircle = Color(0xFFFCECD4)
    val roseCircle = Color(0xFFFCE4E4)

    val features = listOf(
        HomeFeatureSpec("মাল্টি ব্যবসা", Icons.Outlined.Storefront, TallyFeatureGreen, mintCircle, "qa_multi_biz", 0, onOpenMultiBusiness),
        HomeFeatureSpec("স্টক হিসাব", Icons.Outlined.Inventory2, TallyFeatureOrange, peachCircle, "qa_inventory", lowStockCount, onOpenInventory),
        HomeFeatureSpec("ব্যবসার নোট", Icons.Outlined.EditNote, TallyFeatureGreen, mintCircle, "qa_notes", 0, onOpenNotes),
        HomeFeatureSpec("গ্রুপ তাগাদা", Icons.Outlined.NotificationsActive, TallyFeatureRed, roseCircle, "qa_group_tagada", 0, onOpenGroupTagada),
        HomeFeatureSpec("ডাটা ব্যাকআপ", Icons.Outlined.CloudUpload, TallyFeatureGreen, mintCircle, "qa_backup", 0, onOpenBackupSettings),
        HomeFeatureSpec("টালি-মেসেজ", Icons.Outlined.Sms, TallyFeatureOrange, peachCircle, "qa_tally_sms", 0, onOpenTallySms),
        HomeFeatureSpec("ক্যাশবাক্স", Icons.Outlined.AccountBalanceWallet, TallyFeatureGreen, mintCircle, "qa_cashbook", 0, onOpenCashbook),
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = PureWhite.copy(alpha = 0.96f)),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(0.8.dp, Color(0xFFE0ECE5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val rows = features.chunked(4)
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    rowItems.forEach { spec ->
                        HomeFeatureTile(
                            spec = spec,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(4 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeFeatureTile(
    spec: HomeFeatureSpec,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = spec.onClick)
            .padding(horizontal = 3.dp, vertical = 4.dp)
            .testTag(spec.testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box {
            Surface(
                color = spec.circleBg,
                shape = CircleShape,
                shadowElevation = 2.dp,
                modifier = Modifier.size(46.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = spec.icon,
                        contentDescription = spec.title,
                        tint = spec.iconTint,
                        modifier = Modifier.size(23.dp),
                    )
                }
            }
            if (spec.badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 3.dp, y = (-2).dp)
                        .background(DenaRed, CircleShape)
                        .padding(horizontal = 4.5.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = Formatters.toBanglaDigits(spec.badgeCount.toString()),
                        color = PureWhite,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = spec.title,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkPrimary,
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TallyBalanceSummaryCard(
    totalReceivable: Double,
    totalPayable: Double,
    activeFilter: Int,
    onReceivableClick: () -> Unit,
    onPayableClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PureWhite.copy(alpha = 0.97f)),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(0.8.dp, Color(0xFFDCEBE3)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (activeFilter == 1) DenaRed.copy(alpha = 0.06f) else Color.Transparent)
                    .clickable(onClick = onReceivableClick)
                    .padding(vertical = 4.dp, horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                GreenTakaMoneyBagIcon(modifier = Modifier.size(44.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "মোট পাবো",
                        color = HomeLabelBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                    )
                    Text(
                        text = Formatters.formatTaka(totalReceivable, useBanglaDigits = true),
                        color = DenaRed,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 21.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("home_total_paona"),
                    )
                }
            }

            VerticalDivider(
                modifier = Modifier.height(44.dp),
                color = CardBorder,
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (activeFilter == 2) PaonaGreen.copy(alpha = 0.06f) else Color.Transparent)
                    .clickable(onClick = onPayableClick)
                    .padding(vertical = 4.dp, horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                HandHoldingMoneyIcon(modifier = Modifier.size(46.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "মোট দেবো",
                        color = HomeLabelBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                    )
                    Text(
                        text = Formatters.formatTaka(totalPayable, useBanglaDigits = true),
                        color = Color(0xFF16A34A),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 21.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("home_total_dena"),
                    )
                }
            }
        }
    }
}

@Composable
private fun GreenTakaMoneyBagIcon(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Top pouch flare
            val flare = Path().apply {
                moveTo(w * 0.34f, h * 0.24f)
                lineTo(w * 0.26f, h * 0.12f)
                cubicTo(w * 0.42f, h * 0.07f, w * 0.58f, h * 0.07f, w * 0.74f, h * 0.12f)
                lineTo(w * 0.66f, h * 0.24f)
                close()
            }
            drawPath(flare, color = Color(0xFF12B81E))

            // Sack body
            val sack = Path().apply {
                moveTo(w * 0.32f, h * 0.27f)
                cubicTo(w * 0.10f, h * 0.44f, w * 0.12f, h * 0.88f, w * 0.50f, h * 0.90f)
                cubicTo(w * 0.88f, h * 0.88f, w * 0.90f, h * 0.44f, w * 0.68f, h * 0.27f)
                close()
            }
            drawPath(sack, color = Color(0xFF10AF1B))

            // Neck tie ring
            drawLine(
                color = Color(0xFF0A6B11),
                start = Offset(w * 0.32f, h * 0.27f),
                end = Offset(w * 0.68f, h * 0.27f),
                strokeWidth = w * 0.06f,
                cap = StrokeCap.Round,
            )
        }
        Text(
            text = "৳",
            color = PureWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun HandHoldingMoneyIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Gold coin top-left
        drawCircle(
            color = Color(0xFFF59E0B),
            radius = w * 0.14f,
            center = Offset(w * 0.36f, h * 0.25f),
        )
        drawCircle(
            color = Color(0xFFFCD34D),
            radius = w * 0.09f,
            center = Offset(w * 0.36f, h * 0.25f),
        )

        // Green currency note tilted
        rotate(degrees = -14f, pivot = Offset(w * 0.58f, h * 0.42f)) {
            drawRoundRect(
                color = Color(0xFF22C55E),
                topLeft = Offset(w * 0.32f, h * 0.24f),
                size = Size(w * 0.52f, h * 0.32f),
                cornerRadius = CornerRadius(w * 0.06f, w * 0.06f),
            )
            drawRoundRect(
                color = Color(0xFF86EFAC),
                topLeft = Offset(w * 0.38f, h * 0.29f),
                size = Size(w * 0.40f, h * 0.22f),
                cornerRadius = CornerRadius(w * 0.04f, w * 0.04f),
            )
            drawCircle(
                color = Color(0xFF15803D),
                radius = w * 0.065f,
                center = Offset(w * 0.58f, h * 0.40f),
            )
        }

        // Deep navy blue supporting hand
        val hand = Path().apply {
            moveTo(w * 0.10f, h * 0.56f)
            lineTo(w * 0.24f, h * 0.46f)
            lineTo(w * 0.30f, h * 0.60f)
            lineTo(w * 0.72f, h * 0.62f)
            cubicTo(w * 0.84f, h * 0.62f, w * 0.84f, h * 0.76f, w * 0.66f, h * 0.78f)
            lineTo(w * 0.36f, h * 0.86f)
            lineTo(w * 0.14f, h * 0.78f)
            close()
        }
        drawPath(hand, color = Color(0xFF0B2E6B))
    }
}

@Composable
private fun TallySearchAndFilterRow(
    query: String,
    onQueryChange: (String) -> Unit,
    activeFilter: Int,
    onCycleFilter: () -> Unit,
    onDownloadPdf: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            color = PureWhite,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(0.8.dp, Color(0xFFE0ECE5)),
            shadowElevation = 2.dp,
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "খুঁজন",
                    tint = Color(0xFF4B5563),
                    modifier = Modifier.size(21.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = "খুঁজন",
                            color = InkSecondary,
                            fontSize = 14.5.sp,
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = InkPrimary,
                            fontSize = 14.5.sp,
                        ),
                        cursorBrush = SolidColor(HisabGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_search_input"),
                    )
                }
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "মুছুন",
                            tint = InkMuted,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }

        Surface(
            color = if (activeFilter == 0) PureWhite else DenaRed.copy(alpha = 0.08f),
            shape = CircleShape,
            border = BorderStroke(
                0.8.dp,
                if (activeFilter == 0) Color(0xFFE0ECE5) else DenaRed.copy(alpha = 0.45f),
            ),
            shadowElevation = 2.dp,
            modifier = Modifier.size(44.dp),
        ) {
            IconButton(
                onClick = onCycleFilter,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("home_filter_button"),
            ) {
                Icon(
                    imageVector = Icons.Outlined.FilterAlt,
                    contentDescription = "ফিল্টার",
                    tint = if (activeFilter == 0) InkPrimary else DenaRed,
                )
            }
        }

        Surface(
            color = PureWhite,
            shape = CircleShape,
            border = BorderStroke(0.8.dp, Color(0xFFE0ECE5)),
            shadowElevation = 2.dp,
            modifier = Modifier.size(44.dp),
        ) {
            IconButton(
                onClick = onDownloadPdf,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("home_download_button"),
            ) {
                Icon(
                    imageVector = Icons.Outlined.FileDownload,
                    contentDescription = "হিসাব ডাউনলোড",
                    tint = InkPrimary,
                )
            }
        }
    }
}

@Composable
private fun HomeBackupOptionsBar(
    onClearBackupClick: () -> Unit,
    onCloudBackupClick: () -> Unit,
    onGoogleBackupClick: () -> Unit,
) {
    Surface(
        color = HomeSoftSkyCardBg,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, HomeSoftSkyBorder),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_backup_options_bar"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onClearBackupClick)
                    .padding(vertical = 4.dp, horizontal = 4.dp)
                    .testTag("backup_option_clear"),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "ক্লিয়ার ব্যাকআপ",
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "ক্লিয়ার ব্যাকআপ",
                    color = InkPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            VerticalDivider(
                modifier = Modifier.height(34.dp),
                color = HomeSoftSkyBorder,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onCloudBackupClick)
                    .padding(vertical = 4.dp, horizontal = 4.dp)
                    .testTag("backup_option_cloud"),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = "ক্লাউড ব্যাকআপ",
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "ক্লাউড ব্যাকআপ",
                    color = InkPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            VerticalDivider(
                modifier = Modifier.height(34.dp),
                color = HomeSoftSkyBorder,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onGoogleBackupClick)
                    .padding(vertical = 4.dp, horizontal = 4.dp)
                    .testTag("backup_option_google"),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                GoogleGMarkIcon(modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "গুগল ব্যাকআপ",
                    color = InkPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun GoogleGMarkIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.22f
        val inset = strokeWidth / 2f
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        val topLeft = Offset(inset, inset)

        // Red top-left arc
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = -145f,
            sweepAngle = 105f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
        )
        // Yellow left arc
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 145f,
            sweepAngle = 70f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
        )
        // Green bottom arc
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 40f,
            sweepAngle = 105f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
        )
        // Blue right arc
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -15f,
            sweepAngle = 60f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
        )
        // Blue horizontal bar of 'G'
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(size.width * 0.50f, size.height * 0.52f),
            end = Offset(size.width - inset * 0.4f, size.height * 0.52f),
            strokeWidth = strokeWidth * 0.9f,
            cap = StrokeCap.Butt,
        )
    }
}

@Composable
private fun HomeCustomerSupplierCountsBar(
    customerCount: Int,
    supplierCount: Int,
    activePartyFilter: Int,
    onCustomerClick: () -> Unit,
    onSupplierClick: () -> Unit,
) {
    Surface(
        color = HomeSoftSkyCardBg,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, HomeSoftSkyBorder),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_customer_supplier_counts"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (activePartyFilter == 1) HomeAvatarSkyBg.copy(alpha = 0.7f) else Color.Transparent)
                    .clickable(onClick = onCustomerClick)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("home_customer_count_box"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = "কাস্টমার",
                    tint = HomeAvatarSkyText,
                    modifier = Modifier.size(34.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "কাস্টমার",
                        color = InkPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${Formatters.toBanglaDigits(customerCount.toString())} জন",
                        color = InkPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            VerticalDivider(
                modifier = Modifier.height(36.dp),
                color = HomeSoftSkyBorder,
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (activePartyFilter == 2) HomeAvatarSkyBg.copy(alpha = 0.7f) else Color.Transparent)
                    .clickable(onClick = onSupplierClick)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("home_supplier_count_box"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "সাপ্লায়ার",
                    tint = HomeAvatarSkyText,
                    modifier = Modifier.size(32.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "সাপ্লায়ার",
                        color = HomeAvatarSkyText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${Formatters.toBanglaDigits(supplierCount.toString())} জন",
                        color = InkPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun TallyCustomerCardRow(
    customer: Customer,
    photoPath: String,
    activityMillis: Long,
    nowMillis: Long,
    onClick: () -> Unit,
) {
    val timeLabel = Formatters.formatCustomerEntryTimestamp(activityMillis, nowMillis)
    val balanceColor = when {
        customer.balance > 0.0 -> DenaRed
        customer.balance < 0.0 -> PaonaGreen
        else -> InkMuted
    }
    val balanceStatus = when {
        customer.balance > 0.0 -> "পাবো"
        customer.balance < 0.0 -> "দেবো"
        else -> "পরিশোধ"
    }

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.8.dp, Color(0xFFE5ECE8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_customer_row_${customer.id}"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BlueLetterCustomerAvatar(
                name = customer.name,
                photoPath = photoPath,
                size = 44.dp,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary,
                        fontSize = 15.5.sp,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF4B5563),
                        modifier = Modifier.size(13.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = timeLabel,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF374151),
                            fontSize = 11.sp,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = Formatters.formatTakaDecimal(abs(customer.balance), useBanglaDigits = true),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = balanceColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp,
                    ),
                    maxLines = 1,
                )
                Text(
                    text = balanceStatus,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = balanceColor,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    maxLines = 1,
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = InkPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun BlueLetterCustomerAvatar(
    name: String,
    photoPath: String,
    size: Dp,
) {
    if (photoPath.isNotBlank()) {
        AsyncImage(
            model = photoPath,
            contentDescription = "$name ছবি",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .border(1.dp, HomeAvatarSkyBg, CircleShape),
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(HomeAvatarSkyBg),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = Formatters.firstLetterAvatar(name),
                color = HomeAvatarSkyText,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
        }
    }
}
