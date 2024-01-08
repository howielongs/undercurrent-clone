package com.undercurrent.legacy.types.string

import com.undercurrent.legacy.commands.registry.BaseCommand
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.commands.registry.CmdRegistry.CANCEL
import com.undercurrent.legacy.commands.registry.TopCommand
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrder
import com.undercurrent.legacy.routing.SignalSmsConfigurationLoader
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.types.enums.Environment
import org.jetbrains.exposed.sql.transactions.transaction

object PressAgent {

    object Routing {
        fun getRoutingSms(
            environment: Environment,
            role: AppRole
        ): String {
            return SignalSmsConfigurationLoader.findBotSms(environment = environment, role = role)
        }

    }

    fun removeMenuYesNo(): String {
        return "Remove menu from your storefront? " +
                "\n\nThis cannot be undone.${yesNoOptions()}"
    }

    object Validation {

        fun invalidSms(): String {
            return "Invalid SMS number. Please try again."
        }
    }

    object StoreFront {
        fun storefrontWelcomeMsg(): String {
            return "Welcome to my shop! Thanks for taking the time to stay private and secure!"
        }
    }

    object Crypto {
        fun noCryptoAddressForUser(): String {
            return hintText(
                "No payments address found for user. " +
                        "\nPlease add wallet address to get paid."
            )
        }

        fun invalidCryptoAddress(): String {
            return hintText("Invalid address, please try again.", CANCEL)
        }

    }

    object CustomerStrings {

        fun welcomeToStorefrontMsg(displayName: String): String {
            return "Welcome to $displayName's store.\n\n" +
                    "If you’re seeing this message, you have been " +
                    "verified within our chain of trust and are welcome " +
                    "to order our products using this secure Signal chat " +
                    "platform. Within this chat, you will be able to view " +
                    "our current menu, order our products and receive your " +
                    "tracking details.\n\n" +
                    "To begin: type the following word into the chat thread: START"
        }


        fun defaultHelp(): String {
            return "Welcome! I am the Signal shopping bot, the most private and secure way to buy or sell goods and service.\n" +
                    "Please be aware that I am still in beta. Thank you for reporting all bugs and feedback to your onboarding admin.\n" +
                    "\n" +
                    "Let's get started! Here are a few tips for interacting with my system:\n" +
                    "\n" +
                    "Sending the word \"start\" will open my multiple choice menu, allowing you to see all of the different commands available in my system. If you are an advanced user, you can also try typing specific commands to navigate through menus quicker. \n" +
                    "\n" +
                    "Any time you use a command (including the “start” command) you start a process, if you want to use a different command to start a different process you must use the cancel command. If you forget to do this the bot will ask you if want to cancel before proceeding to the new process. You can also send the word \"feedback\" to ask questions or send a message directly to the developers.\n" +
                    "\n" +
                    "To begin shopping you will need to complete a few things:\n" +
                    "\n" +
                    "1. Add a new shop by sending their 11 digit vendor code\n" +
                    "*If you're linked to multiple shops you can enter the vendor code at any point to view that stores inventory\n" +
                    "\n" +
                    "2. Add a payments wallet for payment processing\n" +
                    "\n" +
                    "*At this time my system can only process Bitcoin (MobileCoin coming soon), but don’t worry, buying Bitcoin is now very easy with CashApp! Use the \"bitcoin\" command for a link to the CashApp Bitcoin walkthrough.\n" +
                    "\n" +
                    "Well, what are you waiting for? Send \"start\", \"addcrypto\", or \"menu\" now to get started!\n" +
                    "\n" +
                    "Recommended next actions:\n" +
                    ">addcrypto - Add payments wallet address to make payments\n" +
                    ">listshops - View shops you've joined\n" +
                    ">start - List all commands with choices to select\n" +
                    ">cancel - Cancel current operation\n"

        }

        // clean up with above 'confirmed' function
        fun orderDeclined(order: DeliveryOrder, reason: String): String {
            return transaction {
                "FROM: Vendor (${order.customer.storefront?.displayName})\n\n" +
                        "Order ${order.orderCode} declined\n\n" +
                        "Reason: ``$reason``\n\n" +
                        "Items returned to your cart."
                //todo prompt to do /browse or /menu?

            }
        }

        fun addAnotherSkuQuestion(isFirst: Boolean = true): String {
            val anotherStr = if (isFirst) {
                " a"
            } else {
                " another"
            }
            return "Would you like to add$anotherStr SKU to this product?${yesNoOptions()}"

        }

        fun orderShipped(receipt: String): String {
            return hintText("Your order has been shipped.\n\n${receipt} \n")
        }
    }

    object CartStrings {
        fun checkoutHint(): String {
            return wrappedHint(
                header = "There are still items in your cart." +
                        "\nUse /checkout to order them.",
                content = "Happy shopping!",
                CmdRegistry.CHECKOUT,
                CmdRegistry.MENU,
                CmdRegistry.VIEWCART,
                CmdRegistry.CLEARCART,
                CANCEL
            )

        }

    }

