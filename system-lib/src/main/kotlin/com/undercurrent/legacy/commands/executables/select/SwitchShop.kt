package com.undercurrent.legacy.commands.executables.select

import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.system.context.SessionContext
import com.undercurrent.legacy.dinosaurs.prompting.selectables.*
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomer
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput
import com.undercurrent.legacy.service.StorefrontCustomerLinker
import com.undercurrent.shared.utils.stx

@Deprecated("Clean this up")
class SwitchShop(sessionContext: SessionContext) : Executable(CmdRegistry.SWITCHSHOP, sessionContext) {

    override suspend fun execute() {
        val sessionUser = sessionContext.user


        //todo should migrate this into a better class
        val header = "Which shop would you like to view?"
        val headline = thisCommand.lower()

        val emptyString = "You need to join a shop first via join code"
        val singletString =
            "You are viewing your only shop. Enter another join code to view another shop."

        val storefronts = sessionUser.activeCustomerProfiles.also {
            if (it.isNullOrEmpty()) {
                sessionContext.interrupt(emptyString)
                return
            }
            if (it.size == 1) {
                sessionContext.interrupt(singletString)
                return
            }
        }

        //todo perhaps also put into prompt whether it's the current one or not
        //also should have 'Cancel' in the list of choices
        val options = storefronts.map { SelectableEntity(it, it.switchShopOutLineString()) }

        val finalOptions = ArrayList<SelectableOptionImpl>()
        finalOptions.addAll(options)
        finalOptions.add(SelectableCommand(CmdRegistry.CANCEL, promptText = "Cancel"))

        UserInput.selectAnOption(
            sessionContext, finalOptions, headerText = header, headlineText = headline,
        )?.let { selectedOption ->
            if (selectedOption is SelectedEntity) {
                if (selectedOption.entity is ShopCustomer) {
                    stx {
                        val selectedStorefront = selectedOption.entity.storefront

                        selectedStorefront?.let {
                            StorefrontCustomerLinker(sessionContext, it).linkUserToStorefront(it.joinCode)
                            return@let
                        }
                    }
                }
            } else if (selectedOption is SelectedCommand) {
                selectedOption.command.commandRef.callback?.invoke(sessionContext)
                return
            }
        }
    }
}