package com.undercurrent.shop.nodes

import com.undercurrent.legacy.data_transfer_objects.currency.FiatAmount
import com.undercurrent.legacy.data_transfer_objects.currency.ReceiptValuesFiat
import com.undercurrent.legacy.types.enums.CryptoType
import com.undercurrent.legacyshops.nodes.vendor_nodes.ConfirmOrderNodes
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCode
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCodeBurstEvent
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCodeUsages
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrder
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrders
import com.undercurrent.legacyshops.repository.entities.shop_orders.Invoice
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomer
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopVendor
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.setup.BaseTestClass
import com.undercurrent.setup.TestConsoleCentral
import com.undercurrent.setup.defaultSystemTables
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.repository.entities.AdminProfile
import com.undercurrent.system.repository.entities.User
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

//todo some of the tests verge into needing tests around listing DeliveryOrders and their formats

/**
 * Test cases to cover:
 * - no orders to confirm
 * - orders to confirm, but no receivable BTC wallet added
 * - orders to confirm, receivable BTC wallet added, but no BTC wallet running
 *
 * - orders to confirm: assert formatting
 * - decline order (change selection in menu [test formatting of multiple entries])
 */
class ConfirmOrderNodesTest : BaseTestClass() {
    private val feePctConfirm = 0.15

    @BeforeEach
    fun setUp() {
        //set up mocks for orders to confirm
        //various configs for each test around wallets, num products, orders, etc.
        TestConsoleCentral.setUpTestsCentral(defaultSystemTables, sqlLoggerIsNull = false)
        setupMocks()
        setUpAdminContext()
        setUpFirstCustomerUserContext()
        setUpFirstVendorContext()

        assertCounts(
            map = mapOf(
                User to 3,
                AdminProfile to 1,
                ShopVendor to 1,
                Storefront to 1,
                JoinCode to 1,
                JoinCodeUsages.Entity to 0,
                JoinCodeBurstEvent.Entity to 0,
                ShopCustomer to 0,
                DeliveryOrder to 0,
                Invoice to 0,
            )
        )
    }


    @Test
    fun `should not display option for confirm if no orders`() = runBlocking {

        runVendorInputs(
            { ConfirmOrderNodes(it).execute() },
            hasMap = mapOf(
                "No orders to confirm." to "Should be no orders to confirm",
            ),
            doesNotHaveMap = mapOf(
                "You have 0 orders to confirm." to "No orders to confirm.",
            ),
        )

        assertCounts(
            map = mapOf(
                User to 3,
                AdminProfile to 1,
                ShopVendor to 1,
                Storefront to 1,
                JoinCode to 1,
                JoinCodeUsages.Entity to 0,
                JoinCodeBurstEvent.Entity to 0,
                ShopCustomer to 0,
                DeliveryOrder to 0,
                Invoice to 0,
            )
        )
    }


    private fun setUpConfirmableOrder(
        zipcode: String,
        deliveryAddress: String,
        deliveryName: String,
        notesToVendor: String,
        subtotalAmt: Int,
        customerIn: ShopCustomer = mockCustomer1,
        storefront: Storefront = mockStore1,
        totalAmt: Int = subtotalAmt + (subtotalAmt * feePctConfirm).toInt(),
        feesAmt: Int = (subtotalAmt * feePctConfirm).toInt(),
    ) {

        //todo impl all the way through user checkout
        tx {
            val receiptVals = ReceiptValuesFiat(
                subtotal = FiatAmount(subtotalAmt),
                fees = FiatAmount(feesAmt),
                total = FiatAmount(totalAmt),
            )

            val invoice = Invoice.save(
                cryptoTypeIn = CryptoType.BTC,
                receiptValues = receiptVals,
                storefrontIn = storefront
            ) ?: throw Exception("Invoice should not be null")

            DeliveryOrders.save(
                invoiceIn = invoice,
                customerProfileIn = customerIn,
                shopVendorIn = storefront.vendor,
                zipcodeIn = zipcode,
                deliveryAddressIn = deliveryAddress,
                deliveryNameIn = deliveryName,
                notesToVendorIn = notesToVendor,
            ) ?: throw Exception("DeliveryOrder should not be null")

        }
    }

