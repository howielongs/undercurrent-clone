package com.undercurrent.legacy.repository.entities.system

import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.utils.VARCHAR_SIZE
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.repository.entities.Users
import org.jetbrains.exposed.dao.id.EntityID

class Ancestors(
    var ownerUser: User? = null,
    var ownerRole: AppRole? = null,
    var entityType: String? = null,
    var memo: String = "",
    var oldEntity: Int = -1,
    var newEntity: Int = -1,
) {

    fun save(role: AppRole): ExposedEntityWithStatus2? {
        this@Ancestors.ownerUser?.let { thisUser ->
            return tx {
                new.new {
                    ownerUser = thisUser
                    ownerRole = (this@Ancestors.ownerRole ?: role).name
                    entityType = this@Ancestors.entityType.toString()
                    oldEntityId = this@Ancestors.oldEntity
                    newEntityId = this@Ancestors.newEntity
                    memo = this@Ancestors.memo

                }
            }
        }
        return null
    }

    object Table : ExposedTableWithStatus2( "data_ancestors") {
        val ownerUser = reference("owner_user_id", Users)

        val ownerRole = varchar("owner_role", VARCHAR_SIZE).default("")
        val entityType = varchar("entity_type", VARCHAR_SIZE)
        val memo = varchar("memo", VARCHAR_SIZE)

        val oldEntityId = integer("old_entity_id")
        val newEntityId = integer("new_entity_id")

        //todo see about generic references here
//        val oldEntity = reference("old_entity_id", Ancestors)
//        val newEntity = reference("new_entity_id", Ancestors)
    }

    class new(id: EntityID<Int>) : ExposedEntityWithStatus2(id, thisTable = Table) {
        companion object : RootEntityCompanion0<new>(Table) {
            operator fun invoke(body: Ancestors.() -> Unit = {}): Ancestors {
                with(Ancestors().apply(body)) {
                    return Ancestors(
                            ownerUser = ownerUser,
                            ownerRole = ownerRole,
                            entityType = entityType,
                            oldEntity = oldEntity,
                            newEntity = newEntity,
                            memo = memo,
                    )
                }
            }
        }

        var ownerUser by User referencedOn Table.ownerUser
        var ownerRole by Table.ownerRole
        var entityType by Table.entityType
        var memo by Table.memo

        var oldEntityId by Table.oldEntityId
        var newEntityId by Table.newEntityId

        // impl swap_nodes for this?
//        var oldEntity by Ancestor referencedOn Ancestors.oldEntity
//        var newEntity by Ancestor referencedOn Ancestors.newEntity
    }


}