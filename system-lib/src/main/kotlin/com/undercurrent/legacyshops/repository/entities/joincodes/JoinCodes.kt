package com.undercurrent.legacyshops.repository.entities.joincodes

import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.repository.entities.Users
import com.undercurrent.legacy.types.enums.JoinCodeType
import com.undercurrent.legacyshops.repository.companions.JoinCodeCompanion
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.shared.repository.entities.JoinCodeEntity
import com.undercurrent.shared.repository.entities.JoinCodeTable
import com.undercurrent.shared.utils.VARCHAR_SIZE
import com.undercurrent.shared.utils.tx
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column

object JoinCodes : ExposedTableWithStatus2("shop_join_codes"), JoinCodeTable {
    val burstEvent = optReference("burst_event_id", JoinCodeBurstEvent.Table)
    val ownerUser = reference("owner_user_id", Users)
    override val parent = optReference("parent_id", JoinCodes)

    val entityId = integer("entity_id")
    val tag = varchar("tag", VARCHAR_SIZE).nullable()
    override val code: Column<String> = varchar("value", VARCHAR_SIZE).uniqueIndex()

    // add transform for enum for this type
    val entityType = varchar("entity_type", VARCHAR_SIZE)

    
}

class JoinCode(id: EntityID<Int>) : ExposedEntityWithStatus2(id, JoinCodes), JoinCodeEntity<String> {
    companion object : JoinCodeCompanion()

    var burstEvent by JoinCodeBurstEvent.Entity optionalReferencedOn (JoinCodes.burstEvent)
    var ownerUser: User by User referencedOn (JoinCodes.ownerUser)
    var parent: JoinCode? by JoinCode optionalReferencedOn (JoinCodes.parent)

    override var code: String by JoinCodes.code.transform(
        toColumn = {
            it.trim().replace("/", "")
                .replace(" ", "").uppercase()
        },
        toReal = {
            it.trim().replace("/", "")
                .replace(" ", "").uppercase()
        }
    )
    var tag by JoinCodes.tag

    var entityId by JoinCodes.entityId
    var entityType by JoinCodes.entityType.transform(
        { it.name },
        { JoinCodeType.valueOf(it) }
    )

    //    val allSaleItems by SaleItem referrersOn SaleItems.product
    val usages by JoinCodeUsages.Entity referrersOn JoinCodeUsages.Table.joinCode


    override fun toString(): String {
        return tx {
            code
        }
    }
}