package com.undercurrent.legacy.commands

import com.undercurrent.legacy.commands.registry.UserCommand
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestEditInputToCheckout {

    @BeforeEach
    fun setUp() {
    }

    fun getPromptFromCommand(cmd: UserCommand, expectedStr: String) {

    }

    //todo test other checkout stages

    @Test
    fun `test select field to edit`() {
        val fieldsToEdit = "What do you want to edit? \n" +
                "\n" +
                "- [A] Maggie Johnson\n" +
                "- [B] 23 Main Street, Phoenix AZ\n" +
                "- [C]  85016\n" +
                "- [D] Leave at door\n" +
                "- [E] Don't edit and continue Checkout"

        val nameWithExample = "First and last name for delivery?\n" +
                "\n" +
                "ex: John, Smith"

        val deliveryStreetWithExample = "Full delivery street address including apartment numbers?\n" +
                "\n" +
                "ex: 2222 Main Street, Los Angeles, CA"

        val deliveryZipWithExample = "What is your delivery zipcode?\n" +
                "\n" +
                "ex: 90291"

        val deliveryNoteWithExample = "Enter any special delivery instructions. If none, please enter \"N\"\n" +
                "\n" +
                "ex: Leave at door"

        val youEnteredStr = "You entered:\n" +
                "\n" +
                "Delivery name: R Johnson\n" +
                "Street address: 7770 main street,\n" +
                "Gilbert, AZ\n" +
                "Zipcode: 85999333\n" +
                "Special instructions: none"

        val confirmStr = "Is this correct?\n" +
                " - [Y] Yes\n" +
                " - [N] No"


        val checkoutCompletedStr = "Thank you!\n" +
                "\n" +
                "Your order is currently awaiting approval from the vendor. We'll be sure to let you know once it's been confirmed.\n" +
                "\n" +
                "Please note, this may take up to 3 days."

        val vendorNewOrderStr = "Order Code: TFSNBD\n" +
                "\n" +
                "Follow the next steps to complete payment:\n" +
                "\n" +
                "1. COPY & PASTE payment address into Bitcoin wallet app of your choice, such as Cash App or Coinbase.\n" +
                "\n" +
                "2. COPY & PASTE Bitcoin into your wallet as the amount to send (do not use USD).\n" +
                "\n" +
                "Type 'openorders' to see details of your order(s)."

        val orderCancellationMsg = "(Due to crypto volatility, payment must be received within 3 days to avoid cancelation)"


    }


}