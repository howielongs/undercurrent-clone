package com.undercurrent.swaps.repository.entities

import com.undercurrent.shared.abstractions.swaps.SwapEntity
import com.undercurrent.shared.abstractions.swaps.SwapTable
import com.undercurrent.shared.abstractions.swaps.SwapTransactionEntity
import com.undercurrent.shared.repository.bases.RootEntity0
import com.undercurrent.shared.repository.bases.RootTable0
import com.undercurrent.shared.utils.VARCHAR_SIZE
import com.undercurrent.swaps.repository.companions.SwapCompanion
import org.jetbrains.exposed.dao.id.EntityID

sealed class SwapBotEntity(id: EntityID<Int>, table: SwapBotTable) : RootEntity0(id, table), SwapEntity

sealed class SwapBotTable(
    tableName: String,
) : RootTable0("$tableName"), SwapTable


object SwapTransactions : SwapBotTable("swap_transactions") {
    val swapUser = reference("swap_user_id", SwapUsers)
    val pool = reference("pool_id", LiquidityPools)

    val amountIn = reference("amount_in_id", CryptoCurrencyAmounts)
    val amountOut = reference("amount_out_id", CryptoCurrencyAmounts)
    val totalFees = reference("total_fees_id", CryptoCurrencyAmounts)

    val comments = varchar("comments", VARCHAR_SIZE).nullable()
}

class SwapTransaction(id: EntityID<Int>) : SwapBotEntity(id, SwapTransactions), SwapTransactionEntity {
    var swapUser by SwapUser referencedOn SwapTransactions.swapUser
    var pool by LiquidityPool referencedOn SwapTransactions.pool

    //figure out how to best handle these amount types with interfaces
    var amountIn: CryptoCurrencyAmount by CryptoCurrencyAmount referencedOn SwapTransactions.amountIn
    var amountOut by CryptoCurrencyAmount referencedOn SwapTransactions.amountOut
    var totalFees by CryptoCurrencyAmount referencedOn SwapTransactions.totalFees

    var comments by SwapTransactions.comments

    companion object : SwapCompanion()
}