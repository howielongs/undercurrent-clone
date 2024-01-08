package com.undercurrent.swaps.repository.entities

import com.undercurrent.shared.utils.VARCHAR_SIZE
import com.undercurrent.swaps.repository.companions.SwapTransactionStateCompanion
import org.jetbrains.exposed.dao.id.EntityID

enum class SwapTransactionStatus {
    CREATED, REQUESTED, PENDING, COMPLETED, CANCELLED, DENIED, EXPIRED
}

object SwapTransactionStates : SwapBotTable("swap_tx_states") {
    val swap = reference("swap_id", SwapTransactions)
    val state = varchar("state", VARCHAR_SIZE).clientDefault {
        SwapTransactionStatus.CREATED.name
    }
}

class SwapTransactionState(id: EntityID<Int>) : SwapBotEntity(id, SwapTransactionStates) {
    var swapTransaction by SwapTransaction referencedOn SwapTransactionStates.swap
    var state by SwapTransactionStates.state.transform(
        toColumn = { it.name },
        toReal = { SwapTransactionStatus.valueOf(it) }
    )

    companion object : SwapTransactionStateCompanion()
}