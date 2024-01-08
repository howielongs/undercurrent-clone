package com.undercurrent.system.messaging.outbound

import com.undercurrent.legacy.routing.RunConfig
import com.undercurrent.prompting.components.EmojiSymbol
import com.undercurrent.shared.messages.NotificationMessageEntity
import com.undercurrent.shared.messages.RoutingProps
import com.undercurrent.shared.types.SubjectHeader
import com.undercurrent.shared.types.enums.Environment

@Deprecated("Prefer bottom function: passing env at the very least")
fun notifyAdmins(
    msg: String,
    subject: String = SubjectHeader.BROADCAST.name,
    emoji: EmojiSymbol = EmojiSymbol.LOUDSPEAKER,
    routingProps: RoutingProps? = null,
): NotificationMessageEntity? {
    return AdminsNotifyProvider(
        envIn = routingProps?.environment ?: RunConfig.environment,
        subject = subject,
        emoji = emoji
    ).sendOutput(msg)
}

fun notifyAdmins(
    msg: String,
    env: Environment,
    subject: SubjectHeader = SubjectHeader.BROADCAST,
    emoji: EmojiSymbol = EmojiSymbol.LOUDSPEAKER,
): NotificationMessageEntity? {
    return AdminsNotifyProvider(
        envIn = env,
        subject = subject.name,
        emoji = emoji
    ).sendOutput(msg)
}

fun notifyAdmins(
    msg: String,
    routingProps: RoutingProps,
    subject: SubjectHeader = SubjectHeader.BROADCAST,
    emoji: EmojiSymbol = EmojiSymbol.LOUDSPEAKER,
): NotificationMessageEntity? {
    return notifyAdmins(
        msg = msg,
        env = routingProps.environment,
        subject = subject,
        emoji = emoji
    )
}