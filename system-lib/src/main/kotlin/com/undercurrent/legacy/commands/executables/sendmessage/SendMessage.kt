package com.undercurrent.legacy.commands.executables.sendmessage


import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectableEntity
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectableText
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectedEntity
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectedText
import com.undercurrent.legacy.service.user_role_services.UserRoleFetcher
import com.undercurrent.legacy.types.enums.ListIndexTypeOld
import com.undercurrent.legacy.utils.UtilLegacy
import com.undercurrent.shared.abstractions.CanFetchForAnyAppRole
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.utils.time.unexpiredExpr
import com.undercurrent.system.context.SessionContext
import com.undercurrent.system.messaging.outbound.notifyAdmins
import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.repository.entities.Users
import org.jetbrains.exposed.sql.transactions.transaction

// come back and clean this up
@Deprecated("Use swap_nodes instead")
class SendMessage(sessionContext: SessionContext) : Executable(
    CmdRegistry.SEND, sessionContext
), CanFetchForAnyAppRole<User> {

    override suspend fun fetchRoles(entity: User): Set<AppRole> {
        return UserRoleFetcher().fetchRoles(entity)
    }

    private fun unexpiredUsers(): List<User> {

        return transaction {
            User.find {
                unexpiredExpr(Users)
            }.toList()
        }

    }

    // would be useful to have a 'sendToVendor' cmd too
    // add option to list users or send directly to uid
    override suspend fun execute() {

        val result = UserInput.selectAnOption(
            sessionContext = sessionContext,
            options = unexpiredUsers().map { SelectableEntity(it) },
            indexType = ListIndexTypeOld.UID,
            headerText = "Select a user to send a direct message to:",
            headlineText = CmdRegistry.SEND.withSlash(),
            shouldPromptForInterruptCommands = true,
        ).let {
            if (it is SelectedEntity) {
                it
            } else {
                null
            }
        }


        result?.let { selectedUserEntity ->
            //todo should send to specific user role?
            with(selectedUserEntity.entity as User) {

                val roles = fetchRoles(this@with).toList()

                var selectedRole: AppRole? = if (roles.size == 1) {
                    //todo Issue: this isn't "SelectedText" type, so runs into issues on "is" check below
                    roles.first()
                } else {
                    UserInput.selectAnOption(
                        sessionContext,
                        options = roles.map { SelectableText(it.name) },
                        headerText = "Select which of User #${this.uid}'s roles to send to:",
                    )?.let { selectedRole ->
                        if (selectedRole is SelectedText) {
                            with(UtilLegacy.getRoleEnum(selectedRole.promptText)) {
                                sessionContext.interrupt("You selected ${selectedRole.promptText}")
                                this
                            }
                        } else {
                            null
                        }
                    } ?: null
                }

                selectedRole?.let { selectedRole ->
                    UserInput.promptAndConfirm(
                        promptString = "Message to send to User #${uid} ($role):",
                        sessionContext,
                        confirmTextHeader = "Message to send to User #${uid} ($role):",
                        confirmTextVerb = "Send",
                        noText = "Message not sent to user",
                    )?.let {
                        this.notify(
                            role = selectedRole,
                            msg = "Message from Admin:\n\n`$it`\n\n" +
                                    "Use ${CmdRegistry.FEEDBACK.withSlash()} to reply."
                        )
                        sessionContext.interrupt("Message sent to User #${this.uid}")
                        notifyAdmins(
                            "Message from Admin ($sessionContext) to User #${this.uid}:\n\n`$it`"
                        )
                        return
                    }
                    return
                } ?: run {
                    sessionContext.interrupt("Unable to find role for user selection")
                }
            }
        }
    }
}

