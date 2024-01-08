package com.undercurrent.prompting.views.menubuilding

import com.undercurrent.prompting.views.menubuilding.formatting.MenuPrefixFormatter
import com.undercurrent.shared.utils.OrdinalToAbcTransformer

@Deprecated("Too old and complex. Prefer menuSelectNode")
abstract class SelectAbcPrompt<T, R>(
    header: String,
    footer: String? = null,
    options: List<R>,
    prefixFormatter: MenuPrefixFormatter<String> = MenuPrefixFormatter.WrappedWithBrackets(),
) : SelectPrompt<String, R>(
    header = header,
    footer = footer,
    options = options,
    prefixFormatter = prefixFormatter
) {
    override fun transformHandleToIndex(handle: String): Int {
        return OrdinalToAbcTransformer().reverseTransform(handle)
    }

    override fun transformOrdinal(i: Int): String {
        return OrdinalToAbcTransformer().transform(i)
    }

    override fun cleanUpHandler(handle: String): String {
        return handle.toUpperCase()
    }
}