package com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.promptables.dataprompts

import com.undercurrent.system.context.SessionContext
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.legacy.types.enums.ResponseType

 class DirectDataPrompt(
     override var value: String? = null,
     sessionContext: SessionContext,
     val targetTable: ExposedTableWithStatus2,
     override val prompt: String,
     override val validationType: ResponseType = ResponseType.STRING,
     override var field: String,
     override var displayName: String? = null,
) : DataPrompt(value = value,
        sessionContext = sessionContext,
        field = field,
        prompt = prompt,
        validationType = validationType,
        displayName = displayName ?: field) {

}