    @Deprecated("Make use of new string resources")
    object VendorStrings {

        fun welcomeMsg(): String {
            return "Welcome! I am the Signal shopping bot, the most private and secure way to buy or sell goods" +
                    " and service. Please be aware that I am still in beta. " +
                    "Thank you for reporting all bugs and feedback to your onboarding admin.\n" +
                    "\n" +
                    "Let's get started! Here are a few tips for interacting with my system:\n" +
                    "\n" +
                    "Sending the word \"start\" will open my multiple choice menu, " +
                    "allowing you to see all of the different commands available in " +
                    "my system. If you are an advanced user, you can also try typing " +
                    "specific commands to navigate through menus quicker.\n" +
                    " \n" +
                    "Any time you use a command (including the “start” command) you " +
                    "start a process, if you want to use a different command to " +
                    "start a different process you must use the cancel command. " +
                    "If you forget to do this the bot will ask you if want to " +
                    "cancel before proceeding to the new process. You can also " +
                    "send the word \"feedback\" to ask questions or send a " +
                    "message directly to the developers.\n" +
                    "\n" +
                    "Before your store can open I will need you to complete " +
                    "the following:\n" +
                    "\n" +
                    "1. Add a payments account to process transactions\n" +
                    "\n" +
                    "2. List items for sale in your store and add inventory\n" +
                    "\n" +
                    "Once you have completed these steps, you may use the \"share\" " +
                    "command to send your store info to new customers. (There are many free QR code generators available online if you would like to use our business card template to promote your store) \n" +
                    "\n" +
                    "Well, what are you waiting for? Send \"start\", \"addcrypto\", or \"addproduct\" " +
                    "now to get started!\n" +
                    "\n" +
                    "Recommended next actions:\n" +
                    ">start - List all commands with choices to select\n" +
                    ">addcrypto - Add payments wallet address to receive payments\n" +
                    ">addproduct - Add a product to your inventory\n" +
                    ">cancel - Cancel current operation\n" +
                    "\n"


        }


        fun orderShipped(orderId: Int, receipt: String): String {
            return hintText("Order #$orderId has been marked as shipped.\n\n${receipt} \n")
        }

        fun paymentPendingString(): String {
            return "Payment from customer pending..."
        }


        fun noSaleItems(): String {
            return hintText(
                "No SKUs found (try creating a product first)",
                CmdRegistry.ADDPRODUCT,
            )
        }


        fun nudgeConfirm(timeString: String = ""): String {
            return wrappedHint(
                header = "You still have a pending order to confirm before receiving payment. \n" +
                        "$timeString",
                content = "Please follow these steps:" +
                        "\n1. Type: /CONFIRM " +
                        "\n2. Select by letter the order to confirm (likely `A`) " +
                        "\n3. Add a note for your customer " +
                        "\n\nTHIS IS AN AUTO-GENERATED MESSAGE",
                CmdRegistry.CONFIRM,
            )
        }


        fun promptNameTag(): String {
            return "Enter name tag"
        }

        fun promptSms(): String {
            return "Enter phone number"
        }
    }

    fun iDontUnderstand(data: String = ""): String {
        return "I don`t understand \"$data\"\n\nIf you are trying to enter a shop join code, " +
                "please check the spelling and try again."
    }

