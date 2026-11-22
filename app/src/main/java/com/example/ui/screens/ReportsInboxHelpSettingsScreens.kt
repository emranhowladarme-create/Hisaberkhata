package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.core.content.FileProvider
import com.example.data.model.AppNotification
import com.example.ui.components.EmptyStateBox
import com.example.ui.components.HisaberTopBar
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
import com.example.util.PdfReportGenerator
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.max

// --- Reports & Analytics (with Daily/Weekly/Monthly/Yearly & Downloadable PDF) ---

@Composable
fun ReportsScreen(
    data: HisaberAppData,
    ownerMode: Boolean,
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit,
) {
    val context = LocalContext.current
    var periodIdx by rememberSaveable { mutableIntStateOf(2) } // 0=দৈনিক, 1=সাপ্তাহিক, 2=মাসিক, 3=বার্ষিক
    val periods = listOf("দৈনিক", "সাপ্তাহিক", "মাসিক", "বার্ষিক")

    val cutoffMillis = remember(periodIdx) {
        val cal = Calendar.getInstance()
        when (periodIdx) {
            0 -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
            }
            1 -> cal.add(Calendar.DAY_OF_YEAR, -7)
            2 -> cal.add(Calendar.MONTH, -1)
            else -> cal.add(Calendar.YEAR, -1)
        }
        cal.timeInMillis
    }

    val periodTx = remember(data.transactions, cutoffMillis) {
        data.transactions.filter { it.dateMillis >= cutoffMillis }
    }
    val totalIncome = remember(periodTx) {
        periodTx.filter { it.type == "income" || it.type == "received" }.sumOf { it.amount }
    }
    val totalExpense = remember(periodTx) {
        periodTx.filter { it.type == "expense" || it.type == "gave" }.sumOf { it.amount }
    }
    val profitLoss = totalIncome - totalExpense
    val dueCustomers = remember(data.customers) {
        data.customers.filter { it.balance != 0.0 }.sortedByDescending { abs(it.balance) }
    }

    Column(modifier = Modifier.fillMaxSize().background(SoftCanvas)) {
        HisaberTopBar(
            title = if (ownerMode) "মালিকের রিপোর্ট" else "রিপোর্ট ও বিশ্লেষণ",
            showBack = true,
            onBackClick = onBack,
            actions = {
                IconButton(
                    onClick = {
                        PdfReportGenerator.generateAccountingPdf(
                            context = context,
                            businessName = data.profile.businessName.ifBlank { "হিসাবের খাতা" },
                            periodLabel = periods[periodIdx],
                            totalIncome = totalIncome,
                            totalExpense = totalExpense,
                            totalPaona = data.summary.totalPaona,
                            totalDena = data.summary.totalDena,
                            customers = data.customers,
                            transactions = periodTx,
                        ).onSuccess { pdfUri ->
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, pdfUri)
                                putExtra(Intent.EXTRA_SUBJECT, "হিসাবের খাতা - ${periods[periodIdx]} রিপোর্ট")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "PDF রিপোর্ট ডাউনলোড / শেয়ার করুন"))
                        }.onFailure {
                            onShowMessage("PDF তৈরি করতে সমস্যা হয়েছে")
                        }
                    },
                    modifier = Modifier.testTag("export_pdf_button"),
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF রিপোর্ট", tint = HisabGreen)
                }
            },
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    periods.forEachIndexed { idx, label ->
                        FilterChip(
                            selected = periodIdx == idx,
                            onClick = { periodIdx = idx },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HisabGreenSoft,
                                selectedLabelColor = HisabGreen,
                            ),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // Income, Expense, Profit/Loss Summary Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ReportMetricCard("মোট আয়", Formatters.formatTaka(totalIncome), PaonaGreen, Modifier.weight(1f))
                    ReportMetricCard("মোট ব্যয়", Formatters.formatTaka(totalExpense), DenaRed, Modifier.weight(1f))
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (profitLoss >= 0) PaonaGreenBg else DenaRedBg,
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = if (profitLoss >= 0) "নিট লাভ (${periods[periodIdx]})" else "নিট ক্ষতি (${periods[periodIdx]})",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = "মোট লেনদেন: ${periodTx.size}টি",
                                style = MaterialTheme.typography.bodySmall,
                                color = InkSecondary,
                            )
                        }
                        Text(
                            text = Formatters.formatTaka(abs(profitLoss)),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = if (profitLoss >= 0) PaonaGreen else DenaRed,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }
                }
            }

            // Visual Bar Comparison Chart via Compose Canvas
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = HisabGreenTint),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, HisabGreenBorder),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("আয়-ব্যয় ও পাওনা-দেনা তুলনা", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(14.dp))
                        val maxVal = max(
                            max(totalIncome, totalExpense),
                            max(data.summary.totalPaona, data.summary.totalDena),
                        ).coerceAtLeast(1.0)

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                        ) {
                            val bars = listOf(
                                totalIncome to PaonaGreen,
                                totalExpense to DenaRed,
                                data.summary.totalPaona to HisabGreen,
                                data.summary.totalDena to WarningAmber,
                            )
                            val slotW = size.width / bars.size
                            val barW = slotW * 0.48f
                            bars.forEachIndexed { idx, (value, color) ->
                                val ratio = (value / maxVal).toFloat().coerceIn(0.04f, 1f)
                                val h = size.height * ratio
                                val x = idx * slotW + (slotW - barW) / 2f
                                drawRoundRect(
                                    color = color,
                                    topLeft = Offset(x, size.height - h),
                                    size = Size(barW, h),
                                    cornerRadius = CornerRadius(12f, 12f),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                        ) {
                            Text("আয়", style = MaterialTheme.typography.labelSmall, color = PaonaGreen)
                            Text("ব্যয়", style = MaterialTheme.typography.labelSmall, color = DenaRed)
                            Text("পাওনা", style = MaterialTheme.typography.labelSmall, color = HisabGreen)
                            Text("দেনা", style = MaterialTheme.typography.labelSmall, color = WarningAmber)
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        PdfReportGenerator.generateAccountingPdf(
                            context = context,
                            businessName = data.profile.businessName.ifBlank { "হিসাবের খাতা" },
                            periodLabel = periods[periodIdx],
                            totalIncome = totalIncome,
                            totalExpense = totalExpense,
                            totalPaona = data.summary.totalPaona,
                            totalDena = data.summary.totalDena,
                            customers = data.customers,
                            transactions = periodTx,
                        ).onSuccess { pdfUri ->
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, pdfUri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "PDF রিপোর্ট ডাউনলোড করুন"))
                        }.onFailure {
                            onShowMessage("PDF তৈরি ব্যর্থ হয়েছে")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HisabGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PDF রিপোর্ট ডাউনলোড / শেয়ার করুন", fontWeight = FontWeight.Bold)
                }
            }

            item {
                Text("কাস্টমার বকেয়া রিপোর্ট (${dueCustomers.size})", style = MaterialTheme.typography.titleMedium)
            }

            items(dueCustomers, key = { it.id }) { c ->
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
                            Text(c.name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = if (c.balance > 0) "পাওনা" else "দেনা",
                                style = MaterialTheme.typography.bodySmall,
                                color = InkSecondary,
                            )
                        }
                        Text(
                            text = Formatters.formatTaka(abs(c.balance)),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = if (c.balance > 0) PaonaGreen else DenaRed,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportMetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = HisabGreenTint),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, HisabGreenBorder),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge.copy(color = color, fontWeight = FontWeight.Bold))
        }
    }
}

