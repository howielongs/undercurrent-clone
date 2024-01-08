package com.undercurrent.legacy

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

//class SelectStartPromptTest {
//
//    @BeforeEach
//    fun setUp() {
//    }
//
//    @Test
//    fun `test generate start menu for customer`() {
//
//        val expected = """
//            |Select the letter of a command to run:
//            |A. menu - View items available to add to your cart
//            |B. cart - Customer cart
//            |C. orders - Orders submitted
//            |D. shops - Shops menu
//            |E. crypto - Learn about Bitcoin and MobileCoin wallets
//            |F. home - Return home and cancel all current operations
//            |G. feedback - Provide feedback to admins or ask for help
//            |H. help - Send a message to system admins for support
//            |I. cancel - Cancel current operation
//        """.trimMargin()
//
////        TestAssertUtils.assertContains(
////            resultStr = SelectStartPrompt(
////                listOf(
////                    CoreCommand.MENU,
////                    CoreCommand.CART,
////                    CoreCommand.ORDERS,
////                    CoreCommand.SHOPS,
////                    CoreCommand.CRYPTO,
////                    CoreCommand.HOME,
////                    CoreCommand.FEEDBACK,
////                    CoreCommand.HELP,
////                    CoreCommand.CANCEL,
////                )
////            ).toString(),
////            containsList = listOf(
////                "Select the letter of a command to run:",
////                "A. menu - View items available to add to your cart",
////                "B. cart - Customer cart",
////            )
////        )
//
//        assert(true)
//    }
//
//}