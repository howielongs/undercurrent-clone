package com.undercurrent.legacy.commands.executables.add

import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.system.context.SessionContext
import com.undercurrent.legacyshops.repository.entities.shop_items.ShopProduct
import com.undercurrent.legacy.types.enums.AttachmentType
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectableEntity
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectedEntity
import com.undercurrent.legacy.repository.entities.system.attachments.Attachments
import com.undercurrent.legacy.types.enums.ListIndexTypeOld
import com.undercurrent.shared.messages.CanSendToUser
import com.undercurrent.shared.messages.InterrupterMessageEntity
import com.undercurrent.shared.messages.UserOutputProvider
import com.undercurrent.shared.utils.time.EpochNano
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.messaging.inbound.CanFetchAttachments
import com.undercurrent.system.messaging.inbound.InboundAttachmentsFetcher
import org.jetbrains.exposed.sql.transactions.transaction

@Deprecated("Use Impl as Nodes")

class AddImage(sessionContext: SessionContext) : Executable(CmdRegistry.ADDIMAGE, sessionContext),
    CanSendToUser<InterrupterMessageEntity>,
    CanFetchAttachments {

    private val attachmentsFetcher by lazy {
        InboundAttachmentsFetcher(user = sessionContext.user, sessionContext.routingProps)
    }

    private val interrupter: UserOutputProvider<InterrupterMessageEntity> by lazy {
        sessionContext.interrupter
    }

    override fun sendOutput(msgBody: String): InterrupterMessageEntity? {
        return interrupter.sendOutput(msgBody)
    }

    override suspend fun fetchAttachmentsOrCancel(afterEpochNano: EpochNano): List<Attachments.Entity> {
        return attachmentsFetcher.fetchAttachmentsOrCancel(afterEpochNano)
    }

    override suspend fun execute() {
        //todo this all needs major cleanup

        val products = tx { sessionContext.user.shopVendor?.products } ?: run {
            sendOutput("No products available to select. \n${PressAgent.showHelp()}")
            return
        }

        //todo instead use Node impl
        val result = UserInput.selectAnOption(
            sessionContext = sessionContext,
            options = products.map { SelectableEntity(it) },
            indexType = ListIndexTypeOld.ABC,
            headerText = "Select product to attach images to:",
            headlineText = CmdRegistry.ADDIMAGE.withSlash(),
            shouldPromptForInterruptCommands = true,
        ).let {
            if (it is SelectedEntity) {
                it
            } else {
                null
            }
        }


        result?.let { selectedProduct ->
            val now = EpochNano()
            sendOutput("Please send image(s) (will replace any existing product images). Send `q` to cancel upload.")
            fetchAttachmentsOrCancel(now)?.let { attachments ->

                if (attachments.isEmpty()) {
                    sendOutput("Nothing was changed.")
                    return
                }

                var confirmText = PressAgent.attachYesNoQuestion(attachments.count())

                UserInput.promptYesNo(
                    confirmText, sessionContext, noText = "Product image(s) unchanged"
                )?.let {
                    if (it) {
                        //todo fix this up to prevent ugly expiries
                        with(selectedProduct.entity as ShopProduct) {
                            transaction { attachmentLinks }.forEach { attachmentLink ->
                                attachmentLink.expire()
                            }
                            //could this be problematic for reusing resources?
                            // ^ YES
                            //todo change this expiry functionality
                            transaction { linkedAttachments }.forEach { attachment -> attachment.expire() }

                            attachments.forEach { attachment ->
                                attachment.createLink(
                                    AttachmentType.PRODUCT_IMAGE, parentEntity = this
                                )
                            }
                            sendOutput("Updated product image(s) successfully.")
                        }
                    }

                    // add header to this saying "Your inventory:"
                    //todo make call to node for this
                    startNewCommand(CmdRegistry.LISTPRODUCTS)

                    return
                }
            }
        }
    }


}