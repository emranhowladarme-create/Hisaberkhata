package com.example.data.repository

import android.content.Context
import com.example.R
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
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class HisaberRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth,
) {
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        auth = Firebase.auth,
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("ব্যবহারকারী লগইন করা নেই।")
    }

    private fun userDoc(uid: String) = db.collection("users").document(uid)

    private fun <T> Flow<T>.handleStreamError(
        uid: String,
        operationType: OperationType,
        path: String,
    ): Flow<T> = catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, operationType, path)
        }
        throw error
    }

    // --- User Profile & Bootstrap ---

    fun observeUserProfile(uid: String): Flow<UserProfile?> = flow {
        val docRef = userDoc(uid)
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            UserProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else null
                }
                .handleStreamError(uid, OperationType.GET, docRef.path)
        )
    }

    suspend fun ensureUserProfileExists(
        ownerName: String,
        businessName: String,
        phone: String = "",
    ): Result<UserProfile> = runCatching {
        val uid = requireUserId()
        val uRef = userDoc(uid)
        val existingSnap = uRef.get().await()
        if (existingSnap.exists()) {
            val profile = existingSnap.toObject(
                UserProfile::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            if (profile != null) {
                val updates = mutableMapOf<String, Any>()
                var updatedProfile = profile
                if (profile.ownerName.isBlank() || profile.ownerName == "ব্যবসায়ী" || profile.ownerName == "Md Emran Hawladar") {
                    updates["ownerName"] = "Mohammad Emran Hawladar"
                    updatedProfile = updatedProfile.copy(ownerName = "Mohammad Emran Hawladar")
                }
                if (profile.businessName.isBlank() || profile.businessName == "আমার ব্যবসা") {
                    updates["businessName"] = "ইমরান টি স্টল"
                    updatedProfile = updatedProfile.copy(businessName = "ইমরান টি স্টল")
                }
                if (updates.isNotEmpty()) {
                    updates["updatedAt"] = FieldValue.serverTimestamp()
                    runCatching { uRef.update(updates).await() }
                    return@runCatching updatedProfile
                }
                return@runCatching profile
            }
        }

        val bizRef = uRef.collection("businesses").document()
        val notifRef = uRef.collection("notifications").document()
        val cleanOwner = ownerName.ifBlank { "Mohammad Emran Hawladar" }.take(80)
        val cleanBiz = businessName.ifBlank { "ইমরান টি স্টল" }.take(100)

        val batch = db.batch()
        batch.set(
            bizRef,
            mapOf(
                "userId" to uid,
                "name" to cleanBiz,
                "category" to "সাধারণ ব্যবসা",
                "phone" to phone.take(20),
                "address" to "",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            )
        )
        batch.set(
            uRef,
            mapOf(
                "userId" to uid,
                "ownerName" to cleanOwner,
                "businessName" to cleanBiz,
                "phone" to phone.take(20),
                "address" to "",
                "businessCategory" to "সাধারণ ব্যবসা",
                "activeBusinessId" to bizRef.id,
                "smsBalance" to 0L,
                "language" to "bn",
                "notificationsEnabled" to true,
                "lowStockAlertEnabled" to true,
                "autoReminderEnabled" to false,
                "pinLockEnabled" to false,
                "pinHash" to "",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            )
        )
        batch.set(
            notifRef,
            mapOf(
                "userId" to uid,
                "title" to "হিসাবের খাতায় স্বাগতম!",
                "body" to "আপনার ব্যবসার ডিজিটাল খাতা সফলভাবে তৈরি হয়েছে। কাস্টমার ও দৈনিক হিসাব যোগ করা শুরু করুন।",
                "category" to "update",
                "isRead" to false,
                "createdAt" to FieldValue.serverTimestamp(),
            )
        )
        batch.commit().await()

        UserProfile(
            id = uid,
            userId = uid,
            ownerName = cleanOwner,
            businessName = cleanBiz,
            phone = phone,
            activeBusinessId = bizRef.id,
            smsBalance = 0L,
            language = "bn",
        )
    }.onFailure {
        handleFirestoreError(it, OperationType.WRITE, "users/${auth.currentUser?.uid}")
    }

    suspend fun updateUserProfileFields(updates: Map<String, Any>): Result<Unit> = runCatching {
        val uid = requireUserId()
        val uRef = userDoc(uid)
        val mutable = updates.toMutableMap()
        mutable["updatedAt"] = FieldValue.serverTimestamp()
        uRef.update(mutable).await()
        Unit
    }.onFailure {
        handleFirestoreError(it, OperationType.UPDATE, "users/${auth.currentUser?.uid}")
    }

    // --- Businesses (Multi-Business) ---

    fun observeBusinesses(uid: String): Flow<List<Business>> = flow {
        val col = userDoc(uid).collection("businesses")
        emitAll(
            col.snapshots()
                .map { snap ->
                    snap.toObjects(
                        Business::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .handleStreamError(uid, OperationType.LIST, col.path)
        )
    }

    suspend fun addBusiness(
        name: String,
        category: String,
        phone: String,
        address: String,
        setAsActive: Boolean = true,
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val uRef = userDoc(uid)
        val bizRef = uRef.collection("businesses").document()
        val batch = db.batch()
        batch.set(
            bizRef,
            mapOf(
                "userId" to uid,
                "name" to name.trim().take(100),
                "category" to category.trim().take(60),
                "phone" to phone.trim().take(20),
                "address" to address.trim().take(200),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            )
        )
        if (setAsActive) {
            batch.update(
                uRef,
                mapOf(
                    "activeBusinessId" to bizRef.id,
                    "businessName" to name.trim().take(100),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            )
        }
        batch.commit().await()
        bizRef.id
    }.onFailure {
        handleFirestoreError(it, OperationType.CREATE, "users/${auth.currentUser?.uid}/businesses")
    }

    suspend fun switchActiveBusiness(business: Business): Result<Unit> = runCatching {
        val uid = requireUserId()
        userDoc(uid).update(
            mapOf(
                "activeBusinessId" to business.id,
                "businessName" to business.name,
                "updatedAt" to FieldValue.serverTimestamp(),
            )
        ).await()
        Unit
    }.onFailure {
        handleFirestoreError(it, OperationType.UPDATE, "users/${auth.currentUser?.uid}")
    }

    // --- Customers ---

    fun observeCustomers(uid: String): Flow<List<Customer>> = flow {
        val col = userDoc(uid).collection("customers")
        emitAll(
            col.snapshots()
                .map { snap ->
                    snap.toObjects(
                        Customer::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
                }
                .handleStreamError(uid, OperationType.LIST, col.path)
        )
    }

    suspend fun addCustomer(
        businessId: String,
        name: String,
        phone: String,
        address: String,
        initialBalance: Double = 0.0,
        notes: String = "",
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = userDoc(uid).collection("customers").document()
        docRef.set(
            mapOf(
                "userId" to uid,
                "businessId" to businessId,
                "name" to name.trim().take(100),
                "phone" to phone.trim().take(20),
                "address" to address.trim().take(200),
                "balance" to initialBalance,
                "notes" to notes.trim().take(300),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            )
        ).await()
        docRef.id
    }.onFailure {
        handleFirestoreError(it, OperationType.CREATE, "users/${auth.currentUser?.uid}/customers")
    }

    suspend fun updateCustomer(
        customerId: String,
        name: String,
        phone: String,
        address: String,
        notes: String,
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = userDoc(uid).collection("customers").document(customerId)
        docRef.update(
            mapOf(
                "name" to name.trim().take(100),
                "phone" to phone.trim().take(20),
                "address" to address.trim().take(200),
                "notes" to notes.trim().take(300),
                "updatedAt" to FieldValue.serverTimestamp(),
            )
        ).await()
        Unit
    }.onFailure {
        handleFirestoreError(it, OperationType.UPDATE, "users/${auth.currentUser?.uid}/customers/$customerId")
    }

    suspend fun deleteCustomer(customerId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = userDoc(uid).collection("customers").document(customerId)
        docRef.delete().await()
        Unit
    }.onFailure {
        handleFirestoreError(it, OperationType.DELETE, "users/${auth.currentUser?.uid}/customers/$customerId")
    }

    // --- Transactions (Customer & Cashbook) ---

    fun observeTransactions(uid: String): Flow<List<LedgerTransaction>> = flow {
        val col = userDoc(uid).collection("transactions")
        emitAll(
            col.snapshots()
                .map { snap ->
                    snap.toObjects(
                        LedgerTransaction::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.dateMillis }
                }
                .handleStreamError(uid, OperationType.LIST, col.path)
        )
    }

    suspend fun addCustomerTransaction(
        businessId: String,
        customerId: String,
        customerName: String,
        type: String, // "gave" (+balance পাওনা) or "received" (-balance জমা)
        amount: Double,
        description: String,
        dateMillis: Long,
        attachmentUri: String = "",
        paymentMethod: String = "cash",
    ): Result<String> = runCatching {
        require(amount > 0.0) { "টাকার পরিমাণ শূন্যের বেশি হতে হবে" }
        val uid = requireUserId()
        val custRef = userDoc(uid).collection("customers").document(customerId)
        val txRef = userDoc(uid).collection("transactions").document()

        db.runTransaction { transaction ->
            val custSnap = transaction.get(custRef)
            val currentBal = custSnap.getDouble("balance") ?: 0.0
            val delta = if (type == "gave") amount else -amount
            val newBal = currentBal + delta

            transaction.set(
                txRef,
                mapOf(
                    "userId" to uid,
                    "businessId" to businessId,
                    "customerId" to customerId,
                    "customerName" to customerName.take(100),
                    "entryScope" to "customer",
                    "type" to type,
                    "amount" to amount,
                    "description" to description.trim().take(300),
                    "dateMillis" to dateMillis,
                    "attachmentUri" to attachmentUri.take(500),
                    "paymentMethod" to paymentMethod,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            )
            transaction.update(
                custRef,
                mapOf(
                    "balance" to newBal,
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            )
            txRef.id
        }.await()
    }.onFailure {
        handleFirestoreError(it, OperationType.WRITE, "users/${auth.currentUser?.uid}/transactions")
    }

    suspend fun addCashbookTransaction(
        businessId: String,
        type: String, // "income" or "expense"
        amount: Double,
        description: String,
        dateMillis: Long,
        attachmentUri: String = "",
        paymentMethod: String = "cash",
    ): Result<String> = runCatching {
        require(amount > 0.0) { "টাকার পরিমাণ শূন্যের বেশি হতে হবে" }
        val uid = requireUserId()
        val txRef = userDoc(uid).collection("transactions").document()
        txRef.set(
            mapOf(
                "userId" to uid,
                "businessId" to businessId,
                "customerId" to "",
                "customerName" to "",
                "entryScope" to "cashbook",
                "type" to type,
                "amount" to amount,
                "description" to description.trim().take(300),
                "dateMillis" to dateMillis,
                "attachmentUri" to attachmentUri.take(500),
                "paymentMethod" to paymentMethod,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            )
        ).await()
        txRef.id
    }.onFailure {
        handleFirestoreError(it, OperationType.CREATE, "users/${auth.currentUser?.uid}/transactions")
    }

    suspend fun deleteTransaction(tx: LedgerTransaction): Result<Unit> = runCatching {
        val uid = requireUserId()
        val txRef = userDoc(uid).collection("transactions").document(tx.id)
        if (tx.entryScope == "customer" && tx.customerId.isNotBlank()) {
            val custRef = userDoc(uid).collection("customers").document(tx.customerId)
            db.runTransaction { transaction ->
                val custSnap = transaction.get(custRef)
                if (custSnap.exists()) {
                    val currentBal = custSnap.getDouble("balance") ?: 0.0
                    val reverseDelta = if (tx.type == "gave") -tx.amount else tx.amount
                    transaction.update(
                        custRef,
                        mapOf(
                            "balance" to (currentBal + reverseDelta),
                            "updatedAt" to FieldValue.serverTimestamp(),
                        )
                    )
                }
                transaction.delete(txRef)
            }.await()
        } else {
            txRef.delete().await()
        }
        Unit
    }.onFailure {
        handleFirestoreError(it, OperationType.DELETE, "users/${auth.currentUser?.uid}/transactions/${tx.id}")
    }

    suspend fun updateCustomerTransaction(
        oldTx: LedgerTransaction,
        newAmount: Double,
        newDescription: String,
        newDateMillis: Long,
    ): Result<Unit> = runCatching {
        require(newAmount > 0.0) { "টাকার পরিমাণ শূন্যের বেশি হতে হবে" }
        val uid = requireUserId()
        val txRef = userDoc(uid).collection("transactions").document(oldTx.id)
        if (oldTx.entryScope == "customer" && oldTx.customerId.isNotBlank()) {
            val custRef = userDoc(uid).collection("customers").document(oldTx.customerId)
            db.runTransaction { transaction ->
                val custSnap = transaction.get(custRef)
                if (custSnap.exists()) {
                    val currentBal = custSnap.getDouble("balance") ?: 0.0
                    val oldSigned = if (oldTx.type == "gave") oldTx.amount else -oldTx.amount
                    val newSigned = if (oldTx.type == "gave") newAmount else -newAmount
                    val delta = newSigned - oldSigned
                    transaction.update(
                        custRef,
                        mapOf(
                            "balance" to (currentBal + delta),
                            "updatedAt" to FieldValue.serverTimestamp(),
                        )
                    )
                }
                transaction.update(
                    txRef,
                    mapOf(
                        "amount" to newAmount,
                        "description" to newDescription.trim().take(300),
                        "dateMillis" to newDateMillis,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                )
            }.await()
        } else {
            txRef.update(
                mapOf(
                    "amount" to newAmount,
                    "description" to newDescription.trim().take(300),
                    "dateMillis" to newDateMillis,
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            ).await()
        }
        Unit
    }.onFailure {
        handleFirestoreError(it, OperationType.UPDATE, "users/${auth.currentUser?.uid}/transactions/${oldTx.id}")
    }

    // --- Inventory (Products & Logs) ---

    fun observeProducts(uid: String): Flow<List<Product>> = flow {
        val col = userDoc(uid).collection("products")
        emitAll(
            col.snapshots()
                .map { snap ->
                    snap.toObjects(
                        Product::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedBy { it.name }
                }
                .handleStreamError(uid, OperationType.LIST, col.path)
        )
    }

    fun observeInventoryLogs(uid: String): Flow<List<InventoryLog>> = flow {
        val col = userDoc(uid).collection("inventoryLogs")
        emitAll(
            col.snapshots()
                .map { snap ->
                    snap.toObjects(
                        InventoryLog::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .handleStreamError(uid, OperationType.LIST, col.path)
        )
    }

    suspend fun addProduct(
        businessId: String,
        name: String,
        unit: String,
        purchasePrice: Double,
        sellingPrice: Double,
        quantity: Long,
        lowStockThreshold: Long,
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val uRef = userDoc(uid)
        val prodRef = uRef.collection("products").document()
        val logRef = uRef.collection("inventoryLogs").document()

        val batch = db.batch()
        batch.set(
            prodRef,
            mapOf(
                "userId" to uid,
                "businessId" to businessId,
                "name" to name.trim().take(100),
                "unit" to unit.trim().ifBlank { "পিস" }.take(30),
                "purchasePrice" to purchasePrice.coerceAtLeast(0.0),
                "sellingPrice" to sellingPrice.coerceAtLeast(0.0),
                "quantity" to quantity.coerceAtLeast(0L),
                "lowStockThreshold" to lowStockThreshold.coerceAtLeast(0L),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            )
        )
        batch.set(
            logRef,
            mapOf(
                "userId" to uid,
                "businessId" to businessId,
                "productId" to prodRef.id,
                "productName" to name.trim().take(100),
                "changeQty" to quantity.coerceAtLeast(0L),
                "resultingQty" to quantity.coerceAtLeast(0L),
                "reason" to "নতুন পণ্য যোগ",
                "createdAt" to FieldValue.serverTimestamp(),
            )
        )
        if (quantity <= lowStockThreshold) {
            val notifRef = uRef.collection("notifications").document()
            batch.set(
                notifRef,
                mapOf(
                    "userId" to uid,
                    "title" to "লো-স্টক সতর্কতা: ${name.trim().take(60)}",
                    "body" to "পণ্যটির বর্তমান স্টক মাত্র $quantity ${unit.ifBlank { "পিস" }}। দ্রুত স্টক আপডেট করুন।",
                    "category" to "stock",
                    "isRead" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                )
            )
        }
        batch.commit().await()
        prodRef.id
    }.onFailure {
        handleFirestoreError(it, OperationType.CREATE, "users/${auth.currentUser?.uid}/products")
    }

    suspend fun adjustProductStock(
        product: Product,
        delta: Long,
        reason: String,
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val uRef = userDoc(uid)
        val prodRef = uRef.collection("products").document(product.id)
        val logRef = uRef.collection("inventoryLogs").document()
        val newQty = (product.quantity + delta).coerceAtLeast(0L)

        val batch = db.batch()
        batch.update(
            prodRef,
            mapOf(
                "quantity" to newQty,
                "updatedAt" to FieldValue.serverTimestamp(),
            )
        )
        batch.set(
            logRef,
            mapOf(
                "userId" to uid,
                "businessId" to product.businessId,
                "productId" to product.id,
                "productName" to product.name.take(100),
                "changeQty" to delta,
                "resultingQty" to newQty,
                "reason" to reason.ifBlank { "স্টক পরিবর্তন" }.take(100),
                "createdAt" to FieldValue.serverTimestamp(),
            )
        )
        if (newQty <= product.lowStockThreshold && product.quantity > product.lowStockThreshold) {
            val notifRef = uRef.collection("notifications").document()
            batch.set(
                notifRef,
                mapOf(
                    "userId" to uid,
                    "title" to "স্টক কমে গেছে: ${product.name.take(60)}",
                    "body" to "${product.name} এর স্টক $newQty ${product.unit} এ নেমে এসেছে।",
                    "category" to "stock",
                    "isRead" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                )
            )
        }
        batch.commit().await()
        Unit
    }.onFailure {
        handleFirestoreError(it, OperationType.UPDATE, "users/${auth.currentUser?.uid}/products/${product.id}")
    }

    suspend fun deleteProduct(productId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        userDoc(uid).collection("products").document(productId).delete().await()
        Unit
    }.onFailure {
        handleFirestoreError(it, OperationType.DELETE, "users/${auth.currentUser?.uid}/products/$productId")
    }

    // --- SMS System ---

    fun observeSmsLogs(uid: String): Flow<List<SmsLog>> = flow {
        val col = userDoc(uid).collection("smsLogs")
        emitAll(
            col.snapshots()
                .map { snap ->
                    snap.toObjects(
                        SmsLog::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .handleStreamError(uid, OperationType.LIST, col.path)
        )
    }

    suspend fun logSmsAndOptionalDecrement(
        businessId: String,
        customerId: String,
        customerName: String,
        phone: String,
        message: String,
        messageType: String,
        status: String,
        scheduledAtMillis: Long = 0L,
        decrementBalance: Boolean = false,
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val uRef = userDoc(uid)
        val smsRef = uRef.collection("smsLogs").document()

        db.runTransaction { tx ->
            if (decrementBalance) {
                val uSnap = tx.get(uRef)
                val bal = uSnap.getLong("smsBalance") ?: 0L
                if (bal > 0L) {
                    tx.update(
                        uRef,
                        mapOf(
                            "smsBalance" to (bal - 1L),
                            "updatedAt" to FieldValue.serverTimestamp(),
                        )
                    )
                }
            }
            tx.set(
                smsRef,
                mapOf(
                    "userId" to uid,
                    "businessId" to businessId,
                    "customerId" to customerId.take(128),
                    "customerName" to customerName.trim().take(100),
                    "phone" to phone.trim().take(25),
                    "message" to message.trim().take(320),
                    "messageType" to messageType,
                    "status" to status,
                    "scheduledAtMillis" to scheduledAtMillis,
                    "createdAt" to FieldValue.serverTimestamp(),
                )
            )
            smsRef.id
        }.await()
    }.onFailure {
        handleFirestoreError(it, OperationType.CREATE, "users/${auth.currentUser?.uid}/smsLogs")
    }

    // --- Business Notes ---

    fun observeNotes(uid: String): Flow<List<BusinessNote>> = flow {
        val col = userDoc(uid).collection("notes")
        emitAll(
            col.snapshots()
                .map { snap ->
                    snap.toObjects(
                        BusinessNote::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedWith(
                        compareByDescending<BusinessNote> { it.pinned }
                            .thenByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
                    )
                }
                .handleStreamError(uid, OperationType.LIST, col.path)
        )
    }

    suspend fun saveNote(
        businessId: String,
        title: String,
        content: String,
        pinned: Boolean,
        existingNote: BusinessNote? = null,
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val col = userDoc(uid).collection("notes")
        if (existingNote == null) {
            col.document().set(
                mapOf(
                    "userId" to uid,
                    "businessId" to businessId,
                    "title" to title.trim().take(100),
                    "content" to content.trim().take(1000),
                    "pinned" to pinned,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            ).await()
        } else {
            col.document(existingNote.id).update(
                mapOf(
                    "title" to title.trim().take(100),
                    "content" to content.trim().take(1000),
                    "pinned" to pinned,
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            ).await()
        }
        Unit
    }.onFailure {
        handleFirestoreError(it, OperationType.WRITE, "users/${auth.currentUser?.uid}/notes")
    }

    suspend fun deleteNote(noteId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        userDoc(uid).collection("notes").document(noteId).delete().await()
        Unit
    }.onFailure {
        handleFirestoreError(it, OperationType.DELETE, "users/${auth.currentUser?.uid}/notes/$noteId")
    }

    // --- Notifications (Inbox) ---

    fun observeNotifications(uid: String): Flow<List<AppNotification>> = flow {
        val col = userDoc(uid).collection("notifications")
        emitAll(
            col.snapshots()
                .map { snap ->
                    snap.toObjects(
                        AppNotification::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .handleStreamError(uid, OperationType.LIST, col.path)
        )
    }

    suspend fun markNotificationRead(notifId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        userDoc(uid).collection("notifications").document(notifId)
            .update(mapOf("isRead" to true))
            .await()
        Unit
    }.onFailure {
        handleFirestoreError(it, OperationType.UPDATE, "users/${auth.currentUser?.uid}/notifications/$notifId")
    }

    suspend fun markAllNotificationsRead(notifications: List<AppNotification>): Result<Unit> = runCatching {
        val uid = requireUserId()
        val unread = notifications.filter { !it.isRead }
        if (unread.isEmpty()) return@runCatching
        val batch = db.batch()
        val col = userDoc(uid).collection("notifications")
        unread.forEach { n ->
            batch.update(col.document(n.id), mapOf("isRead" to true))
        }
        batch.commit().await()
        Unit
    }.onFailure {
        handleFirestoreError(it, OperationType.UPDATE, "users/${auth.currentUser?.uid}/notifications")
    }

    // --- Support Tickets (Help Center) ---

    fun observeSupportTickets(uid: String): Flow<List<SupportTicket>> = flow {
        val col = userDoc(uid).collection("supportTickets")
        emitAll(
            col.snapshots()
                .map { snap ->
                    snap.toObjects(
                        SupportTicket::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .handleStreamError(uid, OperationType.LIST, col.path)
        )
    }

    suspend fun createSupportTicket(subject: String, description: String): Result<String> = runCatching {
        val uid = requireUserId()
        val ref = userDoc(uid).collection("supportTickets").document()
        ref.set(
            mapOf(
                "userId" to uid,
                "subject" to subject.trim().take(120),
                "description" to description.trim().take(1000),
                "status" to "pending",
                "reply" to "",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            )
        ).await()
        ref.id
    }.onFailure {
        handleFirestoreError(it, OperationType.CREATE, "users/${auth.currentUser?.uid}/supportTickets")
    }

    // --- QR Payment History ---

    fun observeQrHistory(uid: String): Flow<List<QrPaymentRecord>> = flow {
        val col = userDoc(uid).collection("qrTransactions")
        emitAll(
            col.snapshots()
                .map { snap ->
                    snap.toObjects(
                        QrPaymentRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .handleStreamError(uid, OperationType.LIST, col.path)
        )
    }

    suspend fun recordQrEvent(
        businessId: String,
        customerName: String,
        amount: Double,
        qrPayload: String,
        direction: String,
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val ref = userDoc(uid).collection("qrTransactions").document()
        ref.set(
            mapOf(
                "userId" to uid,
                "businessId" to businessId,
                "customerName" to customerName.trim().ifBlank { "অজানা প্রাপক" }.take(100),
                "amount" to amount.coerceAtLeast(0.0),
                "qrPayload" to qrPayload.take(500),
                "direction" to direction,
                "createdAt" to FieldValue.serverTimestamp(),
            )
        ).await()
        ref.id
    }.onFailure {
        handleFirestoreError(it, OperationType.CREATE, "users/${auth.currentUser?.uid}/qrTransactions")
    }

    suspend fun recordGatewayPayment(
        businessId: String,
        providerId: String,
        providerNameBn: String,
        title: String,
        amount: Double,
        txType: String, // "income" or "expense"
        gatewayTxId: String,
        isDemo: Boolean,
        accountNumber: String = "",
        smsPackageCount: Int = 0,
    ): Result<String> = runCatching {
        require(amount > 0.0) { "টাকার পরিমাণ শূন্যের বেশি হতে হবে" }
        val uid = requireUserId()
        val uRef = userDoc(uid)
        val txRef = uRef.collection("transactions").document()
        val notifRef = uRef.collection("notifications").document()
        val safeMethod = if (providerId in listOf("bkash", "nagad", "rocket")) providerId else "bkash"
        val safeType = if (txType == "income") "income" else "expense"
        val modeTag = if (isDemo) "[ডেমো]" else "[সার্ভার]"
        val acctSuffix = if (accountNumber.isNotBlank()) " • $accountNumber" else ""
        val desc = "$modeTag $title ($providerNameBn)$acctSuffix • Ref: $gatewayTxId".take(300)

        db.runTransaction { transaction ->
            val currentSms = if (smsPackageCount > 0) {
                val uSnap = transaction.get(uRef)
                uSnap.getLong("smsBalance") ?: 0L
            } else {
                0L
            }

            if (smsPackageCount > 0) {
                transaction.update(
                    uRef,
                    mapOf(
                        "smsBalance" to (currentSms + smsPackageCount.toLong()),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                )
            }

            transaction.set(
                txRef,
                mapOf(
                    "userId" to uid,
                    "businessId" to businessId,
                    "customerId" to "",
                    "customerName" to "$providerNameBn ($modeTag)".take(100),
                    "entryScope" to "cashbook",
                    "type" to safeType,
                    "amount" to amount,
                    "description" to desc,
                    "dateMillis" to System.currentTimeMillis(),
                    "attachmentUri" to "",
                    "paymentMethod" to safeMethod,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            )

            val notifBody = if (smsPackageCount > 0) {
                "$modeTag $providerNameBn দিয়ে ৳${amount.toLong()} পরিশোধ করে $smsPackageCount SMS যোগ হয়েছে (Ref: $gatewayTxId)।"
            } else {
                "$modeTag $providerNameBn দিয়ে $title বাবদ ৳${amount.toLong()} লেনদেন সম্পন্ন হয়েছে (Ref: $gatewayTxId)।"
            }
            transaction.set(
                notifRef,
                mapOf(
                    "userId" to uid,
                    "title" to "$modeTag $providerNameBn পেমেন্ট সফল".take(120),
                    "body" to notifBody.take(400),
                    "category" to "payment",
                    "isRead" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                )
            )
            txRef.id
        }.await()
    }.onFailure {
        handleFirestoreError(it, OperationType.WRITE, "users/${auth.currentUser?.uid}/transactions")
    }
}
