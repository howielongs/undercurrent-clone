package com.undercurrent.shared.utils.time

import com.undercurrent.shared.abstractions.TableWithExpiry
import org.jetbrains.exposed.sql.GreaterOp
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.or

fun unexpiredExpr(table: TableWithExpiry): Op<Boolean> {
    return ExpiryUtils.isUnexpiredExpr(table)
}

fun unexpiredExpr(table: TableWithExpiry, epoch: EpochNano): Op<Boolean> {
    return ExpiryUtils.isUnexpiredExpr(table, epoch.value)
}

fun unexpiredExpr(table: TableWithExpiry, epoch: Long): Op<Boolean> {
    return ExpiryUtils.isUnexpiredExpr(table, epoch)
}

object ExpiryUtils {
    fun isUnexpiredExpr(
        table: TableWithExpiry,
        epoch: Long = SystemEpochNanoProvider().getEpochNanoLong()
    ): Op<Boolean> {
        return expiryIsNull(table) or expiryIsInFuture(table, epoch)
    }

    private fun expiryIsInFuture(
        table: TableWithExpiry,
        epoch: Long = SystemEpochNanoProvider().getEpochNanoLong()
    ): GreaterOp {
        return table.expiryEpoch greater epoch
    }


    private fun expiryIsNull(table: TableWithExpiry): Op<Boolean> {
        return table.expiryEpoch eq null or (table.expiryEpoch eq 0L)
    }

}