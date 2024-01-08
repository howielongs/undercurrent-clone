package com.undercurrent.shared.types.strings

class CleanString(private val origStr: String) {
    private var cleanedStr: String = origStr

    fun clean(
        shouldTrim: Boolean = true,
        shouldReplaceSlash: Boolean = true,
        shouldReplaceWhitespace: Boolean = true,
        isUppercase: Boolean = true,
    ): String {
        var outStr = origStr
        if (shouldTrim) {
            outStr = outStr.trim()
        }

        if (shouldReplaceSlash) {
            outStr = outStr.replace("/", "")
        }

        if (shouldReplaceWhitespace) {
            outStr = outStr.replace(" ", "")
        }

        if (isUppercase) {
            outStr = outStr.uppercase()
        }
        cleanedStr = outStr
        return outStr
    }
}