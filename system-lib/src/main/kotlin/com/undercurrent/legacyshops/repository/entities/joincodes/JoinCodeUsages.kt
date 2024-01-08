package com.undercurrent.legacyshops.repository.entities.joincodes




import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.repository.entities.Users
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.shared.utils.tx
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction


class JoinCodeUsages {

    companion object {
        fun save(
            userIn: User,
            codeStrIn: String,
        ): Entity? {
            return JoinCode.fetchByCode(codeStrIn.trim().replace("/", "")
                .replace(" ", "").uppercase())?.let {
                save(userIn, it)
            }
        }

        fun save(
            userIn: User,
            codeValue: JoinCode,
        ): Entity? {
            return transaction {
                Entity.new {
                    user = userIn
                    joinCode = codeValue
                }
            }
        }

    }

    class Entity(id: EntityID<Int>) : ExposedEntityWithStatus2(id, Table) {
        companion object : RootEntityCompanion0<Entity>(Table)

        var user by User referencedOn (Table.user)
        var joinCode by JoinCode referencedOn (Table.joinCode)

        fun fetchTotalUsages(): Int {
            return tx { joinCode.usages }.toList().count()
        }
    }

    object Table : ExposedTableWithStatus2("shop_join_code_usages") {
        val user = reference("user_id", Users)
        val joinCode = reference("join_code_id", JoinCodes)

        
    }

}
