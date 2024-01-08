package com.undercurrent.shared

import com.undercurrent.shared.messages.InteractionProvider
import com.undercurrent.shared.messages.UsesRoutingProps
import com.undercurrent.shared.repository.entities.SignalSms
import com.undercurrent.shared.repository.entities.UserEntity
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.types.enums.Environment

interface UserWithSms {
    val userSms: SignalSms
}

interface OperationContext : InteractionProvider

interface HasUserEntity<T : UserEntity> {
    val user: T
}

interface HasUserEntityId<T : UserEntity> {
    val userId: Int
}

//todo add interface for Dbus typingindicator/expiry variables
interface ProtoContext<T : UserEntity> : HasUserEntity<T>,
    HasUserEntityId<T>, OperationContext, UserWithSms,
    UsesRoutingProps, HasUserRole,
    CanCheckIfTestMode

interface HasUserRole {
    val role: AppRole
}

interface CanCheckIfTestMode : HasEnvironment {
    fun isTestMode(): Boolean
}

interface HasEnvironment {
    val environment: Environment
}
