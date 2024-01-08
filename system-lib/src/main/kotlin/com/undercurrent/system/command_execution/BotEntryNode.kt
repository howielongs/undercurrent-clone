package com.undercurrent.system.command_execution

import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.commands.registry.TopCommand
import com.undercurrent.legacy.commands.registry.UserCommand
import com.undercurrent.legacy.data_transfer_objects.CommandWrapper
import com.undercurrent.legacy.data_transfer_objects.StringToCmdParser
import com.undercurrent.legacy.repository.entities.system.CanDisplayWelcome
import com.undercurrent.legacy.repository.entities.system.IntroEvents
import com.undercurrent.legacy.service.DefaultPermissionsChecker
import com.undercurrent.legacy.service.EmptyMessageChecker
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.legacy.utils.CustomerMembershipToString
import com.undercurrent.legacyshops.nodes.customer_nodes.LinkCustomerToStorefrontCmdNodes
import com.undercurrent.prompting.nodes.interactive_nodes.OperationNode
import com.undercurrent.shared.abstractions.PermissionsChecker
import com.undercurrent.shared.formatters.UserToIdString
import com.undercurrent.shared.messages.CanSendToUserByRole
import com.undercurrent.shared.messages.InterrupterMessageEntity
import com.undercurrent.shared.messages.UserInputProvider
import com.undercurrent.shared.messages.UserOutputProvider
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.tx
import com.undercurrent.shared.view.treenodes.OutputNode
import com.undercurrent.shared.view.treenodes.TreeNode
import com.undercurrent.system.context.SystemContext
import com.undercurrent.system.messaging.outbound.notifyAdmins
import kotlin.reflect.KSuspendFunction1

//todo should be able to pass in all valid commands

class CoreNodes(
    val body: String,
    context: SystemContext,
    inputProvider: UserInputProvider? = null,
    outputProvider: UserOutputProvider<InterrupterMessageEntity>? = null,
) : OperationNode<SystemContext>(
    context = context,
    inputProvider = inputProvider ?: context.inputter,
    nodeInterrupter = outputProvider ?: context.interrupter,
), CanSendToUserByRole {
    override suspend fun next(): TreeNode? {
        return InitialPermissionsCheckNode()
    }

    private val interrupter by lazy {
        nodeInterrupter
    }

    override fun sendOutputByRole(msgBody: String, role: AppRole) {
        interrupter.sendOutputByRole(msgBody, role)
    }


    inner class InitialPermissionsCheckNode(
        private val permissionChecker: PermissionsChecker = DefaultPermissionsChecker(
            user = context.user,
            thisRole = context.role,
            notVendorInterruptFunc = {
                sendOutputByRole(msgBody = it, role = ShopRole.VENDOR)
            }
        )
    ) : OutputNode(interactorsStruct = interactors), PermissionsChecker, EmptyMessageChecker {

        override suspend fun next(): TreeNode? {
            logger.debug("Entering BotEntryNode: User #${tx { context.user.id.value }}")
            if (!hasValidPermissions() || isEmptyMessage(body)) {
                logger.debug("BotEntryNode returning null after permission check and empty msg check")
                return null
            }
            return DisplayWelcomeNode()
        }

        override suspend fun hasValidPermissions(): Boolean {
            logger.debug("Entering BotEntryNode.hasValidPermissions (User #${tx { context.user.id.value }})")
            return permissionChecker.hasValidPermissions()
        }

        override fun isEmptyMessage(msgBody: String): Boolean {
            return if (msgBody == "") {
                val contextStr = "${UserToIdString.toIdStr(context.user)} ${context.role})"
                logger.warn(
                    "Incoming message has empty body: " +
                            "$contextStr \n\t " +
                            "InputHandler:handleNoConversation" +
                            "" +
                            " (User #${context.userId})"
                )
                true
            } else {
                false
            }
        }
    }

    inner class DisplayWelcomeNode(
        private val welcomeDisplayer: KSuspendFunction1<SystemContext, Unit> = IntroEvents.Table::displayWelcomeIfUnseen,
    ) : OutputNode(interactorsStruct = interactors), CanDisplayWelcome {

        override suspend fun displayWelcomeIfUnseen(contextIn: SystemContext) {
            // ensure not necessarily showing welcome for every message coming through
            if (contextIn.role != ShopRole.ADMIN) {
                welcomeDisplayer(contextIn)
            }
        }

        override suspend fun next(): TreeNode? {
            displayWelcomeIfUnseen(context)
            return CommandTypeDeciderNode()
        }
    }

    inner class CommandTypeDeciderNode : OutputNode(interactors), StringToCmdParser {

        override fun parseStringToCmd(data: String, sessionContext: SystemContext): CommandWrapper? {
            return CommandWrapper.parseStr(data, sessionContext)
        }

        override suspend fun next(): TreeNode? {
            logger.debug("Entering CommandTypeDeciderNode (User #${tx { context.user.id.value }})")
            return parseStringToCmd(body, context)?.let {
                when (it.commandRef) {
                    is UserCommand -> UserCommandHandler(
                        context,
                        it.commandRef
                    )

                    is TopCommand -> TopCommandHandlerNode(
                        context,
                        it.commandRef
                    )
                }
            } ?: nonCommandHandlerNode()
        }
    }

    suspend fun nonCommandHandlerNode(): TreeNode? {
        return if (context.role == ShopRole.CUSTOMER) {
            LinkCustomerToStorefrontCmdNodes(context, body).next()
        } else {
            unknownInputHandler()
        }
    }

    fun unknownInputHandler(): TreeNode? {
        return yesNoNode(
            PressAgent.iDontUnderstand(body.trim()), "Send to admins for tech support?",
            ifYes = {
                ("Message from ${CustomerMembershipToString(context).generateString()}:\n\n" +
                        "`${body.trim()}`").let {
                    notifyAdmins(msg = it, routingProps = context.routingProps)
                }

                """|Message sent to admins.
                   |
                   |You can use ${CmdRegistry.FEEDBACK.withSlash()} in the future to send a help message to the admins
                   """.trimMargin().let {
                    sendOutput(it)
                }
                null
            },
            ifNo = {
                sendOutput("Not sending to admins")
                null
            },
            retries = 2,
        )
    }
}