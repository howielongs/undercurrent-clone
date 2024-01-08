package com.undercurrent.system.service.dbus

import com.undercurrent.prompting.messaging.DbusMethod
import com.undercurrent.shared.repository.entities.SignalSms

class DbusSendArrayBuilder(
) {
    private var arr: Array<String> = arrayOf()

    private fun newRoot(): Array<String> {
        arr = arrayOf(
            "dbus-send",
            "--session",
            "--type=method_call",
            "--print-reply",
            "--dest=org.asamk.Signal",
        )
        return arr
    }

    fun newRoot(
        fullDbusPath: String,
        dbusMethod: DbusMethod,
        signalSms: SignalSms
    ): Array<String> {
        newRoot()
        addFullDbusPath(fullDbusPath)
        add(dbusMethod)
        return add(signalSms)
    }


    fun newRoot(
        fullDbusPath: String,
        dbusMethod: DbusMethod,
        thirdString: String
    ): Array<String> {
        newRoot()
        addFullDbusPath(fullDbusPath)
        add(dbusMethod)
        return add(thirdString)
    }

    fun <T> addArr(arg: T): Array<String> {
        val argValue = when (arg) {
            is String -> "array:string:\"${arg}\""
            is Long -> "array:int64:${arg}"
            else -> ""
        }

        arr += arrayOf(
            argValue,
        )
        return arr
    }

    fun addDbusUnderlineExtension(extension: String) {
        arr += arrayOf(
            "/org/asamk/Signal/$extension",
        )
    }

    fun addFullDbusPath(dbusPath: String) {
        val thisPath = "/$dbusPath"
        arr += arrayOf(
            thisPath.replace("//", "/"),
        )
    }

    fun <T> add(arg: T): Array<String> {
        val argValue = when (arg) {
            is String -> "string:\"${arg}\""
            is SignalSms -> "string:\"${arg.value}\""
            is Boolean -> "boolean:${arg}"
            is Int -> "int32:${arg}"
            is DbusMethod -> "org.asamk.Signal.${arg.methodName}"
//            is DbusObjectUrlProvider -> arg.buildDbusObjUrl()
            else -> ""
        }

        arr += arrayOf(
            argValue,
        )
        return arr
    }

}