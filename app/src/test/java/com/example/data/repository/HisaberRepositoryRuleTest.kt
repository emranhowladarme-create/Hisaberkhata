package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HisaberRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun bootstrapProfileAndAddCustomer_authenticatedOwner_succeedsAndEmitsRealtime() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = HisaberRepository(firestore, auth)

        val profile = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.ensureUserProfileExists("রহিম উদ্দিন", "রহিম ট্রেডার্স", "+8801711000111").getOrThrow()
        }
        assertTrue(profile.activeBusinessId.isNotBlank())

        val customerId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.addCustomer(
                businessId = profile.activeBusinessId,
                name = "শাহিন ভাই বাকী",
                phone = "01712345678",
                address = "মিরপুর-১০, ঢাকা",
                initialBalance = 0.0,
            ).getOrThrow()
        }
        assertTrue(customerId.isNotBlank())

        val customers = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeCustomers(uid).first { list -> list.any { it.id == customerId } }
        }
        assertTrue(customers.any { it.id == customerId && it.name == "শাহিন ভাই বাকী" })
    }

    @Test
    fun addCustomerTransaction_updatesCustomerBalanceAtomically() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = HisaberRepository(firestore, auth)
        val profile = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.ensureUserProfileExists("রহিম উদ্দিন", "রহিম ট্রেডার্স").getOrThrow()
        }
        val customerId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.addCustomer(profile.activeBusinessId, "বেলাল মিয়া", "01811223344", "").getOrThrow()
        }

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.addCustomerTransaction(
                businessId = profile.activeBusinessId,
                customerId = customerId,
                customerName = "বেলাল মিয়া",
                type = "gave",
                amount = 5000.0,
                description = "বাকি বিক্রি",
                dateMillis = System.currentTimeMillis(),
            ).getOrThrow()
        }

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.addCustomerTransaction(
                businessId = profile.activeBusinessId,
                customerId = customerId,
                customerName = "বেলাল মিয়া",
                type = "received",
                amount = 1500.0,
                description = "আংশিক জমা",
                dateMillis = System.currentTimeMillis(),
            ).getOrThrow()
        }

        val updated = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeCustomers(uid).first { list ->
                val c = list.find { it.id == customerId }
                c != null && c.balance == 3500.0
            }
        }
        assertEquals(3500.0, updated.first { it.id == customerId }.balance, 0.01)
    }

    @Test
    fun productsSmsNotesTicketsQr_crudAgainstRules_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = HisaberRepository(firestore, auth)
        val profile = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.ensureUserProfileExists("করিম মিয়া", "করিম স্টোর").getOrThrow()
        }

        val prodId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.addProduct(profile.activeBusinessId, "মিনিকেট চাল", "কেজি", 62.0, 70.0, 50L, 10L).getOrThrow()
        }
        assertTrue(prodId.isNotBlank())

        val smsId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.logSmsAndOptionalDecrement(
                businessId = profile.activeBusinessId,
                customerId = "",
                customerName = "রনি",
                phone = "01711111111",
                message = "বকেয়া পরিশোধের অনুরোধ",
                messageType = "tagada",
                status = "sent",
            ).getOrThrow()
        }
        assertTrue(smsId.isNotBlank())

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.saveNote(profile.activeBusinessId, "জরুরি নোট", "মাল অর্ডার দিতে হবে", true).getOrThrow()
        }

        val ticketId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createSupportTicket("QR সমস্যা", "QR জেনারেট টেস্ট").getOrThrow()
        }
        assertTrue(ticketId.isNotBlank())

        val qrId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.recordQrEvent(profile.activeBusinessId, "করিম স্টোর", 500.0, "HISAB://pay?amt=500", "generated").getOrThrow()
        }
        assertTrue(qrId.isNotBlank())

        val products = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeProducts(uid).first { it.any { p -> p.id == prodId } }
        }
        assertTrue(products.isNotEmpty())
    }

    @Test
    fun observeCustomers_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = HisaberRepository(firestore, auth)
        val profile = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.ensureUserProfileExists("Alice", "Alice Shop").getOrThrow()
        }
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.addCustomer(profile.activeBusinessId, "Customer A", "", "").getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = HisaberRepository(firestore, auth)
        var caught: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                bobRepo.observeCustomers(aliceUid).first()
            }
        } catch (t: Throwable) {
            caught = t
        }
        assertNotNull(caught)
        val fsError = caught?.unwrapFirestoreException()
        assertNotNull(fsError)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsError?.code)
    }

    @Test
    fun observeCustomers_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repo = HisaberRepository(firestore, auth)
        var caught: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeCustomers(aliceUid).first()
            }
        } catch (t: Throwable) {
            caught = t
        }
        assertNotNull(caught)
        val fsError = caught?.unwrapFirestoreException()
        assertNotNull(fsError)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsError?.code)
    }

    @Test
    fun recordGatewayPayment_demoSmsPackage_creditsSmsBalanceAndLogsCashbook() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = HisaberRepository(firestore, auth)
        val profile = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.ensureUserProfileExists("করিম মিয়া", "করিম স্টোর").getOrThrow()
        }
        val initialSms = profile.smsBalance

        val txId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.recordGatewayPayment(
                businessId = profile.activeBusinessId,
                providerId = "bkash",
                providerNameBn = "বিকাশ",
                title = "স্টার্টার প্যাকেজ (100 SMS)",
                amount = 99.0,
                txType = "expense",
                gatewayTxId = "DEMO-BKASH-12345678",
                isDemo = true,
                accountNumber = "01711111111",
                smsPackageCount = 100,
            ).getOrThrow()
        }
        assertTrue(txId.isNotBlank())

        val updatedProfile = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeUserProfile(uid).first { it != null && it.smsBalance == initialSms + 100L }
        }
        assertEquals(initialSms + 100L, updatedProfile?.smsBalance)
    }

    private companion object {
        const val ALICE_EMAIL = "hisab_alice@test.com"
        const val BOB_EMAIL = "hisab_bob@test.com"
        const val DEFAULT_TIMEOUT_MS = 8000L
        const val FLOW_TIMEOUT_MS = 5000L
    }
}
