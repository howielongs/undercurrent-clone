package com.undercurrent.system.repository.entities.messages

import com.undercurrent.legacyshops.repository.entities.shop_items.SaleItems.clientDefault
import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.repository.entities.Users
import com.undercurrent.shared.messages.NotificationMessageEntity
import com.undercurrent.shared.messages.OutboundMessageTable
import com.undercurrent.shared.repository.bases.RootEntity0
import com.undercurrent.shared.repository.bases.RootTable0
import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.types.enums.RoleTransformers.fromRole
import com.undercurrent.shared.types.enums.RoleTransformers.fromString
import com.undercurrent.shared.utils.VARCHAR_SIZE
import com.undercurrent.shared.utils.time.EpochNano
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

const val MESSAGE_SPACING_MS = 500L

object NotificationMessages : RootTable0("msgs_out_notifications"), OutboundMessageTable {
    val user = reference("user_id", Users)

    val body = varchar("body", 50000)
    val role = varchar("role", VARCHAR_SIZE).nullable()
    val environment = varchar("environment", VARCHAR_SIZE)
    override val botSenderSms = varchar("bot_sender_sms", VARCHAR_SIZE)
    override val humanReceiverAddr = varchar("human_addr", VARCHAR_SIZE)
    val humanReceiverAddr2 = varchar("human_addr_2", VARCHAR_SIZE).nullable()
    val dbusPath = varchar("dbus_path", VARCHAR_SIZE)

    override val sendAfterEpochNano = long("send_after").nullable()

    override val sentAtDate = datetime("sent_at").nullable()
    override val serverTimestamp: Column<Long?> = long("timestamp_server").nullable()

}

class NotificationMessage(id: EntityID<Int>) : RootEntity0(
    id = id,
    matchingTable = NotificationMessages
), NotificationMessageEntity {

    companion object : NotificationMessageCompanion()

    var user by User referencedOn NotificationMessages.user
    var role by NotificationMessages.role.transform(
        toColumn = { fromRole(it) },
        toReal = { fromString(it) }
    )

    override var body by NotificationMessages.body
    override var senderSms by NotificationMessages.botSenderSms
    override var receiverSms by NotificationMessages.humanReceiverAddr
    override var uuid by NotificationMessages.humanReceiverAddr2
    var dbusPath by NotificationMessages.dbusPath
    override var sendAfterEpochNano: Long? by NotificationMessages.sendAfterEpochNano.clientDefault { EpochNano().value }
    override var sentAtDate: LocalDateTime? by NotificationMessages.sentAtDate
    override var timestamp by NotificationMessages.serverTimestamp
    var environment by NotificationMessages.environment.transform(
        toColumn = { it.name },
        toReal = { Environment.valueOf(it) }
    )
}


