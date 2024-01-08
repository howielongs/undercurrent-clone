package com.undercurrent.swaps.views

import com.undercurrent.prompting.views.menubuilding.SelectYesNoPrompt
import com.undercurrent.prompting.views.menubuilding.SelectableOption
import com.undercurrent.shared.types.validators.Yes
import com.undercurrent.shared.types.validators.YesNoWrapper
import com.undercurrent.testutils.TestAssertUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SelectYesNoPromptTest {

    @BeforeEach
    fun setUp() {
    }
    private fun promptSelectionOfYesNo(): SelectYesNoPrompt {
        val prompt: SelectYesNoPrompt = SelectYesNoPrompt("Select an option:")

        TestAssertUtils().assertContains(
            resultStr = prompt.toString(),
            containsList = listOf(
                "Select an option:",
                "-[Y] YES",
                "-[N] NO",
            )
        )

        val result: SelectableOption<String, YesNoWrapper>? = prompt.selectOption("Y")

        assert(result?.item is Yes)


        return prompt
    }

    @Test
    fun selectYesFromYesNoPrompt() {
        promptSelectionOfYesNo()
    }
}