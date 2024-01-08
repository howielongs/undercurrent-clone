package com.undercurrent.prompting.views.menubuilding

import com.undercurrent.prompting.views.menubuilding.formatting.MenuPrefixFormatter
import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface BuildablePrompt<T, R> {
    fun build()
}

interface SelectableFromPrompt<T, R> {
    fun selectOption(handle: T): SelectableOption<T, R>?
    fun selectFooterIfHandleCorresponds(handle: T): String?
}

abstract class SelectPrompt<T, R>(
    val header: String,
    val footer: String? = null,
    val options: List<R>,
    private val prefixFormatter: MenuPrefixFormatter<T> = MenuPrefixFormatter.WrappedWithBrackets(),
) : BuildablePrompt<T, R>, SelectableFromPrompt<T, R> {

    private var isBuilt: Boolean = false
    private var outString: String? = null
    private var optionsMap: MutableMap<T, SelectableOption<T, R>>? = null

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun build() {
        if (isBuilt) return

        var handle: T
        var lineString: String

        val map: MutableMap<T, SelectableOption<T, R>> = mutableMapOf()
        outString = buildString {
            append("$header\n")
            options.forEachIndexed { index, item ->
                handle = transformOrdinal(index + 1)
                lineString = "${formatPrefix(handle)}$item"

                map[handle] = SelectableOption(
                    handle = handle, outString = lineString, item = item
                )

                append("$lineString\n")
            }

            if (footer != null) {
                handle = transformOrdinal(options.size + 1)
                lineString = "${formatPrefix(handle)}$footer"

                append("$lineString\n")
            }
        }
        optionsMap = map
        isBuilt = true
    }

    override fun selectOption(handle: T): SelectableOption<T, R>? {
        return optionsMap?.get(cleanUpHandler(handle))?.let {
            prettyPrintSelectedOption(it)
            it
        }
    }

    override fun selectFooterIfHandleCorresponds(handle: T): String? {
        return (transformHandleToIndex(handle) == options.size + 1).let {
            if (it) footer else null
        }
    }

    private fun prettyPrintSelectedOption(option: SelectableOption<T, R>) {
        prettyPrintSelectedOption(option.outString)
    }

    private fun prettyPrintSelectedOption(optionString: String) {
        val selectionString = """
            |Selected: 
            |$optionString
            |
        """.trimMargin()
        logger.info("Selected option printed $selectionString")
    }

    override fun toString(): String {
        if (!isBuilt) {
            build()
        }
        logger.debug("Out string printed $outString")
        return outString ?: ""
    }

    private fun formatPrefix(handle: T): String {
        return prefixFormatter.format(handle)
    }

    abstract fun transformOrdinal(i: Int): T

    abstract fun transformHandleToIndex(handle: T): Int

    open fun cleanUpHandler(handle: T): T {
        return handle
    }

}