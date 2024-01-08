package com.undercurrent.shops.repository.proto_versions.companions

import com.undercurrent.shared.abstractions.CreatableEntity
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.utils.tx
import com.undercurrent.shops.repository.proto_versions.ProtoShopProduct
import com.undercurrent.shops.repository.proto_versions.ProtoShopProductsNew
import com.undercurrent.shops.repository.proto_versions.ProtoStorefront

abstract class ProtoShopProductCompanion<E : ProtoShopProduct> :
    RootEntityCompanion0<E>(ProtoShopProductsNew), CreatableEntity<E> {

    private lateinit var thisStorefront: ProtoStorefront
    private lateinit var thisName: String
    private lateinit var thisDescription: String


    operator fun invoke(
        storefrontIn: ProtoStorefront,
        nameIn: String,
        descriptionIn: String,
    ): ProtoShopProductCompanion<E> {
        this.thisStorefront = storefrontIn
        this.thisName = nameIn
        this.thisDescription = descriptionIn
        return this
    }

    override fun create(): E? {
        return tx {
            new {
                this.storefront = thisStorefront
                this.name = thisName
                this.details = thisDescription
            }
        }
    }

}