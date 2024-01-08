package com.undercurrent.shops.unittests

import com.undercurrent.shared.SystemUserNew
import com.undercurrent.shared.utils.tx
import com.undercurrent.shops.TestConsoleShops
import com.undercurrent.shops.TestConsoleShops.ENFORCE_JOIN_CODE_USAGE_COUNT_ASSERTS
import com.undercurrent.shops.TestConsoleShops.validNumsShops
import com.undercurrent.shops.asserts.*
import com.undercurrent.shops.commands.contexts.ShopContext
import com.undercurrent.shops.repository.proto_versions.ProtoShopCustomer
import com.undercurrent.shops.repository.proto_versions.ProtoShopJoinCode
import com.undercurrent.shops.repository.proto_versions.ProtoShopVendor
import com.undercurrent.shops.repository.proto_versions.ProtoStorefront
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import com.undercurrent.legacy.utils.joincodes.UniqueJoinCodeGenerator
import kotlin.test.Ignore
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull


/*
IT - is for Integration Tests

TODO - test has the same name as StorefrontCustomerLinker
But what being tested over here totally differs from what the original class does
 */
class StorefrontCustomerLinkerIT : CreateShopProductITHelper() {

    @BeforeEach
    override fun setUp() {
        TestConsoleShops.resetShopsDb()
    }

    @Test
    fun addNewProduct() {
        TestConsoleShops.resetShopsDb()
        createOneProductCascadingWithAsserts()
    }

    @Test
    fun `customer can join a storefront with valid code`() {
        CountShopCustomers().assertCount(expected = 0)
        val customerUser = SystemUserNew.create(validNumsShops.first()).component1()!!

        assertNotNull(customerUser) { "Expected user to be created, got $customerUser" }

        val product = createOneProductCascadingWithAsserts()

        val joinCodesUser = tx { product.joinCodes }
        assertCount(1, joinCodesUser)

        val firstCode = joinCodesUser.first()
        val codeTextUser = tx { firstCode.code.value.toString() }

        assertNotNull(codeTextUser) { "Expected code to be created, got $codeTextUser" }

        val storefrontProduct1 = tx { product.storefront }

        CountUserAccounts().assertCount(expected = 1)
        CountStorefronts().assertCount(expected = 1)
        CountShopProducts().assertCount(expected = 1)
        CountShopJoinCodes().assertCount(expected = 1)

        val newCustomer = ProtoShopCustomer(userIn = customerUser, storefrontProduct1).create()

        assertNotNull(newCustomer) { "Expected customer to be created, got $newCustomer" }
        CountShopCustomers().assertCount(expected = 1)
    }

    @Test
    fun `customer can join a second storefront with a different join code`() {
        TestConsoleShops.resetShopsDb()

        CountShopCustomers().assertCount(expected = 0)

        val sms1 = validNumsShops[0]
        val sms2 = validNumsShops[1]

        val firstNickname = "test1"
        val secondNickname = "test2"

        val customerUser1 = SystemUserNew.create(sms1).component1()!!
        assertNotNull(customerUser1) { "Expected user to be created, got $customerUser1" }

        val customerUser2 = SystemUserNew.create(sms2).component1()!!
        assertNotNull(customerUser2) { "Expected user to be created, got $customerUser2" }

        val (vendor1, storefront1) = createVendorAndStorefront(sms1, firstNickname)
        assertStorefrontCount(0, 1)
        val product1 = createShopProductForTests(storefront1, context = ShopContext.Vendor())
        assertStorefrontCount(1, 1)

        val (vendor2, storefront2) = createVendorAndStorefront(sms2, secondNickname)
        assertStorefrontCount(1, 2)
        val product2 = createShopProductForTests(storefront2, context = ShopContext.Vendor())
        assertStorefrontCount(2, 2)

        val joinCodesUser1 = tx { product1.joinCodes }
        assertCount(1, joinCodesUser1)

        val joinCodesUser2 = tx { product2.joinCodes }
        assertCount(1, joinCodesUser2)

        val codeTextUser1 = tx { joinCodesUser1.first().code.value.toString() }
        val storefrontProduct1 = tx { product1.storefront }

        val codeTextUser2 = tx { joinCodesUser2.first().code.value.toString() }
        var storefrontProduct2 = tx { product2.storefront }

        assertNotEquals(codeTextUser1, codeTextUser2)

        CountUserAccounts().assertCount(expected = 2)
        CountStorefronts().assertCount(expected = 2)
        CountShopProducts().assertCount(expected = 2)
        CountShopJoinCodes().assertCount(expected = 2)

        val newCustomerStoreFront1 = ProtoShopCustomer(userIn = customerUser1, storefrontProduct1).create()

        assertNotNull(newCustomerStoreFront1) { "Expected customer to be created, got $newCustomerStoreFront1" }
        CountShopCustomers().assertCount(expected = 1)

        val newCustomerStoreFront2 = ProtoShopCustomer(userIn = customerUser2, storefrontProduct2).create()
        assertNotNull(newCustomerStoreFront2) { "Expected customer to be created, got $newCustomerStoreFront2" }
        CountShopCustomers().assertCount(expected = 2)
    }

