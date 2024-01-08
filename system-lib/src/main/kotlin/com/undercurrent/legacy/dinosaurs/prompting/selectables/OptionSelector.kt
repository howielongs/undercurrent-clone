package com.undercurrent.legacy.dinosaurs.prompting.selectables

import com.undercurrent.legacy.types.enums.ListIndexTypeOld
import com.undercurrent.legacy.types.enums.ListIndexTypeOld.*
import com.undercurrent.legacy.utils.UtilLegacy


/**
 * Contains list string to display to user,
 * as well as hashmap of lettered choices corresponding to their values
 *
 * Replaces SelectableValue database table impl. Should
 * improve performance, as fewer db calls will be needed.
 *
 * In addition, ItemSelectionMap can be used for entire lifecycle of a command.
 */
class OptionSelector(
    val options: List<SelectableOptionImpl>,
    headerText: String = "",
    footerText: String = "",
    var indexType: ListIndexTypeOld = ABC,
    val isSelectable: Boolean = true,
    headlineText: String? = null,
    emptyText: String = "No items to display",
) {

    /**
     * Map: <selectableListIndex, element>
     *     selectableListIndex is what the user will input, and what
     *     we will validate against.
     */
    var selectables = HashMap<String, SelectedListOption>()
    var promptString = ""

    var validSelectables: List<String> = emptyList()
        get() = selectables.map { it.value.selectionHandle.uppercase() }

    //todo should pull out to another method?
    init {
        if (options.isEmpty()) {
            promptString = emptyText
        } else {
            var thisHeader = headerText
            headlineText?.let {
                thisHeader = "[$headlineText]\n\n$thisHeader"
            }

            promptString = "$thisHeader"
            if (thisHeader != "") {
                promptString += "\n"
            }

            var counter: Int
            var currentIndex: String
            options.withIndex().forEach { (index, option) ->
                //todo test what happens if TextOption has non-null promptText and CommandOption is current type?
                val lineString: String? = option.promptText ?: defaultDisplayValues(option)

                lineString?.let { line ->
                    counter = if (indexType == UID && option is SelectableEntity) {
                        //what to do if UID is used but is not Entity? Just defer to using index
                        option.entity.uid
                    } else {
                        index + 1
                    }

                    currentIndex = when (indexType) {
                        ABC -> UtilLegacy.getCharForNumber(counter)
                        INTEGER, UID -> counter.toString()
                        BULLET, CHEVRON, NONE -> indexType.char.toString()
                        else -> {
                            ""
                        }
                    }

                    val linePrompt = "$currentIndex${indexType.postfix}$line"
                    promptString += "$linePrompt\n"

                    if (isSelectable) {
                        when (indexType) {
                            //if is BULLET or CHEVRON, can use for simple listing
                            INTEGER, DECIMAL, ABC, UID -> {
                                when (option) {
                                    is SelectableCommand -> {
                                        option.command?.let {
                                            SelectedCommand(
                                                command = it,
                                            )
                                        }
                                    }

                                    is SelectableEntity -> {
                                        option.entity?.let {
                                            SelectedEntity(
                                                entity = it,
                                            )
                                        }
                                    }

                                    is SelectableCallback -> {
                                        //todo come back and fix this up
                                        option.callback?.let {
                                            SelectedCallback(callback = it)
                                        }
                                    }

                                    is SelectableText -> {
                                        SelectedText()
                                    }

                                    is SelectableEnum -> {
                                        option.enumValue?.let {
                                            SelectedEnum(enum = it)
                                        }
                                    }

                                }?.apply {
                                    promptText = lineString
                                    selectionHandleType = indexType
                                    fullLineText = linePrompt
                                    selectionHandle = currentIndex.uppercase()

                                    selectables[currentIndex.uppercase()] = this
                                }
                            }
                            //todo add case for Y/N
                            //todo also special validation for full words "yes" and "no" in that case
                            else -> {}
                        }
                    }
                }
            }
            promptString += footerText
        }
    }

    private fun defaultDisplayValues(option: SelectableOptionImpl): String? {
        return when (option) {
            is SelectableCommand -> option.command.toString()
            is SelectableEntity -> option.entity.toString()
            is SelectableCallback -> null
            is SelectableText -> null
            is SelectableEnum -> option.enumValue.name

        }
    }
}