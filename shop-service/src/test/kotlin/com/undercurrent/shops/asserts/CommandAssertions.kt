package com.undercurrent.shops.asserts

import com.undercurrent.shared.experimental.command_handling.RootCommand
import com.undercurrent.shared.experimental.command_handling.BaseModuleContext
import org.junit.jupiter.api.Assertions

class ShopCommandListAssertions(private val botContext: BaseModuleContext) {
    fun shouldHave(vararg commands: RootCommand): ShopCommandListAssertions {
        return assertCmds(true, *commands)
    }

    private fun shouldHave(command: RootCommand): ShopCommandListAssertions {
        val commands = botContext.commands()
        Assertions.assertTrue(
            commands.contains(command),
            "Should have command $command. Got: ${commands.sortedBy { it.handle() }}"
        )
        return this
    }


    fun shouldNotHave(vararg commands: RootCommand): ShopCommandListAssertions {
        return assertCmds(false, *commands)
    }

    private fun shouldNotHave(command: RootCommand): ShopCommandListAssertions {
        val commands = botContext.commands()
        Assertions.assertFalse(commands.contains(command), "Should not have command $command. Got: $commands")
        return this
    }

    private fun assertCmds(shouldHave: Boolean, vararg commands: RootCommand): ShopCommandListAssertions {
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