package com.undercurrent.system.repository.entities.messages

import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.bases.RootEntity0
import com.undercurrent.shared.repository.bases.RootTable0
import com.undercurrent.shared.messages.InboundMessageEntity
import com.undercurrent.shared.utils.VARCHAR_SIZE
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime


object InboundMessages : RootTable0("msgs_in") {
    val body = varchar("body", 50000)
    val senderSms = varchar("sender_sms", VARCHAR_SIZE)
    val receiverSms = varchar("receiver_sms", VARCHAR_SIZE)
    val dbusPath = varchar("dbus_path", VARCHAR_SIZE)
    val timestampNano = long("timestamp")
    val readAtDate = datetime("read_at").nullable()
    val uuid = varchar("uuid", VARCHAR_SIZE).nullable()
}


class InboundMessage(id: EntityID<Int>) : RootEntity0(id, InboundMessages), InboundMessageEntity {
    companion object : RootEntityCompanion0<InboundMessage>(InboundMessages)

    override var body by InboundMessages.body
    override var senderSms by InboundMessages.senderSms
    override var receiverSms by InboundMessages.receiverSms
    override var timestamp by InboundMessages.timestampNano
    override var readAtDate: LocalDateTime? by InboundMessages.readAtDate
    var dbusPath by InboundMessages.dbusPath
    override var uuid by InboundMessages.uuid
}


//    var groupId: String? = null
//        get() {
//            return tx { groupIdB64 }?.let {
//                Base64.getDecoder().decode(it).toHex()
//            } ?: null
//        }