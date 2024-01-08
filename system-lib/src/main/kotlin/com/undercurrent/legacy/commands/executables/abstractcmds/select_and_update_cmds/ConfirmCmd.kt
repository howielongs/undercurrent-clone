package com.undercurrent.legacy.commands.executables.abstractcmds.select_and_update_cmds

import com.undercurrent.legacy.commands.executables.ExecutableExceptions
import com.undercurrent.legacy.commands.executables.abstractcmds.FetchableVendorCryptoAddresses
import com.undercurrent.legacy.commands.executables.add.addcrypto.AddWalletCmd
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.dinosaurs.prompting.TextBox
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.promptables.PromptableParam
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.promptables.dataprompts.VerifyInputsPrompt
import com.undercurrent.legacy.repository.entities.payments.CryptoAddress
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrder
import com.undercurrent.legacy.service.crypto.BitcoinWalletServices
import com.undercurrent.legacy.types.enums.CryptoType
import com.undercurrent.legacy.types.enums.ResponseType
import com.undercurrent.legacy.types.enums.status.OrderStatus
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.shared.formatters.UserToIdString
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.types.validators.Yes
import com.undercurrent.shared.types.validators.YesNoChar
import com.undercurrent.system.context.SessionContext
import org.jetbrains.exposed.sql.transactions.transaction

class ConfirmCmd(sessionContext: SessionContext) :
    SelectAndUpdateCmd(CmdRegistry.CONFIRM, sessionContext), FetchableVendorCryptoAddresses {

    private lateinit var thisOrder: DeliveryOrder
    private lateinit var newStatus: OrderStatus
    private lateinit var notesResponse: String

    override val operationInfinitiveVerb: String
        get() = "to confirm"


    override fun fetchVendorCryptoAddresses(): List<CryptoAddress> {
        return vendorAddresses
    }

    @Deprecated("Use swap_nodes instead")
    suspend fun confirmOrDeclineOrder(
        order: DeliveryOrder,
        yesNo: ConfirmOrderPrompt = ConfirmOrderPrompt(),
        notes: VendorNotesPrompt = VendorNotesPrompt(),
        declineReason: VendorDeclinePrompt = VendorDeclinePrompt(),
    ): Boolean? {
        thisOrder = order

        val shouldStartWallet =
            if (transaction { thisOrder.cryptoType } == CryptoType.BTC && !BitcoinWalletServices.isWalletRunning()) {
//                "Bitcoin wallet has not yet been initialized. Please try again in a few minutes.".let {
//                    sessionPair.interrupt(
//                        it,
//                        VENDOR
//                    )
//                }
//                Admins.notifyError("confirmOrDeclineOrder: Vendor attempting to confirm an order, but BTC wallet is not yet up. Attempting to start...")
//                BitcoinWalletServices.startWallet()
                true
            } else {
                false
            }


        if (shouldStartWallet) {
            return null

            //todo wrap Wallet start in async?
//            WalletManager.startWallet()
//            return null
        }

        val thisYesNo = yesNo.acquireValue(sessionContext) //could have this return enum status instead

        var confirmYesNoString = "Confirm Order: "
        var notesOutString: String
        var processingText: String = "Processing order "

        val isYes = thisYesNo?.let { YesNoChar.validate(it) is Yes } ?: false

        newStatus = if (isYes) {
            confirmYesNoString += "YES"
            notesResponse = notes.acquireValue(sessionContext).toString()
            notesOutString = "Delivery notes: $notesResponse"
            processingText += "confirmation"
            OrderStatus.CONFIRMED
        } else {
            confirmYesNoString += "NO"
            notesResponse = declineReason.acquireValue(sessionContext).toString()
            notesOutString = "Reason: $notesResponse"
            processingText += "rejection"
            OrderStatus.DECLINED
        }

        val orderStr = UserToIdString.toIdStr(order)

        var verifyPromptStr = TextBox.verifyInputsBox(
            header = "$orderStr",
            footerPrompt = PressAgent.saveAndNotifyCustomer(),
            lineStrings = arrayOf(confirmYesNoString, notesOutString)
        )

        VerifyInputsPrompt(
            sessionContext = sessionContext,
            prompt = verifyPromptStr,
            yesText = "$processingText...",
            noText = "Order not confirmed or declined. Operation complete.",
        ).promptUser()?.let {
            return if (it == true.toString()) {
                //this section feels a bit awkward
                when (newStatus) {
                    OrderStatus.CONFIRMED -> {
                        order.confirmOrder(notesOutString, sessionContext.routingProps)
                        true
                    }

                    OrderStatus.DECLINED -> {
                        order.declineOrder(notesOutString, sessionContext.routingProps)
                        false
                    }

                    else -> {
                        null
                    }
                }
            } else {
                null
            }
        } ?: return null
    }


    // clean up these prompt declarations
    class ConfirmOrderPrompt(
        value: String? = null,
    ) : PromptableParam(
        value,
        field = "isOrderConfirmed",
        validationType = ResponseType.YESNO,
        prompt = PressAgent.vendorConfirmOrder(),
        displayName = "Confirm Order",
    )

    @Deprecated("Use swap_nodes instead")
    class VendorNotesPrompt(
        value: String? = null,
    ) : PromptableParam(
        value,
        field = DeliveryOrder::notesToCustomer.name,
        validationType = ResponseType.STRING,
        prompt = "Enter comments to customer (e.g. delivery estimate, etc.):",
        displayName = "Delivery details",
    )

    @Deprecated("Use swap_nodes instead")
    class VendorDeclinePrompt(
        value: String? = null,
    ) : PromptableParam(
        value,
        field = DeliveryOrder::notesToCustomer.name,
        validationType = ResponseType.STRING,
        prompt = "Enter reason for cancellation to customer (out of stock, etc.):",
        displayName = "Delivery details",
    )

    override suspend fun preFunc(): Boolean {
        if (vendorAddresses.isNullOrEmpty()) {
            AddWalletCmd(sessionContext).execute()
            //todo test retries here
        }
        return super.preFunc()
    }


    override fun sourceList(): List<ExposedEntityWithStatus2> {
        return ordersToConfirm
    }

    private val ordersToConfirm: List<DeliveryOrder> by lazy {
        transaction {
            thisStorefront.unconfirmedOrders
        }.ifEmpty {
            throw ExecutableExceptions.GenericException(
                sessionContext, sessionMsg = "No open orders to confirm", errorLogMsg = "No orders to load for user"
            )
        }
    }
}