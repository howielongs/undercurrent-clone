package com.undercurrent.swaps.repository.entities

import com.undercurrent.shared.repository.entities.Sms
import com.undercurrent.shared.types.validators.SmsValidator
import com.undercurrent.shared.utils.VARCHAR_SIZE
import com.undercurrent.swaps.repository.companions.SwapUserCompanion
import org.jetbrains.exposed.dao.id.EntityID

object SwapUsers : SwapBotTable("swap_users") {
    val signalSms = varchar("sms", 20).uniqueIndex()
    val signalUuid = varchar("signal_uuid", VARCHAR_SIZE).nullable().uniqueIndex()
    val tag = varchar("tag", VARCHAR_SIZE).nullable()
}

class SwapUser(id: EntityID<Int>) : SwapBotEntity(id, SwapUsers) {
    var signalSms by SwapUsers.signalSms.transform(
        { SmsValidator().validate(it.toString()).toString() },
        { Sms(it) })

    val signalUuid by SwapUsers.signalUuid
    var tag by SwapUsers.tag

    companion object : SwapUserCompanion()
}