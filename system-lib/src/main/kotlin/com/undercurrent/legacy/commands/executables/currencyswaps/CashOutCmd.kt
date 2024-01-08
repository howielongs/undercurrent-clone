package com.undercurrent.legacy.commands.executables.currencyswaps




import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput
import com.undercurrent.legacy.repository.entities.payments.CryptoAddress
import com.undercurrent.legacy.repository.entities.payments.CryptoAddresses
import com.undercurrent.legacy.repository.entities.payments.UserCreditLedger
import com.undercurrent.system.repository.entities.Admins
import com.undercurrent.legacy.service.crypto.mobilecoin.MobileCoinSender
import com.undercurrent.legacy.types.enums.CryptoType
import com.undercurrent.legacy.types.enums.TransactionMemo
import com.undercurrent.legacy.types.enums.currency.CurrencyLegacyInterface
import com.undercurrent.legacy.types.enums.status.LedgerEntryStatus
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.shared.formatters.UserToIdString

import com.undercurrent.system.context.SessionContext
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

class CashOutCmd(sessionContext: SessionContext) : CurrencyInOutCmds(
    CmdRegistry.CASHOUT, sessionContext
) {
    /**
     * Select currency where user has a balance (run user.balances() and parse map)
     *
     * Prompt user for amount to withdraw (check wallet balance to see how much can be withdrawn)
     *
     * Update Ledger with "SENT" with User id
     *
     * Assert that 0 balance
     */
    override suspend fun execute() {
        displayBalances()?.let { balances ->
            balances.forEach {
                CryptoAddresses.byCurrencyTypeAndUser(sessionContext.user, it.key)?.let { addr ->
                    promptForCurrencyTypeBeforeCashout(type = it.key, amount = it.value, addr = addr)
                } ?: run {
                    val outStr =
                        "We could not find a wallet address for ${it.key.abbrev()}.\n\nUse ${CmdRegistry.ADDWALLET.upper()} to add " +
                                "an address and then try ${CmdRegistry.CASHOUT.upper()} again."

                    sessionContext.interrupt(outStr)
                    //todo notify about how there is no address for this type and to add one to cash out
                }
            }
        }
        //todo should check with which address types can be supported


    }


    //todo display outbound addresses and types
    //todo offer to send custom amounts
    private suspend fun promptForCurrencyTypeBeforeCashout(
        type: CurrencyLegacyInterface,
        amount: BigDecimal,
        addr: CryptoAddress
    ) {
        if (amount <= BigDecimal(0)) {
            Admins.notifyError(
                "${UserToIdString.toIdStr(sessionContext.user)} attempted to " +
                        "${verb()} with a negative amount (${type} ${amount})."
            )
            return
        }

        if (promptTestAmountOut()) {
            return
        }


        UserInput.promptYesNo(
            confirmText = "Do you wish to cash out your $amount ${type.abbrev()}?\n\n" +
                    "Amount will be sent to the payments address you added to your account: ${
                        transaction
                        { addr.address }
                    } \n\n" +
                    "To review and change, use ${CmdRegistry.ADDWALLET.upper()}${PressAgent.yesNoOptions()}",
            sessionContext = sessionContext,
            yesText = "Amount sent. Please check your wallet. Transaction may take a few minutes to arrive.",
            noText = "Nothing sent. Your balances are unchanged."
        ).let {
            if (it) {

                //todo if type is MOB, perhaps send from here and then set status to "SENT"


                //todo wrap this execution in coroutine/scope? -> see if it gets stuck
                val ledgerStatus = if (type == CryptoType.MOB) {
                    MobileCoinSender(
                        amountMob = amount.abs(),
                        recipientAddress = transaction { addr.address },
                        memo = TransactionMemo.CASHOUT.name,
                    ).send()
                    LedgerEntryStatus.SENT
                } else {
                    LedgerEntryStatus.OUTBOX
                }


                //todo update user's entry in CreditLedger with SENT
                UserCreditLedger.save(
                    userIn = sessionContext.user,
                    roleIn = sessionContext.role,
                    invoiceIn = null,
                    amountIn = amount.abs().multiply(BigDecimal(-1)),
                    currencyInterfaceIn = type,
                    statusIn = ledgerStatus,
                    memoIn = TransactionMemo.CASHOUT.toString(),
                )
            }
        }
    }


}