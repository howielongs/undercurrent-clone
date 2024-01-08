package com.undercurrent.legacyshops.repository.entities.storefronts

import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.legacy.types.enums.ResponseType
import com.undercurrent.legacy.types.enums.StorefrontPrefType
import com.undercurrent.shared.utils.VARCHAR_SIZE
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction


class StorefrontPrefs {
    companion object {
        fun fetchByKey(keyIn: StorefrontPrefType): List<Entity> {
            return transaction {
                return@transaction Entity.find {
                    Table.key eq keyIn.name
                }.filter { it.isNotExpired() }
            }
        }

        fun save(
            storefrontIn: Storefront,
            keyIn: StorefrontPrefType,
            valueIn: Any,
            datatypeIn: ResponseType,
        ): Entity? {
            return transaction {
                Entity.new {
                    storefront = storefrontIn
                    key = keyIn
                    value = valueIn.toString()
                    datatype = datatypeIn
                }
            }
        }

        fun fetchValue(
            storefrontIn: Storefront,
            keyIn: StorefrontPrefType,
        ): Entity? {
            return transaction {
                return@transaction Entity.find {
                    Table.storefront eq storefrontIn.uid and
                            (Table.key eq keyIn.name)
                }.firstOrNull { it.isNotExpired() }
            }
        }

        fun replaceValue(
            storefrontIn: Storefront,
            keyIn: StorefrontPrefType,
            newValue: Any,
        ) {
            //todo consider using Ancestors here
            expireByKey(storefrontIn, keyIn)
            save(storefrontIn, keyIn, newValue, ResponseType.FEE_PERCENT)
        }


        fun expireByKey(
            storefrontIn: Storefront,
            keyIn: StorefrontPrefType,
        ) {
            transaction {
                Entity.find {
                    Table.storefront eq storefrontIn.uid and
                            (Table.key eq keyIn.name)
                }
                    .filter { it.isNotExpired() }
                    .forEach { it.expire() }
            }
        }
    }

    class Entity(id: EntityID<Int>) : ExposedEntityWithStatus2(id, Table) {
        companion object : RootEntityCompanion0<Entity>(Table)

        var storefront by Storefront referencedOn (Table.storefront)

        var key by Table.key.transform(
            { it.name }, { StorefrontPrefType.valueOf(it) }
        )
        var value by Table.value
        var datatype by Table.datatype.transform(
            { it.name }, { ResponseType.valueOf(it) }
        )

        //todo somehow export value as proper type with when clause
    }

    object Table : ExposedTableWithStatus2("shop_storefront_prefs") {
        val storefront = reference("storefront_id", Storefronts)

        val key = varchar("key", VARCHAR_SIZE)
        val value = varchar("value", VARCHAR_SIZE)
        val datatype = varchar("datatype", VARCHAR_SIZE)

        
    }
}