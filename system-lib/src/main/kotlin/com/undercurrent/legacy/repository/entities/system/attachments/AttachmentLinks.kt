package com.undercurrent.legacy.repository.entities.system.attachments

import com.undercurrent.legacy.types.enums.AttachmentType
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.utils.VARCHAR_SIZE
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

object AttachmentLinks {

    fun save(
        attachment: Attachments.Entity,  //use actual attachment obj?
        attachmentTypeIn: AttachmentType,
        parentId: Int,
    ): Entity? {
        return transaction {
            Entity.new {
                parentAttachment = attachment
                attachmentType = attachmentTypeIn.name
                caption = attachment.caption
                parentEntityId = parentId

            }
        }
    }

    fun fetchByTypeAndParentId(type: AttachmentType, parentId: Int): List<Entity> {
        return transaction {
            Entity.find {
                Table.attachmentType eq type.name and
                        (Table.parentEntityId eq parentId)
            }.toList()
        }
    }


    fun fetchByType(
        userId: Int,
        role: AppRole,
        type: AttachmentType = AttachmentType.PRODUCT_IMAGE
    ): List<Entity> {
        var attachLinks = mutableListOf<Entity>()
        val attachments = Attachments.Table.fetchByOwnerUserAndRole(userId, role)

        attachments.forEach { attachment ->
            transaction {
                attachment.links
                    .filter { it.isNotExpired() && it.attachmentType == type.name }
            }?.let {
                attachLinks.addAll(it)
            }
        }
        return attachLinks
    }


    object Table : ExposedTableWithStatus2("attachment_file_links") {
        val parentAttachment = reference("parent_attachment_id", Attachments.Table)

        val attachmentType = varchar("attachment_type", VARCHAR_SIZE)

        val parentEntityId = integer("parent_entity_id").nullable()
        val parentEntityClassName = varchar("parent_entity_class_name", VARCHAR_SIZE).nullable()

        val caption = varchar("caption", VARCHAR_SIZE).default("")

    }

    class Entity(id: EntityID<Int>) : ExposedEntityWithStatus2(id, Table) {
        companion object : RootEntityCompanion0<Entity>(Table)

        var parentAttachment by Attachments.Entity referencedOn Table.parentAttachment

        var attachmentType by Table.attachmentType

        var parentEntityId by Table.parentEntityId
        var parentEntityClassName by Table.parentEntityClassName

        var caption by Table.caption

        var attachment: Attachments.Entity? = null
            get() {

                return transaction { parentAttachment }
            }


    }

}
