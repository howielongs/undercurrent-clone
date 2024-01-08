package com.undercurrent.legacy.repository.entities.system


import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.repository.entities.system.attachments.Attachments
import com.undercurrent.legacy.routing.RunConfig
import com.undercurrent.legacy.types.enums.AttachmentType
import com.undercurrent.legacy.types.enums.IntroEventType.WELCOME_MSG_CUSTOMER
import com.undercurrent.legacy.types.enums.IntroEventType.WELCOME_MSG_VENDOR
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.types.enums.ShopRole.*
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.VARCHAR_SIZE
import com.undercurrent.shared.utils.time.unexpiredExpr
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.context.SystemContext
import com.undercurrent.system.messaging.outbound.sendInterrupt
import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.repository.entities.Users
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

interface CanDisplayWelcome {
    suspend fun displayWelcomeIfUnseen(context: SystemContext)
}


class IntroEvents {

    object Table : ExposedTableWithStatus2("system_intro_events"), CanDisplayWelcome {
        val user = reference("user_id", Users)
        val role = varchar("role", VARCHAR_SIZE)

        val eventType = varchar("event_type", VARCHAR_SIZE)
        val memo = varchar("memo", VARCHAR_SIZE).clientDefault { "" }

        override suspend fun displayWelcomeIfUnseen(sessionContext: SystemContext) {
            val user = sessionContext.user
            val userId = tx { user.uid }
            val role = sessionContext.role

            if (userHasSeenWelcomeMessage(userId, role)) {
                Log.info("User #${userId} has already seen welcome message")
            } else {
                Log.info("User #${userId} has not yet seen welcome message. Displaying now...")
                displayWelcomeMsg(
                    userIn = user,
                    userIdIn = userId,
                    roleIn = role as ShopRole,
                    sessionContext = sessionContext
                )
            }
        }


        //can probably clean this up to be a bit more succinct
        suspend fun displayWelcomeMsg(
            userIn: User,
            userIdIn: Int,
            roleIn: ShopRole,
            sessionContext: SystemContext,
        ) {
            val thisEventType = when (roleIn) {
                VENDOR -> {
                    Attachments.Table.byCaption(AttachmentType.VENDOR_WELCOME)?.let {
                        it.send(
                            userIdIn,
                            captionText = PressAgent.VendorStrings.welcomeMsg(),
                            currentOperationTag = CmdRegistry.WELCOME.lower(),
                            dbusPropsIn = sessionContext.routingProps
                        )
                        return
                    }
                    PressAgent.VendorStrings.welcomeMsg().let {

                        // may want to pass in dep for actual interrupter here?
                        sendInterrupt(user = userIn, role = roleIn, environment = RunConfig.environment, msg = it)
                    }
                    WELCOME_MSG_VENDOR
                }

                CUSTOMER -> {
                    Attachments.Table.byCaption(AttachmentType.CUSTOMER_WELCOME)?.let {
                        it.send(
                            userIdIn,
                            currentOperationTag = CmdRegistry.WELCOME.lower(),
                            captionText = PressAgent.CustomerStrings.defaultHelp(),
                            dbusPropsIn = sessionContext.routingProps
                        )
                        return
                    }
                    PressAgent.CustomerStrings.defaultHelp().let {
                        if (sessionContext.isTestMode()) {
                            //todo put this into a separate node
                            sendInterrupt(
                                user = userIn,
                                role = roleIn,
                                environment = sessionContext.environment,
                                msg = it
                            )
                        }
                    }
                    WELCOME_MSG_CUSTOMER
                }

                ADMIN -> {
                    null
                }

//                else -> {
//                    "No applicable message".let {
//                        sendInterrupt(user = userIn, role = roleIn, environment = RunConfig.environment, msg = it)
//                    }
//                    null
//                }
            }

            thisEventType?.let {
                tx {
                    Entity.new {
                        user = userIn
                        role = roleIn.name
                        eventType = it.name
                    }
                }
            }
        }

        private fun userHasSeenWelcomeMessage(userId: Int, roleIn: AppRole): Boolean {
            return transaction {
                val thisEventType = when (roleIn) {
                    VENDOR -> WELCOME_MSG_VENDOR
                    CUSTOMER -> WELCOME_MSG_CUSTOMER
                    else -> null
                }

                Entity.find {
                    user eq userId and (
                            role eq roleIn.name and (
                                    eventType eq thisEventType.toString() and (
                                            unexpiredExpr(
                                                Table
                                            ))))
                }.toList().isNotEmpty()
            }

        }


    }

    class Entity(id: EntityID<Int>) : ExposedEntityWithStatus2(id, Table) {
        companion object : RootEntityCompanion0<Entity>(Table)

        var user by User referencedOn Table.user
        var role by Table.role

        var eventType by Table.eventType
        var memo by Table.memo


    }


}
