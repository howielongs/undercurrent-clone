package com.undercurrent.legacy.commands.executables.shopping

import com.undercurrent.legacy.commands.executables.ExecutableExceptions
import com.undercurrent.legacy.commands.executables.abstractcmds.CanCheckShouldShow
import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.BaseCommand
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput
import com.undercurrent.legacy.dinosaurs.prompting.selectables.NestedSkuSelectionMap
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectableCommand
import com.undercurrent.legacyshops.repository.entities.shop_items.CartItem
import com.undercurrent.legacyshops.repository.entities.shop_items.SaleItem
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.system.repository.entities.Admins
import com.undercurrent.system.messaging.outbound.notifyAdmins
import com.undercurrent.legacy.repository.schema.toIdRoleStr
import com.undercurrent.legacy.types.enums.ResponseType
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.legacy.types.string.PressAgent.addedToCartString
import com.undercurrent.shared.view.components.CanStartExpirationTimer
import com.undercurrent.shared.view.components.ExpirationTimer
import com.undercurrent.shared.formatters.formatPretty
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.context.SessionContext
import com.undercurrent.system.dbus.SignalExpirationTimer
import kotlinx.coroutines.delay
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Get: List of shop items
 * Create: CartItem(customerId, orderId, qty)
 */
@Deprecated("Replace with nodal impl")
class BrowseMenu(sessionContext: SessionContext) : Executable(CmdRegistry.MENU, sessionContext),
    CanCheckShouldShow, CanStartExpirationTimer {



    override suspend fun execute() {
        startTimer()
        PdfMenuCmds.viewMenu(sessionContext, false)
        menuCmd(sessionContext)
    }

    override fun shouldShow(): Boolean {
        return transaction { theseSaleItems.isNotEmpty() }
    }

    val theseSaleItems: List<SaleItem> by lazy {
        transaction {
            thisStorefront.saleItems
        }.ifEmpty {
            throw ExecutableExceptions.EmptyListException(
                sessionContext, "SKUs", thisCommand.lower()
            )
        }
    }


    private suspend fun promptActionAfterMenuCmd(sessionContext: SessionContext) {
        //todo can massively clean this up
        UserInput.selectAndRunCallback(
            sessionContext,
            listOf(

                SelectableCommand(CmdRegistry.CHECKOUT, "Check out now"),
                SelectableCommand(CmdRegistry.MENU, "Keep shopping"),
                SelectableCommand(CmdRegistry.VIEWCART, "View your cart"),
//            SelectableCommand(UserCommand.EDITCART, "Edit cart"),
                SelectableCommand(CmdRegistry.CLEARCART, "Clear cart contents"),
                SelectableCommand(CmdRegistry.FINISH, "Save for later"),
            ),
            headerText = "What would you like to do next?"
        )
    }


    private suspend fun menuCmd(sessionContext: SessionContext) {
        //can probably do a better job caching this (don't optimize now)

        val selectedSku = selectSkuForMenuBrowse(
            sessionContext = sessionContext,
            displayAttachments = true,
            storefront = thisStorefront,
            command = CmdRegistry.MENU,
        ) ?: run {
            Log.error("Invalid SKU selected $sessionContext")
            return
        }

        //todo create prompt objects for this
        val thisQuantity = UserInput.promptUser(
            "How many items would you like?",
            validationType = ResponseType.INT,
            sessionContext = sessionContext
        ) ?: return

        var theseNotes: String? = null
        var notesLineStr = ""


        // include subtotal/amount this would add to cart
        val confirmString = transaction {
            """
            |[SHOP:${thisStorefront.displayName}]
            |
            |${selectedSku.name?.uppercase()} (${selectedSku.unitSize})
            | • Details: ${selectedSku.details}
            | 
            | • Price per unit: ${'$'}${selectedSku.price}
            | • Quantity: $thisQuantity$notesLineStr
            |
            |${PressAgent.addToCartQuestion()}
        """.trimMargin()
        }

        if (UserInput.promptYesNo(
                confirmString,
                sessionContext,
                noText = "Item(s) not ${addedToCartString()}. Your cart is unchanged."
            )
        ) {
            val quantityInt = try {
                thisQuantity.toInt()
            } catch (e: Exception) {
                Admins.notifyError(
                    "MENU: Unable to convert quantity from String to Int " +
                            "\n${toIdRoleStr(sessionContext)}"
                )
                sessionContext.interrupt("Error processing item. Please try again.")
                return
            }

            transaction {
                CartItem.new {
                    customerProfile = thisCustomerProfile
                    saleItem = selectedSku
                    notes = theseNotes ?: "" //todo should make this nullable
                    quantity = quantityInt
                }

                "Item${if (quantityInt == 1) "" else "s"} ${addedToCartString()}".let {
                    sessionContext.interrupt(it)

                    var amountStr = ""
                    try {
                        //todo also get subtotal of cart at this point
                        //todo make use of Currency util data classes instead of this rubbish
                        var amount = quantityInt * selectedSku.price.toDouble()
                        var amountCurrency = formatPretty(amount.toString())
                        amountStr = "(value: $$amountCurrency)"
                    } catch (e: Exception) {
                        Log.debug("Math type error with calculating cart subtotal")
                    }
                    notifyAdmins("${sessionContext.user} added item to cart $amountStr")
                }
            }

            // this may make it difficult to handle interruptions? (not for read-only commands, but still..)
            startNewCommand(CmdRegistry.VIEWCART)
            promptActionAfterMenuCmd(sessionContext)
        }
    }


    //todo should this be different whether Vendor or Customer?
    private suspend fun selectSkuForMenuBrowse(
        sessionContext: SessionContext,
        emptyText: String = "No products available to select. \n${PressAgent.showHelp()}",
        storefront: Storefront,
        displayAttachments: Boolean = true,
        command: BaseCommand? = null,
    ): SaleItem? {
        //todo apparently this code is duplicated in another class...
        //todo see about using async and await for these
        val products = tx { storefront.products }
        val saleItems = tx { storefront.saleItems }
        if (saleItems.isNullOrEmpty()) {
            emptyText.let { sessionContext.interrupt(it) }
            return null
        }

        val firstProductName = tx { products.first().name.lowercase() }

        //todo verify this works reliably
        val promptText = "Add an item to your cart by entering the two-digit number. " +
                "Ex: For a ${firstProductName}, enter 1.1\n"


        if (displayAttachments) {
            storefront.sendAllProductAttachments(
                tx { sessionContext.user.uid },
                products,
                command = command,
                sessionContext = sessionContext

            )
            //todo a bit smelly: images need time to come in
            if (!sessionContext.isTestMode()) {
                delay(1000L)
            }
        }

        Log.debug("MENU: Finished displaying attachments")

        //todo embed this all with existing SelectableEntity impl
        val selectableMenu = NestedSkuSelectionMap(
            products = products,
            headerText = promptText
        ).loadMenu()
        Log.debug("MENU: Finished loading selectable menu")

        //todo improve this to actually pass entity reference instead of id
        UserInput.chooseSkuIdFromList(
            sessionContext,
            selectableMenu
        )?.let { selectedSku ->
            return tx {
                return@tx SaleItem.findById(selectedSku)
            }
        }
        return null
    }

    private val expirationTimer: ExpirationTimer by lazy {
        SignalExpirationTimer(sessionContext)
    }

    override fun startTimer() {
        expirationTimer.startTimer()
    }

    override fun startTimer(timeSeconds: Int) {
        expirationTimer.startTimer(timeSeconds)
    }

}