package com.undercurrent.legacyshops.repository.entities.joincodes



import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.repository.entities.Users
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefronts
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.shared.utils.tx
import org.jetbrains.exposed.dao.id.EntityID

object JoinCodeBurstEvent {
    object Table : ExposedTableWithStatus2("shop_join_code_burst_event") {
        val storefront = reference("storefront_id", Storefronts)
        val initiatorUser = reference("initiator_user_id", Users)
//        val csvAttachment = optReference("csv_attachment_out_id", AttachmentLinks)

        val quantity = integer("qty")
    }

    class Entity(id: EntityID<Int>) : ExposedEntityWithStatus2(id, Table) {

        var storefront by Storefront referencedOn Table.storefront
        var initiatorUser by User referencedOn Table.initiatorUser
//        var csvAttachment by AttachmentLink optionalReferencedOn (Table.csvAttachment)

        var quantity by Table.quantity

        val joinCodes by JoinCode optionalReferrersOn (JoinCodes.burstEvent)

        companion object : RootEntityCompanion0<Entity>(Table) {
            fun save(
                storefrontIn: Storefront,
                initiatorUserIn: User,
                quantityIn: Int,
//                csvAttachmentIn: AttachmentLink? = null,
            ): Entity? {
                return tx {
                    new {
                        storefront = storefrontIn
                        initiatorUser = initiatorUserIn
                        quantity = quantityIn
//                        csvAttachment = csvAttachmentIn
                    }
                }
            }
        }
    }
}