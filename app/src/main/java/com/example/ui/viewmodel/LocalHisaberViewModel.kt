package com.example.ui.viewmodel

import android.content.Context
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
import com.example.util.Formatters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID
import kotlin.math.abs

/**
 * On-device persistent ledger ViewModel used when a user verifies login
 * via Mobile Number + OTP without connecting Google Cloud Sync yet.
 * Never touches Firestore while auth.currentUser == null.
 */
class LocalHisaberViewModel(
    context: Context,
    verifiedPhone: String,
    initialOwnerName: String = "Mohammad Emran Hawladar",
    initialBusinessName: String = "ইমরান টি স্টল",
) : ViewModel(), HisaberLedgerController {

    private val storageFile = File(context.applicationContext.filesDir, "hisaber_local_ledger_v1.json")
    private val localUid = "phone_${verifiedPhone.filter { it.isDigit() }.ifBlank { "user" }}"

    private var currentData: HisaberAppData = loadOrSeed(
        verifiedPhone = verifiedPhone,
        initialOwnerName = initialOwnerName.ifBlank { "Mohammad Emran Hawladar" },
        initialBusinessName = initialBusinessName.ifBlank { "ইমরান টি স্টল" },
    )

    private val _appState = MutableStateFlow<UiState<HisaberAppData>>(UiState.Success(recompute(currentData)))
    override val appState: StateFlow<UiState<HisaberAppData>> = _appState.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    override val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    override fun clearFeedback() {
        _feedbackMessage.value = null
    }

    override fun showFeedback(msg: String) {
        _feedbackMessage.value = msg
    }

    private fun commit(next: HisaberAppData, feedback: String? = null) {
        currentData = recompute(next)
        _appState.value = UiState.Success(currentData)
        if (feedback != null) _feedbackMessage.value = feedback
        val snapshot = currentData
        viewModelScope.launch(Dispatchers.IO) {
            saveToDisk(snapshot)
        }
    }

    private fun recompute(raw: HisaberAppData): HisaberAppData {
        val activeBizId = raw.profile.activeBusinessId
        val bizCustomers = if (activeBizId.isBlank()) raw.customers else raw.customers.filter { it.businessId == activeBizId }
        val bizTx = if (activeBizId.isBlank()) raw.transactions else raw.transactions.filter { it.businessId == activeBizId }
        val bizProducts = if (activeBizId.isBlank()) raw.products else raw.products.filter { it.businessId == activeBizId }
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
        val unreadCount = raw.notifications.count { !it.isRead }

        return raw.copy(
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
            )
        )
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
        val id = UUID.randomUUID().toString()
        val bizId = businessId.ifBlank { currentData.profile.activeBusinessId }
        val customer = Customer(
            id = id,
            userId = localUid,
            businessId = bizId,
            name = name.trim(),
            phone = phone.trim(),
            address = address.trim(),
            balance = initialBalance,
            notes = notes.trim(),
        )
        commit(
            currentData.copy(customers = listOf(customer) + currentData.customers),
            feedback = "কাস্টমার যোগ করা হয়েছে",
        )
        onDone(id)
    }

    override fun updateCustomer(
        customerId: String,
        name: String,
        phone: String,
        address: String,
        notes: String,
        onDone: () -> Unit,
    ) {
        val updated = currentData.customers.map {
            if (it.id == customerId) {
                it.copy(name = name.trim(), phone = phone.trim(), address = address.trim(), notes = notes.trim())
            } else it
        }
        commit(currentData.copy(customers = updated), feedback = "কাস্টমার তথ্য আপডেট হয়েছে")
        onDone()
    }

    override fun deleteCustomer(customerId: String, onDone: () -> Unit) {
        commit(
            currentData.copy(
                customers = currentData.customers.filterNot { it.id == customerId },
                transactions = currentData.transactions.filterNot { it.customerId == customerId },
            ),
            feedback = "কাস্টমার মুছে ফেলা হয়েছে",
        )
        onDone()
    }

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
    ) {
        val delta = if (type == "gave") amount else -amount
        val tx = LedgerTransaction(
            id = UUID.randomUUID().toString(),
            userId = localUid,
            businessId = businessId.ifBlank { currentData.profile.activeBusinessId },
            customerId = customerId,
            customerName = customerName,
            entryScope = "customer",
            type = type,
            amount = amount,
            description = description,
            dateMillis = dateMillis,
            attachmentUri = attachmentUri,
            paymentMethod = paymentMethod,
        )
        val updatedCustomers = currentData.customers.map {
            if (it.id == customerId) it.copy(balance = it.balance + delta) else it
        }
        commit(
            currentData.copy(
                customers = updatedCustomers,
                transactions = listOf(tx) + currentData.transactions,
            ),
            feedback = "লেনদেন সংরক্ষণ করা হয়েছে",
        )
        onDone()
    }

    override fun saveCashbookTransaction(
        businessId: String,
        type: String,
        amount: Double,
        description: String,
        dateMillis: Long,
        attachmentUri: String,
        paymentMethod: String,
        onDone: () -> Unit,
    ) {
        val tx = LedgerTransaction(
            id = UUID.randomUUID().toString(),
            userId = localUid,
            businessId = businessId.ifBlank { currentData.profile.activeBusinessId },
            entryScope = "cashbook",
            type = type,
            amount = amount,
            description = description,
            dateMillis = dateMillis,
            attachmentUri = attachmentUri,
            paymentMethod = paymentMethod,
        )
        commit(
            currentData.copy(transactions = listOf(tx) + currentData.transactions),
            feedback = "ক্যাশবুক এন্ট্রি সংরক্ষণ হয়েছে",
        )
        onDone()
    }

    override fun deleteTransaction(tx: LedgerTransaction) {
        val updatedCustomers = if (tx.entryScope == "customer" && tx.customerId.isNotBlank()) {
            val revertDelta = if (tx.type == "gave") -tx.amount else tx.amount
            currentData.customers.map {
                if (it.id == tx.customerId) it.copy(balance = it.balance + revertDelta) else it
            }
        } else currentData.customers

        commit(
            currentData.copy(
                customers = updatedCustomers,
                transactions = currentData.transactions.filterNot { it.id == tx.id },
            ),
            feedback = "লেনদেন মুছে ফেলা হয়েছে",
        )
    }

    override fun updateTransaction(
        oldTx: LedgerTransaction,
        newAmount: Double,
        newDescription: String,
        newDateMillis: Long,
        onDone: () -> Unit,
    ) {
        val diff = newAmount - oldTx.amount
        val balanceDelta = if (oldTx.type == "gave") diff else -diff
        val updatedCustomers = if (oldTx.entryScope == "customer" && oldTx.customerId.isNotBlank()) {
            currentData.customers.map {
                if (it.id == oldTx.customerId) it.copy(balance = it.balance + balanceDelta) else it
            }
        } else currentData.customers

        val updatedTx = currentData.transactions.map {
            if (it.id == oldTx.id) {
                it.copy(amount = newAmount, description = newDescription, dateMillis = newDateMillis)
            } else it
        }
        commit(
            currentData.copy(customers = updatedCustomers, transactions = updatedTx),
            feedback = "লেনদেন আপডেট করা হয়েছে",
        )
        onDone()
    }

    override fun addProduct(
        businessId: String,
        name: String,
        unit: String,
        purchasePrice: Double,
        sellingPrice: Double,
        quantity: Long,
        lowStockThreshold: Long,
        onDone: () -> Unit,
    ) {
        val product = Product(
            id = UUID.randomUUID().toString(),
            userId = localUid,
            businessId = businessId.ifBlank { currentData.profile.activeBusinessId },
            name = name,
            unit = unit,
            purchasePrice = purchasePrice,
            sellingPrice = sellingPrice,
            quantity = quantity,
            lowStockThreshold = lowStockThreshold,
        )
        commit(
            currentData.copy(products = listOf(product) + currentData.products),
            feedback = "পণ্য স্টকে যোগ হয়েছে",
        )
        onDone()
    }

    override fun adjustProductStock(product: Product, delta: Long, reason: String) {
        val nextQty = (product.quantity + delta).coerceAtLeast(0L)
        val updatedProducts = currentData.products.map {
            if (it.id == product.id) it.copy(quantity = nextQty) else it
        }
        val log = InventoryLog(
            id = UUID.randomUUID().toString(),
            userId = localUid,
            businessId = product.businessId,
            productId = product.id,
            productName = product.name,
            changeQty = delta,
            resultingQty = nextQty,
            reason = reason,
        )
        commit(
            currentData.copy(products = updatedProducts, inventoryLogs = listOf(log) + currentData.inventoryLogs),
            feedback = "স্টক আপডেট হয়েছে",
        )
    }

    override fun deleteProduct(productId: String) {
        commit(
            currentData.copy(products = currentData.products.filterNot { it.id == productId }),
            feedback = "পণ্য মুছে ফেলা হয়েছে",
        )
    }

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
    ) {
        val log = SmsLog(
            id = UUID.randomUUID().toString(),
            userId = localUid,
            businessId = businessId.ifBlank { currentData.profile.activeBusinessId },
            customerId = customerId,
            customerName = customerName,
            phone = phone,
            message = message,
            messageType = messageType,
            status = status,
            scheduledAtMillis = scheduledAtMillis,
        )
        val nextBalance = if (decrementBalance && currentData.profile.smsBalance > 0L) {
            currentData.profile.smsBalance - 1L
        } else {
            currentData.profile.smsBalance
        }
        commit(
            currentData.copy(
                profile = currentData.profile.copy(smsBalance = nextBalance),
                smsLogs = listOf(log) + currentData.smsLogs,
            ),
            feedback = if (status == "scheduled") "তাগাদা সময় নির্ধারণ করা হয়েছে" else "SMS রেকর্ড সংরক্ষণ হয়েছে",
        )
        onDone()
    }

    override fun addBusiness(
        name: String,
        category: String,
        phone: String,
        address: String,
        onDone: () -> Unit,
    ) {
        val biz = Business(
            id = UUID.randomUUID().toString(),
            userId = localUid,
            name = name,
            category = category,
            phone = phone,
            address = address,
        )
        commit(
            currentData.copy(
                businesses = currentData.businesses + biz,
                profile = currentData.profile.copy(
                    activeBusinessId = biz.id,
                    businessName = biz.name,
                    businessCategory = biz.category,
                ),
            ),
            feedback = "নতুন ব্যবসা যোগ ও সক্রিয় হয়েছে",
        )
        onDone()
    }

    override fun switchBusiness(business: Business) {
        commit(
            currentData.copy(
                profile = currentData.profile.copy(
                    activeBusinessId = business.id,
                    businessName = business.name,
                    businessCategory = business.category,
                ),
            ),
            feedback = "ব্যবসা পরিবর্তন হয়েছে: ${business.name}",
        )
    }

    override fun saveNote(
        businessId: String,
        title: String,
        content: String,
        pinned: Boolean,
        existing: BusinessNote?,
        onDone: () -> Unit,
    ) {
        val nextNotes = if (existing != null) {
            currentData.notes.map {
                if (it.id == existing.id) it.copy(title = title, content = content, pinned = pinned) else it
            }
        } else {
            listOf(
                BusinessNote(
                    id = UUID.randomUUID().toString(),
                    userId = localUid,
                    businessId = businessId.ifBlank { currentData.profile.activeBusinessId },
                    title = title,
                    content = content,
                    pinned = pinned,
                )
            ) + currentData.notes
        }
        commit(currentData.copy(notes = nextNotes), feedback = "নোট সংরক্ষণ করা হয়েছে")
        onDone()
    }

    override fun deleteNote(noteId: String) {
        commit(
            currentData.copy(notes = currentData.notes.filterNot { it.id == noteId }),
            feedback = "নোট মুছে ফেলা হয়েছে",
        )
    }

    override fun markNotificationRead(notifId: String) {
        commit(
            currentData.copy(
                notifications = currentData.notifications.map {
                    if (it.id == notifId) it.copy(isRead = true) else it
                }
            )
        )
    }

    override fun markAllNotificationsRead(notifications: List<AppNotification>) {
        commit(
            currentData.copy(
                notifications = currentData.notifications.map { it.copy(isRead = true) }
            )
        )
    }

    override fun createSupportTicket(subject: String, description: String, onDone: () -> Unit) {
        val ticket = SupportTicket(
            id = UUID.randomUUID().toString(),
            userId = localUid,
            subject = subject,
            description = description,
            status = "pending",
        )
        commit(
            currentData.copy(tickets = listOf(ticket) + currentData.tickets),
            feedback = "সাপোর্ট টিকিট জমা হয়েছে",
        )
        onDone()
    }

    override fun recordQrEvent(
        businessId: String,
        customerName: String,
        amount: Double,
        payload: String,
        direction: String,
    ) {
        val record = QrPaymentRecord(
            id = UUID.randomUUID().toString(),
            userId = localUid,
            businessId = businessId.ifBlank { currentData.profile.activeBusinessId },
            customerName = customerName,
            amount = amount,
            qrPayload = payload,
            direction = direction,
        )
        commit(currentData.copy(qrHistory = listOf(record) + currentData.qrHistory))
    }

    override fun updateProfileSettings(updates: Map<String, Any>, message: String) {
        var p = currentData.profile
        (updates["ownerName"] as? String)?.let { p = p.copy(ownerName = it) }
        (updates["businessName"] as? String)?.let { p = p.copy(businessName = it) }
        (updates["phone"] as? String)?.let { p = p.copy(phone = it) }
        (updates["address"] as? String)?.let { p = p.copy(address = it) }
        (updates["businessCategory"] as? String)?.let { p = p.copy(businessCategory = it) }
        (updates["language"] as? String)?.let { p = p.copy(language = it) }
        (updates["notificationsEnabled"] as? Boolean)?.let { p = p.copy(notificationsEnabled = it) }
        (updates["lowStockAlertEnabled"] as? Boolean)?.let { p = p.copy(lowStockAlertEnabled = it) }
        (updates["autoReminderEnabled"] as? Boolean)?.let { p = p.copy(autoReminderEnabled = it) }
        (updates["pinLockEnabled"] as? Boolean)?.let { p = p.copy(pinLockEnabled = it) }
        (updates["pinHash"] as? String)?.let { p = p.copy(pinHash = it) }
        commit(currentData.copy(profile = p), feedback = message)
    }

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
    ) {
        val tx = LedgerTransaction(
            id = UUID.randomUUID().toString(),
            userId = localUid,
            businessId = businessId.ifBlank { currentData.profile.activeBusinessId },
            entryScope = "cashbook",
            type = txType,
            amount = amount,
            description = "$title ($providerNameBn • $gatewayTxId)",
            dateMillis = System.currentTimeMillis(),
            paymentMethod = providerId,
        )
        val nextSmsBalance = currentData.profile.smsBalance + smsPackageCount.toLong()
        val msg = when {
            smsPackageCount > 0 -> "ডেমো পেমেন্ট সফল! $smsPackageCount SMS ব্যালেন্সে যোগ হয়েছে"
            isDemo -> "ডেমো পেমেন্ট সফলভাবে ক্যাশবুকে সংরক্ষিত হয়েছে"
            else -> "পেমেন্ট সফলভাবে ক্যাশবুকে সংরক্ষিত হয়েছে"
        }
        commit(
            currentData.copy(
                profile = currentData.profile.copy(smsBalance = nextSmsBalance),
                transactions = listOf(tx) + currentData.transactions,
            ),
            feedback = msg,
        )
        onDone()
    }

    private fun loadOrSeed(
        verifiedPhone: String,
        initialOwnerName: String,
        initialBusinessName: String,
    ): HisaberAppData {
        runCatching {
            if (storageFile.exists()) {
                val json = JSONObject(storageFile.readText())
                val profJson = json.getJSONObject("profile")
                val profile = UserProfile(
                    id = localUid,
                    userId = localUid,
                    ownerName = profJson.optString("ownerName", initialOwnerName),
                    businessName = profJson.optString("businessName", initialBusinessName),
                    phone = verifiedPhone.ifBlank { profJson.optString("phone", "") },
                    address = profJson.optString("address", ""),
                    businessCategory = profJson.optString("businessCategory", "সাধারণ ব্যবসা"),
                    activeBusinessId = profJson.optString("activeBusinessId", "biz_default"),
                    smsBalance = profJson.optLong("smsBalance", 0L),
                    language = profJson.optString("language", "bn"),
                    notificationsEnabled = profJson.optBoolean("notificationsEnabled", true),
                    lowStockAlertEnabled = profJson.optBoolean("lowStockAlertEnabled", true),
                    autoReminderEnabled = profJson.optBoolean("autoReminderEnabled", false),
                    pinLockEnabled = profJson.optBoolean("pinLockEnabled", false),
                    pinHash = profJson.optString("pinHash", ""),
                )
                val businesses = parseArray(json.optJSONArray("businesses")) { o ->
                    Business(
                        id = o.optString("id"),
                        userId = localUid,
                        name = o.optString("name"),
                        category = o.optString("category"),
                        phone = o.optString("phone"),
                        address = o.optString("address"),
                    )
                }
                val customers = parseArray(json.optJSONArray("customers")) { o ->
                    Customer(
                        id = o.optString("id"),
                        userId = localUid,
                        businessId = o.optString("businessId"),
                        name = o.optString("name"),
                        phone = o.optString("phone"),
                        address = o.optString("address"),
                        balance = o.optDouble("balance", 0.0),
                        notes = o.optString("notes"),
                    )
                }
                val transactions = parseArray(json.optJSONArray("transactions")) { o ->
                    LedgerTransaction(
                        id = o.optString("id"),
                        userId = localUid,
                        businessId = o.optString("businessId"),
                        customerId = o.optString("customerId"),
                        customerName = o.optString("customerName"),
                        entryScope = o.optString("entryScope", "customer"),
                        type = o.optString("type", "gave"),
                        amount = o.optDouble("amount", 0.0),
                        description = o.optString("description"),
                        dateMillis = o.optLong("dateMillis"),
                        attachmentUri = o.optString("attachmentUri"),
                        paymentMethod = o.optString("paymentMethod", "cash"),
                    )
                }
                val products = parseArray(json.optJSONArray("products")) { o ->
                    Product(
                        id = o.optString("id"),
                        userId = localUid,
                        businessId = o.optString("businessId"),
                        name = o.optString("name"),
                        unit = o.optString("unit", "পিস"),
                        purchasePrice = o.optDouble("purchasePrice", 0.0),
                        sellingPrice = o.optDouble("sellingPrice", 0.0),
                        quantity = o.optLong("quantity", 0L),
                        lowStockThreshold = o.optLong("lowStockThreshold", 5L),
                    )
                }
                val smsLogs = parseArray(json.optJSONArray("smsLogs")) { o ->
                    SmsLog(
                        id = o.optString("id"),
                        userId = localUid,
                        businessId = o.optString("businessId"),
                        customerId = o.optString("customerId"),
                        customerName = o.optString("customerName"),
                        phone = o.optString("phone"),
                        message = o.optString("message"),
                        messageType = o.optString("messageType", "tagada"),
                        status = o.optString("status", "sent"),
                        scheduledAtMillis = o.optLong("scheduledAtMillis"),
                    )
                }
                val notes = parseArray(json.optJSONArray("notes")) { o ->
                    BusinessNote(
                        id = o.optString("id"),
                        userId = localUid,
                        businessId = o.optString("businessId"),
                        title = o.optString("title"),
                        content = o.optString("content"),
                        pinned = o.optBoolean("pinned", false),
                    )
                }
                return HisaberAppData(
                    profile = profile,
                    businesses = businesses.ifEmpty {
                        listOf(
                            Business(
                                id = profile.activeBusinessId,
                                userId = localUid,
                                name = profile.businessName,
                                category = profile.businessCategory,
                                phone = profile.phone,
                            )
                        )
                    },
                    customers = customers,
                    transactions = transactions,
                    products = products,
                    smsLogs = smsLogs,
                    notes = notes,
                )
            }
        }

        val defaultBizId = "biz_default"
        val seeded = HisaberAppData(
            profile = UserProfile(
                id = localUid,
                userId = localUid,
                ownerName = initialOwnerName,
                businessName = initialBusinessName,
                phone = verifiedPhone,
                businessCategory = "সাধারণ ব্যবসা",
                activeBusinessId = defaultBizId,
                smsBalance = 0L,
                language = "bn",
            ),
            businesses = listOf(
                Business(
                    id = defaultBizId,
                    userId = localUid,
                    name = initialBusinessName,
                    category = "সাধারণ ব্যবসা",
                    phone = verifiedPhone,
                )
            ),
            notifications = listOf(
                AppNotification(
                    id = "welcome_otp",
                    userId = localUid,
                    title = "হিসাবের খাতায় স্বাগতম!",
                    body = "মোবাইল নম্বর ($verifiedPhone) OTP ভেরিফিকেশন সফল হয়েছে।",
                    category = "update",
                    isRead = false,
                )
            ),
        )
        saveToDisk(seeded)
        return seeded
    }

    private fun saveToDisk(data: HisaberAppData) {
        runCatching {
            val root = JSONObject().apply {
                put("profile", JSONObject().apply {
                    put("ownerName", data.profile.ownerName)
                    put("businessName", data.profile.businessName)
                    put("phone", data.profile.phone)
                    put("address", data.profile.address)
                    put("businessCategory", data.profile.businessCategory)
                    put("activeBusinessId", data.profile.activeBusinessId)
                    put("smsBalance", data.profile.smsBalance)
                    put("language", data.profile.language)
                    put("notificationsEnabled", data.profile.notificationsEnabled)
                    put("lowStockAlertEnabled", data.profile.lowStockAlertEnabled)
                    put("autoReminderEnabled", data.profile.autoReminderEnabled)
                    put("pinLockEnabled", data.profile.pinLockEnabled)
                    put("pinHash", data.profile.pinHash)
                })
                put("businesses", JSONArray(data.businesses.map { b ->
                    JSONObject().apply {
                        put("id", b.id)
                        put("name", b.name)
                        put("category", b.category)
                        put("phone", b.phone)
                        put("address", b.address)
                    }
                }))
                put("customers", JSONArray(data.customers.map { c ->
                    JSONObject().apply {
                        put("id", c.id)
                        put("businessId", c.businessId)
                        put("name", c.name)
                        put("phone", c.phone)
                        put("address", c.address)
                        put("balance", c.balance)
                        put("notes", c.notes)
                    }
                }))
                put("transactions", JSONArray(data.transactions.map { t ->
                    JSONObject().apply {
                        put("id", t.id)
                        put("businessId", t.businessId)
                        put("customerId", t.customerId)
                        put("customerName", t.customerName)
                        put("entryScope", t.entryScope)
                        put("type", t.type)
                        put("amount", t.amount)
                        put("description", t.description)
                        put("dateMillis", t.dateMillis)
                        put("attachmentUri", t.attachmentUri)
                        put("paymentMethod", t.paymentMethod)
                    }
                }))
                put("products", JSONArray(data.products.map { p ->
                    JSONObject().apply {
                        put("id", p.id)
                        put("businessId", p.businessId)
                        put("name", p.name)
                        put("unit", p.unit)
                        put("purchasePrice", p.purchasePrice)
                        put("sellingPrice", p.sellingPrice)
                        put("quantity", p.quantity)
                        put("lowStockThreshold", p.lowStockThreshold)
                    }
                }))
                put("smsLogs", JSONArray(data.smsLogs.map { s ->
                    JSONObject().apply {
                        put("id", s.id)
                        put("businessId", s.businessId)
                        put("customerId", s.customerId)
                        put("customerName", s.customerName)
                        put("phone", s.phone)
                        put("message", s.message)
                        put("messageType", s.messageType)
                        put("status", s.status)
                        put("scheduledAtMillis", s.scheduledAtMillis)
                    }
                }))
                put("notes", JSONArray(data.notes.map { n ->
                    JSONObject().apply {
                        put("id", n.id)
                        put("businessId", n.businessId)
                        put("title", n.title)
                        put("content", n.content)
                        put("pinned", n.pinned)
                    }
                }))
            }
            storageFile.writeText(root.toString())
        }
    }

    private inline fun <T> parseArray(arr: JSONArray?, mapper: (JSONObject) -> T): List<T> {
        if (arr == null) return emptyList()
        val list = ArrayList<T>(arr.length())
        for (i in 0 until arr.length()) {
            arr.optJSONObject(i)?.let { list.add(mapper(it)) }
        }
        return list
    }
}
