package com.undercurrent.shops.unittests

import com.undercurrent.shared.experimental.command_handling.ModuleContextOld
import com.undercurrent.shared.utils.tx
import com.undercurrent.shops.TestConsoleShops.validNumsShops
import com.undercurrent.shops.asserts.AssertCount
import com.undercurrent.shops.asserts.BasicDBIT
import com.undercurrent.shops.asserts.CountShopProducts
import com.undercurrent.shops.asserts.CountShopVendors
import com.undercurrent.shops.commands.contexts.ShopContext
import com.undercurrent.shops.repository.proto_versions.*
import com.undercurrent.shops.repository.proto_versions.handlers.CreateShopProductHandler

open class CreateShopProductITHelper : BasicDBIT() {

    override fun assertCount(expected: Int) {
        CountShopProducts().assertCount(expected)
    }

    @Deprecated("Reconsider using this sort of cascading creation (risk of side effects).")
    fun createVendorAndStorefront(
        sms: String = validNumsShops[0],
        nickname: String = "test1"
    ): Pair<ProtoShopVendor, ProtoStorefront> {
        with(ProtoShopVendor.create(sms, nickname)!!) {
            return Pair(this, tx { allStorefronts.first() })
        }
    }

    fun createShopProductForTests(
        storefront: ProtoStorefront,
        name: String = "Diet Coke",
        description: String = "Sugar free coke",
        context: ModuleContextOld,
    ): ProtoShopProduct {
        return CreateShopProductHandler(context).create(
            storefront = storefront,
            name = name,
            description = description,
            imagePaths = listOf()
        )!!
    }

    fun createOneProductCascadingWithAsserts(): ProtoShopProduct {
        return createOneProductCascadingWithAsserts(validNumsShops[0], "test1")
    }

    private fun createOneProductCascadingWithAsserts(sms: String, nickname: String): ProtoShopProduct {
        assertStorefrontCount(0, 0)

        val (vendor, storefront) = createVendorAndStorefront(sms, nickname)
        assertStorefrontCount(0, 1)


        val newProduct = createShopProductForTests(storefront, context = ShopContext.Vendor())
        assertStorefrontCount(1, 1)

        return newProduct
    }


    //todo reconsider more accurate name for this method
    @Deprecated("Fix this up to be more tester-friendly")
    fun assertStorefrontCount(
        products: Int,
        vendors: Int,
        saleItems: Int = 0,
        joinCodes: Int = vendors,
        storefronts: Int = vendors,
        joinCodeUsages: Int = 0,
        joinCodeBurstEvents: Int = 0,
    ) {
        CountShopProducts().assertCount(products)
        CountShopVendors().assertCount(vendors)
        AssertCount(ProtoShopSaleItem).assertCount(saleItems)
        AssertCount(ProtoStorefront).assertCount(storefronts)
        AssertCount(ProtoShopJoinCode).assertCount(joinCodes)
        AssertCount(ProtoShopJoinCodeUsage).assertCount(joinCodeUsages)
        AssertCount(ProtoShopJoinCodeBurst).assertCount(joinCodeBurstEvents)
    }
}