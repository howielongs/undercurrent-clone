package com.undercurrent.shops.shopapi.types.strings

import com.undercurrent.shops.types.strings.ShopTextImporter
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.test.assertContains

class ShopTextTest {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun assertYesNo(result: String) {
        assertContains(result, "Y. Yes")
        assertContains(result, "N. No")
    }

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun `invoke addToCartQuestion and validate string out`() {
        ShopTextImporter.Question.YesNoShopQuestion.AddToCart().let {
            val result = it()

            logger.info("Got: $result")
            assertContains(result, "Add to your cart?")
            assertYesNo(result)
        }
    }

    @Test
    fun `invoke addToInventoryQuestion and validate string out`() {
        ShopTextImporter.Question.YesNoShopQuestion.AddToInventory().let {
            val result = it()

            logger.info("Got: $result")
            assertContains(result, "Save SKU to your inventory?")
            assertYesNo(result)
        }
    }
    @Test
    fun `invoke attachSingleImageYesNoQuestion and validate string out`() {
        ShopTextImporter.Question.YesNoShopQuestion.AttachImage().let {
            val result = it()

            logger.info("Got: $result")
            assertContains(result, "Attach this image to this product?")
            assertYesNo(result)
        }
    }

    @Test
    fun `invoke attachImagesYesNoQuestion and validate string out`() {
        ShopTextImporter.Question.YesNoShopQuestion.AttachImages(3).let {
            val result = it()

            logger.info("Got: $result")
            assertContains(result, "Attach 3 images to this product?")
            assertYesNo(result)
        }
    }
}