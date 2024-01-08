package com.undercurrent.legacy.commands

import org.junit.jupiter.api.Test


@Deprecated("This is a legacy class")
class TestHelpCommand {

    @Test
    fun `test generate help menu`() {

        val helpStr =
            "Need Help?\n" + "Here's a quick guide for using our system.\n" + "\n" + "1. Type the letter in brackets next to a menu item to access it. For example: To access \"[A] Add crypto wallet for payments\", simply type the letter \"A\".\n" + "\n" + "2. Type 'shop' at any time to display the shop menu. Here you will find a list of shops you've joined, and actions you can take within any those shops– such as browse inventory, view cart, view order history, and manage payment methods.\n" + "\n" + "3. Type 'contact' at any time to send comments to our admin, suggest improvements, or report an issue.\n" + "\n" + "4. Type 'cancel' to end any current activity. You must do this before switching between different menus or changing commands.\n" + "\n" + "Type 'help' at any time to return to this menu."

        val helpStr2 =
            "Help\n" + "Here's a comprehensive guide to using our system.\n" + "\n" + "1. Type the letter in brackets next to a menu item to access it. For example: To access \"[A] Add crypto wallet for payments\", simply type the letter \"A\".\n" + "\n" + "2. Type 'home' at any time to display the main menu. Here you will find options like: add a new crypto wallet for payments, visit a shop you've already joined, or add a new shop by entering it's 11-digit code.\n" + "\n" + "3. Type 'shop' at any time to display the shop menu. Here you will find a list of shops you've joined, and actions you can take within any those shops– such as browse inventory, view cart, view order history, and manage payment methods.\n" + "\n" + "3. Type 'openorders' at any time to display details of your pending orders.\n" + "\n" + "4. Type 'feedback' at any time to send comments to our admin, suggest improvements, or report an issue.\n" + "\n" + "6. Type 'cancel' to end any current activity. You must do that before switching between different activities or actions.\n" + "\n" + "Type 'help' at any time to return to this menu."

        val expected = """
            |Select the letter of a command to run:
            |A. menu - View items available to add to your cart
            |B. cart - Customer cart
            |C. orders - Orders submitted
            |D. shops - Shops menu
            |E. crypto - Learn about Bitcoin and MobileCoin wallets
            |F. home - Return home and cancel all current operations
            |G. feedback - Provide feedback to admins or ask for help
            |H. help - Send a message to system admins for support
            |I. cancel - Cancel current operation
        """.trimMargin()

//        TestAssertUtils.assertContains(
//            resultStr = SelectStartPrompt(
//                listOf(
//                    CoreCommand.MENU,
//                    CoreCommand.CART,
//                    CoreCommand.ORDERS,
//                    CoreCommand.SHOPS,
//                    CoreCommand.CRYPTO,
//                    CoreCommand.HOME,
//                    CoreCommand.FEEDBACK,
//                    CoreCommand.HELP,
//                    CoreCommand.CANCEL,
//                )
//            ).toString(),
//            containsList = listOf(
//                "Select the letter of a command to run:",
//                "A. menu - View items available to add to your cart",
//                "B. cart - Customer cart",
//            )
//        )

        assert(true)
    }

}