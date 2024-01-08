package com.undercurrent.system.messaging.outbound

import com.undercurrent.system.repository.entities.User
import com.undercurrent.legacy.repository.entities.system.ping.AdminUserFetcher
import com.undercurrent.prompting.components.EmojiSymbol
import com.undercurrent.shared.messages.CanFetchAdminUsers
import com.undercurrent.shared.messages.CanSendToUser
import com.undercurrent.shared.messages.NotificationMessageEntity
import com.undercurrent.shared.messages.RoutingProps
import com.undercurrent.shared.types.SubjectHeader
import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.system.context.DbusProps

// with Pinger, extract and use Notifiers for each fetched user
class AdminsNotifyProvider(
    envIn: Environment,
    private val subject: String = SubjectHeader.BROADCAST.name,
    private val emoji: EmojiSymbol = EmojiSymbol.LOUDSPEAKER,
    private val routingProps: RoutingProps = DbusProps(roleIn = ShopRole.ADMIN, envIn = envIn),
) : CanFetchAdminUsers<User>, CanSendToUser<NotificationMessageEntity> {

    private fun sendForAllAdminUsers(msgBody: String): List<NotificationMessageEntity?> {
        val msgOut = "${emoji.parse()}\t" + "[$subject:ADMIN]\n\n" + "$msgBody"

        return fetchAdminUsers().map {
            Notifier(
                userEntity = it,
                routingProps = routingProps
            ).sendOutput(msgOut)
        }
    }

    override fun sendOutput(msgBody: String): NotificationMessageEntity? {
        return sendForAllAdminUsers(msgBody).lastOrNull()
    }

    private val adminUserFetcher: CanFetchAdminUsers<User> by lazy {
        AdminUserFetcher()
    }

    override fun fetchAdminUsers(): List<User> {
        return adminUserFetcher.fetchAdminUsers()
    }

}