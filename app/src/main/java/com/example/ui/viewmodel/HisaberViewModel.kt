package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppNotification
import com.example.data.model.Business
import com.example.data.model.BusinessNote
import com.example.data.model.Customer
import com.example.data.model.InventoryLog
import com.example.data.model.LedgerTransaction
import com.example.data.model.Product
import com.example.data.model.QrPaymentRecord
import com.example.data.model.SmsLog
import com.example.data.model.SupportTicket
import com.example.data.model.UserProfile
import com.example.data.repository.HisaberRepository
import com.example.util.Formatters
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.abs

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class DashboardSummary(
    val totalPaona: Double = 0.0,
    val totalDena: Double = 0.0,
    val customerCount: Int = 0,
    val todayIncome: Double = 0.0,
    val todayExpense: Double = 0.0,
    val totalIncomeAll: Double = 0.0,
    val totalExpenseAll: Double = 0.0,
    val lowStockCount: Int = 0,
    val unreadNotifications: Int = 0,
)

data class HisaberAppData(
    val profile: UserProfile = UserProfile(),
    val businesses: List<Business> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val transactions: List<LedgerTransaction> = emptyList(),
    val products: List<Product> = emptyList(),
    val inventoryLogs: List<InventoryLog> = emptyList(),
    val smsLogs: List<SmsLog> = emptyList(),
    val notes: List<BusinessNote> = emptyList(),
    val notifications: List<AppNotification> = emptyList(),
    val tickets: List<SupportTicket> = emptyList(),
    val qrHistory: List<QrPaymentRecord> = emptyList(),
    val summary: DashboardSummary = DashboardSummary(),
)

