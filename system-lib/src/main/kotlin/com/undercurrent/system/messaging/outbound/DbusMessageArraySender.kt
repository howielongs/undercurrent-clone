package com.undercurrent.system.messaging.outbound

import com.undercurrent.legacy.repository.entities.system.attachments.Attachments
import com.undercurrent.shared.messages.RoutingProps
import com.undercurrent.system.service.dbus.SystemRuntimeExecutor
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * DBUS formats:
 * INFO DaemonCommand - Exported dbus object: /org/asamk/Signal/_11111111111
 * dbus-send --session --type=method_call --print-reply --dest="org.asamk.Signal" /org/asamk/Signal/_11111111111 org.asamk.Signal.sendMessage string:"Hello there!" array:string:"/home/ubuntu/.local/share/signal-cli/attachments/3136820769134007417" string:"+11111111111"
 */

class DbusMessageArraySender private constructor(
    private val destSms: String,
    private val body: String,
    private val dbusPath: String,
    private val attachmentsStr: String,
    private val dbusEndpointCall: (Array<String>) -> Unit
) {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        fun builder(
            dest: String, body: String, dbusProps: RoutingProps,
        ): Builder {
            return Builder(dest, body, dbusProps)
        }
    }

    fun sendMessage(): Boolean {
        val dbusCommand = arrayOf(
            "dbus-send",
            "--session",
            "--type=method_call",
            "--print-reply",
            "--dest=org.asamk.Signal",
            dbusPath,
            "org.asamk.Signal.sendMessage",
            "string:${body.replace("`", "'")}",
            "array:string:$attachmentsStr",
            "string:$destSms"
        )

        if (logger.isInfoEnabled) {
            "Sending message: ${dbusCommand.contentToString()}".let {
                logger.info(it)
            }
        }

        dbusEndpointCall(dbusCommand)
        return true
    }

    class Builder(
        private val destAddr: String,
        private val body: String,
        private val dbusProps: RoutingProps,
    ) {
        private var attachmentsStr: String = ""
        private var dbusSendRunner: (Array<String>) -> Unit = {
            SystemRuntimeExecutor(dbusProps.environment).executeRuntime(it)
        }

        fun withAttachmentsStr(attachmentsStr: String): Builder {
            this.attachmentsStr = attachmentsStr
            return this
        }

        fun withAttachment(attachment: Attachments.Entity): Builder {
            val path = AttachmentMessageCreator().saveIfAttachment(
                destAddr = destAddr,
                body = body,
                attachment = attachment,
                dbusProps = dbusProps
            ) ?: ""
            return withAttachmentsStr(path)
        }

        fun build(): DbusMessageArraySender {
            return DbusMessageArraySender(
                destSms = destAddr,
                body = body,
                dbusPath = dbusProps.toPath().value,
                attachmentsStr = attachmentsStr,
                dbusEndpointCall = dbusSendRunner
            )
        }
    }
}
