package com.example.ui.viewmodel

import com.example.data.model.AppNotification
import com.example.data.model.Business
import com.example.data.model.BusinessNote
import com.example.data.model.LedgerTransaction
import com.example.data.model.Product
import kotlinx.coroutines.flow.StateFlow

interface HisaberLedgerController {
    val appState: StateFlow<UiState<HisaberAppData>>
    val feedbackMessage: StateFlow<String?>

    fun clearFeedback()
    fun showFeedback(msg: String)

    fun addCustomer(
        businessId: String,
        name: String,
        phone: String,
        address: String,
        initialBalance: Double,
        notes: String,
        onDone: (String) -> Unit = {},
    )

    fun updateCustomer(
        customerId: String,
        name: String,
        phone: String,
        address: String,
        notes: String,
        onDone: () -> Unit = {},
    )

    fun deleteCustomer(customerId: String, onDone: () -> Unit = {})

    fun saveCustomerTransaction(
        businessId: String,
        customerId: String,
        customerName: String,
        type: String,
        amount: Double,
        description: String,
        dateMillis: Long,
        attachmentUri: String,
        paymentMethod: String,
        onDone: () -> Unit = {},
    )

    fun saveCashbookTransaction(
        businessId: String,
        type: String,
        amount: Double,
        description: String,
        dateMillis: Long,
        attachmentUri: String,
        paymentMethod: String,
        onDone: () -> Unit = {},
    )

    fun deleteTransaction(tx: LedgerTransaction)

    fun updateTransaction(
        oldTx: LedgerTransaction,
        newAmount: Double,
        newDescription: String,
        newDateMillis: Long,
        onDone: () -> Unit = {},
    )

    fun addProduct(
        businessId: String,
        name: String,
        unit: String,
        purchasePrice: Double,
        sellingPrice: Double,
        quantity: Long,
        lowStockThreshold: Long,
        onDone: () -> Unit = {},
    )

    fun adjustProductStock(product: Product, delta: Long, reason: String)
    fun deleteProduct(productId: String)

    fun logSms(
        businessId: String,
        customerId: String,
        customerName: String,
        phone: String,
        message: String,
        messageType: String,
        status: String,
        scheduledAtMillis: Long = 0L,
        decrementBalance: Boolean = false,
        onDone: () -> Unit = {},
    )

    fun addBusiness(
        name: String,
        category: String,
        phone: String,
        address: String,
        onDone: () -> Unit = {},
    )

    fun switchBusiness(business: Business)

    fun saveNote(
        businessId: String,
        title: String,
        content: String,
        pinned: Boolean,
        existing: BusinessNote? = null,
        onDone: () -> Unit = {},
    )

    fun deleteNote(noteId: String)
    fun markNotificationRead(notifId: String)
    fun markAllNotificationsRead(notifications: List<AppNotification>)
    fun createSupportTicket(subject: String, description: String, onDone: () -> Unit = {})

    fun recordQrEvent(
        businessId: String,
        customerName: String,
        amount: Double,
        payload: String,
        direction: String,
    )

    fun updateProfileSettings(
        updates: Map<String, Any>,
        message: String = "সেটিংস সংরক্ষণ করা হয়েছে",
    )

    fun recordGatewayPayment(
        businessId: String,
        providerId: String,
        providerNameBn: String,
        title: String,
        amount: Double,
        txType: String,
        gatewayTxId: String,
        isDemo: Boolean,
        accountNumber: String = "",
        smsPackageCount: Int = 0,
        onDone: () -> Unit = {},
    )
}
