package com.undercurrent.shops.unittests

import com.undercurrent.shops.asserts.*
import com.undercurrent.shops.commands.ShopCommand
import com.undercurrent.shops.commands.contexts.ShopContext
import org.junit.jupiter.api.Test

//
//class ShopContextTest : BasicDBIT() {
//    override fun assertCount(expected: Int) {
//        with(CountStorefronts()) {
//            assertCount(expected)
//            assertCountUnexpired(expected)
//        }
//        with(CountShopJoinCodes()) {
//            assertCount(expected)
//            assertCountUnexpired(expected)
//        }
//        with(CountShopVendors()) {
//            assertCount(expected)
//            assertCountUnexpired(expected)
//        }
//    }
//
//    @Test
//    fun addVendorIntoDb() {
//        logger.info("Test add vendor into DB")
//        val context = ShopContext.Admin()
//        assertCount(0)
//
//        ShopCommandListAssertions(context)
//            .shouldHave(ShopCommand.ADDVENDOR)
//
//        AssertAddVendorOperation()
//            .assertAddVendor("+12625637281", "vendor1")
//            .assertVendorInDatabase("+12625637281")
//            .assertVendorNotInDatabase("+12625637381")
//            .assertVendorNotInDatabase("+1262-455-7281")
//        assertCount(1)
//    }
//
//    @Test
//    fun addRepeatedVendor() {
//        with(ShopContext.Admin()) {
//            ShopCommandListAssertions(this)
//                .shouldHave(ShopCommand.ADDVENDOR)
////TODO Daria assertError
//            AssertAddVendorOperation()
//                .assertAddVendor("+12625637281", "test vendor")
////                .assertError("+12625637281", "test vendor", ShopVendorAlreadyExistsError())
////                .assertError("+12625637281", "vendor2", ShopVendorAlreadyExistsError())
////                .assertError("+1262-563-7281", "vendor2", ShopVendorAlreadyExistsError())
//        }
//    }
//
//    @Test
//    fun addVendorByShopAdmin() {
//        with(ShopContext.Admin()) {
//            ShopCommandListAssertions(this)
//                .shouldHave(ShopCommand.ADDVENDOR)
////TODO Daria assertError
//            AssertAddVendorOperation()
//                .assertAddVendor("+12625637281", "test vendor")
//                .assertNoAddVendorException("637281", "vendor2")
////                .assertError("637281", "vendor2", ValidationError.InvalidSmsError())
////                .assertError("637281", "vendor2", ValidationError())
//        }
//    }
//}
//
