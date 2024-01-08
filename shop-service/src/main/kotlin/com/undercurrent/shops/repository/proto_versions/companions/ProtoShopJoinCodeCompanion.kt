package com.undercurrent.shops.repository.proto_versions.companions

import com.undercurrent.shared.abstractions.CanFetchByCode
import com.undercurrent.shared.abstractions.CreatableEntity
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.utils.tx
import com.undercurrent.shops.repository.proto_versions.ProtoShopJoinCode
import com.undercurrent.shops.repository.proto_versions.ProtoShopJoinCodesNew
import com.undercurrent.shops.repository.proto_versions.ProtoStorefront
import com.undercurrent.shops.types.wrappers.JoinCodeValue


abstract class ProtoShopJoinCodeCompanion<E : ProtoShopJoinCode> :
    RootEntityCompanion0<E>(ProtoShopJoinCodesNew), CreatableEntity<E>, CanFetchByCode<ProtoShopJoinCode> {

    private lateinit var thisStorefront: ProtoStorefront
    private lateinit var thisCodeStr: String
    private var thisParent: ProtoShopJoinCode? = null

    operator fun invoke(
        storefrontIn: ProtoStorefront,
        codeStrIn: String,
        parentIn: ProtoShopJoinCode? = null,
    ): ProtoShopJoinCodeCompanion<E> {
        this.thisStorefront = storefrontIn
        this.thisCodeStr = codeStrIn
        this.thisParent = parentIn
        return this
    }

    override fun create(): E? {
        return tx {
            new {
                this.storefront = thisStorefront
                this.code = JoinCodeValue(thisCodeStr)
                this.parent = thisParent
            }
        }
    }

    override fun fetchByCode(codeStr: String): ProtoShopJoinCode? {
        return findByColumn(ProtoShopJoinCode, ProtoShopJoinCodesNew.code, codeStr)
    }


}
