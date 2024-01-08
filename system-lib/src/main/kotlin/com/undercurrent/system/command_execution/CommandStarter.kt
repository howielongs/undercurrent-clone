package com.undercurrent.system.command_execution

import com.undercurrent.shared.CanStartCommand
import com.undercurrent.shared.ValidCmdInput
import com.undercurrent.system.context.SystemContext

abstract class CommandStarter(
    val context: SystemContext,
) : CanStartCommand {

    override suspend fun startNewCommand(cmd: Any) {
        val cmdString = parseToString(cmd)

        val node = CoreNodes(
            body = cmdString,
            context = context
        ).next()

        //todo maybe we don't want to just executeAll, but rather step through nodes?
        node?.execute()
    }

    private fun parseToString(cmd: Any): String {
        return when (cmd) {
            is ValidCmdInput -> cmd.parseToString()
            else -> cmd.toString()
        }
    }

}