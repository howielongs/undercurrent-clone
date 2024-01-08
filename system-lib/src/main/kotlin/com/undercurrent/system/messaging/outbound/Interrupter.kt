package com.undercurrent.system.messaging.outbound

import com.undercurrent.system.repository.entities.User
import com.undercurrent.shared.messages.InterrupterMessageEntity
import com.undercurrent.shared.messages.NotificationMessageEntity
import com.undercurrent.shared.messages.RoleBasedOutputProvider
import com.undercurrent.shared.messages.RoutingProps
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.system.context.DbusProps
import com.undercurrent.system.repository.entities.messages.NotificationMessage
import com.undercurrent.system.repository.entities.messages.OutboundMessage

class Interrupter(
    val userEntity: User,
    routingProps: RoutingProps,
) : RoleBasedOutputProvider<InterrupterMessageEntity, DbusProps>(
    routingProps = routingProps as DbusProps
) {

    override fun sendOutput(msgBody: String): InterrupterMessageEntity? {
        return OutboundMessage.save(
            bodyIn = msgBody,
            userIn = userEntity,
            botSmsIn = routingProps.toBotSms(),
            roleIn = routingProps.role,
        )
    }

    override fun sendOutputByRole(msgBody: String, role: AppRole) {
        val newProps = DbusProps(roleIn = role, envIn = routingProps.environment)
        OutboundMessage.save(
            bodyIn = msgBody,
            userIn = userEntity,
            botSmsIn = newProps.toBotSms(),
            roleIn = newProps.role,
        )
    }
}

class Notifier(
    val userEntity: User,
    routingProps: RoutingProps,
) : RoleBasedOutputProvider<NotificationMessageEntity, DbusProps>(
    routingProps = routingProps as DbusProps
) {

    constructor(
        userEntity: User,
        role: AppRole,
        environment: Environment,
    ) : this(
        userEntity = userEntity,
        routingProps = DbusProps(roleIn = role, envIn = environment)
    )

    override fun sendOutput(msgBody: String): NotificationMessageEntity? {
        return NotificationMessage.addToQueue(
            msgIn = msgBody,
            userIn = userEntity,
            dbusProps = routingProps,
        )
    }

    override fun sendOutputByRole(msgBody: String, role: AppRole) {
        NotificationMessage.addToQueue(
            msgIn = msgBody,
            userIn = userEntity,
            dbusProps = routingProps,
            roleOverride = role
        )
    }
}


fun sendNotify(user: User, role: AppRole, environment: Environment, msg: String): NotificationMessageEntity? {
    return Notifier(
        userEntity = user,
        routingProps = DbusProps(roleIn = role, envIn = environment)
    ).sendOutput(msg)
}


fun sendInterrupt(user: User, role: AppRole, environment: Environment, msg: String): InterrupterMessageEntity? {
    return Interrupter(
        userEntity = user,
        routingProps = DbusProps(roleIn = role, envIn = environment)
    ).sendOutput(msg)
}


//    fun formatForAdminOut(
//        messageBody: String,
//        subject: SubjectHeader? = SubjectHeader.BROADCAST,
//        emojiStatus: Emoji? = Emoji.WARNING
//    ): String {
//        val emojiBlock = emojiStatus?.parse()?.let { "$it\t" } ?: ""
//        val subjectBlock = subject?.let { "[$it:ADMIN]\n\n" } ?: ""
//        val messageBlock = "$messageBody"
//        return "$emojiBlock$subjectBlock$messageBlock"
//    }

//    fun notifyRaw(body: String, subject: SubjectHeader? = null) {
//        val adminUsers = tx { AdminProfile.all().filterOutExpired().map { it.user } }
//
//        val msgOut = if (subject == null) {
//            body
//        } else {
//            "${EmojiParser.parseToUnicode(":loudspeaker:")}\t" + "[$subject:ADMIN]\n\n" + "$body"
//        }
//
//        adminUsers.forEach {
//            //todo not sure which way is better and less icky
//            SessionContext(user = it, role = Rloe.ADMIN).notify(msgOut)
////            it.notify(body, Rloe.ADMIN)
//        }
//    }
