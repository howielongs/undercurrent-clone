package com.undercurrent.shared.messages

import com.undercurrent.shared.CanCheckIfTestMode
import com.undercurrent.shared.HasEnvironment
import com.undercurrent.shared.repository.entities.BotSms
import com.undercurrent.shared.repository.entities.UserEntity
import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.types.enums.AppRole

interface CanFetchAdminUsers<U : UserEntity> {
    fun fetchAdminUsers(): List<U>
}

interface CanSendToUser<T : OutboundMessageEntity> {
    fun sendOutput(msgBody: String): T?
}

interface CanSendToUserByRole {
    fun sendOutputByRole(msgBody: String, role: AppRole)
}

interface UserOutputtable<T : OutboundMessageEntity> : CanSendToUser<T> {
    val outputProvider: UserOutputProvider<T>
}

interface UserOutputProvider<T : OutboundMessageEntity> : CanSendToUser<T>, CanSendToUserByRole
//interface UserRoleOutputProvider<T : OutboundMessageEntity> : UserOutputProvider<T>, CanSendToUserByRole

interface StringWrapper {
    val value: String
}

interface RoutingProps : HasEnvironment, CanCheckIfTestMode {
    override val environment: Environment
    val role: AppRole
    fun toBotSms(): BotSms
    fun toPath(): StringWrapper
}

abstract class RoleBasedOutputProvider<T : OutboundMessageEntity, R : RoutingProps>(
    val routingProps: R,
) : UserOutputProvider<T>
