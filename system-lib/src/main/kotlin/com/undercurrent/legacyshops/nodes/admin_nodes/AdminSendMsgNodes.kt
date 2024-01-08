package com.undercurrent.legacyshops.nodes.admin_nodes

import com.undercurrent.legacy.service.fetchers.UserFetcherById
import com.undercurrent.legacy.service.user_role_services.UserRoleFetcher
import com.undercurrent.prompting.nodes.interactive_nodes.OperationNode
import com.undercurrent.shared.abstractions.CanFetchForAnyAppRole
import com.undercurrent.shared.experimental.command_handling.GlobalCommand
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.view.treenodes.TreeNode
import com.undercurrent.system.context.SystemContext
import com.undercurrent.system.messaging.outbound.Notifier
import com.undercurrent.system.repository.entities.User

interface SendMsgInterface {
    suspend fun userIdInputNode(): TreeNode?
    suspend fun isValidUserIdNode(userId: Int): TreeNode?
    suspend fun selectUserRoleNode(user: User): TreeNode?
    suspend fun msgInputNode(user: User, role: AppRole): TreeNode?
    suspend fun sendMsgToUserAtRoleNode(msg: String, user: User, role: AppRole): TreeNode?
    suspend fun decideToSendAnotherMsgNode(user: User, role: AppRole): TreeNode?

}

class AdminSendMsgNodes(
    context: SystemContext,
    private val roleFetcher: UserRoleFetcher = UserRoleFetcher(),
) : OperationNode<SystemContext>(
    context = context,
), CanFetchForAnyAppRole<User>, SendMsgInterface {

    override suspend fun fetchRoles(entity: User): Set<AppRole> {
        return roleFetcher.fetchRoles(entity)
    }

    override suspend fun next(): TreeNode? {
        return userIdInputNode()
    }

    override suspend fun userIdInputNode(): TreeNode? {
        return inttextInputNode("Enter ID for user to send message to:",
            ifSuccess = { isValidUserIdNode(it) })
    }

    override suspend fun isValidUserIdNode(userId: Int): TreeNode? {
        val user = UserFetcherById.fetch(userId)

        return if (user != null) {
            selectUserRoleNode(user)
        } else {
            sendOutput("Invalid user ID.")
            userIdInputNode()
        }
    }


    override suspend fun selectUserRoleNode(user: User): TreeNode? {
        val roles = fetchRoles(user).toList()

        if (roles.size == 1 && roles.firstOrNull() != null) {
            return msgInputNode(user, roles.first())
        }

        return menuSelectNode(
            options = roles.map { it.toString() },
            "Select a role to send a direct message to:",
            ifSuccess = { i, _ ->
                val selectedRole = roles[i]
                msgInputNode(user, selectedRole)
            },
            ifFail = { userIdInputNode() },
        )
    }


    override suspend fun msgInputNode(user: User, role: AppRole): TreeNode? {
        return textInputNode(
            "Enter message to send to ${user.uid} at role ${role.name}:",
            ifSuccess = { sendMessageConfirmNode(it, user, role) })
    }

    suspend fun sendMessageConfirmNode(msg: String, user: User, role: AppRole): TreeNode? {
        //add option to revise message before sending

        return yesNoNode(
            "Send message to ${user.uid} at role ${role.name}:\n\n$msg",
            ifYes = { sendMsgToUserAtRoleNode(msg, user, role) },
            ifNo = {
                sendOutput("Cancelled sending message to ${user.uid} at role ${role.name}.")
                null
            },
        )
    }

    override suspend fun sendMsgToUserAtRoleNode(msg: String, user: User, role: AppRole): TreeNode? {
        val notifier = Notifier(
            userEntity = user,
            role = role,
            environment = context.environment
        )

        val msgToSend = """
            |Message from Admin:
            |
            |`$msg`
            |
            |Use ${GlobalCommand.FEEDBACK.handle().uppercase()} to reply.
        """.trimMargin()

        notifier.sendOutput(msgToSend)
        sendOutput("Sent message to ${user.uid} at role ${role.name}:\n\n$msgToSend")

        return decideToSendAnotherMsgNode(user, role)
    }

    override suspend fun decideToSendAnotherMsgNode(user: User, role: AppRole): TreeNode? {
        return yesNoNode(
            "Send another message to ${user.uid} at role ${role.name}?",
            ifYes = { msgInputNode(user, role) },
            ifNo = { null },
        )
    }


}