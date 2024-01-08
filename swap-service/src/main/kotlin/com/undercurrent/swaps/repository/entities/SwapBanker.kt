package com.undercurrent.swaps.repository.entities

import com.undercurrent.swaps.repository.companions.SwapBankerCompanion
import org.jetbrains.exposed.dao.id.EntityID


object SwapBankers : SwapBotTable("swap_bankers") {
    val swapUser = reference("swap_user", SwapUsers)
    val pool = reference("pool_id", LiquidityPools)
}

class SwapBanker(id: EntityID<Int>) : SwapBotEntity(id, SwapBankers) {
    var swapUser by SwapUser referencedOn SwapBankers.swapUser
    var pool by LiquidityPool referencedOn SwapBankers.pool

    companion object : SwapBankerCompanion()
}