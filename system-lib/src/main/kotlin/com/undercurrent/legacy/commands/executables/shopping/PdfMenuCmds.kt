package com.undercurrent.legacy.commands.executables.shopping

import com.undercurrent.legacy.commands.executables.ExecutableExceptions
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomer
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomers
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.system.repository.entities.Admins
import com.undercurrent.legacy.repository.entities.system.attachments.AttachmentLinks
import com.undercurrent.system.messaging.outbound.notifyAdmins
import com.undercurrent.legacy.types.enums.AttachmentType
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.prompting.components.EmojiSymbol
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.ctx
import com.undercurrent.shared.utils.time.EpochNano
import com.undercurrent.shared.utils.time.unexpiredExpr
import com.undercurrent.system.context.SystemContext
import com.undercurrent.system.messaging.inbound.InboundAttachmentsFetcher
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

@Deprecated("Dr. Jones, what ever are you doing in such a nasty place?!")
object PdfMenuCmds {
    private const val downloadStr = "Menu download started..."
    private const val vendorEmptyMsg = "You do not have a menu posted"

    @Deprecated("Clean this up")

    private fun emptyCheck(
        sessionContext: SystemContext,
        storefront: Storefront,
        notEmptyMsg: String? = null,
        emptyMsg: String? = null,
        notifyOut: Boolean = true,
    ): List<AttachmentLinks.Entity> {
        with(transaction { storefront.pdfMenus }) {
            val msgOut = if (isNullOrEmpty()) {
                emptyMsg
            } else {
                notEmptyMsg
            }
            if (notifyOut) {
                msgOut?.let { sessionContext.interrupt(it) }
            }
            return this
        }
    }

    //todo clean this all up (condense together with upload for PRODUCT_IMAGE)
    @Deprecated("Clean this up")
    suspend fun uploadMenu(sessionContext: SystemContext) {

        /**
         * Don't display other menu options if they're empty
         */

        val interrupter = sessionContext.interrupter

        val thisStorefront = loadStorefront(sessionContext) ?: run {
            val stringOut = "Unable to load your storefront.\n\n" +
                    "Please contact your admin with ${CmdRegistry.FEEDBACK.upper()}"
            Log.error(stringOut)
            interrupter.sendOutput(stringOut)

            //todo add formatter to append ERROR, exception trace, and whatever else is needed
//            sessionContext.notifyAdmins(stringOut)

            notifyAdmins(stringOut)

            return
        }


        val attachmentsFetcher = InboundAttachmentsFetcher(user = sessionContext.user, sessionContext.routingProps)
        val existingMenus = emptyCheck(sessionContext = sessionContext, thisStorefront)
        val now = EpochNano()

        interrupter.sendOutput("Send your PDF file to me now (send `q` to cancel).")

        val results = attachmentsFetcher.fetchAttachmentsOrCancel(now)

        //prompt here for any captions that customer may want to add?
        results.let { attachments ->
            if (attachments.count() > 1) {
                //todo add error emojis
//                sessionPair.interrupt(
//                    "Please only upload one file at a time.\n\nYou can try again, " +
//                            "or contact admins with ${CmdRegistry.FEEDBACK.handle()}"
//                )
                Admins.notifyError("${sessionContext.userId} tried to upload multiple menus at once")
//                return
            } else if (attachments.isEmpty()) {
                interrupter.sendOutput(
                    "Operation cancelled."
                )
                return
            }

            if (UserInput.promptYesNo(
                    PressAgent.uploadMenuYesNo(showOverwriteWarning = existingMenus.isNotEmpty()),
                    sessionContext,
                    yesText = "Menu posted to your storefront",
                    noText = "Menu not uploaded.\n\nYour storefront is unchanged.",
                    disableEmoji = false,
                )
            ) {
                existingMenus.forEach {
                    it.expire()

                    //todo could expire actual PDF here...
                    //todo could also be because attachments.links is coming back null

                    interrupter.sendOutput("${EmojiSymbol.SUCCESS.prefix()}Removed existing menu")
                    notifyAdmins("${sessionContext.user.oneLinerString(false)} replaced their menu")

                    //todo use Ancestors here
                }

                //take only the latest created attachment

                attachments
                    .maxByOrNull { transaction { it.id } }
                    ?.let {
                        it.createLinkForAttachment(
                            parentId = transaction { thisStorefront.uid },
                            attachmentType = AttachmentType.SHOP_MENU
                        )
                        notifyAdmins("$sessionContext added a new menu")
                    }

                promptToNotifyCustomers(
                    thisStorefront = thisStorefront,
                    sessionContext = sessionContext,
                    prompt = "uploaded a new menu",
                    inputHint = "New menu uploaded! Use ${CmdRegistry.MENU.upper()} to view."
                )
            }
        }
    }

