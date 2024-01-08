package com.undercurrent.shared.experimental.command_handling

import com.undercurrent.shared.types.strings.CleanString

object CommandParser {
    fun toValidCommand(data: String): RootCommand? {
        val parsedToken = CleanString(data).clean()
        return GlobalCommand.values().singleOrNull { it.name.uppercase() == parsedToken }
    }

}