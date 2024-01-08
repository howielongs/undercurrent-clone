package com.undercurrent.legacy.commands.executables.shopping.cancelations

import com.undercurrent.system.context.SystemContext
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.types.enums.CryptoType

class BtcPaymentInstructionsBuilder(
    val sessionContext: SystemContext
) {
    private fun orderAutoCancelText(): String {
        return """
            |Orders are automatically canceled by the system after 3 days of 
            |inactivity.
        """.trimMargin()
    }

    suspend fun build(): String? {
        val orders = OrderCancellationHandler.ordersAwaitingPayment(sessionContext.user)
        orders.firstOrNull()?.let { order ->
            order.cryptoType?.name?.let {
                if (it.lowercase() != CryptoType.STRIPE.name.lowercase()) {
                    return BtcPaymentInstructionsBuilder(sessionContext).buildInstructions(it)
                } else {
                    order.stripePaymentUrl()?.let { url ->
                        return stripeBuildInstructions(url)
                    }
                }
            }
        }
        return null
    }

    private fun stripeBuildInstructions(
        stripeUrl: String,
    ): String {
        return "TO PAY: " +
                "\n\n $stripeUrl" +
                "\n\nUse `${CmdRegistry.OPENORDERS.upper()}` to see details of your order(s)."
    }


    private fun buildInstructions(
        currencyName: String,
    ): String {
        return """
            |USE THE NEXT MESSAGES TO COMPLETE PAYMENT:
            |
            |
            |FIRST: COPY & PASTE payment address into $currencyName wallet app of your choice, 
            |such as Cash App or Coinbase.
            |
            |
            |SECOND: COPY & PASTE $currencyName into your wallet as the amount
            |to send (do not use USD).
            |
            |
            |${orderAutoCancelText()}
            |
            |Use `${CmdRegistry.OPENORDERS.upper()}` to see details of your order(s).
        """.trimMargin()
    }
}