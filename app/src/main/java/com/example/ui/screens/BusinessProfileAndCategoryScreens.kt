package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.CardBorder
import com.example.ui.theme.HisabGreenSoft
import com.example.ui.theme.HisabGreenTint
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PureWhite
import com.example.ui.viewmodel.HisaberAppData
import com.example.util.Formatters
import com.example.util.HishabiPrefsStore

private val EmeraldHeader = Color(0xFF057A4B)

data class BusinessCategorySpec(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val tint: Color,
    val bgColor: Color,
)

val BusinessCategoryCatalog = listOf(
    BusinessCategorySpec("grocery", "মুদি বা জেনারেল স্টোর", Icons.Default.Storefront, Color(0xFF057A4B), Color(0xFFE6F4EE)),
    BusinessCategorySpec("mobile_recharge", "মোবাইল ব্যাংকিং ও রিচার্জ", Icons.Default.PhoneAndroid, Color(0xFF0284C7), Color(0xFFE0F2FE)),
    BusinessCategorySpec("bakery", "বেকারি ও কনফেকশনারি", Icons.Default.Cake, Color(0xFFDB2777), Color(0xFFFCE7F3)),
    BusinessCategorySpec("tea_stall", "চা স্টল", Icons.Default.LocalCafe, Color(0xFFD97706), Color(0xFFFEF3C7)),
    BusinessCategorySpec("electronics", "ইলেকট্রনিক্স", Icons.Default.ElectricalServices, Color(0xFF2563EB), Color(0xFFDBEAFE)),
    BusinessCategorySpec("stationery", "লাইব্রেরি ও স্টেশনারি", Icons.Default.MenuBook, Color(0xFF7C3AED), Color(0xFFEDE9FE)),
    BusinessCategorySpec("hardware", "হার্ডওয়্যার", Icons.Default.Build, Color(0xFF475569), Color(0xFFF1F5F9)),
    BusinessCategorySpec("pharmacy", "ফার্মেসি", Icons.Default.LocalPharmacy, Color(0xFF16A34A), Color(0xFFDCFCE7)),
    BusinessCategorySpec("cosmetics", "কসমেটিক্স ও জুয়েলারি", Icons.Default.Diamond, Color(0xFFE11D48), Color(0xFFFFE4E6)),
    BusinessCategorySpec("restaurant", "রেস্টুরেন্ট বা মিষ্টির দোকান", Icons.Default.Restaurant, Color(0xFFEA580C), Color(0xFFFFEDD5)),
    BusinessCategorySpec("computer", "মোবাইল ও কম্পিউটার", Icons.Default.Computer, Color(0xFF0F766E), Color(0xFFCCFBF1)),
    BusinessCategorySpec("agro", "কৃষি পণ্য ও উপকরণ", Icons.Default.Agriculture, Color(0xFF15803D), Color(0xFFDCFCE7)),
    BusinessCategorySpec("rod_cement", "রড ও সিমেন্ট", Icons.Default.Construction, Color(0xFF64748B), Color(0xFFE2E8F0)),
    BusinessCategorySpec("auto_parts", "গাড়ি ও অটো পার্টস", Icons.Default.DirectionsCar, Color(0xFF1D4ED8), Color(0xFFDBEAFE)),
    BusinessCategorySpec("tailors", "টেইলার্স / গার্মেন্টস", Icons.Default.Checkroom, Color(0xFF9333EA), Color(0xFFF3E8FF)),
    BusinessCategorySpec("other", "অন্যান্য", Icons.Default.MoreHoriz, Color(0xFF057A4B), Color(0xFFE6F4EE)),
)

private fun findCategorySpec(titleOrId: String): BusinessCategorySpec {
    return BusinessCategoryCatalog.find {
        it.title == titleOrId || it.id == titleOrId || titleOrId.contains(it.title.take(5))
    } ?: BusinessCategoryCatalog[3] // Default Tea Stall / General
}

