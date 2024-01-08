package com.undercurrent.swaps.repository.entities

import com.undercurrent.swaps.repository.companions.BankerWithdrawalCompanion
import org.jetbrains.exposed.dao.id.EntityID


object BankerWithdrawals : SwapBotTable("swap_banker_Withdrawal") {
    val banker = reference("banker_id", SwapBankers)
    val pool = reference("pool_id", LiquidityPools)
    val amount = reference("amount_id", CryptoCurrencyAmounts)
}

class BankerWithdrawal(id: EntityID<Int>) : SwapBotEntity(id, BankerWithdrawals) {
    var banker by SwapBanker referencedOn BankerWithdrawals.banker
    var pool by LiquidityPool referencedOn BankerWithdrawals.pool
    var amount by CryptoCurrencyAmount referencedOn BankerWithdrawals.amount

    companion object : BankerWithdrawalCompanion()
}