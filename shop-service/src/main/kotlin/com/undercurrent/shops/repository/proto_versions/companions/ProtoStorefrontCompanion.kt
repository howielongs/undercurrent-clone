package com.undercurrent.shops.repository.proto_versions.companions

import com.undercurrent.shared.abstractions.CreatableEntity
import com.undercurrent.shared.repository.bases.RootEntity0
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.utils.tx
import com.undercurrent.shops.repository.proto_versions.*
import org.jetbrains.exposed.sql.lowerCase

interface FetchableByJoinCode<E : RootEntity0> {
    fun findByJoinCode(joinCode: ProtoShopJoinCode): E?
    fun findByJoinCode(joinCodeText: String): E?
}
abstract class ProtoStorefrontCompanion<E : ProtoStorefront> :
    RootEntityCompanion0<E>(ProtoStorefronts), CreatableEntity<E>, FetchableByJoinCode<ProtoStorefront> {

    private lateinit var thisVendor: ProtoShopVendor

    operator fun invoke(
        vendorIn: ProtoShopVendor,
    ): ProtoStorefrontCompanion<E> {
        this.thisVendor = vendorIn
        return this
    }


    override fun create(): E? {
        return tx {
            new {
                this.vendor = thisVendor
            }
        }
    }

    override fun findByJoinCode(joinCode: ProtoShopJoinCode): ProtoStorefront? {
        return findByJoinCode(tx { joinCode.code.toString() })
    }

    override fun findByJoinCode(joinCodeText: String): ProtoStorefront? {
        return tx {
            ProtoStorefront.find { ProtoShopJoinCodesNew.code.lowerCase() eq joinCodeText.lowercase() }.firstOrNull()
        }
    }


}
