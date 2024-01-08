package com.undercurrent.prompting.nodes.menubuilding

import com.undercurrent.shared.utils.IntToAbcTransformer
import com.undercurrent.shared.utils.Transformable
import com.undercurrent.shared.utils.asAbcHandle


/**
 * Sample menu:
 *
 * --------------------------------------------------------
 * Welcome! What would you like to do?
 *
 * – [a] View statistics
 * – [b] Withdraw liquidity
 * – [c] Deposit liquidity
 * – [d] Add crypto wallet
 * – [e] Remove crypto wallet
 * – [f] Cancel
 * --------------------------------------------------------
 */

/********************************************************
 * Start: Building a single menu line item
 *
 *
 * --------------------------------------------------------
 *  – [a] View statistics
 * --------------------------------------------------------
 *
 ********************************************************/

interface CanBuildMenuLine<I, T> {
    fun buildLine(index: I, line: T): String
}

inline class MenuHandle(val value: String)
inline class MenuLineBody(val value: String)

/**
 * @param I - the type of the index character
 * @param T - the type of the content
 * @param transformHandle - transform the index character
 * @param transformContent - transform the content
 * @param wrapHandle - wrap the index character
 * @param formatLine - format the line
 */
open class MenuLineBuilder<I, T>(
    private val transformHandle: (I) -> MenuHandle = { MenuHandle(it.toString()) },
    private val transformContent: (T) -> MenuLineBody = { MenuLineBody(it.toString()) },
    private val wrapHandle: (MenuHandle) -> String = { "- [${it.value}]" },
    private val formatLine: (MenuHandle, MenuLineBody) -> String = { handle, content ->
        " ${wrapHandle(handle)} ${content.value}"
    }
) : CanBuildMenuLine<I, T> {
    override fun buildLine(index: I, line: T): String {
        return formatLine(transformHandle(index), transformContent(line))
    }
}


/********************************************************
 * Next: Building a list of menu items from the line builder
 *  (no header) -->
 *
 * --------------------------------------------------------
 * – [a] View statistics
 * – [b] Withdraw liquidity
 * – [c] Deposit liquidity
 * – [d] Add crypto wallet
 * – [e] Remove crypto wallet
 * – [f] Cancel
 * --------------------------------------------------------
 ********************************************************/


interface CanBuildMenuFromMap<I, T> {
    fun buildMenu(choices: LinkedHashMap<I, T>): String
}

abstract class BaseMenuBuilder<I, T>(
    private val lineBuilder: MenuLineBuilder<I, T> = MenuLineBuilder()
) : CanBuildMenuFromMap<I, T> {

    var handleToOptionBodyMap: LinkedHashMap<I, T> = linkedMapOf()

    private fun buildLine(index: I, line: T): String {
        return lineBuilder.buildLine(index, line)
    }

    override fun buildMenu(choices: LinkedHashMap<I, T>): String {
        handleToOptionBodyMap = choices
        val sb = StringBuilder()
        choices.forEach { (index, line) ->
            sb.append(buildLine(index, line) + "\n")
        }
        return sb.toString()
    }
}

/********************************************************
 * Next: Building a menu from a list of menu items (autogen indices)
 ********************************************************/

interface CanBuildMenuFromList<I, T> {
    fun buildMenu(choices: List<T>): String
}

inline class CleanHandle(val value: String)

class MenuBuilder<T>(
    private val indexTypeConverter: Transformable<Int, String> = IntToAbcTransformer(),
    private val cleanUpHandle: (String) -> CleanHandle = { CleanHandle(it.asAbcHandle()) },
    private val choicesListToMap: ((List<T>) -> LinkedHashMap<String, T>)? = null,
) : BaseMenuBuilder<String, T>(), CanBuildMenuFromList<String, T> {

    override fun buildMenu(choicesList: List<T>): String {
        return buildMenu(convertListToMap(choicesList))
    }

    private fun convertListToMap(choices: List<T>): LinkedHashMap<String, T> {
        choicesListToMap?.let {
            return it(choices)
        }

        val thisHandleToChoiceBodyMap: LinkedHashMap<String, T> = linkedMapOf()
        choices.forEachIndexed { handle, t ->
            indexTypeConverter.transform(handle + 1).let {
                thisHandleToChoiceBodyMap[cleanUpHandle(it).value] = t
            }
        }
        return thisHandleToChoiceBodyMap
    }
}



