package com.undercurrent.shared.experimental.command_handling

import com.undercurrent.shared.types.enums.SystemApp
import kotlin.reflect.KClass

interface ModuleContextOld {
    fun commands(): Set<RootCommand>
}

abstract class BaseModuleContext(
    val systemApp: SystemApp,
    val cmdsForChildren: Set<RootCommand> = setOf(),
) : ModuleContextOld {
    abstract fun contextToCommands(): Map<KClass<out BaseModuleContext>, Set<RootCommand>>

    override fun commands(): Set<RootCommand> {
        val cmds = cmdsForChildren + globalCommands() + contextToCommands()[this::class].orEmpty()
        return cmds.distinct().toSet()
    }

    private fun globalCommands(): Set<RootCommand> {
        return setOf(
            GlobalCommand.START,
            GlobalCommand.CANCEL,
            GlobalCommand.FEEDBACK,
        )
    }
}
