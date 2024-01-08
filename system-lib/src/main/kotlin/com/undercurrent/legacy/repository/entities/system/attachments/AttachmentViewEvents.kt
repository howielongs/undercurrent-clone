package com.undercurrent.legacy.repository.entities.system.attachments

import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.repository.entities.Users
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.shared.utils.VARCHAR_SIZE
import org.jetbrains.exposed.dao.id.EntityID

class AttachmentViewEvents {

    object Table : ExposedTableWithStatus2("attachment_file_view_events") {
        val attachment = reference("attachment_id", Attachments.Table)
        val viewerUser = reference("viewer_user_id", Users)

        val locationTag = varchar("location_tag", VARCHAR_SIZE)
        val rawContext = varchar("raw_context", VARCHAR_SIZE).nullable()
        
    }

    class Entity(id: EntityID<Int>) : ExposedEntityWithStatus2(id, Table) {
        companion object : RootEntityCompanion0<Entity>(Table)

        var attachment by Attachments.Entity referencedOn Table.attachment
        var viewerUser by User referencedOn Table.viewerUser

        var locationTag by Table.locationTag
        var rawContext by Table.rawContext
    }

}
