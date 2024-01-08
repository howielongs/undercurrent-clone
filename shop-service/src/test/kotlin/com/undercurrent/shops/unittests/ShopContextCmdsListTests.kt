package com.undercurrent.shops.unittests

import com.undercurrent.shops.TestConsoleShops
import com.undercurrent.shops.asserts.CountShopProducts
import com.undercurrent.shops.asserts.CountShopSaleItems
import com.undercurrent.shops.asserts.CountShopVendors
import com.undercurrent.shops.asserts.CountUserAccounts
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.Ignore


class ShopContextCmdsListTests : CreateSaleItemHelper() {
    private fun assertCount(vendors: Int, users: Int, products: Int = 0, saleItems: Int = 0) {
        CountShopVendors().assertCount(vendors)
        CountUserAccounts().assertCount(users)


        CountShopProducts().assertCount(products)
        CountShopSaleItems().assertCount(saleItems)

    }

    @BeforeEach
    override fun setUp() {
        TestConsoleShops.setUpTests()
    }

    @Ignore
    @Test
    fun createSaleItemOnProduct() {
//        createNewSaleItemCascade()
    }

//    @Test
//    fun `test hidden commands as vendor progresses`() {
//        val userSms = TestConsole.validNums[0]
//
//        val alwaysCmds = listOf(START, CANCEL, FEEDBACK).toTypedArray()
//        val neverCmds = listOf(ADDVENDOR, CHECKOUT, LIQUIDITY_AMT, SWAP, MENU).toTypedArray()
//
//        assertCount(0, 0)
//        val vendorUser: User = User.create(userSms).component1()!!
//        assertCount(0, 1)
//
//
//        //LOAD VENDOR USER INTO CONTEXT TO EVALUATE AVAILABLE OPTIONS
//        //CONSIDERING USING CHANNELS INSTEAD OF USER FOR THIS
//        var context = ShopContext.Vendor(vendorUser)
//
//
//        //NONE OF THE VENDOR COMMANDS SHOULD BE AVAILABLE
//        ShopCommandListAssertions(context)
//            .shouldNotHave(ADDITEMSKU, RMITEMSKU, RMPRODUCT)
//            .shouldNotHave(ADDPRODUCT, ADDITEMSKU, CONFIRM, RMITEMSKU, RMPRODUCT)
//            .shouldHave(*alwaysCmds)
//            .shouldNotHave(*neverCmds)
//
//        //VENDOR CREATED, BUT NOTHING IN INVENTORY YET
//        val (vendor, storefront) = createVendorAndStorefront(sms = userSms)
//
//        ShopCommandListAssertions(context)
//            .shouldHave(ADDPRODUCT)
//            .shouldNotHave(ADDITEMSKU, ADDVENDOR, RMPRODUCT, RMITEMSKU, CONFIRM)
//            .shouldHave(*alwaysCmds)
//            .shouldNotHave(*neverCmds)
//
//
//        //FIRST PRODUCT CREATED, NOW ABLE TO ADD ITEMSKU (BUT NOT YET RMITEMSKU)
//        val product = createShopProductForTests(storefront, context = context)
//        assertCount(1, 1, products = 1)
//
//        ShopCommandListAssertions(context)
//            .shouldHave(ADDPRODUCT, ADDITEMSKU, RMPRODUCT)
//            .shouldNotHave(RMITEMSKU, CONFIRM)
//            .shouldHave(*alwaysCmds)
//            .shouldNotHave(*neverCmds)
//
//
//        //FIRST PRODUCT CREATED, NOW ABLE TO ADD ITEMSKU (BUT NOT YET RMITEMSKU)
//        val saleItem = createSaleItemFromProduct(context, product, "1.00", "test")
//
//        ShopCommandListAssertions(context)
//            .shouldHave(ADDPRODUCT, ADDITEMSKU, RMPRODUCT, RMITEMSKU)
//            .shouldNotHave(CONFIRM)
//            .shouldHave(*alwaysCmds)
//            .shouldNotHave(*neverCmds)
//
//        assertCount(1, 1, products = 1, saleItems = 1)
//
//
//        //TODO: Continue with customer going through menus (other tests)
//        //inherit those methods and then continue with vendor context tracking for confirm, etc.
//
//    }
//
//    @Test
//    fun commandsForSwapBotContexts() {
//        ShopCommandListAssertions(SwapAppContext.Banker())
//            .shouldHave(CANCEL, LIQUIDITY_AMT)
//            .shouldNotHave(ADDVENDOR, MYINFO, CHECKOUT, CONFIRM, SWAP)
//
//        ShopCommandListAssertions(SwapAppContext.Admin())
//            .shouldHave(CANCEL, LIQUIDITY_AMT)
//            .shouldNotHave(ADDVENDOR, MYINFO, CHECKOUT, CONFIRM, SWAP)
//
//        ShopCommandListAssertions(SwapAppContext.Client())
//            .shouldHave(CANCEL, SWAP)
//            .shouldNotHave(ADDVENDOR, MYINFO, CHECKOUT, CONFIRM, LIQUIDITY_AMT)
//    }
//
//
//    @Test
//    fun commandsForShopBotContexts() {
//        ShopCommandListAssertions(ShopContext.Admin())
//            .shouldHave(CANCEL, ADDVENDOR, MYINFO)
//            .shouldNotHave(SWAP, CHECKOUT, CONFIRM, LIQUIDITY_AMT)
//
//        ShopCommandListAssertions(ShopContext.Vendor())
//            .shouldHave(CANCEL, MYINFO)
//            .shouldNotHave(ADDVENDOR, SWAP, CHECKOUT, LIQUIDITY_AMT)
//
//        ShopCommandListAssertions(ShopContext.Customer())
//            .shouldHave(CANCEL, CHECKOUT, MYINFO)
//            .shouldNotHave(ADDVENDOR, SWAP, CONFIRM, LIQUIDITY_AMT)
//
//
//    }
}