    //todo turn 'inputHint' into 'default msg' and give a choice whether to send it directly
    private suspend fun promptToNotifyCustomers(
        thisStorefront: Storefront,
        sessionContext: SystemContext,
        prompt: String,
        inputHint: String,
        noText: String = "Message to customers not sent.",
    ) {
        if (UserInput.promptYesNo(
                confirmText = "Would you like to notify your customers that you have $prompt?${PressAgent.yesNoOptions()}",
                sessionContext = sessionContext,
                noText = noText,
                disableEmoji = false,
            )
        ) {
            val msgToSend = if (UserInput.promptYesNo(
                    confirmText = "Would you like to send this message?${PressAgent.yesNoOptions()}" +
                            "\n\n\"$inputHint\"",
                    sessionContext = sessionContext,
                    noText = null,
                    disableEmoji = false,
                )
            ) {
                inputHint
            } else {
                UserInput.promptAndConfirm(
                    promptString = "Please input custom message to your customers:",
                    sessionContext = sessionContext,
                    confirmTextVerb = "Send to customers",
                    noText = "Message to customers not sent.",
                    disableEmoji = false,
                )
            }
            msgToSend?.let { msg ->
                thisStorefront.broadcastToActiveCustomers(msg = msg)
                val customers = fetchActiveCustomers(thisStorefront)
                val customerCountStr = when (val count = customers.count()) {
                        1 -> "$count customer"
                        else -> "$count customers"
                    }
                sessionContext.interrupt("${EmojiSymbol.GREEN_CHECK_MARK.prefix()}Successfully notified $customerCountStr")
            }
        }
    }

    private suspend fun fetchActiveCustomers(thisStorefront: Storefront): List<ShopCustomer> {
        val linkedCustomersExpr =
            ShopCustomers.storefront eq thisStorefront.id and unexpiredExpr(ShopCustomers)

        return ctx {
            ShopCustomer.find { linkedCustomersExpr }
                .toList()
        }
    }

    //todo make use of lazy loading
    private fun loadStorefront(sessionContext: SystemContext, notifyOut: Boolean = true): Storefront? {
        val thisStorefront = when (sessionContext.role) {
            ShopRole.VENDOR -> {
                transaction { sessionContext.user.shopVendor?.currentStorefront }
            }

            ShopRole.CUSTOMER -> {
                transaction { sessionContext.user.currentCustomerProfile?.storefront }
            }

            else -> {
                if (notifyOut) {
                    val msg = "${EmojiSymbol.STOP_SIGN.prefix()}Invalid role for this operation ()\n\n" +
                            "Please contact your admin with ${CmdRegistry.FEEDBACK.lower()}"
                    throw ExecutableExceptions.GenericException(sessionContext, msg)
                }
                return null
            }
        }

        return thisStorefront
    }

    @Deprecated("Clean this up")
    suspend fun viewMenu(sessionContext: SystemContext, notifyOut: Boolean = true): Int {
        var sentCount = 0

        val customerEmptyMsg = "No menu available to view"
        val customerHint = "use /browse to view items for selection"

        loadStorefront(sessionContext, notifyOut)?.let { thisStorefront ->
            val emptyMsg = if (sessionContext.role == ShopRole.VENDOR) {
                vendorEmptyMsg
            } else {
                customerEmptyMsg
            }

            emptyCheck(
                sessionContext,
                storefront = thisStorefront,
                emptyMsg = emptyMsg,
                notEmptyMsg = downloadStr,
                notifyOut = notifyOut,
            ).ifEmpty { return sentCount }
                .let { attachmentLinks ->
                    //todo a bit smelly: images need time to come in
                    if (!sessionContext.isTestMode()) {
                        Thread.sleep(1000L)
                    }

                    if (attachmentLinks.isNotEmpty()) {
                        sessionContext.interrupt("Downloading PDF of menu...")
                    }

                    attachmentLinks.forEach {
                        transaction { it.parentAttachment }?.let {
                            it.send(sessionContext.user, dbusPropsIn = sessionContext.routingProps)
                            sentCount++
                        }
                    }
                }
//            sessionPair.interrupt("Tip: /" + SlashCommands.fromCommandEnum(BROWSE).toString())
        }
        return sentCount
    }


    /**
     * Find attachment linked to vendor that is not expired
     * and has label for MENU.
     *
     * Use this as the opening check on if a MENU is currently
     * being used.
     */
    @Deprecated("Clean this up")
    suspend fun viewMenu(sessionContext: SystemContext) {
        viewMenu(sessionContext, true)
    }

    @Deprecated("Clean this up")
    suspend fun removeMenu(sessionContext: SystemContext) {
        loadStorefront(sessionContext)?.let { thisStorefront ->
            emptyCheck(
                sessionContext = sessionContext,
                storefront = thisStorefront,
                emptyMsg = vendorEmptyMsg
            ).ifEmpty { return }.let { existing ->
                if (UserInput.promptYesNo(
                        PressAgent.removeMenuYesNo(),
                        sessionContext,
                        yesText = "Menu removed from your storefront.",
                        noText = "Menu not removed.\n\nYour storefront is unchanged.",
                        disableEmoji = false,
                    )
                ) {
                    existing.forEach { it.expire() }
                    notifyAdmins("${sessionContext.user} removed their menu")
                }
            }
        }
    }
}