package com.undercurrent.system.messaging.inbound.querybuilders.fetch

import com.undercurrent.system.repository.entities.User
import com.undercurrent.legacy.repository.entities.system.attachments.Attachments
import com.undercurrent.shared.messages.RoutingProps
import com.undercurrent.shared.utils.time.unexpiredExpr
import com.undercurrent.shared.utils.time.EpochNano
import com.undercurrent.system.repository.entities.messages.InboundMessages
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or

interface FetchQueryProvider {
    fun toFetchExpr(afterEpochNano: EpochNano): Op<Boolean>
}

class LatestInboundMessageQueryProvider(
    val user: User,
    val routingProps: RoutingProps,
) : FetchQueryProvider {

    // this can lead to conflicts given that only fetching on User sms/UUID and not role/dbus
    override fun toFetchExpr(afterEpochNano: EpochNano): Op<Boolean> {
        val dbusQuery = InboundMessages.dbusPath eq routingProps.toPath().value
        val smsQuery = InboundMessages.receiverSms eq routingProps.toBotSms().value
        val afterQuery = InboundMessages.timestampNano greaterEq afterEpochNano.value
        val unexpiredExprQuery = unexpiredExpr(InboundMessages, afterEpochNano.value)
        val senderSmsQuery = InboundMessages.senderSms eq user.smsNumber
        val uuidQuery = InboundMessages.uuid eq user.uuid
        val readAtQuery = InboundMessages.readAtDate eq null
        val unreadQuery = afterQuery and unexpiredExprQuery and readAtQuery

        return (dbusQuery or smsQuery) and unreadQuery and (senderSmsQuery or uuidQuery)
    }
}

class LatestInboundAttachmentsQueryProvider(
    val user: User,
    val routingProps: RoutingProps,
) : FetchQueryProvider {

    override fun toFetchExpr(afterEpochNano: EpochNano): Op<Boolean> {
        val role = routingProps.role

        val userQuery = Attachments.Table.ownerUser eq user.id
        val uploadEpochQuery = Attachments.Table.uploadEpochNano greaterEq afterEpochNano.value
        val roleQuery = Attachments.Table.ownerRole eq role.name
        val unexpiredExprQuery = unexpiredExpr(Attachments.Table, afterEpochNano.value)

        return userQuery and uploadEpochQuery and roleQuery and unexpiredExprQuery
    }
}
