package com.undercurrent.system.command_execution

import com.undercurrent.legacy.commands.registry.BaseCommand
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.commands.registry.CmdRegistry.CANCEL
import com.undercurrent.legacy.commands.registry.TopCommand
import com.undercurrent.legacy.data_transfer_objects.CommandWrapper
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectableCommand
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.cleanInboundCommand
import com.undercurrent.shared.utils.tx
import com.undercurrent.shared.view.treenodes.TreeNode
import com.undercurrent.legacyshops.nodes.vendor_nodes.VendorShareNode
import com.undercurrent.system.context.SystemContext

class TopCommandHandlerNode(
    context: SystemContext,
    cmd: TopCommand
) : CommandHandlerBase<TopCommand>(context, cmd) {

    private fun removeDuplicateHandles(validCommands: MutableList<CommandWrapper>): MutableList<CommandWrapper> {
        validCommands.removeIf {
            it.handle.uppercase().cleanInboundCommand() == cmd.lower().uppercase().cleanInboundCommand()
        }
        return validCommands
    }

    private fun addCancelToCmds(validCommands: MutableList<CommandWrapper>): MutableList<CommandWrapper> {
        if (validCommands.none { it.handle == CANCEL.lower() }) {
            validCommands.add(CommandWrapper(CANCEL.lower(), CANCEL.hint, priority = CANCEL.priority))
        }
        return validCommands
    }

    private fun fetchCmdsToHide(): List<BaseCommand> {
        //todo SMELLY
        val shopCount = tx { context.user.activeCustomerProfiles.count() }

        when (shopCount) {
            0 -> {
                sendOutput(
                    "You are not currently a member of any shops. Send a join code to join a shop.\n\n" +
                            "This code is provided by your vendor outside of this Signal bot."
                )
                return listOf(TopCommand.SHOPS, CmdRegistry.MENU, CmdRegistry.CHECKOUT)
            }

            1 -> {
                return listOf(TopCommand.SHOPS)
            }
        }

        return listOf()
    }


    //todo wrap in coroutines to avoid blocking
    override suspend fun handleCmd(): TreeNode? {
        return when (cmd) {
            TopCommand.SHARE -> {
                VendorShareNode(
                    context = context,
                ).next()
            }

            else -> {
                var cmdsList: MutableList<CommandWrapper> =
                    CommandWrapper.fetchUserSlashCommands(context, cmd).toMutableList()

                when (cmd) {
                    TopCommand.START -> {
                        cmdsList = CommandWrapper.fetchTopLevelCommands(context).toMutableList()
                        cmdsList.addAll(CommandWrapper.fetchUserSlashCommands(context, TopCommand.START))
                        cmdsList.sortBy { it.priority }
                    }

                    TopCommand.ALL -> {
                        cmdsList.sortBy { it.handle }
                    }

                    else -> {
                        cmdsList.sortBy { it.priority }
                    }
                }

                cmdsList = addCancelToCmds(cmdsList)
                cmdsList = removeDuplicateHandles(cmdsList)

                if (context.role == ShopRole.CUSTOMER) {
                    val cmdsToHide = fetchCmdsToHide()

                    cmdsList = cmdsList.filter { !cmdsToHide.contains(it.commandRef) }.toMutableList()
                }


                //todo will need to handle this with Nodes
                UserInput.selectAndRunCallback(context, cmdsList.map { SelectableCommand(it.commandRef) })

                null
            }
        }
    }

}