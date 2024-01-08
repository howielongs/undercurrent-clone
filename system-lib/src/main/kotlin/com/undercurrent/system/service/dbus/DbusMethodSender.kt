package com.undercurrent.system.service.dbus

import com.undercurrent.prompting.messaging.DbusMethod
import com.undercurrent.shared.messages.RoutingProps
import com.undercurrent.shared.types.enums.Environment
import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface DbusSendMethod {
    fun send()
    fun build(): DbusMethodSender
}

abstract class DbusMethodSender(
    val dbusMethod: DbusMethod,
    val dbusProps: RoutingProps,
    val endpointCall: (Array<String>) -> Unit = {
        SystemRuntimeExecutor(dbusProps.environment).executeRuntime(it)
    }
) : DbusSendMethod {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    abstract fun buildArgArray(): Array<String>

    override fun build(): DbusMethodSender {
        argArray
        return this@DbusMethodSender
    }

    private val argArray: Array<String> by lazy {
        buildArgArray()
    }

    override fun send() {
        with(argArray) {
            // perhaps can pass in function pointing to Main?

            if (logger.isInfoEnabled) {
                "Sending dbus method: ${this.joinToString(" ")}".let {
                    logger.info(it)
                }
            }

            if (dbusProps.environment == Environment.TEST) {
                "Skipping dbus send for TEST env".let {
                    logger.info(it)
                }
                return
            }

            endpointCall(this)
        }
    }
}