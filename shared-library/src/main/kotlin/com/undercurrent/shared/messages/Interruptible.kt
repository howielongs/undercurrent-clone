package com.undercurrent.shared.messages

import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.types.enums.AppRole

interface UsesRoutingProps {
    val routingProps: RoutingProps
}

interface Inputtable {
    val inputter: UserInputProvider
}

interface InteractionProvider : Inputtable {
    override val inputter: UserInputProvider
    val interrupter: UserOutputProvider<InterrupterMessageEntity>
    val adminNotifier: (String, String, Environment) -> Unit
}


interface Interruptible {
    fun interrupt(msg: String)
}

interface Notifiable {
    fun notify(msg: String)
}

interface CanNotifyByRole : Notifiable {
    fun notifyByRole(msg: String, role: AppRole)
}

interface CanInterruptByRole : Interruptible {
    fun interruptByRole(msg: String, role: AppRole)
}

