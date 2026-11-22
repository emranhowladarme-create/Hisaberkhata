package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.auth.attemptAutoSignIn
import com.example.auth.authStateFlow
import com.example.auth.signOutUser
import com.example.data.repository.HisaberRepository
import com.example.ui.components.BottomNavTab
import com.example.ui.components.EmptyStateBox
import com.example.ui.components.HisaberBottomBar
import com.example.ui.components.LoadingStateBox
import com.example.ui.navigation.AppRoute
import com.example.ui.screens.AddTransactionScreen
import com.example.ui.screens.AuthFlowScreen
import com.example.ui.screens.BusinessCategoryScreen
import com.example.ui.screens.BusinessInfoScreen
import com.example.ui.screens.BusinessNotesScreen
import com.example.ui.screens.CashbookScreen
import com.example.ui.screens.CustomerDetailScreen
import com.example.ui.screens.CustomerEditScreen
import com.example.ui.screens.CustomersListScreen
import com.example.ui.screens.GoldMembershipScreen
import com.example.ui.screens.HelpCenterScreen
import com.example.ui.screens.HisabiGoldScreen
import com.example.ui.screens.HisabiPayScreen
import com.example.ui.screens.HishabiDrawerContent
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InboxScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.LocationPermissionScreen
import com.example.ui.screens.MoreMenuScreen
import com.example.ui.screens.MultiBusinessScreen
import com.example.ui.screens.MyGoldPackageScreen
import com.example.ui.screens.NewCustomerSupplierScreen
import com.example.ui.screens.PaymentGatewayScreen
import com.example.ui.screens.PermissionCenterScreen
import com.example.ui.screens.PinLockScreen
import com.example.ui.screens.QrCodeScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.ServiceChargeDialog
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SmsPackagesScreen
import com.example.ui.screens.SmsSenderScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StaffViewDialog
import com.example.ui.screens.TransactionLimitDialog
import com.example.ui.theme.HisaberKhataTheme
import com.example.ui.theme.PureWhite
import com.example.ui.viewmodel.HisaberAppData
import com.example.ui.viewmodel.HisaberLedgerController
import com.example.ui.viewmodel.HisaberViewModel
import com.example.ui.viewmodel.LocalHisaberViewModel
import com.example.ui.viewmodel.UiState
import com.example.util.CustomerPhotoStore
import com.example.util.GoldPackageManager
import com.example.util.HishabiLocalPrefs
import com.example.util.HishabiPrefsStore
import com.example.util.configureAppCheck
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            HisaberKhataTheme {
                HisaberRootApp()
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        configureAppCheck(this, intent)
    }
}

@Composable
private fun HisaberRootApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val initialPrefs = remember(context) { HishabiPrefsStore.load(context) }
    val localPrefs by HishabiPrefsStore.prefsFlow.collectAsStateWithLifecycle(initialValue = initialPrefs)

    var showSplash by remember { mutableStateOf(true) }
    var pendingOwnerName by rememberSaveable { mutableStateOf("") }
    var pendingBusinessName by rememberSaveable { mutableStateOf("") }
    var pendingPhone by rememberSaveable { mutableStateOf("") }

    val currentUser by Firebase.auth.authStateFlow()
        .collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

    LaunchedEffect(Unit) {
        GoldPackageManager.syncExpiryIfNeeded(context)
        if (!initialPrefs.isPhoneSessionVerified && Firebase.auth.currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {},
                onUnauthenticated = {},
                scope = scope,
            )
        }
        delay(900L)
        showSplash = false
    }

    val performLogout: () -> Unit = {
        pendingOwnerName = ""
        pendingBusinessName = ""
        pendingPhone = ""
        HishabiPrefsStore.update(context) {
            it.copy(isPhoneSessionVerified = false, verifiedPhoneNumber = "")
        }
        signOutUser(credentialManager, scope)
    }

    val connectGoogleCloud: () -> Unit = {
        com.example.auth.onGoogleSignInClicked(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {},
            onAuthError = {},
            scope = scope,
        )
    }

    when {
        showSplash -> SplashScreen()
        currentUser != null -> {
            val uid = currentUser!!.uid
            key(uid) {
                AuthenticatedAppShell(
                    currentUserId = uid,
                    initialOwnerName = pendingOwnerName.ifBlank { "Mohammad Emran Hawladar" },
                    initialBusinessName = pendingBusinessName.ifBlank { "ইমরান টি স্টল" },
                    initialPhone = pendingPhone.ifBlank {
                        localPrefs.verifiedPhoneNumber.ifBlank { currentUser?.phoneNumber ?: "" }
                    },
                    onLogout = performLogout,
                )
            }
        }
        localPrefs.isPhoneSessionVerified -> {
            val phoneKey = localPrefs.verifiedPhoneNumber.ifBlank { pendingPhone.ifBlank { "local_phone" } }
            key(phoneKey) {
                LocalLedgerAppShell(
                    verifiedPhone = phoneKey,
                    initialOwnerName = pendingOwnerName.ifBlank { "Mohammad Emran Hawladar" },
                    initialBusinessName = pendingBusinessName.ifBlank { "ইমরান টি স্টল" },
                    onConnectGoogleCloud = connectGoogleCloud,
                    onLogout = performLogout,
                )
            }
        }
        else -> {
            AuthFlowScreen(
                onOtpVerifiedOnly = { owner, biz, phone ->
                    pendingOwnerName = owner
                    pendingBusinessName = biz
                    pendingPhone = phone
                },
                onGoogleSignInSuccess = { owner, biz, phone ->
                    pendingOwnerName = owner
                    pendingBusinessName = biz
                    pendingPhone = phone
                },
            )
        }
    }
}

