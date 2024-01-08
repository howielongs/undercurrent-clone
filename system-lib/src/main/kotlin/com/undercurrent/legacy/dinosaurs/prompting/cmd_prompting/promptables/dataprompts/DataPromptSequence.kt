package com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.promptables.dataprompts

import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput
import com.undercurrent.legacy.types.enums.ResponseType
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.shared.utils.Log
import com.undercurrent.system.context.SessionContext

@Deprecated("Use get rid of this")
class DataPromptSequence(
    val sessionContext: SessionContext,
    private val targetTable: ExposedTableWithStatus2,
) {

    private var prompts: MutableMap<String, DataPrompt> = mutableMapOf()

    suspend fun promptedInputsToMap(
        params: List<DataPrompt> = prompts.values.toList(),
        autoConfirm: Boolean? = null,
    ): Map<String, String?>? {

        val shouldSkipConfirm = allParamsProvidedAlready(params)
        promptUserForInputs(params)?.let { responses ->
            if (!shouldSkipConfirm) {
                confirmUserInputs(responses, autoConfirm = autoConfirm)?.let { confirmedResponses ->
                    return confirmedResponses.mapValues { it.value?.value }
                }
            } else {
                return responses.mapValues { it.value?.value }
            }
        }
        return null
    }

    //todo difference is in the map type? Only command that uses other impl is AddVendor (rework that)
    private suspend fun confirmUserInputs(
        responsesMap: HashMap<String, DataPrompt?>,
        confirmTextHeader: String = "You entered:",
        autoConfirm: Boolean? = null,
        thisSessionContext: SessionContext = sessionContext,
    ): HashMap<String, DataPrompt?>? {
        var confirmText = "$confirmTextHeader\n"
        val confirmFooterText = "${PressAgent.correctYesNoQuestion()}"

        responsesMap.forEach { userResponse ->
            userResponse.value?.displayName?.let { displayName ->
                userResponse.value?.let {
                    confirmText += " • ${displayName ?: it.field}: ${it.value}\n"
                }

            }
        }

        confirmText += "\n" + confirmFooterText

        autoConfirm?.let {
            return if (it) {
                responsesMap
            } else {
                null
            }
        }
        UserInput.promptYesNo(
            confirmText, thisSessionContext,
        ).let {
            return if (it) {
                responsesMap
            } else {
                "Nothing changed. Operation completed.".let { msg -> sessionContext.interrupt(msg) }
                null
            }
        }
    }


    /**
     * Only indicator that a user needs to be prompted is if a non-null PromptWrapper
     * has it.value which equals null.
     *
     * This indicates that all params have been hardcoded, so no confirm needed
     */
    private fun allParamsProvidedAlready(params: List<DataPrompt>): Boolean {
        params.forEach {
            if (it != null && it.value == null) {
                return false
            }
        }
        return true
    }


    fun prompt(
        prompt: String,
        validationType: ResponseType = ResponseType.STRING,
        fieldTag: String,
        displayName: String? = null,
        value: String? = null,
    ): DirectDataPrompt {
        with(
            DirectDataPrompt(
                sessionContext = sessionContext,
                targetTable = targetTable,
                prompt = prompt,
                validationType = validationType,
                field = fieldTag,
                value = value,
                displayName = displayName ?: fieldTag.capitalize()
            )
        )
        {
            prompts[field] = this
            return this
        }
    }


    private suspend fun promptUserForInputs(
        params: List<DataPrompt>,
    ): HashMap<String, DataPrompt?>? {
        val responsesMap = HashMap<String, DataPrompt?>()

        params.forEach { param ->
            param?.let { thisPromptWrapper ->

                responsesMap[thisPromptWrapper.field] = thisPromptWrapper

                thisPromptWrapper.value ?: run {
                    thisPromptWrapper.promptUser()?.let { userResponse ->
                        responsesMap[thisPromptWrapper.field]?.value = when (userResponse) {
                            is String -> {
                                userResponse
                            }

                            else -> {
                                null
                            }
                        }
                    } ?: run {
                        Log.error("Unable to complete command: runApiCommand")
                        return null
                    }
                }
            }
        }
        return responsesMap
    }
}

