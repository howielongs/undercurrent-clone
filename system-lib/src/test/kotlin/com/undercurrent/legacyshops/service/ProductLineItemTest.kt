package com.undercurrent.legacyshops.service

import com.undercurrent.legacyshops.components.ProductLineItem
import com.undercurrent.legacyshops.components.saleItemToLineStr
import com.undercurrent.legacyshops.repository.entities.shop_items.SaleItem
import com.undercurrent.legacyshops.repository.entities.shop_items.ShopProduct
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.setup.BaseTestClass
import com.undercurrent.setup.TestConsoleCentral
import com.undercurrent.setup.defaultSystemTables
import com.undercurrent.shared.utils.DecimalListTransformer
import com.undercurrent.shared.utils.tx
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ProductLineItemTest : BaseTestClass() {

    @BeforeEach
    fun setUp() {
        TestConsoleCentral.setUpTestsCentral(defaultSystemTables, sqlLoggerIsNull = false)
        setupMocks()
        setUpShopContext()
    }

    private fun generateProduct(
        storefrontIn: Storefront = mockStore1,
        nameIn: String = "Lettuce",
        detailsIn: String = "Green and watery",
    ): ShopProduct {
        return tx {
            ShopProduct.new {
                storefront = storefrontIn
                name = nameIn
                details = detailsIn
            }
        }
    }

    private fun generateSaleItem(
        productIn: ShopProduct,
        priceIn: String = "5.00",
        unitSizeIn: String = "single",
    ): SaleItem {
        return tx {
            SaleItem.new {
                product = productIn
                price = priceIn
                unitSize = unitSizeIn
            }
        }
    }

    @Test
    fun `test display product line item without sale items`() {
        val product = generateProduct()

        val lineItem = ProductLineItem().buildLineItem(product)

        val expected = "Lettuce -> Green and watery"

        Assertions.assertTrue(lineItem.equals(expected)) {
            "Expected: $expected\nActual: $lineItem"
        }
    }

    @Test
    fun `test display sale item line string`() {
        val product = generateProduct()
        val sku1 = generateSaleItem(product, "5.00", "single")

        val expected = "$5.00   / single"

        val lineItem = saleItemToLineStr(sku1)

        Assertions.assertTrue(lineItem == expected) {
            "Expected: $expected\nActual: $lineItem"
        }
    }

    // • LETTUCE ->  Green and watery
//  [1.1]	$5.00	 / single
//  [1.2]	$25	 / half a head
//  [1.3]	$28.30	 / smallest
//  [1.4]	$36.69	 / One head
//  [1.5]	$100.45	 / large pack
    @Test
    fun `test generate inventory list for single product`() {

        val expectedInventoryStr = """
            | • Lettuce -> Green and watery
            | [1.1]	$5.00	 / single
            | [1.2]	$25.00	 / half a head
            | [1.3]	$28.30	 / smallest
            | [1.4]	$36.69	 / One head
            | [1.5]	$100.45	 / large pack
        """.trimMargin().replace("\t", "  ").trim().replace("] \$", "]  \$")

        val product = generateProduct(mockStore1, "Lettuce", "Green and watery")

        val saleItems = listOf(
            generateSaleItem(product, "5.00", "single"),
            generateSaleItem(product, "25", "half a head"),
            generateSaleItem(product, "28.30", "smallest"),
            generateSaleItem(product, "36.69", "One head"),
            generateSaleItem(product, "100.45", "large pack"),
        )

        val products = tx { mockStore1.products.toList() }

        val inventoryStrBuilder = ProductInventoryStringBuilder(
            product = product,
            saleItems = saleItems,
            indexTransformer = { DecimalListTransformer(0).transform(BigDecimal(it)) }
        )
        val inventoryStr = inventoryStrBuilder.buildString().replace("\t", "  ").trim()


        //todo assert combined product with sale item (and their indices)

        Assertions.assertTrue(inventoryStr == expectedInventoryStr) {
            "Expected: $expectedInventoryStr\nActual: $inventoryStr"
        }

        //todo impl this
//        val lineItem = ProductLineItem().buildLineItem(product, "1.1")
//
//        Assertions.assertTrue(lineItem.equals(" • Lettuce -> Green and watery"))
//        println(lineItem)
    }

    fun generateFourProductListingsWithOneHidden(shouldDisplayHidden: Boolean = false): String {

        val product1 = generateProduct(mockStore1, "Lettuce", "Green and watery")
        val product2 = generateProduct(mockStore1, "Bread", "White and fluffy")
        val product3 = generateProduct(mockStore1, "Oranges", "Orange and juicy")
        val product4 = generateProduct(mockStore1, "Apples", "Red and juicy")

        val saleItems1 = listOf(
            generateSaleItem(product1, "5.00", "single"),
            generateSaleItem(product1, "25", "half a head"),
            generateSaleItem(product1, "28.30", "smallest"),
            generateSaleItem(product1, "36.69", "One head"),
            generateSaleItem(product1, "100.45", "large pack"),
        )

        val saleItems2 = listOf(
            generateSaleItem(product2, "5.02", "slice"),
            generateSaleItem(product2, "23", "half a loaf"),
            generateSaleItem(product2, "28.32", "full loaf"),
        )

        val saleItems4 = listOf(
            generateSaleItem(product4, "36.64", "One bushel"),
            generateSaleItem(product4, "100.44", "large barrel"),
        )


        val inventoryStrBuilder1 = ProductInventoryStringBuilder(product1, saleItems1,
            indexTransformer = { DecimalListTransformer(0).transform(BigDecimal(it)) }
        )
        val inventoryStrBuilder2 = ProductInventoryStringBuilder(product2, saleItems2,
            indexTransformer = { DecimalListTransformer(1).transform(BigDecimal(it)) }
        )

        val inventoryStrBuilder3 = ProductInventoryStringBuilder(product3, listOf(),
            indexTransformer = { DecimalListTransformer(2).transform(BigDecimal(it)) }
        )

        val inventoryString3 = if (shouldDisplayHidden) {
            inventoryStrBuilder3.buildString() + "\n"
        } else {
            ""
        }

        val inventoryStrBuilder4 = ProductInventoryStringBuilder(product4, saleItems4,
            indexTransformer = { DecimalListTransformer(3).transform(BigDecimal(it)) }
        )

        val returnInventory =  inventoryStrBuilder1.buildString() + "\n" +
                inventoryStrBuilder2.buildString() + "\n" +
                inventoryString3 +
                inventoryStrBuilder4.buildString()

        return returnInventory.replace("\t", "  ").trim().replace("] \$", "]  \$")
    }


    /**
     * Expected:
     *  • Lettuce -> Green and watery
     *  [1.1]	$5.00   / single
     *  [1.2]	$25.00   / half a head
     *  [1.3]	$28.30   / smallest
     *  [1.4]	$36.69   / One head
     *  [1.5]	$100.45   / large pack
     *
     *  • Bread -> White and fluffy
     *  [2.1]	$5.02   / slice
     *  [2.2]	$23.00   / half a loaf
     *  [2.3]	$28.32   / full loaf
     *
     *  • Oranges -> Orange and juicy
     *
     *  • Apples -> Red and juicy
     *  [4.1]	$36.64   / One bushel
     *  [4.2]	$100.44   / large barrel
     *
     */
    @Test
    fun `test generate inventory list for four products (display one empty)`() {
        val expectedInventoryStr = """
            | • Lettuce -> Green and watery
            | [1.1]	$5.00	 / single
            | [1.2]	$25.00	 / half a head
            | [1.3]	$28.30	 / smallest
            | [1.4]	$36.69	 / One head
            | [1.5]	$100.45	 / large pack
            |
            | • Bread -> White and fluffy
            | [2.1] $5.02	 / slice
            | [2.2] $23.00	 / half a loaf
            | [2.3] $28.32	 / full loaf
            |
            | • Oranges -> Orange and juicy
            |
            | • Apples -> Red and juicy
            | [4.1] $36.64	 / One bushel
            | [4.2] $100.44	 / large barrel
        """.trimMargin().replace("\t", "  ").trim().replace("] \$", "]  \$")


        val inventoryStr = generateFourProductListingsWithOneHidden(true).replace("\t", "  ")

        //todo assert combined product with sale item (and their indices)

        Assertions.assertTrue(inventoryStr == expectedInventoryStr) {
            "Expected: \n$expectedInventoryStr\n\nActual: \n$inventoryStr"
        }

        //todo impl this
//        val lineItem = ProductLineItem().buildLineItem(product, "1.1")
//
//        Assertions.assertTrue(lineItem.equals(" • Lettuce -> Green and watery"))
//        println(lineItem)
    }

    @Test
    fun `test generate inventory list and HIDE for customer for three (one hidden) products`() {
        val expectedInventoryStr = """
            | • Lettuce -> Green and watery
            | [1.1]	$5.00	 / single
            | [1.2]	$25.00	 / half a head
            | [1.3]	$28.30	 / smallest
            | [1.4]	$36.69	 / One head
            | [1.5]	$100.45	 / large pack
            |
            | • Bread -> White and fluffy
            | [2.1] $5.02	 / slice
            | [2.2] $23.00	 / half a loaf
            | [2.3] $28.32	 / full loaf
            |
            | • Apples -> Red and juicy
            | [4.1] $36.64	 / One bushel
            | [4.2] $100.44	 / large barrel
        """.trimMargin().replace("\t", "  ").trim().replace("] \$", "]  \$")


        val inventoryStr = generateFourProductListingsWithOneHidden(false)

        //todo assert combined product with sale item (and their indices)

        Assertions.assertTrue(inventoryStr == expectedInventoryStr) {
            "Expected: $expectedInventoryStr\nActual: $inventoryStr"
        }

        //todo impl this
//        val lineItem = ProductLineItem().buildLineItem(product, "1.1")
//
//        Assertions.assertTrue(lineItem.equals(" • Lettuce -> Green and watery"))
//        println(lineItem)
    }


}