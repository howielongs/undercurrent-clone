package com.undercurrent.legacy.dinosaurs.prompting

import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectedEntity
import com.undercurrent.legacy.types.string.PressAgent

object TextBox {

    open fun removalVerifyString(selection: SelectedEntity): String {
        return """You selected to remove:
            |
            |${selection.fullLineText}
            |
            |${PressAgent.removeYesNoQuestion()}
        """.trimMargin()
    }

    fun verifyInputsBox(
        headline: String? = null,
        header: String = "You entered",
        footerPrompt: String = PressAgent.correctYesNoQuestion(),
        vararg lineStrings: String
    ): String {
        var midSection = ""
        lineStrings.forEach {
            midSection += " • $it\n"
        }

        var headlineStr = if (headline == null) {
            ""
        } else {
            "[$headline]\n"
        }

        return headlineStr + """
            |$header:
            |$midSection
            |$footerPrompt
        """.trimMargin()

    }
}