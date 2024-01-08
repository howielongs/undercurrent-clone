package com.undercurrent.legacy.commands.executables.currencyswaps


import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.executables.info.MyBalancesCmd
import com.undercurrent.legacy.commands.registry.BaseCommand
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectableEnum
import com.undercurrent.legacy.repository.entities.payments.UserCreditLedger
import com.undercurrent.legacy.service.UserBalanceChecker
import com.undercurrent.legacy.types.enums.CryptoType
import com.undercurrent.legacy.types.enums.TransactionMemo
import com.undercurrent.legacy.types.enums.currency.CurrencyLegacyInterface
import com.undercurrent.legacy.types.enums.currency.FiatType
import com.undercurrent.legacy.types.enums.status.LedgerEntryStatus
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.system.context.SessionContext
import java.math.BigDecimal

abstract class CurrencyInOutCmds(thisCommand: BaseCommand, sessionContext: SessionContext) :
    Executable(thisCommand, sessionContext) {

    lateinit var refType: CurrencyLegacyInterface
    lateinit var refValue: BigDecimal

    lateinit var userGives: CryptoType
    lateinit var userGets: CryptoType

    fun verb(): String {
        return when (thisCommand) {
            CmdRegistry.CASHOUT -> "cash out"
//            CmdRegistry.SWAP -> "swap"
            else -> ""
        }
    }

    suspend fun displayBalances(): Map<CurrencyLegacyInterface, BigDecimal>? {
        //todo display USD as well in MyBalances
        MyBalancesCmd(sessionContext).execute()

        return UserBalanceChecker(sessionContext.user).balances()
            .filter {
                it.key.canCashOut && it.value > BigDecimal(0)
            }.ifEmpty {
                //todo need to generate deposit addr for BTC
                "You have no balances to cash out. Please make or deposit some money first.".let {
                    sessionContext.interrupt(it)
                }
                return null
            }
        //todo should display balances with latest exchange rate as well
    }

    //todo display how much of each you have?
    //do bullets for this
    fun swappableCurrencies(): List<SelectableEnum> {
        return CryptoType.values()
            .filter { it.isSwappable }
            .sortedBy { it.priority }
            .map { SelectableEnum(it.selectableLineString(), it) }

    }

    fun amountInputOptions(vararg excludeTypes: CurrencyLegacyInterface): List<SelectableEnum> {
        return setOf(FiatType.USD, CryptoType.BTC, CryptoType.MOB)
            .filter { !excludeTypes.contains(it) }
            .sortedBy { it.priority }
            .map { SelectableEnum(it.selectableLineString(), it) }
    }

//    fun fetchCryptoAddrForType(user: User, cryptoType: CryptoType): CryptoAddress? {
//        return transaction {
//            user.cryptoAddresses
//                .firstOrNull { it.isNotExpired() && it.cryptoType == cryptoType }
//        }
//    }

//    fun freshBtcAddress(notify: Boolean = false): String? {
//        if (isBtcWalletRunning(notify)) {
//            return BitcoinWalletServices.getFreshSendAddress()
//        }
//        return null
//    }

//    fun isBtcWalletRunning(notify: Boolean = false): Boolean {
//        return if (!BitcoinWalletServices.isWalletRunning()) {
//            if (notify) {
//                "Bitcoin wallet has not yet been initialized. Please try again in a few minutes.".let {
//                    sessionPair.interrupt(
//                        it,
//                        Rloe.VENDOR
//                    )
//                }
//                Admins.notifyError("BTC wallet is not yet up. Attempting to start...")
//            }
//            BitcoinWalletServices.startWallet()
//            false
//        } else {
//            true
//        }
//    }

    suspend fun promptTestAmountOut(
        currencyInterface: CurrencyLegacyInterface = CryptoType.BTC,
        amount: String = "-0.0001"
    ): Boolean {
        if (currencyInterface == CryptoType.BTC) {
            UserInput.promptYesNo(
                confirmText = "Do you wish to send a test ${currencyInterface.abbrev()} amount to your address first?\n\n" +
                        //todo display payments address in this step
                        "A small amount will be sent to the payments address you added to your account. " +
                        "To review and change, use ${CmdRegistry.ADDWALLET.upper()}${PressAgent.yesNoOptions()}",
                sessionContext = sessionContext,
                yesText = "Test amount of $amount ${currencyInterface.abbrev()} sent. Please check your wallet. Transaction may take a few minutes to arrive.",
                noText = "Skipping test send."
            ).let {
                if (it) {
                    //todo update user's entry in CreditLedger with SENT
                    UserCreditLedger.save(
                        userIn = sessionContext.user,
                        roleIn = sessionContext.role,
                        invoiceIn = null,
                        amountIn = BigDecimal(amount),
                        currencyInterfaceIn = currencyInterface,
                        statusIn = LedgerEntryStatus.OUTBOX,
                        memoIn = TransactionMemo.CASHOUT.toString(),
                    )
                    return true
                }
            }
        }
        return false
    }

}