package com.undercurrent.legacy.commands.executables

import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectableEntity
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectedEntity
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCode
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCodeBurstEvent
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefronts
import com.undercurrent.legacy.service.csvutils.csv_handlers.JoinCodesCsvHandler
import com.undercurrent.legacy.types.enums.JoinCodeType
import com.undercurrent.legacy.types.enums.ResponseType
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.legacy.utils.joincodes.RandomAbcStringGenerator
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.context.DbusProps
import com.undercurrent.system.context.SessionContext

const val MAX_JOINCODES_PER_STOREFRONT = 1000


/**
 * Generate a bunch of join codes for a storefront
 *
 * 1. Select a storefront from the list
 * 2. Enter number of codes to generate
 * 3. Create JoinCodeBurstEvent
 * 4. Generate codes (link to JoinCodeBurstEvent)
 */
class GenerateManyJoinCodes(sessionContext: SessionContext) :
    Executable(CmdRegistry.GEN_MANY_CODES, sessionContext) {

    override suspend fun execute() {
        val storefronts = Storefronts.unexpired()

        //todo need to pull mystery out of this (such as what method is used for toString)
        //todo just pass in corresponding map of strings (for selectable line items)
        UserInput.selectAnOption(
            sessionContext = sessionContext,
            options = storefronts.map { SelectableEntity(it) },
            headerText = "Select storefront to generate codes for:",
            headlineText = thisCommand.lower(),
        )?.let { selectedListOption ->
            if (selectedListOption is SelectedEntity && selectedListOption.entity is Storefront) {
                val thisStorefront = selectedListOption.entity
                val thisId = tx { thisStorefront.id }


                Log.debug(
                    "User selected " +
                            "Storefront #${thisId} to generate codes for."
                )

                //todo fetch existing codes for storefront
                val codes: List<JoinCode> = tx { thisStorefront.joinCodes }
                val remaining = MAX_JOINCODES_PER_STOREFRONT - codes.count()

                if (remaining <= 0) {
                    sessionContext.interrupt(
                        "You have reached the maximum number of join codes " +
                                "for this storefront (${codes.count()})."
                    )
                    return
                }

                val numCodesStr = UserInput.promptUser(
                    "Enter number of codes to generate (available left this storefront: ${remaining}):",
                    sessionContext,
                    validationType = ResponseType.POSITIVE_INT,
                    minNum = 1,
                    maxNum = remaining,
                ) ?: run {
                    return
                }

                val quantity: Int = try {
                    numCodesStr.toInt()
                } catch (e: NumberFormatException) {
                    Log.error("Could not parse number of codes to generate: $numCodesStr")
                    return
                }


                val confirmString = tx {
                    """
                    |Shop code generation confirmation:
                    | • Storefront: ${tx { thisStorefront.vendor.toString() }}
                    | • Quantity: $quantity
                    |
                    |${PressAgent.generateCodesQuestion()}
                    """.trimMargin()
                }

                if (UserInput.promptYesNo(
                        confirmString,
                        sessionContext,
                        noText = "Codes not generated."
                    )
                ) {
                    JoinCodeBurstEvent.Entity.save(
                        storefrontIn = thisStorefront,
                        initiatorUserIn = sessionContext.user,
                        quantityIn = quantity,
                    )?.let {
                        val newJoinCodes = generateCodes(it, quantity, thisStorefront, sessionContext)

                        //should also allow to download existing? Stats on usage, date created, etc.
                        sessionContext.interrupt(
                            "All $quantity codes generated. We will provide a CSV " +
                                    "file to download all codes you just generated."
                        )

                        thisStorefront.notify(
                            "Your admin has generated $quantity fresh join codes for you." +
                                    "\n\nUse `REFER` to generate new codes, " +
                                    "or `MYCODES` to see your existing codes."
                        )

                        val storefrontUser = tx { thisStorefront.vendor.user }

//                        sessionContext.newContext(Rloe.VENDOR, storefrontUser)

                        val newDbusProps = DbusProps(roleIn = ShopRole.VENDOR, envIn = sessionContext.environment)
//                        val storefrontUserContext = sessionContext.newContext(Rloe.VENDOR, storefrontUser)

                        //todo probably want to make use of CSV created and not generate twice
                        JoinCodesCsvHandler(newJoinCodes, sessionContext).send(
                            user = storefrontUser,
                            dbusProps = newDbusProps
                        )
                        JoinCodesCsvHandler(newJoinCodes, sessionContext).send(
                            user = sessionContext.user,
                            dbusProps = sessionContext.routingProps
                        )

                    }
                    return
                }

            }
        }

    }

    private fun generateCodes(
        joinCodeBurstEvent: JoinCodeBurstEvent.Entity,
        numCodes: Int,
        thisStorefront: Storefront,
        sessionContext: SessionContext
    ): MutableList<JoinCode> {
        val thisUser = sessionContext.user
        val storefrontId = tx { thisStorefront.id.value }

        val newJoinCodes = mutableListOf<JoinCode>()
        for (i in 1..numCodes) {

            val code = RandomAbcStringGenerator().generate()
            JoinCode.save(
                codeStrIn = code,
                ownerUserIn = thisUser,
                entityIdIn = storefrontId,
                entityTypeIn = JoinCodeType.STOREFRONT,
                burstEventIn = joinCodeBurstEvent,
                tagIn = "bulk create $numCodes codes"
            )?.let { newJoinCode ->
                newJoinCodes.add(newJoinCode)
            }
        }
        return newJoinCodes
    }
}