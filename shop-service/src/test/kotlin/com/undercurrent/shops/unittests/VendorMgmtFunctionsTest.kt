package com.undercurrent.shops.unittests

import com.undercurrent.shared.types.errors.SmsValidationException
import com.undercurrent.shops.TestConsoleShops
import com.undercurrent.shops.TestConsoleShops.invalidNumbersShops
import com.undercurrent.shops.TestConsoleShops.validNumsShops
import com.undercurrent.shops.asserts.AssertCount
import com.undercurrent.shops.asserts.BasicDBIT
import com.undercurrent.shops.repository.proto_versions.ProtoShopVendor
import com.undercurrent.shops.types.exceptions.ShopVendorAlreadyExistsException
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith


class VendorMgmtFunctionsTest : BasicDBIT() {

    override fun assertCount(expected: Int) {
        with(AssertCount(ProtoShopVendor)) {
            assertCount(expected)
        }
    }

    private fun createMultipleVendors() {
        val context = adminContext
        assertCount(0)

        assertFailsWith<SmsValidationException>(
            message = "Vendor not created due to error fetching or creating user for $invalidNumbersShops[1].",
            block = {
                ProtoShopVendor.create(invalidNumbersShops[1], "errTest1")
            }
        )
        assertCount(0)

        val sms: String = validNumsShops[0]
        val nickname: String = "test1"

        ProtoShopVendor.create(smsString = validNumsShops[0], nickname = "test1")
//        ShopVendorCreationService.createValidated(sms = validNums[0], nicknameIn = "test1")
        assertCount(1)
        ProtoShopVendor.create(validNumsShops[1], "test2")
        assertCount(2)

        assertFailsWith<ShopVendorAlreadyExistsException>(
            message = "Vendor with SMS $validNumsShops[1] already exists.",
            block = {
                ProtoShopVendor.create(validNumsShops[1], "errTest2")
            }
        )

//TODO Daria assertError
//        AssertAddVendorOperation()
//            .assertError("123", "test", ValidationError.InvalidSmsError())

        assertCount(2)

    }

    @Test
    fun addNewVendorFunc() {
        TestConsoleShops.resetShopsDb()
        createMultipleVendors()
    }


    @Test
    fun listVendors() {
    }

//    @Test
//    fun removeVendor() {
////        createMultipleVendors()
////        VendorHandlers().removeVendor()
//    }

    @Test
    fun testRemoveVendor() {
    }


}