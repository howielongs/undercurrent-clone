package com.undercurrent.prompting.views.menubuilding.formatting

import com.undercurrent.shared.formatters.TextFormatter

sealed interface MenuPrefixFormatter<T> : TextFormatter<T> {
    override fun format(index: T): String

    class SimplePeriod<T> : MenuPrefixFormatter<T> {
        override fun format(index: T): String {
            return "$index. "
        }
    }

    class WrappedWithBrackets<T> : MenuPrefixFormatter<T> {
        override fun format(index: T): String {
            return "-[$index] "
        }
    }
    //todo need to add interfaces for non-ABC (numeric and custom)
}

