package com.undercurrent.shared.repository.entities

import com.undercurrent.shared.repository.bases.system.SystemEntity
import com.undercurrent.shared.repository.bases.system.SystemTable
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.utils.VARCHAR_SIZE
import org.jetbrains.exposed.dao.id.EntityID
@Deprecated("Experimental class: not ready for primetime")

object SignalGroups : SystemTable("signal_groups") {
    val name = varchar("name", VARCHAR_SIZE).nullable()
    val groupId = varchar("group_id", VARCHAR_SIZE).nullable()
    val groupIdB64 = varchar("group_id_b64", VARCHAR_SIZE).nullable()

    val dbusPath = varchar("dbus_path", VARCHAR_SIZE).nullable()

    val avatarPath = varchar("avatar_path", VARCHAR_SIZE).nullable()

    val inviteUrl = varchar("invite_url", VARCHAR_SIZE).nullable()
    val aboutText = varchar("about_text", VARCHAR_SIZE).nullable()
}
@Deprecated("Experimental class: not ready for primetime")

class SignalGroup(id: EntityID<Int>) :
    SystemEntity(id, SignalGroups) {

    var groupIdB64 by SignalGroups.groupIdB64 //include conversion function here
    var groupId by SignalGroups.groupId
    var dbusPath by SignalGroups.dbusPath // can parse from sms

    var name by SignalGroups.name

    var inviteUrl by SignalGroups.inviteUrl
    var avatarPath by SignalGroups.avatarPath
    var aboutText by SignalGroups.aboutText


    companion object : RootEntityCompanion0<SignalGroup>(SignalGroups)
}
