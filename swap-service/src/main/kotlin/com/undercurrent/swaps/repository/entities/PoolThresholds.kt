package com.undercurrent.swaps.repository.entities

import com.undercurrent.shared.utils.VARCHAR_SIZE
import com.undercurrent.swaps.repository.companions.PoolThresholdsCompanion
import org.jetbrains.exposed.dao.id.EntityID


object PoolThresholds : SwapBotTable("pool_thresholds") {
    val pool = reference("pool_id", LiquidityPools)
    val autoStopBalance = varchar("auto_stop_balance", VARCHAR_SIZE).clientDefault { "0" }
    val refillNotificationBalance = varchar("refill_at_balance", VARCHAR_SIZE).clientDefault { "0" }
    val maxSwapAmountPerUser = varchar("max_swap_amount", VARCHAR_SIZE).nullable()
    val expirationTimeMinutes = integer("transaction_expiration_minutes").clientDefault { 60 }
}

class PoolThreshold(id: EntityID<Int>) : SwapBotEntity(id, PoolThresholds) {
    var pool by LiquidityPool referencedOn PoolThresholds.pool
    var autoStopBalance by PoolThresholds.autoStopBalance.transform(
        toColumn = { it.toString() },
        toReal = { it.toBigDecimal() }
    )
    var refillNotificationBalance by PoolThresholds.refillNotificationBalance.transform(
        toColumn = { it.toString() },
        toReal = { it.toBigDecimal() }
    )
    var maxSwapAmountPerUser by PoolThresholds.maxSwapAmountPerUser.transform(
        toColumn = { it.toString() },
        toReal = { it?.toBigDecimal() }
    )
    var expirationTimeMinutes by PoolThresholds.expirationTimeMinutes

    companion object : PoolThresholdsCompanion()
}