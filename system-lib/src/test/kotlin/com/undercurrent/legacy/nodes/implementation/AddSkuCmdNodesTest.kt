package com.undercurrent.legacy.nodes.implementation


import com.undercurrent.legacyshops.nodes.vendor_nodes.AddSkuCmdNodes
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCode
import com.undercurrent.legacyshops.repository.entities.shop_items.ShopProduct
import com.undercurrent.legacyshops.repository.entities.shop_items.SaleItem
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopVendor
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.setup.BaseTestClass
import com.undercurrent.setup.TestConsoleCentral
import com.undercurrent.setup.defaultSystemTables
import com.undercurrent.shared.utils.PROMPT_RETRIES
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.repository.entities.User
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class AddSkuCmdNodesTest : BaseTestClass() {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @BeforeEach
    fun setUp() {
        TestConsoleCentral.setUpTestsCentral(defaultSystemTables)
//        TestConsoleCentral.setUpTestsCentral(defaultSystemTables, dbFileName = "add_product_test")
        setupMocks()
        setUpShopContext()
        setUpProduct()
    }

    @Test
    fun `test AddSkuCmd nodes start to end`() {

        assertNumItems(SaleItem, 0)

        mockVendorInputQueue.add(
            "A", // Choose product to create SKU from
            "y", // add sku
            "125", //add SKU price
            "1 box", // add unit size
            "y", // confirm save new sku
            "n", // add another sku
            "y", // send message to customers
            "New message to customers", //new message to customers
            "y", // send msg to customers
        )

        var node = AddSkuCmdNodes(
            vendorContext,
        ).SelectProductToAddSku()

        runBlocking {
            node.execute()
        }
        logger.info("The list of the commands that were run \n${vendorOutputs.getOutput()}")

        assertDefaultItemValues()
        assertNumItems(SaleItem, 1)
        assertTrue(vendorOutputs.getOutput().contains("SKU created and added to product"))

    }

    @Test
    fun `on sending not digits into SKU price asks again`() {
        PROMPT_RETRIES = 5

        assertNumItems(SaleItem, 0)

        mockVendorInputQueue.add(
            "A", // Choose product to create SKU from
            "y", // add sku
            "twenty five", //non digit SKU price
//            should ask again
            "25", //add SKU digit price
            "1 box", // add unit size
            "y", // confirm save new sku
            "n", // add another sku
            "y", // send message to customers
            "New message to customers", //new message to customers
            "y", // send msg to customers
        )

        var node = AddSkuCmdNodes(
            vendorContext,
        ).SelectProductToAddSku()

        runBlocking {
            node.execute()
        }
        logger.info("The list of the commands that were run \n${vendorOutputs.getOutput()}")

        assertDefaultItemValues()
        assertNumItems(SaleItem, 1)
        assertTrue(vendorOutputs.getOutput().contains("Invalid price value input"))

    }

    @Test
    fun `SKU not saved when Cancel chosen`() {

        assertNumItems(SaleItem, 0)

        mockVendorInputQueue.add(
            "B", // Chooses Cancel instead of product to create SKU from
            "y", // Yes, I'm sure I want to cancel
        )

        var node = AddSkuCmdNodes(
            vendorContext,
        ).SelectProductToAddSku()

        runBlocking {
            node.execute()
        }
        logger.info("The list of the commands that were run \n${vendorOutputs.getOutput()}")

        assertDefaultItemValues()
        assertNumItems(SaleItem, 0)
        assertTrue(vendorOutputs.getOutput().contains("Operation cancelled"))

    }

    @Test
    fun `SKU not saved when chosen no on add SKU question`() {

        assertNumItems(SaleItem, 0)

        mockVendorInputQueue.add(
            "A", // Choose product to create SKU from
            "n", // No to Add sku question
            "n", // No to Send msg to customers question
        )

        var node = AddSkuCmdNodes(
            vendorContext,
        ).SelectProductToAddSku()

        runBlocking {
            node.execute()
        }
        logger.info("The list of the commands that were run \n${vendorOutputs.getOutput()}")

        assertDefaultItemValues()
        assertNumItems(SaleItem, 0)
        assertTrue(vendorOutputs.getOutput().contains("You decided not to add a new SKU"))

    }

    @Test
    fun `SKU not saved after details inserted but the second time chosen not to save`() {

        assertNumItems(SaleItem, 0)

        mockVendorInputQueue.add(
            "A", // Choose product to create SKU from
            "y", // add sku
            "125", //add SKU price
            "1 box", // add unit size
            "n", // confirm save new sku
            "n", // send msg to customers
        )

        var node = AddSkuCmdNodes(
            vendorContext,
        ).SelectProductToAddSku()

        runBlocking {
            node.execute()
        }
        logger.info("The list of the commands that were run \n${vendorOutputs.getOutput()}")

        assertDefaultItemValues()
        assertNumItems(SaleItem, 0)
        assertTrue(vendorOutputs.getOutput().contains("New SKU was not saved"))

    }

    @Test
    fun `promptBuilder doesn't add letters if product array is empty`() {

        val prompt = AddSkuCmdNodes.SelectProductAbcPrompt(options = emptyList())
        val result = prompt.toString()

        assertNotNull(prompt != null) { "Prompt can't not be null" }
        asserts.assertContains(result, "Choose product to create SKU from:")
        asserts.assertContains(result, "A. Cancel") // TODO decide if we need Cancel in this case
        asserts.assertDoesntContain(result, "B.")
    }

    @Test
    fun `promptBuilder adds letters as bullet points`() {

        val product1 = tx {
            ShopProduct.new {
                name = "Product 1"
                details = "productDetails"
                storefront = mockStore1
            }
        }

        val productList = listOf(product1)

        val prompt = AddSkuCmdNodes.SelectProductAbcPrompt(options = productList)
        val result = prompt.toString()

        logger.info("Prompt result \n$result")

        assertNotNull(prompt != null) { "Prompt can't not be null" }
        asserts.assertContains(result, "Choose product to create SKU from:")
        asserts.assertContains(result, "A.")
    }

    @Test
    fun `promptBuilder adds letters + 1 for cancel`() {

        val product1 = tx {
            ShopProduct.new {
                name = "Product 1"
                details = "productDetails"
                storefront = mockStore1
            }
        }

        val productList = listOf(product1)

        val prompt = AddSkuCmdNodes.SelectProductAbcPrompt(options = productList)
        val result = prompt.toString()

        logger.info("Prompt result \n$result")

        assertNotNull(prompt != null) { "Prompt can't not be null" }
        asserts.assertContains(result, "Choose product to create SKU from:")
        asserts.assertContains(result, "A.")
        asserts.assertContains(result, "B. Cancel")
        asserts.assertDoesntContain(result, "C.")
    }

    @Test
    fun `promptBuilder parses footer correctly`() {

        val product1 = tx {
            ShopProduct.new {
                name = "Product 1"
                details = "productDetails"
                storefront = mockStore1
            }
        }

        val productList = listOf(product1)
        val prompt = AddSkuCmdNodes.SelectProductAbcPrompt(options = productList)
        val potentialCancel = prompt.selectFooterIfHandleCorresponds("B")

        logger.info("Prompt Cancel footer actual result \n$potentialCancel")

        asserts.assertContains(potentialCancel.toString(), "Cancel")
    }

    @Test
    fun `promptBuilder return null if footer handle incorrect`() {

        val product1 = tx {
            ShopProduct.new {
                name = "Product 1"
                details = "productDetails"
                storefront = mockStore1
            }
        }

        val productList = listOf(product1)
        val prompt = AddSkuCmdNodes.SelectProductAbcPrompt(options = productList)
        val potentialNull = prompt.selectFooterIfHandleCorresponds("C")

        logger.info("Prompt null footer actual result \n$potentialNull")

        assertNull(potentialNull)
    }

    private fun assertDefaultItemValues() {
        assertNumItems(ShopProduct, 1)
        assertNumItems(User, 3)
        assertNumItems(ShopVendor, 1)
        assertNumItems(ShopVendor, 1)
        assertNumItems(Storefront, 1)
        assertNumItems(JoinCode, 1)
    }
}