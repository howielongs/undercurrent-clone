package com.undercurrent.swaps.repository.entities

import com.undercurrent.swaps.repository.companions.PoolBalanceSnapshotCompanion
import org.jetbrains.exposed.dao.id.EntityID


object PoolBalanceSnapshots : SwapBotTable("pool_balances") {
    val pool = reference("pool_id", LiquidityPools)
    val amount = reference("amount_id", CryptoCurrencyAmounts)
}

class PoolBalanceSnapshot(id: EntityID<Int>) : SwapBotEntity(id, PoolBalanceSnapshots) {
    var pool by LiquidityPool referencedOn PoolBalanceSnapshots.pool

    var state by LiquidityPoolStates.state.transform(
        toColumn = { it.name },
        toReal = { LiquidityPoolStatus.valueOf(it) }
    )

    companion object : PoolBalanceSnapshotCompanion()
}