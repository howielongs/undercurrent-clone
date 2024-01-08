package com.undercurrent.legacy.repository.entities.system.attachments


import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.repository.entities.Users
import com.undercurrent.legacy.types.enums.AttachmentType
import com.undercurrent.shared.messages.RoutingProps
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.VARCHAR_SIZE
import com.undercurrent.shared.utils.time.EpochNano
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.context.DbusProps
import com.undercurrent.system.service.dbus.TypingIndicatorCancellor
import com.undercurrent.system.messaging.outbound.DbusMessageArraySender
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upperCase

//todo this needs cleanup and organization
class Attachments {

    companion object {
        fun save(
            thisUser: User,
            savePath: String,
            captionIn: String = "",
            role: AppRole,
        ): Entity {
            return tx {
                return@tx Entity.new {
                    ownerUser = thisUser
                    ownerRole = role.name
                    path = savePath
                    caption = captionIn.replace("'", "`")
                }
            }
        }
    }


    object Table : ExposedTableWithStatus2("attachment_file_paths") {

        // add references/backReferences for AttachmentLink
        // add backReference from User

        val ownerUser = reference("owner_user_id", Users)
        val ownerRole = varchar("owner_role", VARCHAR_SIZE).default(ShopRole.CUSTOMER.name)

        val path = varchar("path", VARCHAR_SIZE)
        val caption = varchar("caption", VARCHAR_SIZE)
        val uploadEpochNano = long("upload_epoch").clientDefault { EpochNano().value }


        fun all(): List<Entity> {
            return transaction { Entity.all().toList() }
        }


        fun fetchByOwnerUserAndRole(userId: Int, role: AppRole): List<Entity> {
            return tx {
                Entity.find {
                    ownerUser eq userId and
                            (ownerRole.upperCase() eq role.name.uppercase())
                }.toList()
                    .filter { it.isNotExpired() }
            }
        }

        fun byCaption(captionToFind: AttachmentType): Entity? {
            return tx {
                Entity.find {
                    caption.upperCase() eq captionToFind.name.uppercase()
                }.toList().lastOrNull { it.isNotExpired() }
            }
        }


    }

    class Entity(id: EntityID<Int>) : ExposedEntityWithStatus2(id, Table) {

        companion object : RootEntityCompanion0<Entity>(Table)

        var ownerUser by User referencedOn Table.ownerUser
        var ownerRole by Table.ownerRole

        var path: String by Table.path
        var caption by Table.caption
        var uploadEpochNano by Table.uploadEpochNano


        // convert to 'referencedOn' (and handle null cases or cascading deletion)
        var links: List<AttachmentLinks.Entity> = emptyList()
            get() {
                return transaction {
                    AttachmentLinks.Entity.find {
                        AttachmentLinks.Table.parentAttachment eq this@Entity.id
                    }.toList().filter { it.isNotExpired() }
                }
            }

        suspend fun send(
            recipientUser: User, dbusPropsIn: DbusProps? = null,
        ) {
            dbusPropsIn?.let { send(recipientUser = recipientUser, currentOperationTag = "", dbusPropsIn = it) }
        }

        fun updatePath(newPath: String): Entity {
            return transaction {
                path = newPath
                this@Entity
            }
        }

        fun createLink(
            attachmentTypeIn: AttachmentType,
            captionIn: String = this@Entity.caption,
            parentEntity: ExposedEntityWithStatus2? = null,
        ): AttachmentLinks.Entity {
            return tx {
                return@tx AttachmentLinks.Entity.new {
                    parentAttachment = this@Entity
                    attachmentType = attachmentTypeIn.name
                    caption = captionIn

                    parentEntityId = parentEntity?.uid ?: null
                    parentEntityClassName = parentEntity?.javaClass?.simpleName ?: null
                }
            }
        }

        //todo try to enforce usage of dbusPath
        suspend fun send(
            recipientUser: User,
            selectIndex: String = "",
            captionText: String? = null,
            currentOperationTag: String = "",
            dbusPropsIn: RoutingProps,
        ) = coroutineScope {
            val thisEnv = dbusPropsIn?.environment
            if (thisEnv == Environment.TEST) {
                return@coroutineScope
            }

            val thisRole = dbusPropsIn.role

            val dest = tx { recipientUser.userSms }
            TypingIndicatorCancellor(
                recipientSms = dest,
                dbusProps = dbusPropsIn
            ).send()

            Log.debug("Sending attachment: #${tx { this@Entity.uid }}")
            var indexHeader = ""
            if (selectIndex != "") {
                indexHeader = "$selectIndex. "
            }

            val messageBody =
                tx { captionText ?: indexHeader + this@Entity.caption }

            launch {
                coroutineScope {
                    Log.debug("LAUNCHED COROUTINE FOR SENDING ATTACHMENT")

                    val sender = DbusMessageArraySender.builder(
                        dest = dest.value,
                        body = messageBody,
                        dbusProps = dbusPropsIn
                    )
                        .withAttachment(this@Entity)
                        .build()

                    if (sender.sendMessage()) {
                        Log.debug("ATTACHMENT MESSAGE SENT SUCCESSFULLY")

                        //todo next try retry here
                        tx {
                            Log.debug("INSIDE TRANSACTION FOR SAVING ATTACHMENT VIEW EVENTS")

                            AttachmentViewEvents.Entity.new {
                                attachment = this@Entity
                                viewerUser = recipientUser
                                locationTag = currentOperationTag
                                rawContext = currentOperationTag + " ${thisRole.name}"
                            }
                        }?.let {
                            Log.debug("Successfully created AttachmentView event")
                        }
                    } else {
                        Log.error("ATTACHMENT MESSAGE SENT UNSUCCESSFULLY")
                    }

                    Log.debug("FINISHED COROUTINE FOR SENDING ATTACHMENT")
                }
            }

        }

        suspend fun send(
            recipientUserId: Int,
            selectIndex: String = "",
            captionText: String? = null,
            currentOperationTag: String = "",
            dbusPropsIn: RoutingProps,
        ) {
            val destUser = User.findById(recipientUserId) ?: run {
                Log.error("Could not find User #$recipientUserId to send attachment $uid")
                return
            }
            send(
                recipientUser = destUser,
                selectIndex = selectIndex,
                captionText = captionText,
                currentOperationTag = currentOperationTag,
                dbusPropsIn = dbusPropsIn
            )
        }

        //todo consider avoiding "attachmentType" and just stick with class name
        fun createLinkForAttachment(
            parentId: Int,
            attachmentType: AttachmentType,
        ): AttachmentLinks.Entity? {
            return AttachmentLinks.save(
                this,
                attachmentTypeIn = attachmentType,
                parentId = parentId,
            )
        }
    }

}
