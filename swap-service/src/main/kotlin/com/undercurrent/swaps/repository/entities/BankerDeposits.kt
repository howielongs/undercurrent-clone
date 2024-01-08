package com.undercurrent.swaps.repository.entities

import com.undercurrent.swaps.repository.companions.BankerDepositCompanion
import org.jetbrains.exposed.dao.id.EntityID


object BankerDeposits : SwapBotTable("swap_banker_deposit") {
    val banker = reference("banker_id", SwapBankers)
    val pool = reference("pool_id", LiquidityPools)
    val amount = reference("amount_id", CryptoCurrencyAmounts)
}

class BankerDeposit(id: EntityID<Int>) : SwapBotEntity(id, BankerDeposits) {
    var banker by SwapBanker referencedOn BankerDeposits.banker
    var pool by LiquidityPool referencedOn BankerDeposits.pool
    var amount by CryptoCurrencyAmount referencedOn BankerDeposits.amount

    companion object : BankerDepositCompanion()
}