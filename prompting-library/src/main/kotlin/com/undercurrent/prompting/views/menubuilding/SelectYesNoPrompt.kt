package com.undercurrent.prompting.views.menubuilding

import com.undercurrent.shared.types.validators.No
import com.undercurrent.shared.types.validators.Yes
import com.undercurrent.shared.types.validators.YesNoWrapper
import com.undercurrent.shared.utils.OrdinalToYesNoTransformer


open class SelectYesNoPrompt(
    header: String,
) : SelectPrompt<String, YesNoWrapper>(
    header = header,
    options = listOf(Yes(), No())
) {
    override fun transformOrdinal(i: Int): String {
        return OrdinalToYesNoTransformer().transform(i)
    }

    override fun transformHandleToIndex(handle: String): Int {
        TODO("Not yet implemented") // And most likely never will be needed
    }
}

