package com.undercurrent.legacy.commands.executables.list

import com.undercurrent.shared.utils.Log
import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.system.context.SessionContext
import com.undercurrent.legacy.repository.entities.system.attachments.Attachments
import com.undercurrent.legacy.utils.UtilLegacy

class ListImageAttachments(sessionContext: SessionContext) : Executable(
    CmdRegistry.VIEWIMAGES, sessionContext
) {

    override suspend fun execute() {
        val sessionUser = sessionContext.user
        val role = sessionContext.role

        //todo should display what they're each attached to

        with(Attachments.Table.fetchByOwnerUserAndRole(sessionUser.uid, role)) {
            if (this.isEmpty()) {
                sessionContext.interrupt("No attachments in gallery.")
                return
            }
            forEachIndexed { index, it ->
                Log.debug("Sending attachment: $it")
                it.send(
                    recipientUserId = sessionUser.uid,
                    selectIndex = UtilLegacy.getCharForNumber(index + 1),
                    currentOperationTag = thisCommand.lower(),
                    dbusPropsIn = sessionContext.routingProps
                )
            }
        }
    }
}