package com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting

import com.undercurrent.legacy.commands.executables.abstractcmds.SystemCommandStarter
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.data_transfer_objects.CommandWrapper
import com.undercurrent.legacy.dinosaurs.prompting.InputValidator
import com.undercurrent.legacy.dinosaurs.prompting.selectables.*
import com.undercurrent.legacy.repository.schema.toIdRoleStr
import com.undercurrent.legacy.types.enums.ListIndexTypeOld
import com.undercurrent.legacy.types.enums.ResponseType
import com.undercurrent.prompting.components.EmojiSymbol
import com.undercurrent.shared.types.validators.YesNoValidator.isValidYes
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.newScope
import com.undercurrent.shared.utils.time.EpochNano
import com.undercurrent.system.context.SessionContext
import com.undercurrent.system.context.SystemContext
import com.undercurrent.system.messaging.inbound.InboundMessageFetcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async

@Deprecated("Get rid of this, ultimately")
object UserInput {

    //todo 4 usages only
    @Deprecated("Make use of InputProviders")
    suspend fun selectAndRunCallback(
        sessionContext: SystemContext,
        options: List<SelectableOptionImpl>,
        headerText: String = "Select the letter of a command to run:",
        footerText: String = "",
        headlineText: String? = null,
        appendCancel: Boolean = true,
        cancelPrompt: String = "Cancel",
    ) {
        val selection: SelectedListOption? = selectAnOption(
            sessionContext = sessionContext,
            headerText = headerText,
            footerText = footerText,
            options = options,
            headlineText = headlineText,
            appendCancel = appendCancel,
            cancelPrompt = cancelPrompt,
        )

        when (selection) {
            is SelectedCommand -> {
                selection.command.commandRef.callback?.let {
                    it(sessionContext)
                    return
                }

                SystemCommandStarter(sessionContext).startNewCommand(selection.command)
            }

            is SelectedCallback -> {
                selection.callback?.let {
                    it(sessionContext)
                    return
                }
            }

            else -> {
                Log.debug("User has started a new operation ${toIdRoleStr(sessionContext)}")
            }
        }
    }


    /*
 *  null returns when operation is canceled by an interruption
 */
    @Deprecated("Make use of InputProviders")
    suspend fun selectAnOption(
        sessionContext: SystemContext,
        options: List<SelectableOptionImpl>,
        indexType: ListIndexTypeOld = ListIndexTypeOld.ABC,
        headerText: String = "",
        footerText: String = "",
        headlineText: String? = null,
        shouldPromptForInterruptCommands: Boolean = false,
        appendCancel: Boolean = true,
        cancelPrompt: String = "Cancel",
    ): SelectedListOption? {
        //todo should perhaps have rules around "promptForInterruption" bool check
        //either by type of options or by command?

        //todo this can probably be done more professionally
        var finalChoices = options.toMutableList()

        if (appendCancel && !CommandContainsChecker().optionsContainsCancel(options)) {
            //todo add default promptText for Commands
            finalChoices.add(
                SelectableCommand(
                    commandEnum = CmdRegistry.CANCEL,
                    promptText = cancelPrompt
                )
            )
        } else {
            finalChoices
        }

        val optionSelector = OptionSelector(
            options = finalChoices,
            headerText = headerText,
            footerText = footerText,
            indexType = indexType,
            headlineText = headlineText
        )

        promptUser(
            promptString = optionSelector.promptString,
            sessionContext = sessionContext,
            validationType = ResponseType.INDEX,
            shouldPromptForInterruptCommands = shouldPromptForInterruptCommands,
            validIndices = optionSelector.validSelectables,
            selectionIndexType = optionSelector.indexType
        )?.let {
            return optionSelector.selectables[it.uppercase()]
        }
        return null
    }

    //todo only 2 usages
    // use sealed class returns here for an easier time
    // figure out generic solution with "chooseEntityIdFromList()"
    @Deprecated("Make use of InputProviders")
    suspend fun chooseSkuIdFromList(
        sessionContext: SessionContext,
        nestedSkuSelectionMap: NestedSkuSelectionMap,
        maxAttempts: Int = 1
    ): Int? {
        //todo add hintText for user retrying input (should go with validation type)


        var dereferencedResponse: Int? = null
        var numAttempts = 0

        while (dereferencedResponse == null && numAttempts < maxAttempts) {
            numAttempts++
            val response = promptUser(
                nestedSkuSelectionMap.promptString,
                sessionContext,
                validIndices = nestedSkuSelectionMap.indexedSelectablesMap.map { it.value.displayedIndex },
                validationType = ResponseType.INDEX,
                shouldPromptForInterruptCommands = true,
                selectionIndexType = ListIndexTypeOld.DECIMAL
            )
            if (response != null) {
                dereferencedResponse = InputValidator.dereferenceSelectedItem(
                    response,
                    nestedSkuSelectionMap.indexedSelectablesMap
                )
                if (dereferencedResponse != null) {
                    return dereferencedResponse
                }
            }
        }
        return null
    }

    // allow option to Edit (when doing multi-select YES/NO)
    // perhaps add function params to take place on yes?
    // add YesNo automatically
    @Deprecated("Make use of InputProviders")
    suspend fun promptYesNo(
        confirmText: String,
        sessionContext: SystemContext,
        yesText: String? = null,
        noText: String? = null,
        shouldPromptForInterruptCommands: Boolean = true,
        emojiStatus: EmojiSymbol = EmojiSymbol.SUCCESS,
        disableEmoji: Boolean = true,
    ): Boolean {
        promptUser(
            confirmText,
            sessionContext,
            shouldPromptForInterruptCommands = shouldPromptForInterruptCommands,
            validationType = ResponseType.YESNO,
        )?.let { response ->
            var prefix = if (disableEmoji) {
                ""
            } else {
                emojiStatus.prefix()
            }
            return if (isValidYes(response)) {
                yesText?.let { sessionContext.interrupt("$prefix$it") }
                true
            } else {
                noText?.let { sessionContext.interrupt("$prefix$it") }
                false
            }
        }
        return false
    }

