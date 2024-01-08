package com.undercurrent.shared.types.strings

import com.undercurrent.shared.utils.systemLocale
import java.util.*

abstract class TextLookup(
    private val filename: String,
    private val key: String,
    private val thisLocale: Locale = systemLocale,
    private val bundle: ResourceBundle,
) {
    constructor(bundle: ResourceBundle, key: String, thisLocale: Locale = systemLocale) : this(
        bundle = bundle,
        filename = bundle.baseBundleName,
        key = key,
        thisLocale = thisLocale
    )

    constructor(filename: String, key: String, thisLocale: Locale = systemLocale) : this(
        bundle = ResourceBundle.getBundle(filename, thisLocale),
        filename = filename,
        key = key,
        thisLocale = thisLocale
    )

    open operator fun invoke(vararg args: Any): String {
        with(bundle.getString(key)) {
            return String.format(this, *args)
        }
    }
}


