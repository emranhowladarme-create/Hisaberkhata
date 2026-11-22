package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Storefront
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.model.Customer
import com.example.data.model.LedgerTransaction
import com.example.ui.components.CustomerAvatarCircle
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
import com.example.ui.viewmodel.HisaberAppData
import com.example.util.CustomerPhotoStore
import com.example.util.Formatters
import java.io.File
import kotlin.math.abs

@Composable
fun CustomersListScreen(
    data: HisaberAppData,
    onOpenDrawer: () -> Unit,
    onCustomerClick: (String) -> Unit,
    onAddCustomerClick: () -> Unit,
) {
    val context = LocalContext.current
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var filterType by rememberSaveable { mutableStateOf("all") } // all, paona, dena

    val filtered = remember(data.customers, searchQuery, filterType) {
        data.customers.filter { c ->
            val matchesQuery = searchQuery.isBlank() ||
                c.name.contains(searchQuery, ignoreCase = true) ||
                c.phone.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (filterType) {
                "paona" -> c.balance > 0
                "dena" -> c.balance < 0
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite),
    ) {
        HisaberTopBar(
            title = "কাস্টমার",
            showMenu = true,
            onMenuClick = onOpenDrawer,
            actions = {
                IconButton(
                    onClick = onAddCustomerClick,
                    modifier = Modifier.testTag("add_customer_top_button"),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Customer/Supplier",
                        tint = InkPrimary,
                    )
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            // Search Bar matching reference
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("কাস্টমার খুঁজুন", color = InkMuted) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "খুঁজুন", tint = InkSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "মুছুন")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = HisabGreenTint,
                    focusedContainerColor = HisabGreenTint,
                    unfocusedBorderColor = HisabGreenBorder,
                    focusedBorderColor = HisabGreen,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_search_input"),
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("all" to "সব (${data.customers.size})", "paona" to "পাওনা", "dena" to "দেনা").forEach { (key, label) ->
                    FilterChip(
                        selected = filterType == key,
                        onClick = { filterType = key },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HisabGreenSoft,
                            selectedLabelColor = HisabGreen,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (filtered.isEmpty()) {
                EmptyStateBox(
                    icon = Icons.Default.Groups,
                    title = "কোনো কাস্টমার পাওয়া যায়নি",
                    subtitle = "নতুন কাস্টমার যোগ করে তাদের পাওনা ও দেনার হিসাব রাখা শুরু করুন।",
                    actionLabel = "+ নতুন কাস্টমার যোগ করুন",
                    onAction = onAddCustomerClick,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    items(filtered, key = { it.id }) { customer ->
                        CustomerListRow(
                            customer = customer,
                            photoPath = CustomerPhotoStore.getPhotoPath(context, customer.id),
                            onClick = { onCustomerClick(customer.id) },
                        )
                        HorizontalDivider(color = CardBorder)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCustomerSupplierScreen(
    onBack: () -> Unit,
    onSaveCustomer: (
        isCustomer: Boolean,
        name: String,
        mobileNumber: String,
        previousBalance: Double,
        dateMillis: Long,
        photoUri: Uri?,
        attachmentUri: String,
    ) -> Unit,
) {
    val context = LocalContext.current
    var isCustomer by rememberSaveable { mutableStateOf(true) }
    var name by rememberSaveable { mutableStateOf("") }
    var mobileNumber by rememberSaveable { mutableStateOf("") }
    var previousBalanceText by rememberSaveable { mutableStateOf("") }
    var selectedDateMillis by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedPhotoUriString by rememberSaveable { mutableStateOf("") }
    var docAttachmentUri by rememberSaveable { mutableStateOf("") }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val selectedPhotoUri = remember(selectedPhotoUriString) {
        if (selectedPhotoUriString.isBlank()) null else Uri.parse(selectedPhotoUriString)
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedPhotoUriString = uri.toString()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCameraUri != null) {
            docAttachmentUri = pendingCameraUri.toString()
        }
    }

    val cameraPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val file = File(context.cacheDir, "party_doc_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            validationError = "ছবি তুলতে ক্যামেরা অনুমতি প্রয়োজন।"
        }
    }

    val phonebookLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contactUri = result.data?.data
            if (contactUri != null) {
                runCatching {
                    val projection = arrayOf(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                    )
                    context.contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                            val phoneIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            val pickedName = if (nameIdx >= 0) cursor.getString(nameIdx).orEmpty() else ""
                            val pickedPhone = if (phoneIdx >= 0) cursor.getString(phoneIdx).orEmpty() else ""
                            if (pickedName.isNotBlank()) name = pickedName.trim()
                            if (pickedPhone.isNotBlank()) {
                                mobileNumber = pickedPhone.replace(Regex("[^+0-9]"), "").take(15)
                            }
                            validationError = null
                        }
                    }
                }.onFailure {
                    validationError = "ফোনবুক থেকে নম্বর পড়তে সমস্যা হয়েছে।"
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                        showDatePicker = false
                    },
                ) {
                    Text("ঠিক আছে", color = DenaRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("বাতিল", color = InkSecondary)
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite)
            .imePadding(),
    ) {
        HisaberTopBar(
            title = "নতুন কাস্টমার/সাপ্লায়ার",
            showBack = true,
            centerTitle = true,
            onBackClick = onBack,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // 1. Circular Profile Photo Area with Camera Icon & 'ছবি যোগ করুন'
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .testTag("new_customer_photo_upload"),
            ) {
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(HisabGreenTint)
                        .border(1.5.dp, HisabGreenBorder, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (selectedPhotoUri != null) {
                        AsyncImage(
                            model = selectedPhotoUri,
                            contentDescription = "প্রোফাইল ছবি",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "ছবি যোগ করুন",
                            tint = HisabGreen,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ছবি যোগ করুন",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = HisabGreen,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }

            // 2. Two Round Toggle Buttons Side-by-Side: 'কাস্টমার' & 'সাপ্লায়ার'
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PartyTypeToggleOption(
                    label = "কাস্টমার",
                    icon = Icons.Default.Person,
                    selected = isCustomer,
                    activeColor = DenaRed,
                    testTag = "toggle_customer",
                    onClick = { isCustomer = true },
                    modifier = Modifier.weight(1f),
                )
                PartyTypeToggleOption(
                    label = "সাপ্লায়ার",
                    icon = Icons.Default.Storefront,
                    selected = !isCustomer,
                    activeColor = HisabGreen,
                    testTag = "toggle_supplier",
                    onClick = { isCustomer = false },
                    modifier = Modifier.weight(1f),
                )
            }

            // 3. Large Button: 'ফোনবুক থেকে যোগ করুন' with Phonebook Icon
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_PICK).apply {
                        type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                    }
                    runCatching {
                        phonebookLauncher.launch(intent)
                    }.onFailure {
                        validationError = "এই ডিভাইসে ফোনবুক অ্যাপ পাওয়া যায়নি।"
                    }
                },
                border = BorderStroke(1.2.dp, HisabGreen),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = HisabGreenTint,
                    contentColor = HisabGreen,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("add_from_phonebook_button"),
            ) {
                Icon(
                    imageVector = Icons.Default.ContactPhone,
                    contentDescription = null,
                    tint = HisabGreen,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ফোনবুক থেকে যোগ করুন",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HisabGreen,
                )
            }

            // 4. First Input Box: 'নাম'
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    validationError = null
                },
                label = { Text("নাম") },
                placeholder = { Text(if (isCustomer) "কাস্টমারের নাম লিখুন" else "সাপ্লায়ারের নাম লিখুন", color = InkMuted) },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = HisabGreen)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = HisabGreenTint,
                    focusedContainerColor = PureWhite,
                    unfocusedBorderColor = HisabGreenBorder,
                    focusedBorderColor = HisabGreen,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("new_customer_name_input"),
            )

            // 5. Second Input Box: 'মোবাইল নম্বর'
            OutlinedTextField(
                value = mobileNumber,
                onValueChange = {
                    mobileNumber = it.filter { ch -> ch.isDigit() || ch == '+' }.take(15)
                    validationError = null
                },
                label = { Text("মোবাইল নম্বর") },
                placeholder = { Text("01XXXXXXXXX", color = InkMuted) },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = HisabGreen)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = HisabGreenTint,
                    focusedContainerColor = PureWhite,
                    unfocusedBorderColor = HisabGreenBorder,
                    focusedBorderColor = HisabGreen,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("new_customer_phone_input"),
            )

            // 6. Third Input Box: 'পুরোনো ব্যালেন্স' with '৳' symbol beside it
            OutlinedTextField(
                value = previousBalanceText,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() || it == '.' }
                    if (filtered.count { it == '.' } <= 1) {
                        previousBalanceText = filtered.take(12)
                        validationError = null
                    }
                },
                label = { Text("পুরোনো ব্যালেন্স") },
                placeholder = { Text("০", color = InkMuted) },
                trailingIcon = {
                    Surface(
                        color = (if (isCustomer) DenaRed else PaonaGreen).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        Text(
                            text = "৳",
                            color = if (isCustomer) DenaRed else PaonaGreen,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                },
                supportingText = {
                    Text(
                        text = if (isCustomer) {
                            "আগের বাকি পাওনা (মোট পাবো)"
                        } else {
                            "আগের বাকি দেনা (মোট দেবো)"
                        },
                        color = if (isCustomer) DenaRed else PaonaGreen,
                        fontSize = 11.5.sp,
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = HisabGreenTint,
                    focusedContainerColor = PureWhite,
                    unfocusedBorderColor = HisabGreenBorder,
                    focusedBorderColor = HisabGreen,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("new_customer_balance_input"),
            )

            // 7. Date Selection Field on the left + Small Camera Icon directly to its right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = HisabGreenTint,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, HisabGreenBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showDatePicker = true }
                        .testTag("new_customer_date_field"),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "তারিখ নির্বাচন",
                            tint = HisabGreen,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "তারিখ",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = InkSecondary,
                                    fontSize = 10.5.sp,
                                ),
                            )
                            Text(
                                text = Formatters.formatBanglaDate(selectedDateMillis),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = InkPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = InkSecondary,
                        )
                    }
                }

                // Small Camera Icon directly to the right of Date field
                Surface(
                    color = if (docAttachmentUri.isNotBlank()) HisabGreenSoft else HisabGreenTint,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (docAttachmentUri.isNotBlank()) HisabGreen else HisabGreenBorder),
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("new_customer_camera_button"),
                ) {
                    IconButton(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                                PackageManager.PERMISSION_GRANTED
                            ) {
                                val file = File(context.cacheDir, "party_doc_${System.currentTimeMillis()}.jpg")
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                pendingCameraUri = uri
                                cameraLauncher.launch(uri)
                            } else {
                                cameraPermLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "ক্যামেরা দিয়ে ছবি তুলুন",
                            tint = HisabGreen,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }

            if (docAttachmentUri.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AsyncImage(
                        model = Uri.parse(docAttachmentUri),
                        contentDescription = "সংযুক্ত ছবি",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, HisabGreenBorder, RoundedCornerShape(8.dp)),
                    )
                    Text(
                        text = "রসিদ/ডকুমেন্টের ছবি সংযুক্ত হয়েছে",
                        style = MaterialTheme.typography.bodySmall,
                        color = PaonaGreen,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { docAttachmentUri = "" }) {
                        Text("মুছুন", color = DenaRed, fontSize = 12.sp)
                    }
                }
            }

            if (validationError != null) {
                Text(
                    text = validationError!!,
                    color = DenaRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // 8. Large Red 'নিশ্চিত করুন' Button at the Bottom
        Surface(
            color = PureWhite,
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            Button(
                onClick = {
                    val parsedBalance = previousBalanceText.trim().toDoubleOrNull() ?: 0.0
                    when {
                        name.isBlank() -> validationError = "অনুগ্রহ করে নাম লিখুন"
                        parsedBalance < 0.0 -> validationError = "ব্যালেন্স ঋণাত্মক হতে পারবে না"
                        else -> onSaveCustomer(
                            isCustomer,
                            name.trim(),
                            mobileNumber.trim(),
                            parsedBalance,
                            selectedDateMillis,
                            selectedPhotoUri,
                            docAttachmentUri,
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DenaRed,
                    contentColor = PureWhite,
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(54.dp)
                    .testTag("new_customer_confirm_button"),
            ) {
                Text("নিশ্চিত করুন", fontSize = 16.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PartyTypeToggleOption(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    activeColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = if (selected) activeColor else PureWhite,
        shape = CircleShape,
        border = if (selected) null else BorderStroke(1.dp, CardBorder),
        shadowElevation = if (selected) 2.dp else 0.dp,
        modifier = modifier
            .height(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag(testTag),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) PureWhite else InkSecondary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = if (selected) PureWhite else InkPrimary,
                fontSize = 14.5.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun CustomerListRow(
    customer: Customer,
    photoPath: String = "",
    onClick: () -> Unit,
) {
    val isPaona = customer.balance >= 0.0
    val statusText = when {
        customer.balance > 0.0 -> "পাওনা"
        customer.balance < 0.0 -> "দেনা"
        else -> "পরিশোধিত"
    }
    val amountColor = when {
        customer.balance > 0.0 -> PaonaGreen
        customer.balance < 0.0 -> DenaRed
        else -> InkSecondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
            .testTag("customer_row_${customer.id}"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CustomerAvatarCircle(name = customer.name, size = 46.dp, photoPath = photoPath)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = customer.name,
                style = MaterialTheme.typography.titleMedium,
                color = InkPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = InkSecondary,
            )
        }
        Text(
            text = Formatters.formatTaka(abs(customer.balance)),
            style = MaterialTheme.typography.titleMedium.copy(
                color = amountColor,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    data: HisaberAppData,
    initialCustomerId: String,
    initialScope: String,
    onBack: () -> Unit,
    onSaveCustomerTx: (
        customerId: String,
        customerName: String,
        type: String,
        amount: Double,
        description: String,
        dateMillis: Long,
        attachmentUri: String,
        paymentMethod: String,
    ) -> Unit,
    onSaveCashbookTx: (
        type: String,
        amount: Double,
        description: String,
        dateMillis: Long,
        attachmentUri: String,
        paymentMethod: String,
    ) -> Unit,
) {
    val context = LocalContext.current
    var scopeMode by rememberSaveable { mutableStateOf(initialScope) } // "customer" or "cashbook"
    var selectedCustomerId by rememberSaveable {
        mutableStateOf(
            initialCustomerId.ifBlank { data.customers.firstOrNull()?.id ?: "" }
        )
    }
    var txType by rememberSaveable {
        mutableStateOf(if (initialScope == "cashbook") "income" else "gave")
    }
    var amountText by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var dateMillis by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    var paymentMethod by rememberSaveable { mutableStateOf("cash") }
    var attachmentUri by rememberSaveable { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    var showCustomerPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val selectedCustomer = remember(data.customers, selectedCustomerId) {
        data.customers.find { it.id == selectedCustomerId } ?: data.customers.firstOrNull()
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) attachmentUri = uri.toString()
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCameraUri != null) {
            attachmentUri = pendingCameraUri.toString()
        }
    }

    val cameraPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val file = File(context.cacheDir, "tx_photo_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            validationError = "ছবি তুলতে ক্যামেরা অনুমতি প্রয়োজন।"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite)
            .imePadding(),
    ) {
        HisaberTopBar(
            title = if (scopeMode == "customer") "কাস্টমার লেনদেন" else "ক্যাশবুক এন্ট্রি",
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
            // Mode Switch: কাস্টমার লেনদেন vs ক্যাশবুক (আয়/খরচ)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = scopeMode == "customer",
                    onClick = {
                        scopeMode = "customer"
                        txType = "gave"
                    },
                    label = { Text("কাস্টমার হিসাব") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = HisabGreenSoft,
                        selectedLabelColor = HisabGreen,
                    ),
                )
                FilterChip(
                    selected = scopeMode == "cashbook",
                    onClick = {
                        scopeMode = "cashbook"
                        txType = "income"
                    },
                    label = { Text("ক্যাশবুক (আয় / ব্যয়)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = HisabGreenSoft,
                        selectedLabelColor = HisabGreen,
                    ),
                )
            }

            // Customer Selector Card matching reference
            if (scopeMode == "customer") {
                Surface(
                    color = SoftCanvas,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCustomerPicker = true }
                        .testTag("tx_customer_selector"),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CustomerAvatarCircle(
                            name = selectedCustomer?.name ?: "কাস্টমার",
                            size = 44.dp,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedCustomer?.name ?: "কাস্টমার নির্বাচন করুন",
                                style = MaterialTheme.typography.titleMedium,
                                color = InkPrimary,
                            )
                            Text(
                                text = selectedCustomer?.phone?.ifBlank { "মোবাইল নম্বর নেই" }
                                    ?: "প্রথমে কাস্টমার যোগ করুন",
                                style = MaterialTheme.typography.bodySmall,
                                color = InkSecondary,
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "কাস্টমার বদলান",
                            tint = InkSecondary,
                        )
                    }
                }
            }

            // Segmented Toggle: দিলাম / বেচা vs পেলাম (or জমা / আয় vs খরচ / ব্যয়) matching reference
            val leftLabel = if (scopeMode == "customer") "দিলাম / বেচা" else "জমা / আয়"
            val rightLabel = if (scopeMode == "customer") "পেলাম" else "খরচ / ব্যয়"
            val leftKey = if (scopeMode == "customer") "gave" else "income"
            val rightKey = if (scopeMode == "customer") "received" else "expense"

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, HisabGreenBorder, RoundedCornerShape(10.dp)),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(if (txType == leftKey) HisabGreen else PureWhite)
                        .clickable { txType = leftKey }
                        .testTag("tx_type_left"),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = leftLabel,
                        fontWeight = FontWeight.Bold,
                        color = if (txType == leftKey) PureWhite else InkPrimary,
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(if (txType == rightKey) HisabGreen else PureWhite)
                        .clickable { txType = rightKey }
                        .testTag("tx_type_right"),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = rightLabel,
                        fontWeight = FontWeight.Bold,
                        color = if (txType == rightKey) PureWhite else InkPrimary,
                    )
                }
            }

            // Amount Input with ৳ prefix matching reference
            Column {
                Text("টাকার পরিমাণ", style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { raw ->
                        amountText = raw.filter { it.isDigit() || it == '.' }
                        validationError = null
                    },
                    prefix = {
                        Text(
                            text = "৳ ",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary,
                        )
                    },
                    placeholder = { Text("5,000", fontSize = 22.sp, color = InkMuted) },
                    textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = SoftCanvas,
                        focusedContainerColor = PureWhite,
                        focusedBorderColor = HisabGreen,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tx_amount_input"),
                )
            }

            // Description Input
            Column {
                Text("বিবরণ", style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("পূর্বের পাওনার আংশিক পরিশোধ") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = SoftCanvas,
                        focusedContainerColor = PureWhite,
                        focusedBorderColor = HisabGreen,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tx_description_input"),
                )
            }

            // Date Picker Field matching reference
            Column {
                Text("তারিখ", style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = SoftCanvas,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                        .testTag("tx_date_picker"),
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = Formatters.formatBanglaDate(dateMillis),
                            style = MaterialTheme.typography.bodyLarge,
                            color = InkPrimary,
                        )
                        Icon(Icons.Default.CalendarMonth, contentDescription = "তারিখ নির্বাচন", tint = InkSecondary)
                    }
                }
            }

            // Payment Method Chips
            Column {
                Text("মাধ্যম", style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val methods = listOf(
                        "cash" to "নগদ",
                        "bkash" to "বিকাশ",
                        "nagad" to "নগদ (Nagad)",
                        "rocket" to "রকেট",
                        "bank" to "ব্যাংক",
                    )
                    items(methods) { (key, label) ->
                        FilterChip(
                            selected = paymentMethod == key,
                            onClick = { paymentMethod = key },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HisabGreenSoft,
                                selectedLabelColor = HisabGreen,
                            ),
                        )
                    }
                }
            }

            // Photo Attachment matching reference (Camera & Gallery)
            Column {
                Text("ছবি যুক্ত করুন", style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        color = SoftCanvas,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .clickable {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                                    PackageManager.PERMISSION_GRANTED
                                ) {
                                    val file = File(context.cacheDir, "tx_photo_${System.currentTimeMillis()}.jpg")
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                    pendingCameraUri = uri
                                    cameraLauncher.launch(uri)
                                } else {
                                    cameraPermLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                            .testTag("tx_camera_button"),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "ক্যামেরা থেকে ছবি", tint = InkPrimary)
                        }
                    }
                    Surface(
                        color = SoftCanvas,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .clickable {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .testTag("tx_gallery_button"),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "গ্যালারি থেকে ছবি", tint = InkPrimary)
                        }
                    }
                }
                if (attachmentUri.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = attachmentUri,
                            contentDescription = "সংযুক্ত ছবি",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(12.dp)),
                        )
                        IconButton(
                            onClick = { attachmentUri = "" },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .background(PureWhite.copy(alpha = 0.85f), CircleShape),
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "ছবি সরান", tint = DenaRed)
                        }
                    }
                }
            }

            if (validationError != null) {
                Text(
                    text = validationError!!,
                    color = DenaRed,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        // Bottom Save Button matching reference
        Surface(
            color = PureWhite,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    when {
                        amt == null || amt <= 0.0 -> {
                            validationError = "অনুগ্রহ করে সঠিক টাকার পরিমাণ লিখুন"
                        }
                        scopeMode == "customer" && selectedCustomer == null -> {
                            validationError = "প্রথমে একটি কাস্টমার নির্বাচন বা যোগ করুন"
                        }
                        scopeMode == "customer" && selectedCustomer != null -> {
                            onSaveCustomerTx(
                                selectedCustomer.id,
                                selectedCustomer.name,
                                txType,
                                amt,
                                description,
                                dateMillis,
                                attachmentUri,
                                paymentMethod,
                            )
                        }
                        else -> {
                            onSaveCashbookTx(
                                txType,
                                amt,
                                description,
                                dateMillis,
                                attachmentUri,
                                paymentMethod,
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp)
                    .testTag("tx_save_button"),
                colors = ButtonDefaults.buttonColors(containerColor = HisabGreen),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("সংরক্ষণ করুন", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showCustomerPicker) {
        ModalBottomSheet(onDismissRequest = { showCustomerPicker = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("কাস্টমার নির্বাচন করুন", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(12.dp))
                if (data.customers.isEmpty()) {
                    Text("কোনো কাস্টমার নেই। কাস্টমার ট্যাব থেকে কাস্টমার যোগ করুন।")
                } else {
                    LazyColumn(modifier = Modifier.height(280.dp)) {
                        items(data.customers, key = { it.id }) { cust ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCustomerId = cust.id
                                        showCustomerPicker = false
                                    }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CustomerAvatarCircle(name = cust.name, size = 38.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(cust.name, style = MaterialTheme.typography.titleSmall)
                                    Text(cust.phone, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                                }
                            }
                            HorizontalDivider(color = CardBorder)
                        }
                    }
                }
            }
        }
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
                ) { Text("ঠিক আছে", color = HisabGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("বাতিল") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
fun CustomerFormDialog(
    title: String,
    confirmLabel: String,
    showInitialBalance: Boolean,
    initialName: String = "",
    initialPhone: String = "",
    initialAddress: String = "",
    initialNotes: String = "",
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, address: String, initialBalance: Double, notes: String) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }
    var address by remember { mutableStateOf(initialAddress) }
    var initBalanceText by remember { mutableStateOf("") }
    var initBalanceType by remember { mutableStateOf("paona") } // paona or dena
    var notes by remember { mutableStateOf(initialNotes) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("কাস্টমারের নাম *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_customer_name"),
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it.take(15) },
                    label = { Text("মোবাইল নম্বর") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_customer_phone"),
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("ঠিকানা") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (showInitialBalance) {
                    OutlinedTextField(
                        value = initBalanceText,
                        onValueChange = { initBalanceText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("পূর্বের ব্যালেন্স (ঐচ্ছিক)") },
                        prefix = { Text("৳ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = initBalanceType == "paona",
                            onClick = { initBalanceType = "paona" },
                            label = { Text("পাওনা (+)") },
                        )
                        FilterChip(
                            selected = initBalanceType == "dena",
                            onClick = { initBalanceType = "dena" },
                            label = { Text("দেনা (-)") },
                        )
                    }
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("নোট") },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (error != null) {
                    Text(error!!, color = DenaRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        error = "কাস্টমারের নাম অবশ্যই দিতে হবে"
                    } else {
                        val rawBal = initBalanceText.toDoubleOrNull() ?: 0.0
                        val signedBal = if (initBalanceType == "dena") -abs(rawBal) else abs(rawBal)
                        onConfirm(name, phone, address, signedBal, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = HisabGreen),
                modifier = Modifier.testTag("dialog_customer_save"),
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        },
    )
}
