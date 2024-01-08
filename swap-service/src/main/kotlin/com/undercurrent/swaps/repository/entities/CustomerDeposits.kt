package com.undercurrent.swaps.repository.entities

import com.undercurrent.swaps.repository.companions.CustomerDepositCompanion
import org.jetbrains.exposed.dao.id.EntityID


object CustomerDeposits : SwapBotTable("swap_customer_deposits") {
    val swapUser = reference("user_id", SwapUsers)
    val poolAddress = reference("pool_address_id", PoolCryptoAddresses)
    val amount = reference("amount_id", CryptoCurrencyAmounts)
}

class CustomerDeposit(id: EntityID<Int>) : SwapBotEntity(id, CustomerDeposits) {
    var swapUser by SwapUser referencedOn CustomerDeposits.swapUser
    var poolAddress by PoolCryptoAddress referencedOn CustomerDeposits.poolAddress
    var amount by CryptoCurrencyAmount referencedOn CustomerDeposits.amount

    companion object : CustomerDepositCompanion()
}