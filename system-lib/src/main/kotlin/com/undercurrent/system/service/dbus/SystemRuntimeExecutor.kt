package com.undercurrent.system.service.dbus

import com.undercurrent.shared.types.enums.Environment

class SystemRuntimeExecutor(val environment: Environment) {
    fun executeRuntime(args: Array<String>) {
        if (environment != Environment.TEST) {
            Runtime.getRuntime().exec(args)
        }
    }
}