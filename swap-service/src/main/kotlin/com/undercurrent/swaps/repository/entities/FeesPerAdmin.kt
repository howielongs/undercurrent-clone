package com.undercurrent.swaps.repository.entities

import com.undercurrent.swaps.repository.companions.FeePerAdminCompanion
import org.jetbrains.exposed.dao.id.EntityID


object FeesPerAdmin : SwapBotTable("swap_fees_per_admin") {
    val swapAdmin = reference("swap_admin_id", SwapAdmins)
    val swap = reference("swap_transaction_id", SwapTransactions)
    val amount = reference("amount_id", CryptoCurrencyAmounts)
    val received = bool("received").clientDefault { false }
}

class FeePerAdmin(id: EntityID<Int>) : SwapBotEntity(id, FeesPerAdmin) {
    var swapAdmin by SwapAdmin referencedOn FeesPerAdmin.swapAdmin
    var swapTransaction by SwapTransaction referencedOn FeesPerAdmin.swap
    var amount by CryptoCurrencyAmount referencedOn FeesPerAdmin.amount
    var received by FeesPerAdmin.received

    companion object : FeePerAdminCompanion()
}