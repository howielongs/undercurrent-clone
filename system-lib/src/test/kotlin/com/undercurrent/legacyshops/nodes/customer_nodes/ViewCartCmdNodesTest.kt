package com.undercurrent.legacyshops.nodes.customer_nodes

import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCode
import com.undercurrent.legacyshops.repository.entities.shop_items.ShopProduct
import com.undercurrent.legacyshops.repository.entities.shop_items.SaleItem
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopVendor
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.setup.BaseTestClass
import com.undercurrent.setup.TestConsoleCentral
import com.undercurrent.setup.defaultSystemTables
import com.undercurrent.system.repository.entities.User
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ViewCartCmdNodesTest : BaseTestClass() {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @BeforeEach
    fun setUp() {
        TestConsoleCentral.setUpTestsCentral(defaultSystemTables)
        setupMocks()
        setUpShopContext()
        setUpProduct()
    }

    @Test
    fun `view cart nodes start to end`() = runBlocking {
        assertNumItems(ShopProduct, 1)
        assertNumItems(SaleItem, 0)


        var node = CustomerCartNodes(
            customerContext1,
        ).displayCartContentsNode()

        node?.execute()
        logger.info(customerOutputs1.getOutput())

        assertNumItems(ShopProduct, 1)
//        assertNumItems(SaleItem, 1)
        assertNumItems(User, 3)
        assertNumItems(ShopVendor, 1)
        assertNumItems(Storefront, 1)
        assertNumItems(JoinCode, 1)
        Assertions.assertTrue(customerOutputs1.getOutput().contains("YOUR CART:"))


    }

}