package com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.promptables.dataprompts

import com.undercurrent.system.context.SessionContext
import com.undercurrent.legacy.types.enums.ResponseType
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput

abstract class DataPrompt(
    open var value: String? = null,
    open var field: String,
    open val sessionContext: SessionContext,
    open val prompt: String,
    open val validationType: ResponseType = ResponseType.STRING,
    open var displayName: String? = null,
) {

    open suspend fun promptUser(): String? {
        return value ?: UserInput.promptUser(
                sessionContext = sessionContext,
                promptString = prompt,
                validationType = validationType,
        )
    }

}