package com.undercurrent.legacy.commands.executables.info

import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.system.context.SessionContext
import com.undercurrent.legacy.repository.entities.system.attachments.Attachments
import com.undercurrent.legacy.repository.repository_service.AttachmentsManager
import com.undercurrent.legacy.utils.UtilLegacy
import com.undercurrent.legacy.routing.RunConfig
import com.undercurrent.system.context.SystemContext

class MigrateAttachments(sessionContext: SystemContext) : Executable(CmdRegistry.MIGRATEIMAGES, sessionContext) {


    /**
     * Migrate all attachments in env to appropriate folder.
     */
    private fun migrateAttachmentsCmd(sessionContext: SessionContext) {
        var count = 0

        Attachments.Table.all().forEach {
            val cleanedPath = UtilLegacy.stripOptional(it.path)

            if (cleanedPath != it.path) {
                sessionContext.interrupt("$cleanedPath was cleaned of an Optional path")
            }

            with(AttachmentsManager.moveToAttachmentEnvDir(it.path, sessionContext.routingProps)) {
                if (this != cleanedPath) {
                    count++
                    it.updatePath(this)
                }
            }
        }
        sessionContext.interrupt("Attachments moved to ${RunConfig.environment} subdirectory: $count")
    }


    override suspend fun execute() {
        migrateAttachmentsCmd(sessionContext)
    }
}