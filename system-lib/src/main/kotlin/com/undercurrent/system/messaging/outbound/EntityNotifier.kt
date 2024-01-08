package com.undercurrent.system.messaging.outbound

import com.undercurrent.system.repository.entities.User
import com.undercurrent.legacy.routing.RunConfig
import com.undercurrent.shared.HasEnvironment
import com.undercurrent.shared.HasUserEntity
import com.undercurrent.shared.HasUserRole
import com.undercurrent.shared.messages.CanNotifyByRole
import com.undercurrent.shared.messages.Notifiable
import com.undercurrent.shared.messages.RoutingProps
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.context.SessionContext

class EntityNotifier(
    private val startingEntity: HasUserEntity<User>,
    val sessionContext: SessionContext? = null,
    val routingProps: RoutingProps? = sessionContext?.routingProps ?: null,
    val envIn: Environment? = routingProps?.environment ?: null,
    val roleIn: AppRole
) : Notifiable, HasUserEntity<User>, CanNotifyByRole, HasUserRole, HasEnvironment {

    override val user: User by lazy {
        tx {
            startingEntity.user
        }
    }

    override val role: AppRole by lazy {
        roleIn
    }

    override val environment: Environment by lazy {
        envIn ?: sessionContext?.environment ?: RunConfig.environment
    }

    override fun notifyByRole(msg: String, role: AppRole) {
        sendNotify(
            user = user,
            role = role,
            environment = environment,
            msg = msg
        )
    }

    override fun notify(msg: String) {
        sendNotify(
            user = user,
            role = role,
            environment = environment,
            msg = msg
        )
    }

}