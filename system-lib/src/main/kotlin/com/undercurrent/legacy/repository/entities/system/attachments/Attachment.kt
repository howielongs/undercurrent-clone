package com.undercurrent.legacy.repository.entities.system.attachments


import com.undercurrent.system.repository.entities.User
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import org.jetbrains.exposed.dao.id.EntityID

class Attachment(id: EntityID<Int>) : ExposedEntityWithStatus2(id, Attachments.Table) {

    companion object : RootEntityCompanion0<Attachment>(Attachments.Table)

    var ownerUser by User referencedOn Attachments.Table.ownerUser

    var path by Attachments.Table.path
    var caption by Attachments.Table.caption


}