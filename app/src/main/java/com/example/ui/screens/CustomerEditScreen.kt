package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.model.Customer
import com.example.ui.components.EmptyStateBox
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DenaRed
import com.example.ui.theme.HisabGreen
import com.example.ui.theme.HisabGreenSoft
import com.example.ui.theme.HisabGreenTint
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PureWhite
import com.example.util.CustomerPhotoStore
import com.example.util.Formatters
import java.io.File

private val EditCustomerGoldRing = Color(0xFFF5B400)
private val EditCustomerGreenCta = Color(0xFF0A9E3C)

/**
 * Panel 3: কাস্টমার এডিট করুন (Customer Edit Screen).
 * Displays only the selected customer's details and photo, letting the user
 * capture or pick a photo (zero-permission PickVisualMedia + Camera) and save edits.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerEditScreen(
    customer: Customer?,
    onBack: () -> Unit,
    onSaveCustomer: (name: String, phone: String, address: String, notes: String, newPhotoUri: Uri?) -> Unit,
) {
    val context = LocalContext.current

    if (customer == null) {
        Column(modifier = Modifier.fillMaxSize().background(PureWhite)) {
            Surface(color = PureWhite, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "পিছনে যান",
                            tint = InkPrimary,
                        )
                    }
                    Text(
                        text = "কাস্টমার এডিট করুন",
                        color = InkPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            EmptyStateBox(
                icon = Icons.Default.Person,
                title = "কাস্টমার খুঁজে পাওয়া যায়নি",
                subtitle = "তালিকায় ফিরে যান।",
                actionLabel = "ফিরে যান",
                onAction = onBack,
            )
        }
        return
    }

    val existingPhotoPath = remember(customer.id) {
        CustomerPhotoStore.getPhotoPath(context, customer.id)
    }

    var name by rememberSaveable(customer.id) { mutableStateOf(customer.name) }
    var phone by rememberSaveable(customer.id) { mutableStateOf(customer.phone) }
    var selectedPhotoUriString by rememberSaveable(customer.id) { mutableStateOf("") }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showPhotoSourceSheet by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val selectedPhotoUri = remember(selectedPhotoUriString) {
        if (selectedPhotoUriString.isBlank()) null else Uri.parse(selectedPhotoUriString)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedPhotoUriString = uri.toString()
            validationError = null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCameraUri != null) {
            selectedPhotoUriString = pendingCameraUri.toString()
            validationError = null
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val file = File(context.cacheDir, "edit_cust_${customer.id}_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            validationError = "ক্যামেরা দিয়ে ছবি তুলতে অনুমতি প্রয়োজন।"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite)
            .imePadding(),
    ) {
        // Clean White Top Bar matching Panel 3
        Surface(color = PureWhite, shadowElevation = 1.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("edit_customer_back_button"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "পিছনে যান",
                        tint = InkPrimary,
                    )
                }
                Text(
                    text = "কাস্টমার এডিট করুন",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = InkPrimary,
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
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Centered Circular Customer Photo with Golden Ring & Overlapping Black Camera Badge
            Box(
                modifier = Modifier
                    .size(124.dp)
                    .clip(CircleShape)
                    .clickable { showPhotoSourceSheet = true }
                    .testTag("edit_customer_photo_avatar"),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(116.dp)
                        .clip(CircleShape)
                        .background(HisabGreenSoft)
                        .border(2.5.dp, EditCustomerGoldRing, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    val photoModel: Any? = selectedPhotoUri ?: existingPhotoPath.takeIf { it.isNotBlank() }
                    if (photoModel != null) {
                        AsyncImage(
                            model = photoModel,
                            contentDescription = customer.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Text(
                            text = Formatters.firstLetterAvatar(name.ifBlank { customer.name }),
                            color = HisabGreen,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 6.dp, bottom = 6.dp)
                        .size(32.dp)
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
                        modifier = Modifier.size(17.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { showPhotoSourceSheet = true }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "ছবি যোগ করুন",
                    color = InkPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "ক্যামেরা অথবা গ্যালারি থেকে ছবি নিন",
                    color = InkSecondary,
                    fontSize = 12.5.sp,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Labeled Input 1: কাস্টমারের নাম
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "কাস্টমারের নাম",
                    color = InkPrimary,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        validationError = null
                    },
                    placeholder = { Text("কাস্টমারের নাম লিখুন") },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE5E7EB)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = InkPrimary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EditCustomerGreenCta,
                        unfocusedBorderColor = CardBorder,
                        focusedContainerColor = PureWhite,
                        unfocusedContainerColor = PureWhite,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_customer_name_input"),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Labeled Input 2: মোবাইল নাম্বার
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "মোবাইল নাম্বার",
                    color = InkPrimary,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { raw ->
                        phone = raw.filter { it.isDigit() || it == '+' }.take(15)
                        validationError = null
                    },
                    placeholder = { Text("+8801XXXXXXXXX") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = InkPrimary,
                            modifier = Modifier.size(22.dp),
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EditCustomerGreenCta,
                        unfocusedBorderColor = CardBorder,
                        focusedContainerColor = PureWhite,
                        unfocusedContainerColor = PureWhite,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_customer_phone_input"),
                )
            }

            if (validationError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = validationError!!,
                    color = DenaRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // Bottom Green Pill Save CTA Button ("পরবর্তী →") matching Panel 3
        Surface(
            color = PureWhite,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(
                onClick = {
                    val trimmedName = name.trim()
                    if (trimmedName.isBlank()) {
                        validationError = "অনুগ্রহ করে কাস্টমারের নাম লিখুন"
                    } else {
                        onSaveCustomer(
                            trimmedName,
                            phone.trim(),
                            customer.address,
                            customer.notes,
                            selectedPhotoUri,
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = EditCustomerGreenCta,
                    contentColor = PureWhite,
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .height(52.dp)
                    .testTag("edit_customer_save_button"),
            ) {
                Text(
                    text = "পরবর্তী",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }

    if (showPhotoSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoSourceSheet = false },
            containerColor = PureWhite,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 26.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "ছবি যোগ করুন",
                    color = InkPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                )
                OutlinedButton(
                    onClick = {
                        showPhotoSourceSheet = false
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    border = BorderStroke(1.dp, EditCustomerGreenCta),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("edit_customer_gallery_pick"),
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = EditCustomerGreenCta)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("গ্যালারি থেকে ছবি নিন", color = EditCustomerGreenCta, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = {
                        showPhotoSourceSheet = false
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_GRANTED
                        ) {
                            val file = File(context.cacheDir, "edit_cust_${customer.id}_${System.currentTimeMillis()}.jpg")
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                            pendingCameraUri = uri
                            cameraLauncher.launch(uri)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    border = BorderStroke(1.dp, CardBorder),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = HisabGreenTint),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("edit_customer_camera_pick"),
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = InkPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("ক্যামেরা দিয়ে ছবি তুলুন", color = InkPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
