package com.undercurrent.legacy.commands.registry.lifecycle.nodes

import com.undercurrent.system.repository.entities.User
import com.undercurrent.setup.BaseTestClass
import com.undercurrent.setup.TestConsoleCentral
import com.undercurrent.setup.defaultSystemTables
import com.undercurrent.legacyshops.nodes.vendor_nodes.AddProductCmdNodes
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCode
import com.undercurrent.legacyshops.repository.entities.shop_items.ShopProduct
import com.undercurrent.legacyshops.repository.entities.shop_items.SaleItem
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopVendor
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SwapStartNodeTest : BaseTestClass() {

    @BeforeEach
    fun setUp() {
        TestConsoleCentral.setUpTestsCentral(defaultSystemTables)
        setupMocks()
        setUpShopContext()

    }

    @Test
    fun `test start with btc and mob addresses`() {
        mockCustomerInputQueue1.add(
            "a", // add product action
            "New product 1", //enter name
            "Detailed details", //enter details
            "n", // add attachments?
            "y", // confirm to save
            "y", // add sku
            "12.01", //add sku price
            "large", // add unit size
            "y", // confirm save new sku
            "n", // add another sku
            "y", // send message to customers
            "New message to customers", //new message to customers
            "y", // send msg to customers
        )


    }


    @Test
    fun `test start with no btc or mob addresses`() {
        runBlocking {

            //todo impl with specifics for Swap service

            assertNumItems(ShopProduct, 0)
            assertNumItems(SaleItem, 0)

            mockVendorInputQueue.add(
                "a", // add product action
                "New product 1", //enter name
                "Detailed details", //enter details
                "n", // add attachments?
                "y", // confirm to save
                "y", // add sku
                "12.01", //add sku price
                "large", // add unit size
                "y", // confirm save new sku
                "n", // add another sku
                "y", // send message to customers
                "New message to customers", //new message to customers
                "y", // send msg to customers
            )

            var node = AddProductCmdNodes(
                vendorContext,
            ).startAddProduct()

            node?.execute()
        }
        println(vendorOutputs.getOutput())

        assertNumItems(ShopProduct, 1)
        assertNumItems(SaleItem, 1)
        assertNumItems(User, 3)
        assertNumItems(ShopVendor, 1)
        assertNumItems(Storefront, 1)
        assertNumItems(JoinCode, 1)

    }
}