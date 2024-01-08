package com.undercurrent.legacy.commands.executables

import com.undercurrent.shared.types.validators.YesNoValidator.isValidYes
import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.system.context.SessionContext
import com.undercurrent.legacyshops.repository.entities.storefronts.StorefrontPrefs
import com.undercurrent.legacy.types.enums.ResponseType
import com.undercurrent.legacy.types.enums.StorefrontPrefType
import com.undercurrent.legacy.types.string.PressAgent

import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput
import org.jetbrains.exposed.sql.transactions.transaction


class SetAutoConfirmCmd(sessionContext: SessionContext) :
    Executable(CmdRegistry.AUTOCONFIRM, sessionContext) {
    override suspend fun execute() {
        //todo check that storefront even exists

        val currentValue = StorefrontPrefs.fetchValue(thisStorefront, StorefrontPrefType.AUTOCONFIRM)
        sessionContext.interrupt("Auto-confirm currently set to ${transaction { currentValue?.value?.uppercase() }}")

        UserInput.promptAndConfirm(
            promptString = "Would you like to set this storefront to auto-confirm " +
                    "all incoming orders?${PressAgent.yesNoOptions()}",
            sessionContext = sessionContext,
            validationType = ResponseType.YESNO,
            confirmTextVerb = "Save",
        )?.let {
            val valueIn = isValidYes(it)

            //todo consider using replace() method
            StorefrontPrefs.expireByKey(thisStorefront, StorefrontPrefType.AUTOCONFIRM)

            StorefrontPrefs.save(
                storefrontIn = thisStorefront,
                keyIn = StorefrontPrefType.AUTOCONFIRM,
                valueIn = valueIn,
                datatypeIn = ResponseType.BOOLEAN
            )

            sessionContext.interrupt("Updated auto-confirm to ${valueIn.toString().uppercase()}")

        }
    }
}