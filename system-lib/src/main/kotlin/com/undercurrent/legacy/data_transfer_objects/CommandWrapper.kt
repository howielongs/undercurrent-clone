package com.undercurrent.legacy.data_transfer_objects

import com.undercurrent.legacy.commands.registry.*
import com.undercurrent.shared.ValidCmdInput
import com.undercurrent.shared.utils.Log
import com.undercurrent.system.context.SystemContext

interface StringToCmdParser {
    fun parseStringToCmd(
        data: String,
        sessionContext: SystemContext,
    ): CommandWrapper?
}

//todo fix this up into interface-based class
class CommandWrapper(
    var handle: String = "",
    private var description: String = "",
    val priority: Int = 0,
    val prompt: String = "",
    val commandRef: BaseCommand = TopCommand.START
) : StringToCmdParser, ValidCmdInput {

    override fun parseToString(): String {
        return commandRef.parseToString()
    }

    override fun toString(): String {
        return "$handle - $description"
    }

    override fun parseStringToCmd(data: String, sessionContext: SystemContext): CommandWrapper? {
        return parseStr(data, sessionContext)
    }

    companion object {
        /**
         * Check for leading `/` char
         * Remove any whitespace at front
         * Ignore anything after '/' command
         * Check against enum for valid commands
         */
        fun parseStr(
            data: String,
            sessionContext: SystemContext,
        ): CommandWrapper? {
            val parsedToken = parseSlashCommand(data)?.lowercase()
            fetchAllCommands(sessionContext)
                .filter { it.handle == parsedToken }
                .let {
                    return when (it.size) {
                        1 -> it[0]
                        0 -> {
                            Log.debug(
                                "Found no commands matching `$data`: \n" +
                                        "${it.joinToString(",\n")}"
                            )
                            null
                        }

                        else -> {
                            Log.warn(
                                "Found too many commands matching `$data`: \n" +
                                        "${it.joinToString(",\n")}"
                            )
                            null
                        }
                    }
                }
        }

        private fun parseSlashCommand(data: String): String? {
            var inputString = data.replace(" ", "")
            if (inputString.isNotEmpty()) {
                if (inputString[0] == '/') {
                    return inputString.split(" ")[0].substring(1).lowercase()
                }
            }
            return inputString
        }

        private fun fetchAllCommands(
            sessionContext: SystemContext,
        ): List<CommandWrapper> {
            val commands = ArrayList<CommandWrapper>()
            fetchUserSlashCommands(sessionContext).forEach {
                commands.add(it)
            }
            fetchTopLevelCommands(sessionContext, showHidden = true).forEach {
                commands.add(it)
            }
            return commands.sortedWith(compareBy<CommandWrapper> { it.priority }.thenBy { it.handle })
        }

        fun fromCommandEnum(
            command: BaseCommand
        ): CommandWrapper {
            with(command) {
                return CommandWrapper(
                    lower(),
                    hint,
                    priority = priority,
                    prompt = prompt ?: hint,
                    commandRef = this
                )
            }
        }

        fun fetchTopLevelCommands(
            sessionContext: SystemContext,
            showHidden: Boolean = false,
        ): List<CommandWrapper> {
            var cmdList = ArrayList<CommandWrapper>()

            with(TopCommand.values()) {
                filter { sessionContext.role in it.permissions }
                    .filter {
                        if (showHidden) {
                            true
                        } else {
                            !it.isHiddenFromListing
                        }
                    }
                    .forEach {
                        cmdList.add(
                            CommandWrapper(
                                it.name.lowercase(),
                                it.hint,
                                priority = it.priority,
                                commandRef = it
                            )
                        )
                    }
                return cmdList.sortedWith(compareBy<CommandWrapper> { it.priority }.thenBy { it.handle })
            }
        }

        private fun <T> merge(first: List<T>, second: List<T>): List<T> {
            return first + second
        }

        fun fetchUserSlashCommands(
            sessionContext: SystemContext,
            commandGroup: TopCommand? = null,
        ): List<CommandWrapper> {
            var cmdList = ArrayList<CommandWrapper>()
            var role = sessionContext.role

            if (commandGroup == TopCommand.ALL) {
                return fetchAllCommands(sessionContext)
            }

            val srcCommands: List<UserCommand> =
                merge(
                    CmdRegistry.values().toList(),
                    DemoCommand.values().toList()
                )

            //todo make generic version to support DemoCommands
            val cmdsForRole: List<UserCommand> =
                when (commandGroup) {
                    null -> {
                        srcCommands.filter {
                            role in it.permissions
                        }
                    }

                    else -> {
                        srcCommands.filter {
                            (role in it.permissions && commandGroup in it.commandGroup)
                        }
                    }
                }

            cmdsForRole.forEach {
                cmdList.add(
                    CommandWrapper(
                        it.toString().lowercase(),
                        it.hint,
                        priority = it.priority,
                        prompt = it.prompt ?: it.hint,
                        commandRef = it
                    )
                )
            }

            return cmdList.sortedWith(compareBy<CommandWrapper> { it.priority }.thenBy { it.handle })
        }
    }

}