    @Test
    fun fetchConfirmableOrdersNode() = runBlocking {
        with(setUpMockCustomerJoinedToMockStore(mockCustomerUser1, customerContext1)) {
            customerContext1 = this.second
            mockCustomer1 = this.first
        }

        assertCounts(
            map = mapOf(
                User to 3,
                AdminProfile to 1,
                ShopVendor to 1,
                Storefront to 1,
                JoinCode to 1,
                JoinCodeUsages.Entity to 1,
                JoinCodeBurstEvent.Entity to 0,
                ShopCustomer to 1,
                DeliveryOrder to 0,
                Invoice to 0,
            )
        )

        setUpConfirmableOrder(
            zipcode = "12345",
            deliveryAddress = "123 Main St",
            deliveryName = "John Doe",
            notesToVendor = "Leave on porch",
            subtotalAmt = 100,
            customerIn = mockCustomer1,
        )

        assertCounts(
            map = mapOf(
                User to 3,
                AdminProfile to 1,
                ShopVendor to 1,
                Storefront to 1,
                JoinCode to 1,
                JoinCodeUsages.Entity to 1,
                JoinCodeBurstEvent.Entity to 0,
                ShopCustomer to 1,
                DeliveryOrder to 1,
                Invoice to 1,
            )
        )

        runVendorInputs(
            { ConfirmOrderNodes(it).execute() },
            hasMap = mapOf(
                "You have 1 order to confirm" to "Should be 1 order to confirm",
                "Confirm order..." to "Should have option to confirm order",
                "Decline order..." to "Should have option to decline order",
                "Skip for now" to "Should have option to skip order",
                "Cancel" to "Should have option to cancel",
            ),
            doesNotHaveMap = mapOf(
                "Select order to confirm or decline:" to "Should be prompted to select order to confirm or decline",
                "No orders to confirm." to "Should be no orders to confirm",
                "You have 0 orders to confirm." to "No orders to confirm.",
            ),
        )

//
//        with(setUpMockCustomerJoinedToMockStore(mockCustomerUser2, customerContext2)) {
//            customerContext2 = this.second
//            mockCustomer2 = this.first
//        }
//        assertCounts(
//            map = mapOf(
//                User to 3,
//                AdminProfile to 1,
//                ShopVendor to 1,
//                Storefront to 1,
//                JoinCode to 1,
//                JoinCodeUsages.Entity to 1,
//                JoinCodeBurstEvent.Entity to 0,
//                ShopCustomer to 1,
//                DeliveryOrder to 2,
//                Invoice to 1,
//            )
//        )
//
//
//        setUpConfirmableOrder(
//            zipcode = "44444",
//            deliveryAddress = "888 George St",
//            deliveryName = "Jane Doe",
//            notesToVendor = "Do NOT drop",
//            subtotalAmt = 200,
//            customerIn = mockCustomer2,
//        )
//
//        assertCounts(
//            map = mapOf(
//                User to 3,
//                AdminProfile to 1,
//                ShopVendor to 1,
//                Storefront to 1,
//                JoinCode to 1,
//                JoinCodeUsages.Entity to 1,
//                JoinCodeBurstEvent.Entity to 0,
//                ShopCustomer to 1,
//                DeliveryOrder to 2,
//                Invoice to 1,
//            )
//        )
//
//        runVendorInputs(
//            { VendorConfirmNodes(it).execute() },
//            hasMap = mapOf(
//                "You have 1 order to confirm" to "Should be 1 order to confirm",
//            ),
//            doesNotHaveMap = mapOf(
//                "No orders to confirm." to "Should be no orders to confirm",
//                "You have 0 orders to confirm." to "No orders to confirm.",
//            ),
//        )

    }

    @Test
    fun notifyNoOrdersToConfirm() {
    }

    @Test
    fun selectOrderToConfirm() {
    }

    @Test
    fun confirmOrderNode() {
    }

    @Test
    fun declineOrderNode() {
    }

    @Test
    fun checkForReceivableWallet() {
    }

    @Test
    fun `from START should not display option for confirm if no orders`() = runBlocking {
        // show commands displaying to user
    }

}