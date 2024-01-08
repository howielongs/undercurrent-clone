package com.undercurrent.system.repository.entities.messages

import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.repository.entities.Users
import com.undercurrent.shared.repository.dinosaurs.EntityWithStatus
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.shared.repository.dinosaurs.entityHasStatus
import com.undercurrent.shared.repository.dinosaurs.EntityHasStatusField
import com.undercurrent.shared.messages.InterrupterMessageEntity
import com.undercurrent.shared.messages.OutboundMessageTable
import com.undercurrent.shared.utils.VARCHAR_SIZE
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime


object OutboundMessages : ExposedTableWithStatus2("msgs_out"), OutboundMessageTable {
    val user = reference("user_id", Users).nullable()

    val body = varchar("body", 50000)
    override val botSenderSms: Column<String> = varchar("sender", VARCHAR_SIZE)
    override val humanReceiverAddr: Column<String> = varchar("receiver", VARCHAR_SIZE)
    override val serverTimestamp: Column<Long?> = long("timestamp").nullable()
    val role = varchar("role", VARCHAR_SIZE).nullable()
    val uuid = varchar("uuid", VARCHAR_SIZE).nullable()

    override val sentAtDate: Column<LocalDateTime?> = datetime("sent_at").nullable()
    override val sendAfterEpochNano: Column<Long?> = long("send_after").nullable()
}

class OutboundMessage(id: EntityID<Int>) : ExposedEntityWithStatus2(
    id = id,
    thisTable = OutboundMessages
),
    EntityHasStatusField,
    InterrupterMessageEntity,
    EntityWithStatus {

    companion object : OutboundMessageCompanion()

    var user by User optionalReferencedOn OutboundMessages.user

    override var body by OutboundMessages.body
    override var senderSms by OutboundMessages.botSenderSms
    override var receiverSms by OutboundMessages.humanReceiverAddr
    override var timestamp by OutboundMessages.serverTimestamp
    var role by OutboundMessages.role
    override var uuid by OutboundMessages.uuid

    override var sendAfterEpochNano by OutboundMessages.sendAfterEpochNano
    override var sentAtDate by OutboundMessages.sentAtDate


    //todo get rid of this
    override fun hasStatus(status: String): Boolean {
        return entityHasStatus(status)
    }

    override fun toString(): String {
        return """
            body: $body
            status: $status
            sender: $senderSms
            receiver: $receiverSms
            timestamp: $timestamp
            uid: $uid
            createdDate: $createdDate
            updatedDate: $updatedDate
            expiryEpoch: $expiryEpoch
        """.trimIndent()
    }

}

//todo also remove STATUS
// add some of these fields:
//    val environment = varchar("environment", VARCHAR_SIZE)
//    override val botSenderSms = varchar("bot_sender_sms", VARCHAR_SIZE)
//    override val humanReceiverAddr = varchar("human_addr", VARCHAR_SIZE)
//    val humanReceiverAddr2 = varchar("human_addr_2", VARCHAR_SIZE).nullable()
//    val dbusPath = varchar("dbus_path", VARCHAR_SIZE)


