package com.undercurrent.shared

import com.undercurrent.shared.repository.bases.system.SystemEntity
import com.undercurrent.shared.repository.bases.system.SystemTable
import com.undercurrent.shared.repository.dinosaurs.SystemUserEntityCompanion1
import com.undercurrent.shared.types.strings.SmsText
import com.undercurrent.shared.utils.VARCHAR_SIZE
import org.jetbrains.exposed.dao.id.EntityID


object SystemUsersNew : SystemTable("system_users") {
    val signalSms = varchar("signal_sms", VARCHAR_SIZE).nullable().uniqueIndex()
    val signalUuid = varchar("signal_uuid", VARCHAR_SIZE).nullable().uniqueIndex()
    val isAdmin = bool("is_admin").clientDefault { false }
}

class SystemUserNew(id: EntityID<Int>) : SystemEntity(id, SystemUsersNew) {
    var signalSms by SystemUsersNew.signalSms
        .transform(
            toColumn = { SmsText(it).validate() },
            toReal = { SmsText(it).validate() })

    var signalUuid by SystemUsersNew.signalUuid
    var isAdmin by SystemUsersNew.isAdmin

    companion object : SystemUserEntityCompanion1<SystemUserNew>(SystemUsersNew)
}