    //todo only 2 usages (in AddWalletCmd)
    @Deprecated("Make use of InputProviders")
    suspend fun promptAndConfirm(
        sessionContext: SystemContext,
        inputField: InputField,
        confirmTextHeader: String = "You entered:",
        confirmTextVerb: String = "Continue",
        confirmTextFooter: String = "$confirmTextVerb?\n  Y. Yes\n  N. No",
        yesText: String? = null,
        noText: String? = null,
    ): String? {
        val result = with(inputField) {
            promptUser(
                prompt,
                sessionContext,
                validationType = type,
                shouldPromptForInterruptCommands = true,
            )
        }

        result?.let { msg ->
            promptYesNo(
                "$confirmTextHeader\n\n`$msg`\n\n$confirmTextFooter",
                sessionContext,
                yesText,
                noText
            )?.let {
                if (it) {
                    return msg
                }
            }
        }
        return null
    }


    /**
     * Will want to overload this to include more complex prompting
     */
    @Deprecated("Make use of InputProviders")
    suspend fun promptAndConfirm(
        promptString: String,
        sessionContext: SystemContext,
        validationType: ResponseType = ResponseType.STRING,
        maxAttempts: Int = 5,
        shouldPromptForInterruptCommands: Boolean = true,
        validIndices: List<String> = emptyList(),
        selectionIndexType: ListIndexTypeOld? = null,
        confirmTextHeader: String = "You entered:",
        confirmTextVerb: String = "Continue",
        confirmTextFooter: String = "$confirmTextVerb?\n  Y. Yes\n  N. No",
        yesText: String? = null,
        noText: String? = null,
        disableEmoji: Boolean = true,
    ): String? {
        promptUser(
            promptString = promptString,
            sessionContext,
            validationType,
            maxAttempts,
            shouldPromptForInterruptCommands,
            validIndices,
            selectionIndexType
        )?.let { msg ->
            promptYesNo(
                "$confirmTextHeader\n\n`$msg`\n\n$confirmTextFooter",
                sessionContext,
                yesText,
                noText,
                disableEmoji = disableEmoji
            )?.let {
                if (it) {
                    return msg
                }
            }
        }
        return null
    }

    //todo should have sealed class for response types (such as YesNo)
    //todo probably separate selectionIndex code from this to simplify and focus functionality
    @Deprecated("Use SystemInputProvider instead")
    suspend fun promptUser(
        promptString: String,
        sessionContext: SystemContext,
        validationType: ResponseType = ResponseType.STRING,
        maxAttempts: Int = 5,
        shouldPromptForInterruptCommands: Boolean = true,
        validIndices: List<String> = emptyList(),
        selectionIndexType: ListIndexTypeOld? = null,
        minNum: Int? = null,
        maxNum: Int? = null,
    ): String? {
        val scope = newScope("promptUser #${sessionContext.userId}")
        var epochNano = EpochNano()

        if (promptString != "") {
            sessionContext.interrupt(promptString)
        }

        val inputProvider = InboundMessageFetcher(sessionContext.user, sessionContext.routingProps)

        var validatedResult: String? = null
        var numAttempts = 0

        /**
         * Potential types:
         *  -> Next Command
         *  -> Data input
         *  -> Invalid response
         */
        while (validatedResult == null && numAttempts < maxAttempts) {
            numAttempts++
            // consider using repeat() + timeout in here

            val job = scope.async(start = CoroutineStart.LAZY) {
                Log.debug("Attempt #${numAttempts}: Waiting for user to respond to $promptString")
                Log.debug(
                    "Starting job async. Expecting valid indices: ${
                        validIndices.joinToString(
                            ", "
                        )
                    }"
                )
                inputProvider.getRawInput(epochNano)
            }

            job.await()?.let { inputData ->
                CommandWrapper.parseStr(inputData, sessionContext)?.let { cmd ->
                    // should also prompt to "accept input as is" (not as command)
                    if (ShouldRunNewCommandDecider(sessionContext).shouldRunNewCommand(
                            inputData,
                            shouldPromptForInterruptCommands
                        )
                    ) {
                        SystemCommandStarter(sessionContext).startNewCommand(cmd.commandRef)
                        return null
                    } else {
                        numAttempts = 0
                        if (promptString != "") {
                            epochNano = EpochNano()
                            sessionContext.interrupt(promptString)
                        }
                    }
                } ?: run {
                    InputValidator.validateInput(
                        sessionContext = sessionContext,
                        inputData = inputData,
                        validationType = validationType,
                        validIndices = validIndices,
                        selectionIndexType = selectionIndexType,
                        minNum = minNum,
                        maxNum = maxNum,
                    )?.let {
                        return it
                    } ?: run {
                        numAttempts = 0
                        if (promptString != "") {
                            epochNano = EpochNano()
                            sessionContext.interrupt(promptString)
                        }
                    }
                }
            }

            if (numAttempts >= maxAttempts) {
                sessionContext.interrupt("Operation timed out. Have a nice day!")
            }

        }
        //todo add message for "ran out of attempts" and then run /cancel
        return null
    }

}