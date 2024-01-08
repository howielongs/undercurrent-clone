package com.undercurrent.system.context

import com.undercurrent.system.repository.entities.User
import com.undercurrent.legacy.repository.schema.toIdRoleStr
import com.undercurrent.shared.ProtoContext
import com.undercurrent.shared.messages.*
import com.undercurrent.shared.repository.entities.SignalSms
import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.messaging.inbound.InboundMessageFetcher
import com.undercurrent.system.messaging.outbound.AdminsNotifyProvider
import com.undercurrent.system.messaging.outbound.Interrupter

typealias SystemContext = SessionContext

class SessionContext(
    override val user: User,
    override val routingProps: RoutingProps,
    override val inputter: UserInputProvider = InboundMessageFetcher(user, routingProps),
    override val interrupter: UserOutputProvider<InterrupterMessageEntity> = Interrupter(
        user, routingProps as DbusProps
    ),
    override val adminNotifier: (String, String, Environment) -> Unit = { msg, subject, env ->
        AdminsNotifyProvider(
            envIn = env, subject = subject
        ).sendOutput(msg)
    },
) : ProtoContext<User>, Interruptible {

    override fun isTestMode(): Boolean {
        return routingProps.isTestMode()
    }

    @Deprecated("Use interrupter.sendOutput(msg) instead")
    override fun interrupt(msg: String) {
        interrupter.sendOutput(msg)
    }

    override val role: AppRole by lazy {
        routingProps.role
    }

    override val environment by lazy {
        routingProps.environment
    }

    override fun toString(): String {
        return toIdRoleStr(user, routingProps.role)
    }

    override val userId: Int by lazy {
        tx { user.id.value }
    }

    override val userSms: SignalSms by lazy {
        user.userSms
    }


}