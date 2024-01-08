package com.undercurrent.swaps.repository.entities

import com.undercurrent.shared.utils.VARCHAR_SIZE
import com.undercurrent.swaps.repository.companions.LiquidityPoolStateCompanion
import org.jetbrains.exposed.dao.id.EntityID


enum class LiquidityPoolStatus {
    ACTIVE, INACTIVE
}

object LiquidityPoolStates : SwapBotTable("pool_states") {
    val pool = reference("pool_id", LiquidityPools)
    val state = varchar("state", VARCHAR_SIZE).clientDefault {
        LiquidityPoolStatus.INACTIVE.name
    }
}

class LiquidityPoolState(id: EntityID<Int>) : SwapBotEntity(id, LiquidityPoolStates) {
    var pool by LiquidityPool referencedOn LiquidityPoolStates.pool

    var state by LiquidityPoolStates.state.transform(
        toColumn = { it.name },
        toReal = { LiquidityPoolStatus.valueOf(it) }
    )

    companion object : LiquidityPoolStateCompanion()
}