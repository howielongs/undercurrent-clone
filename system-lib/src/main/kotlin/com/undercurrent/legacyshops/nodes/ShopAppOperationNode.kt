package com.undercurrent.legacyshops.nodes

import com.undercurrent.legacyshops.nodes.vendor_nodes.HasTypingIndicatorDelay
import com.undercurrent.prompting.nodes.interactive_nodes.OperationNode
import com.undercurrent.shared.repository.entities.SignalSms
import com.undercurrent.system.context.SystemContext
import com.undercurrent.system.service.dbus.TypingIndicatorSender
import kotlinx.coroutines.delay

interface AppOperationNode
abstract class ShopAppOperationNode(
    context: SystemContext
) : OperationNode<SystemContext>(context),
    AppOperationNode, HasTypingIndicatorDelay {
    //could add fetcher/interactor here

    private val outputGap = 1000L

    override suspend fun sendTypingIndicatorWithDelay() {
        if (!context.isTestMode()) {
            sendTypingIndicator(recipientHumanAddr = context.userSms)
            delay(outputGap)
        }
    }

    //todo see how this might better fit the interface
    fun sendTypingIndicator() {
        TypingIndicatorSender(
            humanRecipientAddr = context.userSms,
            shouldCancel = false,
            dbusProps = context.routingProps,
        ).send()
    }

    //todo see about doing this without the input param
    override fun sendTypingIndicator(recipientHumanAddr: SignalSms) {
        TypingIndicatorSender(
            humanRecipientAddr = recipientHumanAddr,
            shouldCancel = false,
            dbusProps = context.routingProps,
        ).send()
    }


}

