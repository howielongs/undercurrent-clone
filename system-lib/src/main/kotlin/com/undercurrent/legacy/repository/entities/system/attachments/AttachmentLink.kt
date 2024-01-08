package com.undercurrent.legacy.repository.entities.system.attachments

import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.legacy.types.enums.AttachmentType
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

class AttachmentLink(id: EntityID<Int>) : ExposedEntityWithStatus2(id, AttachmentLinks.Table) {
    companion object : RootEntityCompanion0<AttachmentLink>(AttachmentLinks.Table) {
        fun save(
            attachment: Attachment,  //use actual attachment obj?
            attachmentTypeIn: AttachmentType,
            parentId: Int,
        ): AttachmentLink? {
            return transaction {
                new {
                    parentAttachment = attachment
                    attachmentType = attachmentTypeIn.name
                    caption = attachment.caption
                    parentEntityId = parentId

                }
            }
        }

    }

    var parentAttachment by Attachment referencedOn AttachmentLinks.Table.parentAttachment

    var attachmentType by AttachmentLinks.Table.attachmentType

    var parentEntityId by AttachmentLinks.Table.parentEntityId
    var parentEntityClassName by AttachmentLinks.Table.parentEntityClassName

    var caption by AttachmentLinks.Table.caption

    var attachment: Attachment? = null
        get() {

            return transaction { parentAttachment }
        }


}