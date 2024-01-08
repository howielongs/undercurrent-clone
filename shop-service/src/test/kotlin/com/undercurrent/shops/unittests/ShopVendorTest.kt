package com.undercurrent.shops.unittests

import com.undercurrent.shared.SystemUserNew
import com.undercurrent.shared.utils.tx
import com.undercurrent.shops.TestConsoleShops
import com.undercurrent.shops.TestConsoleShops.uuidsShops
import com.undercurrent.shops.TestConsoleShops.validNumsShops
import com.undercurrent.shops.asserts.AssertCount
import com.undercurrent.shops.asserts.BasicDBIT
import com.undercurrent.shops.asserts.CountShopVendors
import com.undercurrent.shops.asserts.CountUserAccounts
import com.undercurrent.shops.repository.proto_versions.ProtoShopVendor
import com.undercurrent.testutils.SystemUserBaseError
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.Ignore


class ShopVendorTest : BasicDBIT() {
    @BeforeEach
    override fun setUp() {
        TestConsoleShops.setUpTests()
    }

    @Ignore
    @Test
    fun `test create user then vendor using user object`() {

        val sms = validNumsShops[0]
        val uuid = uuidsShops[0]
        val vendorNickname = "test1"

        CountUserAccounts().assertCount(expected = 0)
        val userCreationResult: SystemUserBaseError = SystemUserNew.create(sms, uuid)
        CountUserAccounts().assertCount(expected = 1)

        val newUser = userCreationResult.component1()!!

        AssertCount<ProtoShopVendor>(ProtoShopVendor).assertCount(expected = 0)

        val newVendor = ProtoShopVendor(newUser, vendorNickname).create()

        AssertCount<ProtoShopVendor>(ProtoShopVendor).assertCount(expected = 1)
        assertVendorFields(newVendor, sms, uuid, false, vendorNickname)

        val newVendor2 = ProtoShopVendor(newUser, vendorNickname).create()

        assertNull(newVendor2) { "Vendor should be null as already exists on this user" }
    }

    @Test
    fun `test create user then vendor using sms`() {
        TestConsoleShops.resetShopsDb()
        //todo impl this with new Create() functionality

        val sms = validNumsShops[0]
        val uuid = uuidsShops[0]
        val vendorNickname = "test1"

        CountUserAccounts().assertCount(expected = 0)
        val userCreationResult: SystemUserBaseError = SystemUserNew.create(sms, uuid)
        CountUserAccounts().assertCount(expected = 1)

        AssertCount<ProtoShopVendor>(ProtoShopVendor).assertCount(expected = 0)
        val vendorCreationResult: ProtoShopVendor? = ProtoShopVendor.create(sms, vendorNickname)
        AssertCount<ProtoShopVendor>(ProtoShopVendor).assertCount(expected = 1)
        assertVendorFields(vendorCreationResult, sms, uuid, false, vendorNickname)
//      TODO  assertNoException(vendorCreationResult.getError())
    }

    @Test
    fun `test vendor created with correct parameters and can be fetched by sms`() {
        TestConsoleShops.resetShopsDb()

        val vendorNickname = "test1"

        assertCount(0)

        val vendorCreationResult: ProtoShopVendor? = ProtoShopVendor.create(validNumsShops[0], vendorNickname)

        assertCount(1)
        assertVendorExists(validNumsShops[0])
        assertCount(1)
        assertVendorFields(vendorCreationResult, validNumsShops[0], null, false, vendorNickname)
//        TODO assertNoException(vendorCreationResult.getError())
    }


    @Test
    fun `test ShopVendor fetchBySignalSms doesn't create new vendor`() {
        TestConsoleShops.resetShopsDb()

        assertCount(0)
        assertVendorDoesntExist("123")
        assertCount(0)
    }

    override fun assertCount(expected: Int) {
        CountShopVendors().assertCount(expected)
        CountUserAccounts().assertCount(expected)
    }


    private fun assertVendorFields(
        vendor: ProtoShopVendor?,
        signalSms: String?,
        signalUuid: String?,
        isAdmin: Boolean,
        vendorNickname: String
    ) {
        tx {
            assertNotNull(vendor)
            Assertions.assertEquals(signalSms, vendor?.user?.signalSms)
            Assertions.assertEquals(signalUuid, vendor?.user?.signalUuid)
            Assertions.assertEquals(isAdmin, vendor?.user?.isAdmin)
            Assertions.assertEquals(vendorNickname, vendor?.nickname)
        }
    }

    private fun assertVendorDoesntExist(sms: String) {
        val vendor: ProtoShopVendor? = ProtoShopVendor.fetchBySignalSms(sms)
        assertNull(vendor)
    }

    private fun assertVendorExists(sms: String) {
        val vendor = ProtoShopVendor.fetchBySignalSms(sms)
        assertNotNull(vendor)
    }

}