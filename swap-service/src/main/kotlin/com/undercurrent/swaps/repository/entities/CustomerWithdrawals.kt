package com.undercurrent.swaps.repository.entities

import com.undercurrent.swaps.repository.companions.CustomerWithdrawalCompanion
import org.jetbrains.exposed.dao.id.EntityID


object CustomerWithdrawals : SwapBotTable("swap_customer_withdrawals") {
    val swapUser = reference("user_id", SwapUsers)
    val poolAddress = reference("pool_address_id", PoolCryptoAddresses)
    val amount = reference("amount_id", CryptoCurrencyAmounts)
}

class CustomerWithdrawal(id: EntityID<Int>) : SwapBotEntity(id, CustomerWithdrawals) {
    var swapUser by SwapUser referencedOn CustomerWithdrawals.swapUser
    var poolAddress by PoolCryptoAddress referencedOn CustomerWithdrawals.poolAddress
    var amount by CryptoCurrencyAmount referencedOn CustomerWithdrawals.amount

    companion object : CustomerWithdrawalCompanion()
}