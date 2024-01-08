package com.undercurrent.system.messaging.outbound.dispatchers

import com.undercurrent.shared.messages.OutboundMessageEntity
import com.undercurrent.shared.messages.OutboundMessageTable
import com.undercurrent.shared.repository.entities.BotSms
import com.undercurrent.shared.repository.entities.SignalSms
import com.undercurrent.shared.utils.time.EpochNano
import com.undercurrent.shared.utils.time.unexpiredExpr
import com.undercurrent.shared.utils.tx
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or

typealias FetchAroundEntityQueryFunc<E> = (ExprBuilderFunc) -> List<E>
typealias ExprBuilderFunc = () -> Op<Boolean>

open class OutboundMessageFetcher<E : OutboundMessageEntity, T : OutboundMessageTable>(
    private val thisBotSenderSms: BotSms,
    private val excludedNumbers: List<SignalSms>,
    private val table: T,
    private val fetchItems: FetchAroundEntityQueryFunc<E>,
    ) {
    //todo SMELLY

    fun fetchMessages(now: EpochNano): List<E> {
        val expr = ExprBuilder().buildFetchExpr(now)
        return tx {
            fetchItems { expr }
        }
    }

    private inner class ExprBuilder {
        fun buildFetchExpr(now: EpochNano): Op<Boolean> {
            val botSenderQuery = table.botSenderSms eq thisBotSenderSms.value
            val notSentYetExpr = table.sentAtDate eq null
            val notInListQuery = table.humanReceiverAddr notInList excludedNumbers.map { it.value }
            val pastSendAtTime =
                table.sendAfterEpochNano lessEq now.value or (table.sendAfterEpochNano eq null)

            val unexpiredExpr = unexpiredExpr(table, now.value)
            return botSenderQuery and unexpiredExpr and notSentYetExpr and notInListQuery and pastSendAtTime
        }
    }
}

