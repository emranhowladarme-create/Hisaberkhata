package com.example.ui.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoute {
    @Serializable data object Home : AppRoute
    @Serializable data object Customers : AppRoute
    @Serializable data object AddCustomer : AppRoute
    @Serializable data class AddTransaction(
        val customerId: String = "",
        val entryScope: String = "customer",
    ) : AppRoute
    @Serializable data object Cashbook : AppRoute
    @Serializable data object HisabiPay : AppRoute
    @Serializable data object More : AppRoute

    @Serializable data class CustomerDetail(val customerId: String) : AppRoute
    @Serializable data class EditCustomer(val customerId: String) : AppRoute
    @Serializable data class SmsSender(
        val customerId: String = "",
        val groupMode: Boolean = false,
    ) : AppRoute
    @Serializable data object SmsPackages : AppRoute
    @Serializable data object Inventory : AppRoute
    @Serializable data object BusinessNotes : AppRoute
    @Serializable data object MultiBusiness : AppRoute
    @Serializable data class QrSystem(val initialTab: Int = 0) : AppRoute
    @Serializable data class Reports(val ownerMode: Boolean = false) : AppRoute
    @Serializable data object Inbox : AppRoute
    @Serializable data class HelpCenter(val startOnFaq: Boolean = false) : AppRoute
    @Serializable data object Settings : AppRoute
    @Serializable data object PermissionCenter : AppRoute
    @Serializable data object LocationPermission : AppRoute
    @Serializable data object BusinessInfo : AppRoute
    @Serializable data object BusinessCategory : AppRoute
    @Serializable data object HisabiGold : AppRoute
    @Serializable data object GoldMembership : AppRoute
    @Serializable data object MyGoldPackage : AppRoute
    @Serializable data class PaymentGateway(
        val providerId: String = "bkash",
        val title: String = "পেমেন্ট গেটওয়ে",
        val amount: Double = 0.0,
        val smsPackageCount: Int = 0,
        val activateGold: Boolean = false,
        val goldPackageId: String = "",
    ) : AppRoute
}
