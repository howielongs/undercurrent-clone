package com.undercurrent.legacy.repository.entities.system.ping

import com.undercurrent.system.repository.entities.AdminProfile
import com.undercurrent.system.repository.entities.Admins
import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.service.VersionFetcher
import com.undercurrent.legacy.service.user_role_services.UserRoleFetcher
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.shared.messages.CanFetchAdminUsers
import com.undercurrent.shared.messages.RoutingProps
import com.undercurrent.shared.abstractions.CanFetchForAnyAppRole
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.Util
import com.undercurrent.shared.utils.time.unexpiredExpr
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.context.DbusProps
import com.undercurrent.system.messaging.outbound.Notifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AdminUserFetcher : CanFetchAdminUsers<User> {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    //todo this is a bit smelly (extra processing here)
    private val fetchAdminUsersFunc: () -> List<User> = {
        //todo SMELLY
        AdminProfile.find { unexpiredExpr(Admins) }
            .toList()
            .filter { it.user != null && it.user.isNotExpired() }
            .mapNotNull { it.user }
    }

    //todo SMELLY
    override fun fetchAdminUsers(): List<User> {
        with(tx { fetchAdminUsersFunc() }) {
            if (logger.isInfoEnabled) {
                logger.info("Fetched $size admin users for notify $this")
            }
            return this
        }
    }
}

//todo make it so any message can be passed in
// should pass in stringBuilder to then support emojis, etc.
class Pinger(
    val dbusProps: RoutingProps,
    private val roleFetcher: CanFetchForAnyAppRole<User> = UserRoleFetcher(),
    private val outputProvider: (User, RoutingProps, String) -> Unit = { user: User, dbusProps: RoutingProps, msg: String ->
        Notifier(user, dbusProps).sendOutput(msg)
    }
) : CanPerformPing,
    CanFetchForAnyAppRole<User>,
    CanFetchAdminUsers<User> {

    val environment by lazy {
        dbusProps.environment
    }

    private val adminUserFetcher: CanFetchAdminUsers<User> by lazy {
        AdminUserFetcher()
    }

    override suspend fun pingAllAdmins() {
        //todo unify with AdminsNotifyProvider
        pingUsers(fetchAdminUsers())
    }

    override suspend fun pingUsers(users: List<User>) = coroutineScope {
        users.forEach { user ->
            launch(Dispatchers.IO) {
                pingAllRolesForUser(user)
            }
        }
    }

    override suspend fun pingAllRolesForUser(user: User) = coroutineScope {
        val roles = fetchRoles(user)
        val channelDirectoryStr = fetchChannelDirectoryString(roles)
        val hasAdminProfile = roles.contains(ShopRole.ADMIN)

        roles.forEach { role ->
            launch {
                val strToSend = buildString(role, hasAdminProfile, channelDirectoryStr)

                val newDbusProps = DbusProps(
                    roleIn = role,
                    envIn = environment
                )

                outputProvider(
                    user,
                    newDbusProps,
                    strToSend
                )
            }
        }
    }

    override fun fetchAdminUsers(): List<User> {
        return adminUserFetcher.fetchAdminUsers()
    }

    override suspend fun fetchRoles(entity: User): Set<AppRole> {
        return roleFetcher.fetchRoles(entity)
    }

    private val roleUrlMap = mutableMapOf<AppRole, String>().withDefault { role ->
        " • ${role.name} -> ${getRoutingUrl(environment, role)}\n"
    }

    private fun fetchChannelDirectoryString(roles: Set<AppRole>): String {
        return buildString {
            append("Your channels:\n")
            roles.forEach { role ->
                append(roleUrlMap.getValue(role))
            }
        }
    }

    private fun buildString(
        role: AppRole,
        hasAdminProfile: Boolean,
        channelDirectoryStr: String,
    ): String {
        return buildString {
            append("This is your $role channel")
            if (hasAdminProfile || environment != Environment.PROD) {
                append(systemInfoStr)
            }
            append("\n\n")
            append(channelDirectoryStr)
        }
    }


    //consider adding this to DbusProps
    private fun getRoutingUrl(
        environment: Environment, role: AppRole
    ): String {
        return "https://signal.me/#p/${PressAgent.Routing.getRoutingSms(environment, role)}"
    }


    private val systemInfoStr: String by lazy {
        "\n$isMacString\nDeployed version: ${environment.name} ${VersionFetcher.fetchVersion()}\n"
    }

    private val isMacString by lazy {
        if (Util.isMacOs()) {
            "(running locally on Mac OS)"
        } else {
            ""
        }
    }


}