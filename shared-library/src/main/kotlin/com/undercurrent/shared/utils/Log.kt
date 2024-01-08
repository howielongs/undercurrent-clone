package com.undercurrent.shared.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

enum class LogLevel {
    TEST, FATAL, ERROR, WARN, DEBUG, INFO, TRACE
}

object Log {
     val logLevel = LogLevel.WARN

    private fun logOut(
        msg: String, level: LogLevel, exception: Exception? = null, sourceClass: String? = null
    ) {
//        println(level.name.uppercase() + ": " + msg + "\n" + exception?.stackTraceToString())

        val logger: Logger = sourceClass?.let {
            LoggerFactory.getLogger(it)
        } ?: LoggerFactory.getLogger(this::class.java)

        when (level) {
            LogLevel.FATAL -> logger.error("[FATAL] *** $msg", exception)
            LogLevel.ERROR -> logger.error(msg, exception)
            LogLevel.WARN -> logger.warn(msg, exception)
            LogLevel.DEBUG -> logger.debug(msg, exception)
            LogLevel.INFO -> logger.info(msg, exception)
            LogLevel.TRACE -> logger.trace(msg, exception)
            else -> logger.info(msg, exception)
        }
    }


    fun fatal(message: String?, exception: Exception? = null, sourceClass: String? = null) {
//        notifyAdmins(message.toString(), "FATAL_ERROR")
//        exception?.stackTraceToString()?.let { notifyAdmins(it, "FATAL_ERROR") }

        logOut(
            message.toString(), exception = exception, sourceClass = sourceClass, level = LogLevel.FATAL
        )
    }

    fun error(message: String?, exception: Exception? = null, sourceClass: String? = null) {
//        notifyAdmins(message.toString(), "ERROR_LOG")
//        exception?.stackTraceToString()?.let { notifyAdmins(it, "ERROR_LOG") }
        logOut(
            message.toString(), exception = exception, sourceClass = sourceClass, level = LogLevel.ERROR
        )
    }


    fun test(message: String?) {
        val horizLine = "---------------------------"
        logOut("\n" + message + "\n$horizLine\n", level = LogLevel.DEBUG)
    }

    fun warn(message: String?, exception: Exception? = null, sourceClass: String? = null) {
        if (logLevel.ordinal < LogLevel.WARN.ordinal) {
            return
        }

        logOut(
            message.toString(), exception = exception, sourceClass = sourceClass, level = LogLevel.WARN
        )
    }

    fun debug(message: String?, sourceClass: String? = null) {
        if (logLevel.ordinal < LogLevel.DEBUG.ordinal) {
            return
        }

//        if (logger.isInfoEnabled) {
//            "Sending dbus method: ${this.joinToString(" ")}".let {
//                logger.info(it)
//            }
//        }


        logOut(message.toString(), level = LogLevel.DEBUG, sourceClass = sourceClass)

    }

    fun info(message: String?) {
        if (logLevel.ordinal < LogLevel.INFO.ordinal) {
            return
        }

        logOut(message.toString(), level = LogLevel.INFO)

    }

    fun trace(message: String?) {
        if (logLevel.ordinal < LogLevel.TRACE.ordinal) {
            return
        }

        logOut(message.toString(), level = LogLevel.TRACE)
    }


}