package com.undercurrent.legacy.repository.entities.payments


import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.legacy.service.crypto.mobilecoin.requests.MobAcctNameText
import com.undercurrent.legacy.service.crypto.mobilecoin.requests.MobileCoinText
import com.undercurrent.legacy.types.enums.status.ActiveMutexStatus
import com.undercurrent.shared.utils.VARCHAR_SIZE
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

object MobAccounts {
    object Table : ExposedTableWithStatus2("mobilecoin_accounts") {
        //may want some unique constraints on this
        val accountId = varchar("account_id", VARCHAR_SIZE).uniqueIndex()
        val name = varchar("name", VARCHAR_SIZE).uniqueIndex()
        val mainAddress = varchar("main_address", VARCHAR_SIZE)

        //todo make this nullable
        val rawJson = varchar("raw_json", VARCHAR_SIZE)
        
    }

    class Entity(id: EntityID<Int>) : ExposedEntityWithStatus2(id, Table) {
        var accountId by Table.accountId
        var name by Table.name
        var mainAddress by Table.mainAddress
        var rawJson by Table.rawJson

        companion object : RootEntityCompanion0<Entity>(Table) {
            fun save(
                accountIdIn: MobileCoinText,
                mainAddressIn: MobileCoinText,
                nameIn: MobAcctNameText,
                rawJsonIn: String = "",
            ): Entity? {
                Log.debug("Saving MobAccount: ${nameIn.cleanedValue}")
                return transaction {
                    Entity.new {
                        accountId = accountIdIn.cleanedValue
                        mainAddress = mainAddressIn.cleanedValue
                        name = nameIn.cleanedValue
                        rawJson = rawJsonIn
                        status = ActiveMutexStatus.ACTIVE.name
                    }
                }
            }

            fun fetch(nameIn: MobAcctNameText): Entity? {
                return transaction {
                    Entity.all().singleOrNull {
                        nameIn.cleanedValue == it.name && it.isNotExpired()
                    }
                }
            }
        }
    }


}
