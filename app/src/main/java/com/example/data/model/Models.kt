package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val ownerName: String = "",
    val businessName: String = "",
    val phone: String = "",
    val address: String = "",
    val businessCategory: String = "",
    val activeBusinessId: String = "",
    val smsBalance: Long = 0L,
    val language: String = "bn",
    val notificationsEnabled: Boolean = true,
    val lowStockAlertEnabled: Boolean = true,
    val autoReminderEnabled: Boolean = false,
    val pinLockEnabled: Boolean = false,
    val pinHash: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class Business(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val category: String = "",
    val phone: String = "",
    val address: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class Customer(
    @DocumentId val id: String = "",
    val userId: String = "",
    val businessId: String = "",
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val balance: Double = 0.0, // Positive = পাওনা (Receivable), Negative = দেনা (Payable)
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class LedgerTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val businessId: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val entryScope: String = "customer", // "customer" or "cashbook"
    val type: String = "gave", // "gave", "received", "income", "expense"
    val amount: Double = 0.0,
    val description: String = "",
    val dateMillis: Long = 0L,
    val attachmentUri: String = "",
    val paymentMethod: String = "cash", // "cash", "bkash", "nagad", "rocket", "bank", "qr"
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class Product(
    @DocumentId val id: String = "",
    val userId: String = "",
    val businessId: String = "",
    val name: String = "",
    val unit: String = "পিস",
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val quantity: Long = 0L,
    val lowStockThreshold: Long = 5L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    val isLowStock: Boolean get() = quantity <= lowStockThreshold
}

@IgnoreExtraProperties
data class InventoryLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val businessId: String = "",
    val productId: String = "",
    val productName: String = "",
    val changeQty: Long = 0L,
    val resultingQty: Long = 0L,
    val reason: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class SmsLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val businessId: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val phone: String = "",
    val message: String = "",
    val messageType: String = "tagada", // "tagada", "order", "custom", "group"
    val status: String = "sent", // "sent", "scheduled"
    val scheduledAtMillis: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class BusinessNote(
    @DocumentId val id: String = "",
    val userId: String = "",
    val businessId: String = "",
    val title: String = "",
    val content: String = "",
    val pinned: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class AppNotification(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val body: String = "",
    val category: String = "update", // "payment", "promo", "qr", "feature", "stock", "update"
    val isRead: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class SupportTicket(
    @DocumentId val id: String = "",
    val userId: String = "",
    val subject: String = "",
    val description: String = "",
    val status: String = "pending", // "pending", "answered", "closed"
    val reply: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class QrPaymentRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val businessId: String = "",
    val customerName: String = "",
    val amount: Double = 0.0,
    val qrPayload: String = "",
    val direction: String = "generated", // "generated", "scanned"
    @ServerTimestamp val createdAt: Timestamp? = null,
)