// --- Inbox / Notifications (recreates reference ইনবক্স screen) ---

@Composable
fun InboxScreen(
    notifications: List<AppNotification>,
    onBack: () -> Unit,
    onMarkRead: (String) -> Unit,
    onMarkAllRead: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(PureWhite)) {
        HisaberTopBar(
            title = "ইনবক্স",
            showBack = true,
            onBackClick = onBack,
            actions = {
                if (notifications.any { !it.isRead }) {
                    IconButton(onClick = onMarkAllRead) {
                        Icon(Icons.Default.DoneAll, contentDescription = "সব পঠিত চিহ্নিত করুন", tint = HisabGreen)
                    }
                }
            },
        )

        if (notifications.isEmpty()) {
            EmptyStateBox(
                icon = Icons.Default.Notifications,
                title = "ইনবক্স খালি",
                subtitle = "পেমেন্ট, স্টক সতর্কতা ও অ্যাপ আপডেট নোটিফিকেশন এখানে আসবে।",
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(notifications, key = { it.id }) { notif ->
                    val (icon, bgColor) = when (notif.category) {
                        "payment" -> Icons.Default.CheckCircle to PaonaGreen
                        "promo" -> Icons.Default.Sms to Color(0xFFAB47BC)
                        "qr" -> Icons.Default.QrCode2 to WarningAmber
                        "stock" -> Icons.Default.Inventory2 to DenaRed
                        "feature" -> Icons.Default.Notifications to Color(0xFF42A5F5)
                        else -> Icons.Default.SystemUpdate to HisabGreen
                    }

                    Card(
                        onClick = { if (!notif.isRead) onMarkRead(notif.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (notif.isRead) PureWhite else HisabGreenTint,
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (notif.isRead) CardBorder else HisabGreenBorder),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(bgColor),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(icon, contentDescription = null, tint = PureWhite, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = notif.title,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = if (notif.isRead) FontWeight.Medium else FontWeight.Bold,
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f),
                                    )
                                    val dateMillis = notif.createdAt?.toDate()?.time ?: System.currentTimeMillis()
                                    Text(
                                        text = Formatters.formatShortDate(dateMillis),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = InkMuted,
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = notif.body,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


