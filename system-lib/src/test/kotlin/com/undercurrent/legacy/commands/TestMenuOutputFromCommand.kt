package com.undercurrent.legacy.commands

import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.commands.registry.UserCommand
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestMenuOutputFromCommand {

    @BeforeEach
    fun setUp() {
    }

    fun getPromptFromCommand(cmd: UserCommand, expectedStr: String) {

    }

    @Test
    fun `test select to add new shop by code`() {
        val shopStr = "Awesome, let's get this Shop added.\n" +
                "\n" +
                "Please type the 11 digit code, or copy and paste the invitation message you received from the vendor.\n" +
                "\n" +
                "Type 'cancel' if you'd like to go back."

        val successAddShopStr = "Great, we've added {shop code} to your account."

        val cancelStr = "Operation cancelled."
    }

    @Test
    fun `test select from mult shops`() {
        val shopSelectStr = "Where would you like to shop?\n" +
                "\n" +
                "– [A] Shop code [selected]\n" +
                "– [B] Shop code\n" +
                "– [C] Shop code\n" +
                "– [D] Add a new shop\n" +
                "– [E] Return to shop menu"
    }

    @Test
    fun `test shop selected in menu`() {
        val shopMenuStr = "Shop menu\n" +
                "Hello! You're shopping with: \n" +
                "[shop code]\n" +
                "\n" +
                "– [A] View shop inventory\n" +
                "– [B] View cart\n" +
                "– [C] View order history\n" +
                "– [D] Manage payment methods\n" +
                "– [E] Switch to another shop\n" +
                "– [F] Return 'Home'\n" +
                "– [G] Help\n" +
                "\n" +
                "Type 'shop' at any time to return to this menu."

    }

    @Test
    fun `test start menu`() {
        val cmdIn: String = "start" // also HOME

        val responseMapWithoutWallet = hashMapOf(
            "A" to CmdRegistry.ADDWALLET,
//            "B" to CmdRegistry.LINK,
            "C" to CmdRegistry.SWITCHSHOP,
            "D" to CmdRegistry.FEEDBACK,
//            "E" to CmdRegistry.HELP,
        )

        val homeWithNoWallet = "Home Menu\n" +
                "What's your next action?\n" +
                "\n" +
                "- [A] Add crypto wallet for payments\n" +
                "- [B] Add a new shop\n" +
                "- [C] Visit a shop you've joined\n" +
                "- [D] Send Feedback\n" +
                "- [E] Help\n" +
                "\n" +
                "Type 'home' any time to return to this menu."


        val responseMapWithWallet = hashMapOf(
            "A" to CmdRegistry.ADDWALLET,
//            "B" to CmdRegistry.LINK,
            "C" to CmdRegistry.SWITCHSHOP,
            "D" to CmdRegistry.FEEDBACK,
//            "E" to CmdRegistry.HELP,
        )

        val homeWithWallet = "Home Menu\n" +
                "What's your next action?\n" +
                "\n" +
                "- [A] Manage payment methods\n" +
                "- [B] Add a new shop\n" +
                "- [C] Visit a shop you've joined\n" +
                "- [D] Send Feedback\n" +
                "- [E] Help\n" +
                "\n" +
                "Type 'home' any time to return to this menu."


    }


}