@Composable
private fun AuthenticatedAppShell(
    currentUserId: String,
    initialOwnerName: String,
    initialBusinessName: String,
    initialPhone: String,
    onLogout: () -> Unit,
) {
    val viewModel: HisaberViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                HisaberViewModel(
                    repository = HisaberRepository(db),
                    currentUserId = currentUserId,
                    initialOwnerName = initialOwnerName,
                    initialBusinessName = initialBusinessName,
                    initialPhone = initialPhone,
                )
            }
        },
    )

    LedgerShellHost(
        sessionKey = currentUserId,
        controller = viewModel,
        isCloudConnected = true,
        onConnectGoogleCloud = {},
        onLogout = onLogout,
    )
}

@Composable
private fun LocalLedgerAppShell(
    verifiedPhone: String,
    initialOwnerName: String,
    initialBusinessName: String,
    onConnectGoogleCloud: () -> Unit,
    onLogout: () -> Unit,
) {
    val viewModel: LocalHisaberViewModel = viewModel(
        key = "local_$verifiedPhone",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                LocalHisaberViewModel(
                    context = app,
                    verifiedPhone = verifiedPhone,
                    initialOwnerName = initialOwnerName,
                    initialBusinessName = initialBusinessName,
                )
            }
        },
    )

    LedgerShellHost(
        sessionKey = "local_$verifiedPhone",
        controller = viewModel,
        isCloudConnected = false,
        onConnectGoogleCloud = onConnectGoogleCloud,
        onLogout = onLogout,
    )
}

@Composable
private fun LedgerShellHost(
    sessionKey: String,
    controller: HisaberLedgerController,
    isCloudConnected: Boolean,
    onConnectGoogleCloud: () -> Unit,
    onLogout: () -> Unit,
) {
    val uiState by controller.appState.collectAsStateWithLifecycle()
    val feedback by controller.feedbackMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var isPinUnlocked by rememberSaveable(sessionKey) { mutableStateOf(false) }

    LaunchedEffect(feedback) {
        if (feedback != null) {
            snackbarHostState.showSnackbar(feedback!!)
            controller.clearFeedback()
        }
    }

    when (val state = uiState) {
        is UiState.Loading -> LoadingStateBox("হিসাবের খাতা লোড হচ্ছে...")
        is UiState.Error -> {
            Box(modifier = Modifier.fillMaxSize().background(PureWhite)) {
                EmptyStateBox(
                    icon = Icons.Default.Notifications,
                    title = "ডাটা লোড করা যায়নি",
                    subtitle = state.message,
                    actionLabel = "লগআউট",
                    onAction = onLogout,
                )
            }
        }
        is UiState.Success -> {
            val data = state.data
            if (data.profile.pinLockEnabled && data.profile.pinHash.isNotBlank() && !isPinUnlocked) {
                PinLockScreen(
                    expectedPinHash = data.profile.pinHash,
                    onUnlocked = { isPinUnlocked = true },
                    onForgotPinLogout = onLogout,
                )
            } else {
                HisaberMainNavigation(
                    data = data,
                    viewModel = controller,
                    snackbarHostState = snackbarHostState,
                    isCloudConnected = isCloudConnected,
                    onConnectGoogleCloud = onConnectGoogleCloud,
                    onLogout = onLogout,
                )
            }
        }
    }
}