    fun uploadMenuYesNo(showOverwriteWarning: Boolean = false): String {
        return "Attach this menu to your storefront?${
            if (showOverwriteWarning) {
                "\n\nThis will overwrite the current posted menu"
            } else {
                ""
            }
        }${yesNoOptions()}"

    }


    fun yesNoOptions(): String {
        return "\n  Y. Yes\n  N. No"
    }

    fun addToCartQuestion(): String {
        return "Add to your cart?${yesNoOptions()}"
    }

    fun generateCodesQuestion(): String {
        return "Generate join codes?${yesNoOptions()}"
    }

    fun addToInventoryQuestion(): String {
        return "Save SKU to your inventory?${yesNoOptions()}"
    }


    fun correctYesNoQuestion(): String {
        return "Save?${yesNoOptions()}"
    }

    fun attachYesNoQuestion(numImages: Int): String {
        if (numImages == 1) {
            return attachYesNoQuestion()
        }
        return "Attach $numImages images to this product? " +
                "\n\n(This will overwrite any existing images for this product)${yesNoOptions()}"
    }

    fun attachYesNoQuestion(): String {
        return "Attach this image to this product? " +
                "\n\n(This will overwrite any existing images for this product)${yesNoOptions()}"
    }

    fun selectCryptoPrompt(): String {
        return "Select payment type"
    }

    fun addedToCartString(): String {
        return "added to your cart"
    }

    fun enterPricePrompt(): String {
        return "Enter unit price"
    }

    fun removeYesNoQuestion(): String {
        return "This operation cannot be undone.\n" +
                "Proceed with removal?${yesNoOptions()}"
    }

    fun saveAndNotifyCustomer(): String {
        return "Save and notify customer?${yesNoOptions()}"
    }

    fun continueYesNoQuestion(): String {
        return "Continue?${yesNoOptions()}"
    }

    fun vendorConfirmOrder(): String {
        return "Will you fulfill this order?${yesNoOptions()}"
    }

    fun specialDeliveryNotes(): String {
        return "Enter any special delivery instructions. If none, please enter \"None\""

    }


    fun showHelp(): String {
        return hintText("")
    }

    fun customerNotLinked(): String {
        return hintText(
            "Not linked to a vendor. \n\n" +
                    "Enter vendor join code to continue, or contact your vendor for help."
        )
    }

    fun customerEmptyCart(): String {
        return hintText(
            "Your cart is empty.",
            CmdRegistry.MENU,
        )
    }

    fun vendorNoAddressYet(): String {
        return hintText(
            "Your vendor has not yet added a payment method. Please try again shortly.",
        )
    }

    private fun cmdToLower(cmd: BaseCommand): String {
        return cmd.toString().lowercase()
    }


    fun wrappedHint(header: String, content: String, vararg cmds: BaseCommand): String {
        return "$header\n\n" +
                "$content${hintText("", *cmds)}"
    }

    @Deprecated("Get away from using 'Recommended next actions'")
    fun hintText(header: String, vararg cmds: BaseCommand): String {
        var outString = "$header\n\nRecommended next actions:\n"

        val listIndexChar = ">"

        for (cmd in cmds) {
            outString += "$listIndexChar ${cmdToLower(cmd)} - ${cmd.hint}\n"
        }

        if (!cmds.contains(TopCommand.START)) {
            var cmd = TopCommand.START
            outString += "$listIndexChar ${cmdToLower(cmd)} - ${cmd.hint}\n"
        }
        if (!cmds.contains(CANCEL)) {
            var cmd = CANCEL
            outString += "$listIndexChar ${cmdToLower(cmd)} - ${cmd.hint}\n"
        }

        return outString
    }

    fun noVendorProductsHint(): String {
        return hintText(
            "No products found. Create a product first.",
            CmdRegistry.ADDPRODUCT,
        )
    }

    fun termsOfService(): String {
        return "Please read these terms of service, they are the shortest you will find anywhere.\n" +
                " \n" +
                "\n" +
                "PRIVACY POLICY\n" +
                "\n" +
                "The only information exposed to the “Chat Bot” is a user’s phone number and the last time " +
                "a user interacted with the “Chat Bot.” All of your product and sales information is ENCRYPTED " +
                "and not visible to the “Chat Bot,” this excludes financial data such as sending address and " +
                "payment amount (This is how we determine fees). We take the highest precautions of security " +
                "by utilizing Signal service for optimal End-to-End Encryption and top-tier encryption " +
                "protocols to secure your shop and purchasing data on the back end. Any information given " +
                "to the “Chat Bot” is confidential between the user and the “Chat Bot.” If a user loses " +
                "any data or deletes any data, it can not be recovered by the “Chat Bot” or its creator(s).\n" +
                "\n" +
                "LIABILITY AGREEMENT\n" +
                "\n" +
                "You expressly understand and agree that in no event will the creator(s) of this “Chat Bot” " +
                "or its suppliers or licensors, be liable with respect to any subject matter of this " +
                "agreement under any contract, negligence, strict liability or other legal or equitable " +
                "theory for: (i) any special, incidental or consequential damages; (ii) the cost of " +
                "procurement or substitute products or service; (iii) interruption of use or loss or " +
                "corruption of data; (iv) any statements or conduct of any third party on the service; " +
                "or (v) any unauthorized access to or alterations of your Content. We shall have no " +
                "liability for any failure or delay due to any conceivable matters.\n" +
                "\n" +
                "All content posted to the “Chat Bot” in any way is the responsibility of the poster of " +
                "that content, if that content is deemed illegal by any jurisdiction over that user then " +
                "that user will take sole responsibility for their legal infractions, if authorities in " +
                "that jurisdiction request information on the user from the creator(s) of the “Chat Bot” " +
                "then the creator(s) will be forced to hand over all information on that user (Total " +
                "information includes: Phone number, date/time of first message to “Chat Bot”, and " +
                "date/time of most recent interaction with “Chat Bot”).\n" +
                "\n" +
                "This “Chat Bot” is provided \"as is.\" The creator(s) and its suppliers and licensors " +
                "hereby disclaim all warranties of any kind, express or implied, including, without " +
                "limitation, the warranties of merchantability, fitness for a particular purpose and " +
                "non-infringement. Neither the Creator(s), nor its suppliers and licensors, makes any " +
                "warranty that the “Chat Bot” will be error free or that access to the “Chat Bot” will " +
                "be continuous or uninterrupted. You agree that any interruptions to the service will " +
                "not qualify for reimbursement or compensation. You understand that you purchase from, " +
                "or otherwise obtain products or service through, the “Chat Bot” at your own discretion " +
                "and risk.\n" +
                "\n" +
                "No advice or information, whether oral or written, obtained by you in any fashion shall " +
                "create any warranty.\n"
    }

}

