package com.undercurrent.legacy.commands.executables

import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCode
import com.undercurrent.system.messaging.outbound.notifyAdmins
import com.undercurrent.legacy.types.enums.JoinCodeType
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.legacy.utils.joincodes.RandomAbcStringGenerator
import com.undercurrent.shared.utils.Log
import com.undercurrent.system.context.SessionContext
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Give helpful description (tips for using tags)
 * Prompt for a tag
 * Create db table for mapping link codes to repository
 * Db table for tracking usage of various codes
 * Map link codes to their parent
 * Ensure customers can provide links to a storefront
 *
 * Later: allow linking to particular products/items (links to Storefront in nested call)
 *
 * Enable sending directly via SMS?
 *  -> ask for each sms, like for asking for SKU? (future)
 *
 */
@Deprecated("Clean this up")
class ReferCmd(sessionContext: SessionContext) :
    Executable(CmdRegistry.REFER, sessionContext) {
    override suspend fun execute() {
        JoinCode.codesForStorefrontString(thisStorefront)?.let {
            sessionContext.interrupt("Your current join codes:\n${it}")
        }

        //todo consider the need to manually set join codes (have some custom inputs from user)

        UserInput.promptYesNo(
            sessionContext = sessionContext,
            //todo add yesNo to end of confirmText
            confirmText = "Would you like to create a new code for customers to link to your store?${PressAgent.yesNoOptions()}",
        ).let { willCreateNewCode ->
            if (willCreateNewCode) {
                //later come and enable codes for more than just storefronts
                Log.debug("$sessionContext is going to generate a new code for customers to link to shop")

                var newJoinCode = RandomAbcStringGenerator().generate()

                UserInput.promptYesNo(
                    sessionContext = sessionContext,
                    confirmText = "Would you like to auto-generate a new code for customers to link to your store?${PressAgent.yesNoOptions()}",
                ).let {
                    if (!it) {
                        var attemptsLeft = 5

                        while (attemptsLeft > 0) {
                            UserInput.promptUser(
                                promptString = "Enter the join code you'd like to use:",
                                sessionContext = sessionContext,
                            )?.let { customJoinCodeText ->
                                val cleanedCodeText = customJoinCodeText.trim().replace("/", "")
                                    .replace(" ", "").uppercase()

                                //check for existence
                                JoinCode.fetchByCode(cleanedCodeText)?.let {
                                    if (attemptsLeft > 1) {
                                        sessionContext.interrupt("This code already exists. Please try again with a different code.")
                                        attemptsLeft--
                                    } else {
                                        sessionContext.interrupt("This code already exists. Defaulting to auto-generated code.")
                                        attemptsLeft = 0
                                    }
                                } ?: run {
                                    newJoinCode = cleanedCodeText
                                    attemptsLeft = 0
                                }
                            }
                        }
                    }
                }

                val promptStr = """
                    |Your new join code will be: $newJoinCode
                    |
                    |Enter a tag to use to identify when this code is used (e.g. 'Social media marketing'):
                """.trimMargin()

                //todo come back and enhance with things like expiry dates, etc.
                UserInput.promptAndConfirm(
                    promptString = promptStr,
                    sessionContext = sessionContext,
                )?.let { newTag ->
                    //todo ultimately come back and enable this for multiple storefronts

                    JoinCode.save(
                        codeStrIn = newJoinCode.trim().replace("/", "")
                            .replace(" ", "").uppercase(),
                        ownerUserIn = sessionContext.user,
                        entityIdIn = transaction { thisStorefront.uid },
                        tagIn = newTag,
                        entityTypeIn = JoinCodeType.STOREFRONT,
                    )
                    JoinCode.codesForStorefrontString(thisStorefront)?.let {
                        sessionContext.interrupt("Your current join codes:\n${it}")
                    }
                    notifyAdmins("$sessionContext added a new join code")
                    return
                }
            }
        }

        sessionContext.interrupt("Operation complete.")
    }
}