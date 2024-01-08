package com.undercurrent.shops.repository.proto_versions.companions

import com.undercurrent.shared.SystemUserNew
import com.undercurrent.shared.abstractions.CreatableEntity
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.utils.tx
import com.undercurrent.shops.repository.proto_versions.ProtoShopCustomer
import com.undercurrent.shops.repository.proto_versions.ProtoShopCustomers
import com.undercurrent.shops.repository.proto_versions.ProtoShopJoinCode
import com.undercurrent.shops.repository.proto_versions.ProtoStorefront


open class ProtoShopCustomerCompanion<E : ProtoShopCustomer> : RootEntityCompanion0<E>(ProtoShopCustomers),
    CreatableEntity<E> {

    private lateinit var thisSystemUser: SystemUserNew
    private var thisStorefront: ProtoStorefront? = null
    private lateinit var thisJoinCode: ProtoShopJoinCode

    operator fun invoke(
        userIn: SystemUserNew,
        storefrontIn: ProtoStorefront,
    ): ProtoShopCustomerCompanion<E> {
        this.thisSystemUser = userIn
        this.thisStorefront = storefrontIn
        return this
    }

    private fun fetchByUser(user: SystemUserNew): E? {
        return tx {
            all().singleOrNull { it.isNotExpired() && it.user.id == user.id }
        }
    }

    //todo will also want to create JoinCodeUsage on top of this?
    override fun create(): E? {
        fetchByUser(thisSystemUser)?.let {
            return null
        }

        val storefrontToSave = thisStorefront ?: tx { thisJoinCode.storefront }

        return tx {
            new {
                this.user = thisSystemUser
                this.storefront = storefrontToSave
            }
        }
    }
}

