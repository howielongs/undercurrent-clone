package com.undercurrent.shops.asserts

import com.undercurrent.shared.SystemUserNew
import com.undercurrent.shared.repository.bases.RootEntity0
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.utils.tx
import com.undercurrent.shops.repository.proto_versions.*
import kotlin.test.assertEquals


sealed class BaseAssertCount : BasicDBIT()

class CountShopProducts : AssertCount<ProtoShopProduct>(ProtoShopProduct)
class CountShopSaleItems : AssertCount<ProtoShopSaleItem>(ProtoShopSaleItem)

class CountShopJoinCodes : AssertCount<ProtoShopJoinCode>(ProtoShopJoinCode)
class CountStorefronts : AssertCount<ProtoStorefront>(ProtoStorefront)
class CountShopVendors : AssertCount<ProtoShopVendor>(ProtoShopVendor)
class CountShopCustomers : AssertCount<ProtoShopCustomer>(ProtoShopCustomer)

open class CountUserAccounts : AssertCount<SystemUserNew>(SystemUserNew)

open class AssertCount<E : RootEntity0>(private val compObj: RootEntityCompanion0<E>) : BaseAssertCount() {


     fun assertCountUnexpired(expected: Int) {
        assertCount(expected, true)
    }

    override fun assertCount(expected: Int) {
        assertCount(expected, false)
    }

    private fun assertCount(expected: Int, isUnexpired: Boolean = false) {
        val labelStr = if (isUnexpired) " unexpired" else ""
        val actual = if (isUnexpired) countUnexpired() else count()
        val typeName = requireNotNull(compObj.javaClass.enclosingClass?.simpleName) {
            "Type name should not be null"
        }

        assertEquals(
            expected = expected,
            actual = actual,
            message = "$typeName$labelStr count: $actual (expected: $expected)"
        )
    }

    private fun countUnexpired(): Int {
        return fetchUnexpired().count()
    }

    private fun count(): Int {
        return fetchAll().count()
    }

    private fun fetchUnexpired(): List<E> {
        return compObj.fetchUnexpired(compObj)
    }

    open fun fetchAll(): List<E> {
        return tx { compObj.all().toList() }
    }

    private fun assertTypeName(typeName: String?) {
        val containsCompanionInName = typeName?.lowercase()?.contains("companion")
        assert(containsCompanionInName != true) { "Should not include 'Companion' in name: $typeName" }
    }


}