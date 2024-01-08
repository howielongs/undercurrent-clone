package com.undercurrent.shops

import com.undercurrent.setup.TestConsoleCentral
import com.undercurrent.setup.TestConsoleCentral.baseSmsNums
import com.undercurrent.shared.SystemUsersNew
import com.undercurrent.shops.repository.proto_versions.*

object TestConsoleShops {

    fun setUpTests() {
        resetShopsDb()
    }

    const val ENFORCE_JOIN_CODE_USAGE_COUNT_ASSERTS = false

    val validNumsShops = baseSmsNums
    val invalidNumbersShops = listOf("37281", "2691")
    val uuidsShops = TestConsoleCentral.uuids

    fun resetShopsDb() {
        val shopTables = listOf(
            SystemUsersNew,
            ProtoShopCustomers,
            ProtoShopJoinCodeBursts,
            ProtoShopJoinCodesNew,
            ProtoShopJoinCodeUsages,
            ProtoShopProductsNew,
            ProtoShopSaleItems,
            ProtoShopVendorsNew,
            ProtoStorefronts,
            ProtoShopSKUs,
            ProtoShopOrders,
            ProtoShopOrderItems,
        )
//         TestConsoleCentral.setUpTestsCentral(tablesToReset = shopTables, dbFileName = "lemur_shop_test")
        TestConsoleCentral.setUpTestsCentral(tablesToReset = shopTables)
    }
}