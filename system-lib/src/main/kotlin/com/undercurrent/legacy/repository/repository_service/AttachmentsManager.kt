package com.undercurrent.legacy.repository.repository_service


import com.undercurrent.system.repository.entities.User
import com.undercurrent.legacy.repository.entities.system.attachments.Attachments
import com.undercurrent.legacy.service.user_role_services.UserRoleChecker
import com.undercurrent.legacy.types.enums.AttachmentType
import com.undercurrent.legacy.utils.UtilLegacy
import com.undercurrent.shared.messages.RoutingProps
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.tx
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class FileAttachmentLinker(
    val thisUser: User,
    val savePath: String,
    val captionIn: String = "",
    val attachmentType: AttachmentType,
    val routingProps: RoutingProps,
) {
    fun link(): Attachments.Entity? {
        return Attachments.save(
            thisUser = thisUser,
            savePath = savePath,
            captionIn = captionIn,
            role = routingProps.role
        )?.let {
            it.createLinkForAttachment(
                parentId = tx { it.uid },
                attachmentType = attachmentType
            )
            it
        }
    }
}

object AttachmentsManager {

//    suspend fun createAttachmentAndLink(
//        sessionContext: SessionContext,
//        filePath: String,
//        attachmentType: AttachmentType,
//    ): Attachments.Entity? {
//        //caption possibility: user report with date?
//
//        val attachment = Attachments.save(
//            thisUser = sessionContext.user,
//            savePath = filePath,
//            captionIn = ""
//        )
//
//        return if (attachment != null) {
//            attachment.createLinkForAttachment(
//                parentId = tx { attachment.uid },
//                attachmentType = attachmentType
//            )
//            attachment
//        } else {
//            null
//        }
//    }

    /**
     * Get InputCursor, check if expecting Image attachment
     * If no attachment, do 'skip'
     * If attachment, handle and then continue addproduct
     *
     * Make general when implementing for welcome messaging and logos
     */
//    fun handleAttachments(
//        senderSms: String,
//        attachments: List<String> = listOf(),
//        body: String,
//        role: Role?,
//    ): Int {
//        var numAttachmentsSaved = 0
//        attachments.forEach {
//            Log.debug("Handling incoming attachment from $senderSms")
//            parseAndSaveAttachment(it, body, senderSms, role).let { parseSuccess ->
//                if (parseSuccess) {
//                    numAttachmentsSaved++
//                }
//            }
//        }
//        //todo consider output messaging here ("Uploaded 3 attachments")
//        return numAttachmentsSaved
//    }


    //todo do this migration for existing?
    //todo probably do this differently for testing modes
    fun moveToAttachmentEnvDir(originalPath: String, dbusProps: RoutingProps): String {
        /**
         * 1. Ensure new path/dir exists
         * 2. Generate new path
         * 3. Move attachment to new path
         * 4. Return path name
         */
        // sampleAttachPath: /home/ubuntu/.local/share/signal-cli/attachments/4138180931217243189
        val userhome = "user.home"
        val path = System.getProperty(userhome)
        val envPath =
            "$path/.local/share/signal-cli/attachments/${dbusProps.environment.name.lowercase()}"
        Files.createDirectories(Paths.get(envPath))

        try {
            val tail = originalPath.split("attachments/")[1]

            if (tail.contains(dbusProps.environment.name.lowercase() + "/")) {
                Log.debug("File already in correct directory: \n\t$originalPath")
                return originalPath
            }

            val newPath = "$envPath/$tail"

            if (newPath == originalPath) {
                Log.debug("File already in correct directory: \n\t$originalPath")
                return originalPath
            }

            val from = File(originalPath)
            val to = File(newPath)

            Files.move(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING)
            Log.debug(
                "FILE_MOVE: Attachment moved successfully:\n" +
                        "\tFrom: $originalPath\n" +
                        "\tTo: $newPath"
            )
            return newPath

        } catch (e: Exception) {
            Log.error(
                "FILE_MOVE: Error moving attachment file:\n" +
                        "\tFrom: $originalPath"
            )

        }
        return originalPath
    }

    /**
     * 1. Create: File Attachment
     * 2. Create: AttachmentLink
     * 3. Update: InputCursor with new FileAttachment uid
     * 4. Update: InputCursor to AWAITING_CONFIRMED
     */
    @Deprecated("Needs fixing up (avoid the user fetching part)")
    suspend fun parseAndSaveAttachment(
        attachmentPath: String,
        messageBody: String,
        thisUser: User,
        role: AppRole,
        dbusProps: RoutingProps,
        uploadEpoch: Long,
    ): Boolean {
        if (UserRoleChecker(thisUser).matchesAtLeastOneRole(ShopRole.ADMIN, ShopRole.VENDOR)) {
            tx {
                val savePath = moveToAttachmentEnvDir(
                    originalPath = UtilLegacy.stripOptional(attachmentPath),
                    dbusProps = dbusProps
                )

                Attachments.Entity.new {
                    ownerUser = thisUser
                    ownerRole = role.name
                    path = savePath
                    caption = messageBody.replace("'", "`")
                    uploadEpochNano = uploadEpoch
                }
            }
            return true
        }
        return false
    }


    /**
     * 1. Create: File Attachment
     * 2. Create: AttachmentLink
     * 3. Update: InputCursor with new FileAttachment uid
     * 4. Update: InputCursor to AWAITING_CONFIRMED
     */
//    private fun parseAndSaveAttachment(
//        attachmentPath: String,
//        messageBody: String,
//        senderSms: String,
//        role: Role?,
//    ): Boolean {
//        Users.fetchBySms(senderSms)?.let { thisUser ->
//            if (UserRoleFetcher(thisUser).hasAtLeastOneRole(Rloe.ADMIN, Rloe.VENDOR)) {
//                transaction {
//                    val savePath = moveToAttachmentEnvDir(UtilLegacy.stripOptional(attachmentPath))
//
//                    Attachments.Entity.new {
//                        ownerUser = thisUser
//                        ownerRole = RunConfig.role.toString()
//                        path = savePath
//                        caption = messageBody.replace("'", "`")
//                    }
//                }
//                return true
//            } else {
//                thisUser.interrupt("You do not have access to upload images", role)
//            }
//        } ?: return false
//        return false
//    }
}