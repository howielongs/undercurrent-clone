package com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.promptables.dataprompts

import com.undercurrent.shared.types.validators.YesNoValidator
import com.undercurrent.system.context.SessionContext
import com.undercurrent.legacy.types.enums.ResponseType
import com.undercurrent.legacy.types.string.PressAgent.continueYesNoQuestion
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput


 class VerifyInputsPrompt(
     override var value: String? = null,
     sessionContext: SessionContext,
     override val prompt: String,
     override val validationType: ResponseType = ResponseType.YESNO,
     override var displayName: String? = null,
     val yesText: String? = null,
     val noText: String? = null,
     footerVerb: String = continueYesNoQuestion(),
     val preFunc: (() -> Boolean)? = null,

     val yesFunc: (() -> Boolean)? = null,
     private val noFunc: (() -> Boolean)? = null,
     private val finallyFunc: (() -> Boolean)? = null,
) : DataPrompt(value = value,
        sessionContext = sessionContext,
        prompt = prompt,
        validationType = validationType,
        displayName = displayName ?: footerVerb,
        field = "confirm") {

    override suspend fun promptUser(): String? {
        preFunc?.let {
            if (!it()) {
                return null
            }
        }

        UserInput.promptUser(
                prompt,
                sessionContext,
                validationType = ResponseType.YESNO,
        )?.let { response ->
            if (YesNoValidator.isValidYes(response)) {
                yesFunc?.let { it() }
                yesText?.let { sessionContext.interrupt(it) }
                finallyFunc?.let { it() }
                return true.toString()
            }
            noFunc?.let {
                it()
            }
            noText?.let { sessionContext.interrupt(it) }
        }
        //todo for confirm, perhaps allow finally to do a return value like 'boolean?'
        finallyFunc?.let { it() }
        return null
    }
}
