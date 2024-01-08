package com.undercurrent.swaps.repository.entities

import com.undercurrent.shared.utils.VARCHAR_SIZE
import com.undercurrent.swaps.repository.companions.PoolAddressCompanion
import org.jetbrains.exposed.dao.id.EntityID

object PoolCryptoAddresses : SwapBotTable("swap_pool_addresses") {
    val pool = reference("pool_id", LiquidityPools)
    val address = varchar("address", VARCHAR_SIZE)
    val label = varchar("label", VARCHAR_SIZE).nullable()
}

class PoolCryptoAddress(id: EntityID<Int>) : SwapBotEntity(id, PoolCryptoAddresses) {
    var pool by LiquidityPool referencedOn PoolCryptoAddresses.pool
    var address by PoolCryptoAddresses.address
    var label by PoolCryptoAddresses.label

    companion object : PoolAddressCompanion()
}