package com.undercurrent.legacy.commands.executables.abstractcmds.select_and_update_cmds

import com.undercurrent.legacy.commands.executables.abstractcmds.SelectEntityCmd
import com.undercurrent.legacy.commands.registry.BaseCommand
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.commands.registry.CmdRegistry.CONFIRM
import com.undercurrent.legacy.commands.registry.CmdRegistry.MARKSHIPPED
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectableEntity
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectedEntity
import com.undercurrent.legacy.repository.entities.payments.StripeApiKeys
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrder
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.legacy.routing.RunConfig
import com.undercurrent.legacy.types.enums.StripeKeyType
import com.undercurrent.shared.formatters.UserToIdString
import com.undercurrent.shared.repository.dinosaurs.EntityWithLabels1
import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.utils.Log
import com.undercurrent.system.context.SessionContext
import org.jetbrains.exposed.sql.transactions.transaction

interface CanSelectEntity {
    suspend fun select(): EntityWithLabels1?
}


abstract class SelectAndUpdateCmd(
    override val thisCommand: BaseCommand,
    sessionContext: SessionContext,
) : SelectEntityCmd(thisCommand, sessionContext), CanSelectEntity {
    abstract val operationInfinitiveVerb: String

    override suspend fun select(): EntityWithLabels1? {
        UserInput.selectAnOption(
            sessionContext = sessionContext,
            options = sourceList().map { SelectableEntity(it) },
            headerText = listHeaderText(operationInfinitiveVerb),
            shouldPromptForInterruptCommands = true,
//            headlineText = thisCommand.handle(),
            indexType = listIndexType(),
        )?.let { selection ->
            if (selection is SelectedEntity) {
                Log.debug("${UserToIdString.toIdStr(sessionContext.user)} selected for ${thisCommand.lower()}: ${singularItem()} #${selection.entity.uid}")
                return selection.entity
            }
        }
        return null
    }


    override suspend fun execute() {
        if (!preFunc()) {
            return
        }
        select()?.let {
            when (it) {
                is DeliveryOrder -> {
                    when (thisCommand) {
                        CONFIRM -> {
                            ConfirmCmd(sessionContext).apply {
                                confirmOrDeclineOrder(order = it)?.let {
                                    return
                                }
                            }
                        }

                        MARKSHIPPED -> {
                            MarkShippedCmd(sessionContext).markShipped(it, sessionContext = sessionContext)
                            true
                        }

                        else -> {}
                    }
                }

                is Storefront -> {
                    when (thisCommand) {
                        CmdRegistry.ENABLE_STRIPE -> {
                            val stripeKeyType = if (RunConfig.environment == Environment.PROD) {
                                StripeKeyType.LIVE
                            } else {
                                StripeKeyType.TEST
                            }

                            //todo create entry in linker table for vendors which can use Stripe
                            transaction {
                                StripeApiKeys.Entity.new {
                                    storefront = it
                                    type = stripeKeyType.name
                                }
                            }
                            sessionContext.interrupt("Success enabling Stripe key (${stripeKeyType.name}) for vendor storefront.")
                            it.notify("Stripe payments enabled for your storefront! Use ${CmdRegistry.ADDWALLET.upper()} to add your secret key (${stripeKeyType.name} mode).")
                            return
                        }

                        else -> {}
                    }
                }

                else -> {}
            }
        }
        sessionContext.interrupt(unchangedMsg())
    }
}

