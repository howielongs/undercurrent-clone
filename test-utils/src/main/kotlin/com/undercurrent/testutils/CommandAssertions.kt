package com.undercurrent.testutils

import com.undercurrent.shared.experimental.command_handling.RootCommand
import com.undercurrent.shared.experimental.command_handling.BaseModuleContext
import org.junit.jupiter.api.Assertions

class AssertCommandsForContext(private val botContext: BaseModuleContext) {
    fun shouldHave(vararg commands: RootCommand): AssertCommandsForContext {
        return assertCmds(true, *commands)
    }

    private fun shouldHave(command: RootCommand): AssertCommandsForContext {
        val commands = botContext.commands()
        Assertions.assertTrue(
            commands.contains(command),
            "Should have command $command. Got: ${commands.sortedBy { it.handle() }}"
        )
        return this
    }


    fun shouldNotHave(vararg commands: RootCommand): AssertCommandsForContext {
        return assertCmds(false, *commands)
    }

    private fun shouldNotHave(command: RootCommand): AssertCommandsForContext {
        val commands = botContext.commands()
        Assertions.assertFalse(commands.contains(command), "Should not have command $command. Got: $commands")
        return this
    }

    private fun assertCmds(shouldHave: Boolean, vararg commands: RootCommand): AssertCommandsForContext {
        commands.forEach {
            if (shouldHave) {
                shouldHave(it)
            } else {
                shouldNotHave(it)
            }
        }
        return this
    }
}