class HisaberViewModel(
    private val repository: HisaberRepository,
    private val currentUserId: String,
    initialOwnerName: String = "",
    initialBusinessName: String = "",
    initialPhone: String = "",
) : ViewModel(), HisaberLedgerController {

    private val _busy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    override val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureUserProfileExists(
                ownerName = initialOwnerName.ifBlank { "Mohammad Emran Hawladar" },
                businessName = initialBusinessName.ifBlank { "ইমরান টি স্টল" },
                phone = initialPhone,
            )
        }
    }

    private fun Throwable.unwrapFirestoreMessage(fallback: String): String {
        val firestoreError = generateSequence(this) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && this is CancellationException) throw this
        val root = firestoreError ?: this
        Log.w(TAG, fallback, root)
        return root.message ?: fallback
    }

    private val coreFlow = combine(
        repository.observeUserProfile(currentUserId),
        repository.observeBusinesses(currentUserId),
        repository.observeCustomers(currentUserId),
        repository.observeTransactions(currentUserId),
        repository.observeProducts(currentUserId),
    ) { profile, businesses, customers, transactions, products ->
        CoreBundle(profile ?: UserProfile(userId = currentUserId), businesses, customers, transactions, products)
    }

    private val secondaryFlow = combine(
        repository.observeInventoryLogs(currentUserId),
        repository.observeSmsLogs(currentUserId),
        repository.observeNotes(currentUserId),
        repository.observeNotifications(currentUserId),
        repository.observeSupportTickets(currentUserId),
        repository.observeQrHistory(currentUserId),
    ) { arr ->
        @Suppress("UNCHECKED_CAST")
        SecondaryBundle(
            inventoryLogs = arr[0] as List<InventoryLog>,
            smsLogs = arr[1] as List<SmsLog>,
            notes = arr[2] as List<BusinessNote>,
            notifications = arr[3] as List<AppNotification>,
            tickets = arr[4] as List<SupportTicket>,
            qrHistory = arr[5] as List<QrPaymentRecord>,
        )
    }

    override val appState: StateFlow<UiState<HisaberAppData>> = combine(coreFlow, secondaryFlow) { core, sec ->
        val activeBizId = core.profile.activeBusinessId
        val bizCustomers = if (activeBizId.isBlank()) core.customers else core.customers.filter { it.businessId == activeBizId }
        val bizTx = if (activeBizId.isBlank()) core.transactions else core.transactions.filter { it.businessId == activeBizId }
        val bizProducts = if (activeBizId.isBlank()) core.products else core.products.filter { it.businessId == activeBizId }
        val bizInvLogs = if (activeBizId.isBlank()) sec.inventoryLogs else sec.inventoryLogs.filter { it.businessId == activeBizId }
        val bizSms = if (activeBizId.isBlank()) sec.smsLogs else sec.smsLogs.filter { it.businessId == activeBizId }
        val bizNotes = if (activeBizId.isBlank()) sec.notes else sec.notes.filter { it.businessId == activeBizId }
        val bizQr = if (activeBizId.isBlank()) sec.qrHistory else sec.qrHistory.filter { it.businessId == activeBizId }

        val totalPaona = bizCustomers.filter { it.balance > 0.0 }.sumOf { it.balance }
        val totalDena = bizCustomers.filter { it.balance < 0.0 }.sumOf { abs(it.balance) }
        val todayMillis = System.currentTimeMillis()
        val todayIncome = bizTx.filter {
            Formatters.isSameDay(it.dateMillis, todayMillis) && (it.type == "received" || it.type == "income")
        }.sumOf { it.amount }
        val todayExpense = bizTx.filter {
            Formatters.isSameDay(it.dateMillis, todayMillis) && (it.type == "gave" || it.type == "expense")
        }.sumOf { it.amount }
        val totalIncomeAll = bizTx.filter { it.type == "received" || it.type == "income" }.sumOf { it.amount }
        val totalExpenseAll = bizTx.filter { it.type == "gave" || it.type == "expense" }.sumOf { it.amount }
        val lowStockCount = bizProducts.count { it.isLowStock }
        val unreadCount = sec.notifications.count { !it.isRead }

        UiState.Success(
            HisaberAppData(
                profile = core.profile,
                businesses = core.businesses,
                customers = bizCustomers,
                transactions = bizTx,
                products = bizProducts,
                inventoryLogs = bizInvLogs,
                smsLogs = bizSms,
                notes = bizNotes,
                notifications = sec.notifications,
                tickets = sec.tickets,
                qrHistory = bizQr,
                summary = DashboardSummary(
                    totalPaona = totalPaona,
                    totalDena = totalDena,
                    customerCount = bizCustomers.size,
                    todayIncome = todayIncome,
                    todayExpense = todayExpense,
                    totalIncomeAll = totalIncomeAll,
                    totalExpenseAll = totalExpenseAll,
                    lowStockCount = lowStockCount,
                    unreadNotifications = unreadCount,
                ),
            )
        ) as UiState<HisaberAppData>
    }.catch { error ->
        emit(UiState.Error(error.unwrapFirestoreMessage("ডাটা লোড করতে সমস্যা হয়েছে")))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = UiState.Loading,
    )

    override fun clearFeedback() {
        _feedbackMessage.value = null
    }

    override fun showFeedback(msg: String) {
        _feedbackMessage.value = msg
    }

    private fun runMutation(successMessage: String? = null, block: suspend () -> Result<Any?>, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _busy.value = true
            val res = block()
            _busy.value = false
            res.onSuccess {
                if (successMessage != null) _feedbackMessage.value = successMessage
                onSuccess()
            }.onFailure { err ->
                _feedbackMessage.value = err.localizedMessage ?: "অপারেশন ব্যর্থ হয়েছে"
            }
        }
    }

    override fun addCustomer(
        businessId: String,
        name: String,
        phone: String,
        address: String,
        initialBalance: Double,
        notes: String,
        onDone: (String) -> Unit,
    ) {
        viewModelScope.launch {
            _busy.value = true
            val res = repository.addCustomer(businessId, name, phone, address, initialBalance, notes)
            _busy.value = false
            res.onSuccess { customerId ->
                _feedbackMessage.value = "কাস্টমার যোগ করা হয়েছে"
                onDone(customerId)
            }.onFailure { err ->
                _feedbackMessage.value = err.localizedMessage ?: "অপারেশন ব্যর্থ হয়েছে"
            }
        }
    }

    override fun updateCustomer(
        customerId: String,
        name: String,
        phone: String,
        address: String,
        notes: String,
        onDone: () -> Unit,
    ) = runMutation("কাস্টমার তথ্য আপডেট হয়েছে", {
        repository.updateCustomer(customerId, name, phone, address, notes)
    }, onDone)

    override fun deleteCustomer(customerId: String, onDone: () -> Unit) = runMutation("কাস্টমার মুছে ফেলা হয়েছে", {
        repository.deleteCustomer(customerId)
    }, onDone)

    override fun saveCustomerTransaction(
        businessId: String,
        customerId: String,
        customerName: String,
        type: String,
        amount: Double,
        description: String,
        dateMillis: Long,
        attachmentUri: String,
        paymentMethod: String,
        onDone: () -> Unit,
    ) = runMutation("লেনদেন সংরক্ষণ করা হয়েছে", {
        repository.addCustomerTransaction(
            businessId, customerId, customerName, type, amount, description, dateMillis, attachmentUri, paymentMethod
        )
    }, onDone)

    override fun saveCashbookTransaction(
        businessId: String,
        type: String,
        amount: Double,
        description: String,
        dateMillis: Long,
        attachmentUri: String,
        paymentMethod: String,
        onDone: () -> Unit,
    ) = runMutation("ক্যাশবুক এন্ট্রি সংরক্ষণ হয়েছে", {
        repository.addCashbookTransaction(businessId, type, amount, description, dateMillis, attachmentUri, paymentMethod)
    }, onDone)

    override fun deleteTransaction(tx: LedgerTransaction) = runMutation("লেনদেন মুছে ফেলা হয়েছে", {
        repository.deleteTransaction(tx)
    })

    override fun updateTransaction(
        oldTx: LedgerTransaction,
        newAmount: Double,
        newDescription: String,
        newDateMillis: Long,
        onDone: () -> Unit,
    ) = runMutation("লেনদেন আপডেট করা হয়েছে", {
        repository.updateCustomerTransaction(oldTx, newAmount, newDescription, newDateMillis)
    }, onDone)

    override fun addProduct(
        businessId: String,
        name: String,
        unit: String,
        purchasePrice: Double,
        sellingPrice: Double,
        quantity: Long,
        lowStockThreshold: Long,
        onDone: () -> Unit,
    ) = runMutation("পণ্য স্টকে যোগ হয়েছে", {
        repository.addProduct(businessId, name, unit, purchasePrice, sellingPrice, quantity, lowStockThreshold)
    }, onDone)

    override fun adjustProductStock(product: Product, delta: Long, reason: String) = runMutation("স্টক আপডেট হয়েছে", {
        repository.adjustProductStock(product, delta, reason)
    })

    override fun deleteProduct(productId: String) = runMutation("পণ্য মুছে ফেলা হয়েছে", {
        repository.deleteProduct(productId)
    })

    override fun logSms(
        businessId: String,
        customerId: String,
        customerName: String,
        phone: String,
        message: String,
        messageType: String,
        status: String,
        scheduledAtMillis: Long,
        decrementBalance: Boolean,
        onDone: () -> Unit,
    ) = runMutation(
        if (status == "scheduled") "তাগাদা সময় নির্ধারণ করা হয়েছে" else "SMS রেকর্ড সংরক্ষণ হয়েছে",
        {
            repository.logSmsAndOptionalDecrement(
                businessId, customerId, customerName, phone, message, messageType, status, scheduledAtMillis, decrementBalance
            )
        },
        onDone,
    )

    override fun addBusiness(name: String, category: String, phone: String, address: String, onDone: () -> Unit) =
        runMutation("নতুন ব্যবসা যোগ ও সক্রিয় হয়েছে", {
            repository.addBusiness(name, category, phone, address, setAsActive = true)
        }, onDone)

    override fun switchBusiness(business: Business) = runMutation("ব্যবসা পরিবর্তন হয়েছে: ${business.name}", {
        repository.switchActiveBusiness(business)
    })

    override fun saveNote(businessId: String, title: String, content: String, pinned: Boolean, existing: BusinessNote?, onDone: () -> Unit) =
        runMutation("নোট সংরক্ষণ করা হয়েছে", {
            repository.saveNote(businessId, title, content, pinned, existing)
        }, onDone)

    override fun deleteNote(noteId: String) = runMutation("নোট মুছে ফেলা হয়েছে", {
        repository.deleteNote(noteId)
    })

    override fun markNotificationRead(notifId: String) {
        viewModelScope.launch { repository.markNotificationRead(notifId) }
    }

    override fun markAllNotificationsRead(notifications: List<AppNotification>) {
        viewModelScope.launch { repository.markAllNotificationsRead(notifications) }
    }

    override fun createSupportTicket(subject: String, description: String, onDone: () -> Unit) =
        runMutation("সাপোর্ট টিকিট জমা হয়েছে", {
            repository.createSupportTicket(subject, description)
        }, onDone)

    override fun recordQrEvent(businessId: String, customerName: String, amount: Double, payload: String, direction: String) {
        viewModelScope.launch {
            repository.recordQrEvent(businessId, customerName, amount, payload, direction)
        }
    }

    override fun updateProfileSettings(updates: Map<String, Any>, message: String) =
        runMutation(message, {
            repository.updateUserProfileFields(updates)
        })

    override fun recordGatewayPayment(
        businessId: String,
        providerId: String,
        providerNameBn: String,
        title: String,
        amount: Double,
        txType: String,
        gatewayTxId: String,
        isDemo: Boolean,
        accountNumber: String,
        smsPackageCount: Int,
        onDone: () -> Unit,
    ) = runMutation(
        if (smsPackageCount > 0) {
            "ডেমো পেমেন্ট সফল! $smsPackageCount SMS ব্যালেন্সে যোগ হয়েছে"
        } else if (isDemo) {
            "ডেমো পেমেন্ট সফলভাবে ক্যাশবুকে সংরক্ষিত হয়েছে"
        } else {
            "পেমেন্ট সফলভাবে ক্যাশবুকে সংরক্ষিত হয়েছে"
        },
        {
            repository.recordGatewayPayment(
                businessId = businessId,
                providerId = providerId,
                providerNameBn = providerNameBn,
                title = title,
                amount = amount,
                txType = txType,
                gatewayTxId = gatewayTxId,
                isDemo = isDemo,
                accountNumber = accountNumber,
                smsPackageCount = smsPackageCount,
            )
        },
        onDone,
    )

    private data class CoreBundle(
        val profile: UserProfile,
        val businesses: List<Business>,
        val customers: List<Customer>,
        val transactions: List<LedgerTransaction>,
        val products: List<Product>,
    )

    private data class SecondaryBundle(
        val inventoryLogs: List<InventoryLog>,
        val smsLogs: List<SmsLog>,
        val notes: List<BusinessNote>,
        val notifications: List<AppNotification>,
        val tickets: List<SupportTicket>,
        val qrHistory: List<QrPaymentRecord>,
    )

    private companion object {
        const val TAG = "HisaberVM"
    }
}