@Composable
private fun HisaberMainNavigation(
    data: HisaberAppData,
    viewModel: HisaberLedgerController,
    snackbarHostState: SnackbarHostState,
    isCloudConnected: Boolean,
    onConnectGoogleCloud: () -> Unit,
    onLogout: () -> Unit,
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val destRoute = backStackEntry?.destination?.route ?: ""

    val selectedBottomTab = when {
        destRoute.contains("Cashbook") -> BottomNavTab.CASHBOX
        destRoute.contains("QrSystem") -> BottomNavTab.QR_CODE
        destRoute.contains("HisabiPay") || destRoute.contains("PaymentGateway") -> BottomNavTab.TALIPAY
        destRoute.contains("More") -> BottomNavTab.MENU
        else -> BottomNavTab.TALI
    }

    val showBottomBar = destRoute.contains("Home") ||
        destRoute.contains("Customers") ||
        destRoute.contains("Cashbook") ||
        destRoute.contains("HisabiPay") ||
        destRoute.contains("More") ||
        destRoute.isEmpty()

    val drawerContext = LocalContext.current
    var showStaffDialog by remember { mutableStateOf(false) }
    var showLimitDialog by remember { mutableStateOf(false) }
    var showChargeDialog by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showBottomBar,
        drawerContent = {
            HishabiDrawerContent(
                data = data,
                onOpenBusinessInfo = {
                    scope.launch { drawerState.close() }
                    navController.navigate(AppRoute.BusinessInfo)
                },
                onOpenAccounts = {
                    scope.launch { drawerState.close() }
                    navController.navigate(AppRoute.MultiBusiness)
                },
                onOpenReports = { ownerMode ->
                    scope.launch { drawerState.close() }
                    navController.navigate(AppRoute.Reports(ownerMode = ownerMode))
                },
                onOpenSalesLedger = {
                    scope.launch { drawerState.close() }
                    navController.navigate(AppRoute.Home) {
                        popUpTo(AppRoute.Home) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onOpenExpenses = {
                    scope.launch { drawerState.close() }
                    navController.navigate(AppRoute.Cashbook)
                },
                onOpenDueLedger = {
                    scope.launch { drawerState.close() }
                    navController.navigate(AppRoute.Customers)
                },
                onOpenCashLedger = {
                    scope.launch { drawerState.close() }
                    navController.navigate(AppRoute.Cashbook)
                },
                onOpenSettings = {
                    scope.launch { drawerState.close() }
                    navController.navigate(AppRoute.Settings)
                },
                onOpenPermissionCenter = {
                    scope.launch { drawerState.close() }
                    navController.navigate(AppRoute.PermissionCenter)
                },
                onOpenGoldMembership = {
                    scope.launch { drawerState.close() }
                    navController.navigate(AppRoute.HisabiGold)
                },
                onOpenMyGoldPackage = {
                    scope.launch { drawerState.close() }
                    navController.navigate(AppRoute.MyGoldPackage)
                },
                onOpenSupportFaq = {
                    scope.launch { drawerState.close() }
                    navController.navigate(AppRoute.HelpCenter(startOnFaq = true))
                },
                onShowStaffView = {
                    scope.launch { drawerState.close() }
                    showStaffDialog = true
                },
                onShowTransactionLimits = {
                    scope.launch { drawerState.close() }
                    showLimitDialog = true
                },
                onShowServiceCharge = {
                    scope.launch { drawerState.close() }
                    showChargeDialog = true
                },
                onLogout = {
                    scope.launch { drawerState.close() }
                    onLogout()
                },
            )
        },
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (showBottomBar) {
                    HisaberBottomBar(
                        selectedTab = selectedBottomTab,
                        onSelectTali = {
                            navController.navigate(AppRoute.Home) {
                                popUpTo(AppRoute.Home) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onSelectCashbox = {
                            navController.navigate(AppRoute.Cashbook) {
                                popUpTo(AppRoute.Home)
                                launchSingleTop = true
                            }
                        },
                        onSelectQrCode = {
                            navController.navigate(AppRoute.QrSystem(initialTab = 0)) {
                                launchSingleTop = true
                            }
                        },
                        onSelectTalipay = {
                            navController.navigate(AppRoute.HisabiPay) {
                                popUpTo(AppRoute.Home)
                                launchSingleTop = true
                            }
                        },
                        onSelectMenu = {
                            navController.navigate(AppRoute.More) {
                                popUpTo(AppRoute.Home)
                                launchSingleTop = true
                            }
                        },
                    )
                }
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding()),
            ) {
                NavHost(
                    navController = navController,
                    startDestination = AppRoute.Home,
                ) {
                    composable<AppRoute.Home> {
                        HomeScreen(
                            data = data,
                            isCloudConnected = isCloudConnected,
                            onConnectGoogleCloud = onConnectGoogleCloud,
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onOpenNotifications = { navController.navigate(AppRoute.Inbox) },
                            onOpenHelpCenter = { navController.navigate(AppRoute.HelpCenter(startOnFaq = false)) },
                            onOpenMultiBusiness = { navController.navigate(AppRoute.MultiBusiness) },
                            onOpenGoldMembership = { navController.navigate(AppRoute.HisabiGold) },
                            onOpenInventory = { navController.navigate(AppRoute.Inventory) },
                            onOpenNotes = { navController.navigate(AppRoute.BusinessNotes) },
                            onOpenGroupTagada = { navController.navigate(AppRoute.SmsSender(groupMode = true)) },
                            onOpenBackupSettings = { navController.navigate(AppRoute.Settings) },
                            onOpenTallySms = { navController.navigate(AppRoute.SmsSender()) },
                            onOpenCashbook = {
                                navController.navigate(AppRoute.Cashbook) {
                                    popUpTo(AppRoute.Home)
                                    launchSingleTop = true
                                }
                            },
                            onOpenQr = { navController.navigate(AppRoute.QrSystem(0)) },
                            onAddCustomerClick = { navController.navigate(AppRoute.AddCustomer) },
                            onCustomerClick = { id -> navController.navigate(AppRoute.CustomerDetail(id)) },
                            onShowMessage = { msg -> viewModel.showFeedback(msg) },
                        )
                    }

                    composable<AppRoute.Customers> {
                        CustomersListScreen(
                            data = data,
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onCustomerClick = { id -> navController.navigate(AppRoute.CustomerDetail(id)) },
                            onAddCustomerClick = { navController.navigate(AppRoute.AddCustomer) },
                        )
                    }

                    composable<AppRoute.AddCustomer> {
                        val appContext = LocalContext.current
                        NewCustomerSupplierScreen(
                            onBack = { navController.popBackStack() },
                            onSaveCustomer = { isCustomer, name, mobileNumber, previousBalance, dateMillis, photoUri, attachmentUri ->
                                viewModel.addCustomer(
                                    businessId = data.profile.activeBusinessId,
                                    name = name,
                                    phone = mobileNumber,
                                    address = "",
                                    initialBalance = 0.0,
                                    notes = if (isCustomer) "Customer" else "Supplier",
                                ) { createdCustomerId ->
                                    if (photoUri != null) {
                                        CustomerPhotoStore.persistPhotoForCustomer(
                                            context = appContext,
                                            customerId = createdCustomerId,
                                            sourceUri = photoUri,
                                        )
                                    }
                                    if (previousBalance > 0.0) {
                                        viewModel.saveCustomerTransaction(
                                            businessId = data.profile.activeBusinessId,
                                            customerId = createdCustomerId,
                                            customerName = name,
                                            type = if (isCustomer) "gave" else "received",
                                            amount = previousBalance,
                                            description = "পুরোনো ব্যালেন্স",
                                            dateMillis = dateMillis,
                                            attachmentUri = attachmentUri,
                                            paymentMethod = "cash",
                                        ) {
                                            navController.popBackStack()
                                        }
                                    } else {
                                        navController.popBackStack()
                                    }
                                }
                            },
                        )
                    }

                    composable<AppRoute.CustomerDetail> { entry ->
                        val route = entry.toRoute<AppRoute.CustomerDetail>()
                        val customer = data.customers.find { it.id == route.customerId }
                        val custTx = data.transactions.filter { it.customerId == route.customerId }
                        CustomerDetailScreen(
                            customer = customer,
                            businessName = data.profile.businessName.ifBlank { "হিসাবি খাতা" },
                            transactions = custTx,
                            onBack = { navController.popBackStack() },
                            onSaveTransaction = { type, amt, desc, dateMillis, attachUri ->
                                viewModel.saveCustomerTransaction(
                                    businessId = data.profile.activeBusinessId,
                                    customerId = route.customerId,
                                    customerName = customer?.name ?: "",
                                    type = type,
                                    amount = amt,
                                    description = desc,
                                    dateMillis = dateMillis,
                                    attachmentUri = attachUri,
                                    paymentMethod = "cash",
                                )
                            },
                            onUpdateTransaction = { oldTx, newAmt, newDesc, newDate ->
                                viewModel.updateTransaction(oldTx, newAmt, newDesc, newDate)
                            },
                            onSendSms = {
                                navController.navigate(AppRoute.SmsSender(customerId = route.customerId))
                            },
                            onOpenQr = { navController.navigate(AppRoute.QrSystem(0)) },
                            onUpdateCustomer = { name, phone, address, notes ->
                                viewModel.updateCustomer(route.customerId, name, phone, address, notes)
                            },
                            onDeleteCustomer = {
                                viewModel.deleteCustomer(route.customerId) {
                                    navController.popBackStack()
                                }
                            },
                            onDeleteTransaction = { tx -> viewModel.deleteTransaction(tx) },
                            onShowMessage = { msg -> viewModel.showFeedback(msg) },
                            onOpenEditCustomer = { navController.navigate(AppRoute.EditCustomer(route.customerId)) },
                            onOpenHisabiGold = { navController.navigate(AppRoute.HisabiGold) },
                        )
                    }

                    composable<AppRoute.EditCustomer> { entry ->
                        val route = entry.toRoute<AppRoute.EditCustomer>()
                        val appContext = LocalContext.current
                        val customer = data.customers.find { it.id == route.customerId }
                        CustomerEditScreen(
                            customer = customer,
                            onBack = { navController.popBackStack() },
                            onSaveCustomer = { name, phone, address, notes, newPhotoUri ->
                                if (newPhotoUri != null) {
                                    CustomerPhotoStore.persistPhotoForCustomer(
                                        context = appContext,
                                        customerId = route.customerId,
                                        sourceUri = newPhotoUri,
                                    )
                                }
                                viewModel.updateCustomer(route.customerId, name, phone, address, notes)
                                viewModel.showFeedback("কাস্টমার তথ্য আপডেট হয়েছে")
                                navController.popBackStack()
                            },
                        )
                    }

                    composable<AppRoute.AddTransaction> { entry ->
                        val route = entry.toRoute<AppRoute.AddTransaction>()
                        AddTransactionScreen(
                            data = data,
                            initialCustomerId = route.customerId,
                            initialScope = route.entryScope,
                            onBack = { navController.popBackStack() },
                            onSaveCustomerTx = { custId, custName, type, amt, desc, dateMillis, attachUri, payMethod ->
                                viewModel.saveCustomerTransaction(
                                    businessId = data.profile.activeBusinessId,
                                    customerId = custId,
                                    customerName = custName,
                                    type = type,
                                    amount = amt,
                                    description = desc,
                                    dateMillis = dateMillis,
                                    attachmentUri = attachUri,
                                    paymentMethod = payMethod,
                                ) {
                                    navController.popBackStack()
                                }
                            },
                            onSaveCashbookTx = { type, amt, desc, dateMillis, attachUri, payMethod ->
                                viewModel.saveCashbookTransaction(
                                    businessId = data.profile.activeBusinessId,
                                    type = type,
                                    amount = amt,
                                    description = desc,
                                    dateMillis = dateMillis,
                                    attachmentUri = attachUri,
                                    paymentMethod = payMethod,
                                ) {
                                    navController.popBackStack()
                                }
                            },
                        )
                    }

                    composable<AppRoute.Cashbook> {
                        CashbookScreen(
                            data = data,
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onAddCashbookEntry = {
                                navController.navigate(AppRoute.AddTransaction(entryScope = "cashbook"))
                            },
                            onDeleteTransaction = { tx -> viewModel.deleteTransaction(tx) },
                        )
                    }

                    composable<AppRoute.HisabiPay> {
                        HisabiPayScreen(
                            data = data,
                            onOpenNotifications = { navController.navigate(AppRoute.Inbox) },
                            onOpenHelpCenter = { navController.navigate(AppRoute.HelpCenter(startOnFaq = false)) },
                            onOpenMultiBusiness = { navController.navigate(AppRoute.MultiBusiness) },
                            onOpenPaymentGateway = { providerId, title ->
                                navController.navigate(
                                    AppRoute.PaymentGateway(
                                        providerId = providerId,
                                        title = title,
                                    )
                                )
                            },
                            onOpenQr = { navController.navigate(AppRoute.QrSystem(initialTab = 0)) },
                        )
                    }

                    composable<AppRoute.More> {
                        MoreMenuScreen(
                            unreadNotifications = data.summary.unreadNotifications,
                            onNotificationClick = { navController.navigate(AppRoute.Inbox) },
                            onOpenPaymentGateway = { providerId, title ->
                                navController.navigate(AppRoute.PaymentGateway(providerId = providerId, title = title))
                            },
                            onOpenMyQr = { navController.navigate(AppRoute.QrSystem(initialTab = 0)) },
                            onOpenScanQr = { navController.navigate(AppRoute.QrSystem(initialTab = 1)) },
                            onOpenReports = { ownerMode -> navController.navigate(AppRoute.Reports(ownerMode = ownerMode)) },
                            onOpenHelpCenter = { navController.navigate(AppRoute.HelpCenter(startOnFaq = true)) },
                            onOpenSettings = { navController.navigate(AppRoute.Settings) },
                            onOpenPermissionCenter = { navController.navigate(AppRoute.PermissionCenter) },
                            onOpenBusinessInfo = { navController.navigate(AppRoute.BusinessInfo) },
                            onOpenGoldMembership = { navController.navigate(AppRoute.HisabiGold) },
                        )
                    }

                    composable<AppRoute.PaymentGateway> { entry ->
                        val route = entry.toRoute<AppRoute.PaymentGateway>()
                        PaymentGatewayScreen(
                            providerId = route.providerId,
                            title = route.title,
                            amount = route.amount,
                            smsPackageCount = route.smsPackageCount,
                            activateGold = route.activateGold,
                            goldPackageId = route.goldPackageId,
                            userId = data.profile.userId,
                            onBack = { navController.popBackStack() },
                            onGoldActivated = {
                                navController.navigate(AppRoute.MyGoldPackage) {
                                    popUpTo(AppRoute.GoldMembership) { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                            onRecordPaymentComplete = { provider, paidAmt, txType, gatewayTxId, isDemo, acct ->
                                viewModel.recordGatewayPayment(
                                    businessId = data.profile.activeBusinessId,
                                    providerId = provider.id,
                                    providerNameBn = provider.displayNameBn,
                                    title = route.title,
                                    amount = paidAmt,
                                    txType = txType,
                                    gatewayTxId = gatewayTxId,
                                    isDemo = isDemo,
                                    accountNumber = acct,
                                    smsPackageCount = route.smsPackageCount,
                                )
                            },
                        )
                    }

                    composable<AppRoute.SmsSender> { entry ->
                        val route = entry.toRoute<AppRoute.SmsSender>()
                        SmsSenderScreen(
                            data = data,
                            initialCustomerId = route.customerId,
                            initialGroupMode = route.groupMode,
                            onBack = { navController.popBackStack() },
                            onOpenPackages = { navController.navigate(AppRoute.SmsPackages) },
                            onLogSms = { custId, custName, phone, msg, msgType, status, schedMillis, decrement ->
                                viewModel.logSms(
                                    businessId = data.profile.activeBusinessId,
                                    customerId = custId,
                                    customerName = custName,
                                    phone = phone,
                                    message = msg,
                                    messageType = msgType,
                                    status = status,
                                    scheduledAtMillis = schedMillis,
                                    decrementBalance = decrement,
                                )
                            },
                        )
                    }

                    composable<AppRoute.SmsPackages> {
                        SmsPackagesScreen(
                            currentSmsBalance = data.profile.smsBalance,
                            packageHistory = data.transactions,
                            onBack = { navController.popBackStack() },
                            onBuyPackage = { pkgTitle, price, smsCount ->
                                navController.navigate(
                                    AppRoute.PaymentGateway(
                                        providerId = "bkash",
                                        title = pkgTitle,
                                        amount = price,
                                        smsPackageCount = smsCount,
                                    )
                                )
                            },
                        )
                    }

                    composable<AppRoute.Inventory> {
                        InventoryScreen(
                            data = data,
                            onBack = { navController.popBackStack() },
                            onAddProduct = { name, unit, buy, sell, qty, low ->
                                viewModel.addProduct(data.profile.activeBusinessId, name, unit, buy, sell, qty, low)
                            },
                            onAdjustStock = { prod, delta, reason -> viewModel.adjustProductStock(prod, delta, reason) },
                            onDeleteProduct = { id -> viewModel.deleteProduct(id) },
                        )
                    }

                    composable<AppRoute.BusinessNotes> {
                        BusinessNotesScreen(
                            data = data,
                            onBack = { navController.popBackStack() },
                            onSaveNote = { title, content, pinned, existing ->
                                viewModel.saveNote(data.profile.activeBusinessId, title, content, pinned, existing)
                            },
                            onDeleteNote = { id -> viewModel.deleteNote(id) },
                        )
                    }

                    composable<AppRoute.MultiBusiness> {
                        MultiBusinessScreen(
                            data = data,
                            onBack = { navController.popBackStack() },
                            onSwitchBusiness = { biz -> viewModel.switchBusiness(biz) },
                            onAddBusiness = { name, cat, phone, addr -> viewModel.addBusiness(name, cat, phone, addr) },
                        )
                    }

                    composable<AppRoute.QrSystem> { entry ->
                        val route = entry.toRoute<AppRoute.QrSystem>()
                        QrCodeScreen(
                            data = data,
                            initialTab = route.initialTab,
                            onBack = { navController.popBackStack() },
                            onRecordQrEvent = { custName, amt, payload, dir ->
                                viewModel.recordQrEvent(data.profile.activeBusinessId, custName, amt, payload, dir)
                            },
                            onNavigateToAddTxFromQr = { _, _ ->
                                navController.navigate(AppRoute.AddTransaction(entryScope = "cashbook"))
                            },
                        )
                    }

                    composable<AppRoute.Reports> { entry ->
                        val route = entry.toRoute<AppRoute.Reports>()
                        ReportsScreen(
                            data = data,
                            ownerMode = route.ownerMode,
                            onBack = { navController.popBackStack() },
                            onShowMessage = { msg -> viewModel.showFeedback(msg) },
                        )
                    }

                    composable<AppRoute.Inbox> {
                        InboxScreen(
                            notifications = data.notifications,
                            onBack = { navController.popBackStack() },
                            onMarkRead = { id -> viewModel.markNotificationRead(id) },
                            onMarkAllRead = { viewModel.markAllNotificationsRead(data.notifications) },
                        )
                    }

                    composable<AppRoute.HelpCenter> { entry ->
                        val route = entry.toRoute<AppRoute.HelpCenter>()
                        HelpCenterScreen(
                            appData = data,
                            tickets = data.tickets,
                            startOnFaq = route.startOnFaq,
                            onBack = { navController.popBackStack() },
                            onOpenSettings = { navController.navigate(AppRoute.Settings) },
                            onNavigateHome = {
                                navController.navigate(AppRoute.Home) {
                                    popUpTo(AppRoute.Home) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onNavigateLedger = { navController.navigate(AppRoute.Customers) },
                            onNavigateReports = { navController.navigate(AppRoute.Reports(ownerMode = false)) },
                            onNavigateProfile = { navController.navigate(AppRoute.BusinessInfo) },
                            onCreateTicket = { subj, desc -> viewModel.createSupportTicket(subj, desc) },
                        )
                    }

                    composable<AppRoute.Settings> {
                        SettingsScreen(
                            data = data,
                            onBack = { navController.popBackStack() },
                            onUpdateSettings = { updates, msg -> viewModel.updateProfileSettings(updates, msg) },
                            onOpenBusinessInfo = { navController.navigate(AppRoute.BusinessInfo) },
                            onOpenBusinessCategory = { navController.navigate(AppRoute.BusinessCategory) },
                            onOpenGoldMembership = { navController.navigate(AppRoute.HisabiGold) },
                            onOpenMultiBusiness = { navController.navigate(AppRoute.MultiBusiness) },
                            onOpenPermissionCenter = { navController.navigate(AppRoute.PermissionCenter) },
                            onOpenHelpCenter = { navController.navigate(AppRoute.HelpCenter(startOnFaq = false)) },
                            isCloudConnected = isCloudConnected,
                            onConnectGoogleCloud = onConnectGoogleCloud,
                            onLogout = onLogout,
                            onDeleteAccount = {
                                val oldLogo = HishabiPrefsStore.load(drawerContext).businessLogoPath
                                if (oldLogo.isNotBlank()) {
                                    runCatching { java.io.File(oldLogo).delete() }
                                }
                                HishabiPrefsStore.update(drawerContext) {
                                    HishabiLocalPrefs()
                                }
                                onLogout()
                            },
                        )
                    }

                    composable<AppRoute.PermissionCenter> {
                        PermissionCenterScreen(
                            onBack = { navController.popBackStack() },
                            onOpenLocationDetails = { navController.navigate(AppRoute.LocationPermission) },
                        )
                    }

                    composable<AppRoute.LocationPermission> {
                        LocationPermissionScreen(
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable<AppRoute.BusinessInfo> {
                        BusinessInfoScreen(
                            data = data,
                            onBack = { navController.popBackStack() },
                            onSelectCategoryClick = { navController.navigate(AppRoute.BusinessCategory) },
                            onSaveBusinessInfo = { updates, msg ->
                                viewModel.updateProfileSettings(updates, msg)
                            },
                        )
                    }

                    composable<AppRoute.BusinessCategory> {
                        BusinessCategoryScreen(
                            currentCategory = data.profile.businessCategory,
                            onClose = { navController.popBackStack() },
                            onConfirmCategory = { categoryTitle, _ ->
                                viewModel.updateProfileSettings(
                                    mapOf("businessCategory" to categoryTitle.take(60)),
                                    "ব্যবসার ধরন সংরক্ষিত হয়েছে: $categoryTitle",
                                )
                                navController.popBackStack()
                            },
                        )
                    }

                    composable<AppRoute.HisabiGold> {
                        HisabiGoldScreen(
                            onBack = { navController.popBackStack() },
                            onOpenGoldPackages = {
                                navController.navigate(AppRoute.GoldMembership) {
                                    launchSingleTop = true
                                }
                            },
                            onOpenMyGoldPackage = {
                                navController.navigate(AppRoute.MyGoldPackage) {
                                    launchSingleTop = true
                                }
                            },
                        )
                    }

                    composable<AppRoute.GoldMembership> {
                        GoldMembershipScreen(
                            onBack = { navController.popBackStack() },
                            onOpenMyGoldPackage = {
                                navController.navigate(AppRoute.MyGoldPackage) { launchSingleTop = true }
                            },
                            onProceedToPayment = { plan, providerId ->
                                navController.navigate(
                                    AppRoute.PaymentGateway(
                                        providerId = providerId,
                                        title = "গোল্ড প্যাকেজ • ${plan.bannerTitleBn}",
                                        amount = plan.priceTaka.toDouble(),
                                        smsPackageCount = 100,
                                        activateGold = true,
                                        goldPackageId = plan.id,
                                    )
                                )
                            },
                        )
                    }

                    composable<AppRoute.MyGoldPackage> {
                        MyGoldPackageScreen(
                            onBack = { navController.popBackStack() },
                            onGoHome = {
                                navController.navigate(AppRoute.Home) {
                                    popUpTo(AppRoute.Home) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onBuyOrRenewPackage = {
                                navController.navigate(AppRoute.GoldMembership) {
                                    launchSingleTop = true
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    if (showStaffDialog) {
        StaffViewDialog(
            businessName = data.profile.businessName.ifBlank { "হিসাবি খাতা" },
            customerCount = data.customers.size,
            onDismiss = { showStaffDialog = false },
            onOpenSettings = { navController.navigate(AppRoute.Settings) },
        )
    }

    if (showLimitDialog) {
        val localPrefs = remember { HishabiPrefsStore.load(drawerContext) }
        val dailyUsed = data.summary.todayIncome + data.summary.todayExpense
        val monthlyUsed = data.summary.totalIncomeAll + data.summary.totalExpenseAll
        TransactionLimitDialog(
            dailyUsed = dailyUsed,
            monthlyUsed = monthlyUsed,
            isGoldMember = localPrefs.goldMembershipActive,
            onDismiss = { showLimitDialog = false },
            onUpgradeGold = {
                showLimitDialog = false
                navController.navigate(AppRoute.HisabiGold)
            },
        )
    }

    if (showChargeDialog) {
        ServiceChargeDialog(onDismiss = { showChargeDialog = false })
    }
}