    @Test
    fun `customer using different join codes linking to same storefront will be notified they're already a member`() {
        CountShopCustomers().assertCount(expected = 0)

        val vendorSms = validNumsShops[0]
        val customerSms = validNumsShops[1]
        val vendorNickname = "test1"


        CountUserAccounts().assertCount(expected = 0)
        val vendorUser = SystemUserNew.create(vendorSms).component1()!!
        CountUserAccounts().assertCount(expected = 1)
        assertNotNull(vendorUser) { "Expected vendor user to be created, got $vendorUser" }

        val customerUser = SystemUserNew.create(customerSms).component1()!!
        CountUserAccounts().assertCount(expected = 2)
        assertNotNull(customerUser) { "Expected customer user to be created, got $customerUser" }

        val (vendor1: ProtoShopVendor, storefront: ProtoStorefront) = createVendorAndStorefront(
            sms = vendorSms,
            nickname = vendorNickname
        )
        assertStorefrontCount(products = 0, vendors = 1, storefronts = 1, joinCodes = 1, joinCodeUsages = 0)
        CountUserAccounts().assertCount(expected = 2)

        val joinCode1 = tx { storefront.joinCodes.first() }

        assertNumCustomers(0)
        val customer1: ProtoShopCustomer? = ProtoShopCustomer(userIn = customerUser, storefrontIn = storefront!!).create()
        assertNumCustomers(1)

        val existenceChecker = { code: String ->
            ProtoShopJoinCode.fetchByCode(code) != null
        }

        val codeGenerator = UniqueJoinCodeGenerator(existenceChecker = existenceChecker)

        val joinCode2: ProtoShopJoinCode? =
            ProtoShopJoinCode(storefrontIn = storefront, codeStrIn = codeGenerator.generate()!!).create()

        assertStorefrontCount(0, 1, joinCodes = 2, storefronts = 1)

        //todo add assert to match joinCode2!!.storefront.id and storefront.id
        val storefrontFromJoinCode2: ProtoStorefront = tx { joinCode2!!.storefront }

        val customer2: ProtoShopCustomer? = ProtoShopCustomer(
            userIn = customerUser,
            storefrontIn = storefrontFromJoinCode2
        ).create()

        assert(customer2 == null) { "Expecting null when joining on second joinCode (customer already member of storefront)" }
        assertNumCustomers(1)

        if (ENFORCE_JOIN_CODE_USAGE_COUNT_ASSERTS) {
            assertStorefrontCount(0, 1, joinCodes = 2, storefronts = 1, joinCodeUsages = 2)
        } else {
            assertStorefrontCount(0, 1, joinCodes = 2, storefronts = 1)
        }
    }

    private fun assertNumCustomers(expected: Int) {
        assertCount(expected, tx { ProtoShopCustomer.all().toList() })
    }

    @Ignore
    @Test
    fun `customer using same join code will be notified they're already a member`() {

//        val customer1: ShopCustomer? = ShopCustomer(
//            userIn = customerUser,
//            storefrontIn = storefrontFromJoinCode2
//        ).create()
//
//// Should return null
//        val customer2: ShopCustomer? = ShopCustomer(
//            userIn = customerUser,
//            storefrontIn = storefrontFromJoinCode2
//        ).create()

    }

//    @Ignore
//    @Test
//    fun `customers can join the same storefront using different join codes`() {
//        //todo this belongs at service layer for parsing joinCodes to storefronts
//        TODO()
////3 users (1 is vendor)
//        // 2 users -> 2 customers
//        // create storeFront linked to Vendor
//
//        // extract joinCode1 from Storefront
//        // create joinCode2 using same storefront
//    }

    @Ignore
    @Test
    fun `customer using different join codes linking to different storefronts will be notified their current storefront has changed`() {
        TODO()

    }

    @Ignore
    @Test
    fun `customers can join a storefront with the same join code`() {
        TODO()

    }

    @Ignore
    @Test
    fun `customer unable to join a storefront if joincode is marked as expired (vendor should be notified)`() {
        TODO()

    }

    @Ignore
    @Test
    fun `customer unable to join a storefront if invalid join code`() {
        TODO()

    }


}
