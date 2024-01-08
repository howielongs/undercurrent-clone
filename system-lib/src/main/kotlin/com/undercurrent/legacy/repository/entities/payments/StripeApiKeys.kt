package com.undercurrent.legacy.repository.entities.payments


import com.undercurrent.legacy.repository.abstractions.BaseEventsEntity
import com.undercurrent.legacy.repository.abstractions.BaseEventsTable
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefronts
import com.undercurrent.legacy.types.enums.StripeKeyType
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.utils.VARCHAR_SIZE
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

object StripeApiKeys {
    fun save(storefrontIn: Storefront, keyValue: String, stripeKeyType: StripeKeyType = StripeKeyType.TEST): Entity? {
        return transaction {
            Entity.new {
                storefront = storefrontIn
                stripeApiSecretKey = keyValue
                type = stripeKeyType.name
            }
        }
    }

    object Table : BaseEventsTable("stripe_api_keys") {
        val storefront = reference("storefront_id", Storefronts)
        val stripeApiSecretKey = varchar("stripe_secret_key", VARCHAR_SIZE).nullable()
    }

    class Entity(id: EntityID<Int>) : BaseEventsEntity(id, Table) {
        companion object : RootEntityCompanion0<Entity>(Table)

        var storefront by Storefront referencedOn Table.storefront
        var stripeApiSecretKey by Table.stripeApiSecretKey

    }
}

