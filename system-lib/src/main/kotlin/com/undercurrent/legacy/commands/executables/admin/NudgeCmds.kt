package com.undercurrent.legacy.commands.executables.admin


import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.BaseCommand
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacyshops.repository.entities.shop_items.CartItems
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrders
import com.undercurrent.system.messaging.outbound.notifyAdmins
import com.undercurrent.legacy.types.enums.status.OrderStatus
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.shared.messages.CanSendToUserByRole
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.utils.ctx
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.context.SessionContext
import com.undercurrent.system.messaging.outbound.sendInterrupt
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.transactions.transaction


sealed class NudgeCmds(
    override val thisCommand: BaseCommand,
    sessionContext: SessionContext,
) : Executable(
    thisCommand = thisCommand,
    sessionContext = sessionContext,
)

data class NudgeCheckout(override val sessionContext: SessionContext) : NudgeCmds(
    CmdRegistry.NUDGECHECKOUT, sessionContext
), CanSendToUserByRole {

    private val interrupter by lazy {
        sessionContext.interrupter
    }

    override fun sendOutputByRole(msgBody: String, role: AppRole) {
        interrupter.sendOutputByRole(msgBody, role)
    }

    override suspend fun execute() {
        val scope = CoroutineScope(Dispatchers.IO + CoroutineName("NudgeCheckout"))
        val cartItems = CartItems.fetchWithoutOrder()

        //todo improve transaction wrapping in this
        cartItems.map { transaction { it.customerProfile } }
            .distinct()
            .apply {
                sendOutputByRole(
                    "Found ${count()} shop members with cart items",
                    ShopRole.ADMIN
                )

                forEach {
                    scope.launch {
                        var userId = 0
                        val thisUser = async {
                            ctx {
                                with(it.user) {
                                    userId = this.id.value
                                    this
                                }
                            }
                        }.await()

                        launch {
                            notifyAdmins(
                                "Prompting CHECKOUT for User #${userId}",
                            )
                            sendInterrupt(
                                thisUser,
                                ShopRole.CUSTOMER,
                                sessionContext.environment,
                                PressAgent.CartStrings.checkoutHint()
                            )
                        }
                    }
                }
            }
    }
}

data class NudgeConfirm(override val sessionContext: SessionContext) : NudgeCmds(
    CmdRegistry.NUDGECONFIRM, sessionContext
) {
    override suspend fun execute() {
        val submittedOrders = DeliveryOrders.byStatus(OrderStatus.SUBMITTED.name)

        if (submittedOrders.isEmpty()) {
            sessionContext.interrupt("No orders to confirm")
            return
        }

        submittedOrders.forEach {
            var outString = PressAgent.VendorStrings.nudgeConfirm(
                "(Order submitted ${it.ageNanoString})"
            )

            tx { it.customer?.shopVendor?.user }?.let { it1 ->
                sendInterrupt(
                    user = it1,
                    role = ShopRole.VENDOR,
                    environment = sessionContext.environment,
                    msg = outString
                )
            }
            notifyAdmins(
                "Prompting CONFIRM for " +
                        "\n\n$this\n\nText:\n$outString"
            )


        }

    }
}