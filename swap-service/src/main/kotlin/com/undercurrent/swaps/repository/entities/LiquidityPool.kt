package com.undercurrent.swaps.repository.entities

import com.undercurrent.legacyswaps.types.SwappableCrypto
import com.undercurrent.shared.utils.VARCHAR_SIZE
import com.undercurrent.swaps.repository.companions.LiquidityPoolCompanion
import org.jetbrains.exposed.dao.id.EntityID

object LiquidityPools : SwapBotTable("swap_pool") {
    val currencyType = varchar("currency", VARCHAR_SIZE)
    val balance = varchar("balance", VARCHAR_SIZE)
}

class LiquidityPool(id: EntityID<Int>) : SwapBotEntity(id, LiquidityPools) {
    var currencyType by LiquidityPools.currencyType.transform(
        toColumn = { it.name },
        toReal = { SwappableCrypto.valueOf(it) }
    )
    var balance by LiquidityPools.balance.transform(
        toColumn = { it.toString() },
        toReal = { it.toBigDecimal() }
    )

    companion object : LiquidityPoolCompanion()
}