@Composable
fun BusinessInfoScreen(
    data: HisaberAppData,
    onBack: () -> Unit,
    onSelectCategoryClick: () -> Unit,
    onSaveBusinessInfo: (updates: Map<String, Any>, feedbackMessage: String) -> Unit,
) {
    val context = LocalContext.current
    val prefsState by HishabiPrefsStore.prefsFlow.collectAsState()
    val localPrefs = remember(prefsState) { HishabiPrefsStore.load(context) }

    var businessName by rememberSaveable(data.profile.businessName) {
        mutableStateOf(data.profile.businessName.ifBlank { "ইমরান টি স্টল" })
    }
    var ownerName by rememberSaveable(data.profile.ownerName) {
        mutableStateOf(data.profile.ownerName.ifBlank { "ইমরান স্টোর" })
    }
    var nidNumber by rememberSaveable(localPrefs.nidNumber) {
        mutableStateOf(localPrefs.nidNumber.ifBlank { "4186402238" })
    }
    var businessAge by rememberSaveable(localPrefs.businessAgeYears) {
        mutableStateOf(localPrefs.businessAgeYears.ifBlank { "২ বছর" })
    }
    var address by rememberSaveable(data.profile.address) {
        mutableStateOf(data.profile.address.ifBlank { "সোনাডাঙ্গা, খুলনা, বাংলাদেশ" })
    }
    var nomineeName by rememberSaveable(localPrefs.nomineeName) {
        mutableStateOf(localPrefs.nomineeName.ifBlank { "ইমরান স্টোর" })
    }
    var nomineeRelation by rememberSaveable(localPrefs.nomineeRelation) {
        mutableStateOf(localPrefs.nomineeRelation.ifBlank { "পরিবার ও নমিনি" })
    }

    var ageDropdownExpanded by remember { mutableStateOf(false) }
    var showNomineeDialog by remember { mutableStateOf(false) }

    val selectedCategorySpec = remember(data.profile.businessCategory, localPrefs.personalUseMode) {
        if (localPrefs.personalUseMode) {
            BusinessCategorySpec("personal", "ব্যক্তিগত হিসাব", Icons.Default.Storefront, EmeraldHeader, HisabGreenSoft)
        } else {
            findCategorySpec(data.profile.businessCategory.ifBlank { "চা স্টল" })
        }
    }

    val logoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            HishabiPrefsStore.persistBusinessLogo(context, uri)
        }
    }

    val ageOptions = listOf("১ বছরের কম", "১ বছর", "২ বছর", "৩ বছর", "৫ বছর", "৬ বছর", "১০+ বছর")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite)
            .imePadding(),
    ) {
        // Top Emerald Header matching Panel 3
        Surface(color = EmeraldHeader, shadowElevation = 2.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(58.dp)
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("biz_info_back_button"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "পিছনে যান",
                        tint = PureWhite,
                    )
                }
                Text(
                    text = "ব্যবসা প্রতিষ্ঠানের তথ্য",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                    ),
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .align(Alignment.CenterHorizontally)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Circular Logo Upload matching Panel 3 & Screenshot
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .clickable {
                            logoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                        .testTag("biz_logo_upload_box"),
                    contentAlignment = Alignment.Center,
                ) {
                    if (localPrefs.businessLogoPath.isNotBlank()) {
                        AsyncImage(
                            model = localPrefs.businessLogoPath,
                            contentDescription = "ব্যবসা লোগো",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(2.dp, EmeraldHeader, CircleShape),
                        )
                    } else {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(color = Color(0xFFF3F6F4))
                            drawCircle(
                                color = EmeraldHeader.copy(alpha = 0.55f),
                                style = Stroke(
                                    width = 2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f),
                                ),
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "লোগো আপলোড",
                            tint = EmeraldHeader,
                            modifier = Modifier.size(30.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ব্যবসা লোগো আপলোড",
                    color = InkSecondary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text("ব্যবসা প্রতিষ্ঠানের নাম") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldHeader,
                    focusedLabelColor = EmeraldHeader,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("biz_info_name_input"),
            )

            OutlinedTextField(
                value = ownerName,
                onValueChange = { ownerName = it },
                label = { Text("আপনার নাম") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldHeader,
                    focusedLabelColor = EmeraldHeader,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("biz_info_owner_input"),
            )

            // Business Category Tile -> opens Screen 4 BusinessCategoryScreen
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "ব্যবসার ধরণ",
                    color = InkSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp),
                )
                Surface(
                    onClick = onSelectCategoryClick,
                    color = Color(0xFFF3F6F4),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("biz_info_category_selector"),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(selectedCategorySpec.bgColor),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = selectedCategorySpec.icon,
                                contentDescription = selectedCategorySpec.title,
                                tint = selectedCategorySpec.tint,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = selectedCategorySpec.title,
                            color = InkPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                        )
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(CardBorder.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "ধরণ পরিবর্তন",
                                tint = InkPrimary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = nidNumber,
                onValueChange = { raw -> nidNumber = raw.filter { it.isDigit() }.take(17) },
                label = { Text("NID নম্বর") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldHeader,
                    focusedLabelColor = EmeraldHeader,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("biz_info_nid_input"),
            )

            // Business Age Dropdown matching Panel 3
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = businessAge,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("ব্যবসার আনুমানিক বয়স (বছর)") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "বয়স নির্বাচন",
                            tint = InkSecondary,
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldHeader,
                        focusedLabelColor = EmeraldHeader,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { ageDropdownExpanded = true }
                        .testTag("biz_info_age_dropdown"),
                )
                DropdownMenu(
                    expanded = ageDropdownExpanded,
                    onDismissRequest = { ageDropdownExpanded = false },
                ) {
                    ageOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                businessAge = option
                                ageDropdownExpanded = false
                            },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("ব্যবসা প্রতিষ্ঠানের ঠিকানা") },
                minLines = 2,
                maxLines = 3,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldHeader,
                    focusedLabelColor = EmeraldHeader,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("biz_info_address_input"),
            )

            // Nominee Info Section matching Panel 3 ('নমিনির তথ্য' + Verified Badge)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "নমিনির তথ্য",
                    color = InkPrimary,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                )

                Surface(
                    onClick = { showNomineeDialog = true },
                    color = PureWhite,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("biz_info_nominee_card"),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(HisabGreenSoft),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = Formatters.firstLetterAvatar(nomineeName),
                                color = EmeraldHeader,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = nomineeName,
                                color = InkPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "নমিনি: $nomineeRelation",
                                color = InkSecondary,
                                fontSize = 12.5.sp,
                            )
                        }
                        Surface(
                            color = Color(0xFFE6F4EE),
                            shape = CircleShape,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = EmeraldHeader,
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Verified",
                                    color = EmeraldHeader,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "নমিনি সম্পাদনা",
                            tint = InkMuted,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }

        // Bottom Full-Width Emerald Save Button ('সেট করুন')
        Surface(color = PureWhite, shadowElevation = 6.dp, modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    val cleanBiz = businessName.trim().ifBlank { "হিসাবি খাতা" }.take(100)
                    val cleanOwner = ownerName.trim().ifBlank { cleanBiz }.take(80)
                    val cleanAddr = address.trim().take(200)
                    HishabiPrefsStore.update(context) {
                        it.copy(
                            nidNumber = nidNumber.trim(),
                            businessAgeYears = businessAge.trim(),
                            nomineeName = nomineeName.trim().ifBlank { cleanOwner },
                            nomineeRelation = nomineeRelation.trim(),
                            nomineeVerified = true,
                        )
                    }
                    onSaveBusinessInfo(
                        mapOf(
                            "businessName" to cleanBiz,
                            "ownerName" to cleanOwner,
                            "address" to cleanAddr,
                        ),
                        "ব্যবসা প্রতিষ্ঠানের তথ্য সংরক্ষিত হয়েছে",
                    )
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldHeader),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .height(52.dp)
                    .testTag("biz_info_save_button"),
            ) {
                Text(
                    text = "সেট করুন",
                    color = PureWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }

    if (showNomineeDialog) {
        var draftNominee by remember { mutableStateOf(nomineeName) }
        var draftRel by remember { mutableStateOf(nomineeRelation) }
        AlertDialog(
            onDismissRequest = { showNomineeDialog = false },
            containerColor = PureWhite,
            title = { Text("নমিনির তথ্য সম্পাদনা", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = draftNominee,
                        onValueChange = { draftNominee = it },
                        label = { Text("নমিনির নাম") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = draftRel,
                        onValueChange = { draftRel = it },
                        label = { Text("সম্পর্ক") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (draftNominee.isNotBlank()) {
                            nomineeName = draftNominee.trim()
                            nomineeRelation = draftRel.trim().ifBlank { "পরিবার ও নমিনি" }
                        }
                        showNomineeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldHeader),
                ) { Text("নিশ্চিত") }
            },
            dismissButton = {
                TextButton(onClick = { showNomineeDialog = false }) { Text("বাতিল") }
            },
        )
    }
}

@Composable
fun BusinessCategoryScreen(
    currentCategory: String,
    onClose: () -> Unit,
    onConfirmCategory: (categoryTitle: String, personalUse: Boolean) -> Unit,
) {
    val context = LocalContext.current
    val initialPrefs = remember { HishabiPrefsStore.load(context) }

    var selectedTitle by rememberSaveable(currentCategory) {
        mutableStateOf(findCategorySpec(currentCategory).title)
    }
    var personalUse by rememberSaveable { mutableStateOf(initialPrefs.personalUseMode) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite)
            .statusBarsPadding(),
    ) {
        // Header matching Panel 4 & Screenshot: 'আপনার ব্যবসার ধরণ কি?' + Close X
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 8.dp, top = 12.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "আপনার ব্যবসার ধরণ কি?",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = InkPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("biz_category_close_button"),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "বন্ধ করুন",
                    tint = InkPrimary,
                )
            }
        }

        // 3-Column Category Grid matching Screenshot & Panel 4
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(BusinessCategoryCatalog, key = { it.id }) { cat ->
                val isSelected = !personalUse && selectedTitle == cat.title
                Card(
                    onClick = {
                        personalUse = false
                        selectedTitle = cat.title
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFECFDF5) else PureWhite,
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) EmeraldHeader else CardBorder,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp)
                        .testTag("biz_cat_${cat.id}"),
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldHeader),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "নির্বাচিত",
                                    tint = PureWhite,
                                    modifier = Modifier.size(12.dp),
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(cat.bgColor),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = cat.title,
                                    tint = cat.tint,
                                    modifier = Modifier.size(26.dp),
                                )
                            }
                            Spacer(modifier = Modifier.height(7.dp))
                            Text(
                                text = cat.title,
                                color = if (isSelected) EmeraldHeader else InkPrimary,
                                fontSize = 11.5.sp,
                                lineHeight = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }

        // Bottom Personal Use Toggle + Confirm Button
        Surface(
            color = PureWhite,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    color = if (personalUse) Color(0xFFECFDF5) else Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (personalUse) EmeraldHeader else CardBorder),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ব্যক্তিগত হিসাব রাখি / নিজস্ব ব্যবহার",
                                color = InkPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "দোকান নয়, নিজের দৈনন্দিন হিসাবের জন্য",
                                color = InkSecondary,
                                fontSize = 11.5.sp,
                            )
                        }
                        Switch(
                            checked = personalUse,
                            onCheckedChange = { personalUse = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureWhite,
                                checkedTrackColor = EmeraldHeader,
                                uncheckedThumbColor = PureWhite,
                                uncheckedTrackColor = Color(0xFFCBD5E1),
                            ),
                            modifier = Modifier.testTag("biz_cat_personal_toggle"),
                        )
                    }
                }

                Button(
                    onClick = {
                        val finalCat = if (personalUse) "ব্যক্তিগত হিসাব" else selectedTitle
                        HishabiPrefsStore.update(context) { it.copy(personalUseMode = personalUse) }
                        onConfirmCategory(finalCat, personalUse)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldHeader),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("biz_category_confirm_button"),
                ) {
                    Text(
                        text = "নিশ্চিত করুন",
                        color = PureWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
