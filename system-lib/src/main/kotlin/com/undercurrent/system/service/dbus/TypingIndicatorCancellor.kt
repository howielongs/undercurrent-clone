package com.undercurrent.system.service.dbus

import com.undercurrent.shared.messages.RoutingProps
import com.undercurrent.shared.repository.entities.SignalSms


// have this able to send multiple messaging to same user
//@Deprecated("Use older sender, but check in here and test why this doesn't work")
//class DBusMessageSender(
//    private val msg: String,
//    private val recipientSms: String,
//    private val attachmentPaths: List<String> = listOf(),
//    fullDbusPath: String, environment: Environment,
//) : DbusMethodSender(DbusMethod.SEND_MESSAGE, fullDbusPath, environment) {
//
//    override fun buildArgArray(): Array<String> {
//        DbusSendArrayBuilder().apply {
//            newRoot(fullDbusPath, dbusMethod, msg.replace("`", "'"))
//            addArr(attachmentPaths.joinToString(","))
//            return add(recipientSms)
//        }
//    }
//}

// take in fewer params and derive what is needed
class TypingIndicatorCancellor(
    recipientSms: SignalSms,
    dbusProps: RoutingProps,
) : TypingIndicatorSender(humanRecipientAddr = recipientSms, shouldCancel = true, dbusProps = dbusProps)