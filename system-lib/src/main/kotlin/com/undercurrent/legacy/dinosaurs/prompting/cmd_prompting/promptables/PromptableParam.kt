package com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.promptables

import com.undercurrent.shared.utils.Log
import com.undercurrent.system.context.SessionContext
import com.undercurrent.legacy.types.enums.ResponseType
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput

//todo output formatting may be needed (such as pre/post-pending with currency symbol)
open class PromptableParam(
    var value: String? = null,
    var field: String,
    val displayName: String? = null,
    var prompt: String? = null,
    var validationType: ResponseType = ResponseType.STRING,
    var sessionContext: SessionContext? = null,
) {

    suspend fun acquireValue(
        sessionContext: SessionContext,
    ): String? {
        return value ?: promptUser(sessionContext)
    }


    suspend fun promptUser(
        sessionContext: SessionContext,
    ): String? {
        prompt?.let {
            UserInput.promptUser(
                sessionContext = sessionContext,
                promptString = it,
                validationType = validationType,
            )?.let {
                return it
            } ?: run {
                Log.warn("Unable to parse field: $field")
                return null
            }
        }
        return value
    }

}
