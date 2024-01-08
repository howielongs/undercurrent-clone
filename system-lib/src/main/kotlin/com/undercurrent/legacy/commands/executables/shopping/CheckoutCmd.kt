package com.undercurrent.legacy.commands.executables.shopping


import com.undercurrent.legacy.commands.executables.ExecutableExceptions
import com.undercurrent.legacy.commands.executables.abstractcmds.CanCheckShouldShow
import com.undercurrent.legacy.commands.executables.abstractcmds.CanRunPreFunc
import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.executables.abstractcmds.FetchableVendorCryptoAddresses
import com.undercurrent.legacy.commands.executables.shopping.cancelations.OrderCancellationHandler
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.commands.registry.CmdRegistry.CHECKOUT
import com.undercurrent.legacy.commands.registry.UserCommand
import com.undercurrent.legacy.data_transfer_objects.currency.FiatAmount
import com.undercurrent.legacy.data_transfer_objects.currency.ReceiptValuesFiat
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.promptables.dataprompts.DataPromptSequence
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectableEnum
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectedEnum
import com.undercurrent.legacy.repository.entities.payments.CryptoAddress
import com.undercurrent.legacy.service.PermissionsValidator
import com.undercurrent.legacy.types.enums.CryptoType
import com.undercurrent.legacy.types.enums.ResponseType
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.legacyshops.repository.entities.shop_orders.Invoice
import com.undercurrent.legacyshops.repository.entities.shop_items.CartItem
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrders
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.view.components.CanStartExpirationTimer
import com.undercurrent.shared.view.components.ExpirationTimer
import com.undercurrent.system.context.SessionContext
import com.undercurrent.system.dbus.SignalExpirationTimer
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

/**
 * If customer has no items in their cart, inform them and end operation
 * - Should hide checkout option if this is the case
 *
 * If customer has items in a cart:
 *  - If vendor has multiple payment methods, ask customer which payment method
 *      they'd like to use (e.g. BTC, MOB, etc.)
 *      - If vendor only has one method, then don't ask customer
 *      - If vendor hasn't added a payment method, then notify customer and vendor
 *          - Notify customer when payment ready to be accepted
 *          - Consider changing this in the future (as of 11-10-22)
 *
 * Once past all that:
 *  - prompt customer for delivery instructions (if is delivery storefront)
 *  -
 * Take customer's cart items
 */
