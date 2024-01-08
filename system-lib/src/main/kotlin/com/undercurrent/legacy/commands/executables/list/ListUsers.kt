package com.undercurrent.legacy.commands.executables.list


import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.system.context.SessionContext
import com.undercurrent.system.repository.entities.User
import com.undercurrent.legacy.types.enums.ListIndexTypeOld
import com.undercurrent.legacy.dinosaurs.prompting.selectables.OptionSelector
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectableEntity
import org.jetbrains.exposed.sql.transactions.transaction

// add option to group by activity
//also cache recent message stats
class ListUsers(sessionContext: SessionContext) : Executable(
    CmdRegistry.LISTUSERS, sessionContext
) {

    override suspend fun execute() {
        val result = with(fetchAllUsers().map { SelectableEntity(it) }) {
            if (this.isEmpty()) {
                null
            } else {
                OptionSelector(
                    this,
                    headerText = "Active users:",
                    indexType = ListIndexTypeOld.INTEGER,
                    isSelectable = false,
                    footerText = "",
                    headlineText = null,
                ).let {
                    it.promptString
                }
            }
        }

        result?.let {
            sessionContext.interrupt(it)
        } ?: run {
            sessionContext.interrupt("No users found")
        }
    }

    private fun fetchAllUsers(): List<User> {
        return transaction { User.all().filter { it.isNotExpired() } }
    }

}