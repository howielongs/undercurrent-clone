package com.undercurrent.legacy.commands.executables.info


import com.undercurrent.shared.utils.Log
import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.system.context.SessionContext
import com.undercurrent.legacy.service.UserBalanceChecker
import com.undercurrent.legacy.types.enums.CryptoType
import java.math.BigDecimal

class MyBalancesCmd(sessionContext: SessionContext) : Executable(CmdRegistry.BALANCES, sessionContext) {
    override suspend fun execute() {
        //todo display each with latest value in USD/fiat type of choice
        var outString = "Balances:\n"
        with(UserBalanceChecker(sessionContext.user).balances()) {
            filter { it.value.toDouble() != BigDecimal(0).toDouble() || it.key == CryptoType.BTC }
                .forEach {
                    //todo output value in fiat (USD)
                    outString += "${it.key.selectableLineString()}: ${it.value}\n"
                }

            var hintStr = if (filter { it.value.toDouble() < BigDecimal(0).toDouble() }.toList().isNotEmpty()) {
                "\n\nUse ${CmdRegistry.OPENORDERS.upper()} for instructions on how to complete your payment"
            } else {
                ""
            }
            sessionContext.interrupt("$outString$hintStr")
            Log.debug("$sessionContext $outString$hintStr")

        }
    }
}