@Deprecated("Use TreeNodes instead")
class CheckoutCmd(sessionContext: SessionContext) :
    Executable(CHECKOUT, sessionContext),
    CanCheckShouldShow,
    CanRunPreFunc,
    CanStartExpirationTimer,
    FetchableVendorCryptoAddresses {


    private val expirationTimer: ExpirationTimer by lazy {
        SignalExpirationTimer(sessionContext)
    }

    override fun startTimer() {
        expirationTimer.startTimer()
    }

    override fun startTimer(timeSeconds: Int) {
        expirationTimer.startTimer(timeSeconds)
    }

    private val receipt: String by lazy {
        thisCustomerProfile.getCartContentsString(
            cartItems, receiptFooter = thisCustomerProfile.generateReceiptFooter(
                subtotalAmount = subtotal, feesAmount = fees, totalAmount = subtotal.add(fees)
            )
        )
    }

    private val cartItems: List<CartItem> by lazy {
        transaction {
            thisCustomerProfile.cartContents
        }.apply {
            ifEmpty {
                throw ExecutableExceptions.EmptyCartException(sessionContext)
            }
        }
    }


    override suspend fun preFunc(): Boolean {
        if (PermissionsValidator.hasValidPermissionsForOperation(
                sessionContext,
                thisCommand
            )
        ) {
            if (OrderCancellationHandler(sessionContext).promptUserIfUnpaidOrders()) {
                return false
            }

            sessionContext.interrupt(receipt)

            startTimer()
            return true
        }
        return false
    }

    suspend fun doCheckout(
        zipcodeIn: String? = null,
        deliveryAddressIn: String? = null,
        deliveryNameIn: String? = null,
        notesToVendorIn: String? = null,
    ) {
        //transaction {   sessionPair.user.activeCustomers[0].vendor.uid }
        if (!preFunc()) {
            return
        }

        //enable cryptoType as input arg
        //can also insert a raw value for a field into DataPromptSequence

        with(DataPromptSequence(sessionContext, DeliveryOrders)) {
            //todo add 'selectIndex' (for remove ops) and 'selectInvoice'

            //todo try to wrap payments selection in a DataPrompt class instance

            prompt(
                value = zipcodeIn,
                prompt = "What is your delivery zipcode?",
                validationType = ResponseType.ZIPCODE,
                fieldTag = DeliveryOrders::zipcode.name,
            )
            prompt(
                value = deliveryAddressIn,
                prompt = "Full delivery street address including apartment numbers?",
                fieldTag = DeliveryOrders::deliveryAddress.name,
                displayName = "Street address"
            )
            prompt(
                value = deliveryNameIn,
                prompt = "First and last name for delivery?",
                fieldTag = DeliveryOrders::deliveryName.name,
                displayName = "Delivery name"
            )
            prompt(
                value = notesToVendorIn,
                prompt = PressAgent.specialDeliveryNotes(),
                fieldTag = DeliveryOrders::notesToVendor.name,
                displayName = "Notes"
            )

            selectInvoiceByPaymentMethod()?.let {
                DeliveryOrders.save(
                    sessionContext,
                    invoiceIn = it,
                    customerProfileIn = thisCustomerProfile,
                    shopVendorIn = thisShopVendor,
                    promptSequence = this
                )
            }
        }
    }

    override suspend fun execute() {
        doCheckout()
    }


    val invoices: List<Invoice> by lazy {
        transaction {
            var savedInvoices = ArrayList<Invoice>()

            //todo see how this might play better with suspendTransactions...
            vendorAddresses.forEach { address ->
                address.cryptoType?.let { type ->
                    Invoice.save(
                        cryptoTypeIn = type,
                        receiptValues = receiptValues,
                        receiptText = receipt,
                        storefrontIn = thisStorefront,
                    )?.let {
                        savedInvoices.add(it)
                    }
                }
            }
            savedInvoices
        }
    }

    private val receiptValues: ReceiptValuesFiat by lazy {
        transaction { ReceiptValuesFiat(subtotal, fees, total) }
    }

    private val total: FiatAmount by lazy {
        transaction {
            subtotal.add(fees)
        }
    }

    private val feePercentDecimal: BigDecimal by lazy {
        //todo Impl dynamic feePct fetch by storefront
        transaction { thisStorefront.feePercentDecimal() }
    }

    private val fees: FiatAmount by lazy {
        transaction {
            subtotal.multiply(feePercentDecimal)
        }
    }

    private val subtotal: FiatAmount by lazy {
        transaction {
            thisCustomerProfile.cartSubtotal
        } ?: throw ExecutableExceptions.CartSubtotalNullException(sessionContext)
    }


    private fun gatherTotalsFromInvoice(invoices: List<Invoice?>): List<SelectableEnum> {
        return transaction {
            val options = ArrayList<SelectableEnum>()
            invoices.forEach { invoice ->
                invoice?.cryptoType?.let { type ->
                    val promptText = invoice.currencyChoicePrompt
                    //todo this can probably be much better
                    options.add(SelectableEnum(promptText, enumValue = type))
                }
            }
            //todo ensure CANCEL is not duplicated
            options.add(SelectableEnum(promptText = "Cancel", enumValue = CmdRegistry.CANCEL))
            options
        }
    }

    //todo fix this up with new Invoice type
    private suspend fun selectInvoiceByPaymentMethod(): Invoice? {
        if (invoices.size == 1) {
            return invoices[0]
        } else if (invoices.size > 1) {
            val options = gatherTotalsFromInvoice(invoices.sortedBy { it.cryptoType?.priority })

            UserInput.selectAnOption(
                sessionContext,
                options,
                headerText = "Select a payment method:",
                shouldPromptForInterruptCommands = true,
                footerText = "\n(Note that actual values may change due to market volatility.)",
                appendCancel = false,
            )?.let { item ->
                if (item is SelectedEnum) {
                    Log.debug("User $sessionContext selected $item")

                    with(item.enum) {
                        when (this) {
                            is CryptoType -> {
                                return invoices.firstOrNull { it?.cryptoType?.abbrev() == this.abbrev() }
                            }

                            is UserCommand -> {
                                //todo how to handle handlerClass if switching to that?
                                this.callback?.invoke(sessionContext)
                                return null
                            }

                            else -> {
                                return null
                            }
                        }
                    }
                }
            }
        }
        return null
    }


    override fun shouldShow(): Boolean {
        //todo determine if necessary to save sessionPair to class field
        return transaction {
            if (thisCommand.permissions.contains(sessionContext.role)) {
                return@transaction when (sessionContext.role) {
                    ShopRole.CUSTOMER -> {
                        return@transaction sessionContext.user.currentCustomerProfile?.cartContents?.isNotEmpty()
                            ?: false
                    }

                    else -> false
                }
            }
            return@transaction false
        }
    }

    override fun fetchVendorCryptoAddresses(): List<CryptoAddress> {
        return vendorAddresses
    }

}