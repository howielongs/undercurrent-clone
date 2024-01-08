package com.undercurrent.legacy.nodes.implementation

import com.undercurrent.system.repository.entities.User
import com.undercurrent.setup.BaseTestClass
import com.undercurrent.setup.TestConsoleCentral
import com.undercurrent.setup.defaultSystemTables
import com.undercurrent.shared.utils.tx
import com.undercurrent.shared.view.treenodes.TreeNode
import com.undercurrent.legacyshops.nodes.vendor_nodes.AddProductCmdNodes
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCode
import com.undercurrent.legacyshops.repository.entities.shop_items.ShopProduct
import com.undercurrent.legacyshops.repository.entities.shop_items.SaleItem
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopVendor
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AddProductCmdNodesTest : BaseTestClass() {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @BeforeEach
    fun setUp() {
        TestConsoleCentral.setUpTestsCentral(defaultSystemTables)
        setupMocks()
        setUpShopContext()
    }

    @Test
    fun `test addProduct nodes start to end`() = runBlocking {
        assertNumItems(ShopProduct, 0)
        assertNumItems(SaleItem, 0)

        mockVendorInputQueue.add(
            "a", // add product action
            "New product 1", //enter name// TODO 2 or b
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
        logger.info(vendorOutputs.getOutput())

        assertNumItems(ShopProduct, 1)
        assertNumItems(SaleItem, 1)
        assertNumItems(User, 3)
        assertNumItems(ShopVendor, 1)
        assertNumItems(Storefront, 1)
        assertNumItems(JoinCode, 1)
        Assertions.assertTrue(vendorOutputs.getOutput().contains("Product created: New product 1"))
        Assertions.assertTrue(vendorOutputs.getOutput().contains("SKU created and added to product"))
        Assertions.assertTrue(vendorOutputs.getOutput().contains("You decided not to add a new SKU."))
        Assertions.assertTrue(vendorOutputs.getOutput().contains("Successfully notified "))

    }

    @Test
    fun `test addProduct nodes start to end with two SKUs`() = runBlocking {
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
            "y", // add another sku// with the second SKU
            "8.09", //add sku price
            "medium", // add unit size
            "y", // confirm save new sku
            "n", // create one more sku
            "y", // send message to customers
            "New message to customers", //new message to customers
            "y", // send msg to customers
        )

        var node = AddProductCmdNodes(
            vendorContext,
        ).startAddProduct()

        node?.execute()
        logger.info(vendorOutputs.getOutput())

        assertNumItems(ShopProduct, 1)
        assertNumItems(SaleItem, 2)
        assertNumItems(User, 3)
        assertNumItems(ShopVendor, 1)
        assertNumItems(Storefront, 1)
        assertNumItems(JoinCode, 1)

        Assertions.assertTrue(vendorOutputs.getOutput().contains("Product created: New product 1"))
        Assertions.assertTrue(vendorOutputs.getOutput().contains("SKU created and added to product"))
        Assertions.assertTrue(vendorOutputs.getOutput().contains("You decided not to add a new SKU."))
        Assertions.assertTrue(vendorOutputs.getOutput().contains("Successfully notified"))

    }

    @Test
    fun `test addProduct nodes start to end without saving SKU`() = runBlocking {
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
            "n", // confirm save new sku // changed from y to n
            "y", // send message to customers
            "New message to customers", //new message to customers
            "y", // send msg to customers
        )

        var node = AddProductCmdNodes(
            vendorContext,
        ).startAddProduct()

        node?.execute()
        logger.info(vendorOutputs.getOutput())

        assertNumItems(ShopProduct, 1)
        assertNumItems(SaleItem, 0)
        assertNumItems(User, 3)
        assertNumItems(ShopVendor, 1)
        assertNumItems(Storefront, 1)
        assertNumItems(JoinCode, 1)

        Assertions.assertTrue(vendorOutputs.getOutput().contains("Product created: New product 1"))
        Assertions.assertFalse(vendorOutputs.getOutput().contains("SKU created and added to product"))
        Assertions.assertFalse(vendorOutputs.getOutput().contains("You decided not to add a new SKU."))
        Assertions.assertTrue(vendorOutputs.getOutput().contains("New SKU was not saved."))
        Assertions.assertTrue(vendorOutputs.getOutput().contains("Successfully notified"))

    }

    @Test
    fun `test addProduct nodes start to end without confirming to save`() = runBlocking {
        assertNumItems(ShopProduct, 0)
        assertNumItems(SaleItem, 0)

        mockVendorInputQueue.add(
            "a", // add product action
            "New product 1", //enter name
            "Detailed details", //enter details
            "n", // add attachments?
            "n", // confirm to save // changed from y to n
        )

        var node = AddProductCmdNodes(
            vendorContext,
        ).startAddProduct()

        node?.execute()
        logger.info(vendorOutputs.getOutput())

        assertNumItems(ShopProduct, 0)
        assertNumItems(SaleItem, 0)
        assertNumItems(User, 3)
        assertNumItems(ShopVendor, 1)
        assertNumItems(Storefront, 1)
        assertNumItems(JoinCode, 1)

        Assertions.assertTrue(vendorOutputs.getOutput().contains("Product not saved: New product 1"))

    }

    @Test
    fun `test addProduct nodes start to end without sending message to customers`() = runBlocking {
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
            "n", // send message to customers// changed from y to n
        )

        var node = AddProductCmdNodes(
            vendorContext,
        ).startAddProduct()

        node?.execute()
        logger.info(vendorOutputs.getOutput())

        assertNumItems(ShopProduct, 1)
        assertNumItems(SaleItem, 1)
        assertNumItems(User, 3)
        assertNumItems(ShopVendor, 1)
        assertNumItems(Storefront, 1)
        assertNumItems(JoinCode, 1)

        Assertions.assertTrue(vendorOutputs.getOutput().contains("Product created: New product 1"))
        Assertions.assertTrue(vendorOutputs.getOutput().contains("SKU created and added to product"))
        Assertions.assertTrue(vendorOutputs.getOutput().contains("You decided not to add a new SKU."))
        Assertions.assertFalse(vendorOutputs.getOutput().contains("New SKU was not saved."))
        Assertions.assertTrue(vendorOutputs.getOutput().contains("Customers not notified."))

    }


    private fun assertNumProducts(expected: Int) {
        tx { ShopProduct.all().count().toInt() }.let {
            assert(it == expected) { "Expected $expected products, but found $it" }
        }
    }

    private suspend fun createNewProduct(startingCount: Int = 0): TreeNode? {
        assertNumProducts(startingCount)

        mockVendorInputQueue.add(
            "a", // add product action
            "New product 1", //enter name
            "Detailed details", //enter details
            "n", // add attachments?
            "y", // confirm to save
            "n", // add sku: No
        )

        var node = AddProductCmdNodes(
            vendorContext,
        ).startAddProduct()

        var latestNode = node?.execute(7)
        assertNumProducts(startingCount + 1)

        logger.info(vendorOutputs.getOutput())

        return latestNode

    }

    private fun fetchLatestProduct(): ShopProduct? {
        return tx { ShopProduct.all().lastOrNull() }
    }

    @Test
    fun `test create new product only`() = runBlocking {
        val node = createNewProduct(0)
        val newProduct = fetchLatestProduct()

        assert(newProduct != null) { "Product should not be null" }
//        Assertions.assertTrue(node is AddProductCmdNodes.DecideToAddSkuToProduct) { "DecideToAddSkuToProduct" }
        Assertions.assertTrue(vendorOutputs.getOutput().contains("Product created: New product 1"))

    }
}