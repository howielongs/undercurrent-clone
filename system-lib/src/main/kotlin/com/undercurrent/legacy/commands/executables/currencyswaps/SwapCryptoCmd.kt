package com.undercurrent.legacy.commands.executables.currencyswaps


import com.undercurrent.legacy.commands.executables.info.MyBalancesCmd
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectedEnum
import com.undercurrent.legacy.repository.entities.payments.CryptoAddresses
import com.undercurrent.legacy.repository.entities.payments.LegacyExchangeRates
import com.undercurrent.legacy.repository.entities.payments.UserCreditLedger
import com.undercurrent.system.messaging.outbound.notifyAdmins
import com.undercurrent.legacy.routing.RunConfig
import com.undercurrent.legacy.types.enums.CryptoType
import com.undercurrent.legacy.types.enums.ResponseType
import com.undercurrent.legacy.types.enums.TransactionMemo
import com.undercurrent.legacy.types.enums.currency.CurrencyLegacyInterface
import com.undercurrent.legacy.types.enums.currency.FiatType
import com.undercurrent.legacy.types.enums.status.LedgerEntryStatus
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.system.context.SessionContext
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.math.RoundingMode

class SwapCryptoCmd(sessionContext: SessionContext) :
    CurrencyInOutCmds(CmdRegistry.SWAP, sessionContext) {

    override suspend fun execute() {
//        swapCrypto()

        if (RunConfig.environment == Environment.PROD || RunConfig.environment == Environment.QA) {
            "Operation under construction. We appreciate your patience.".let {
                sessionContext.interrupt(it)
                notifyAdmins("$sessionContext attempted swap command in Prod")
            }
            return
        }

//        if (!isBtcWalletRunning(true)) {
//            return
//        }
//
//        if (swapByCashout()) {
//            CashOutCmd(sessionPair).execute()
//        }
    }

    private suspend fun swapByCashout(): Boolean {
        displayBalances()?.let { balances ->
            balances
                .filter { it.key.isSwappable }
                .forEach {
                    if (promptForSwap(it.key, it.value)) {
                        return true
                    }
                }
        }
        return false
    }

    private suspend fun promptForSwap(
        giveType: CurrencyLegacyInterface,
        giveAmount: BigDecimal,
    ): Boolean {
        if (giveType !is CryptoType) {
            return false
        }

        val getType: CryptoType = if (giveType == CryptoType.BTC) {
            CryptoType.MOB
        } else {
            CryptoType.BTC
        }

        CryptoAddresses.byCurrencyTypeAndUser(sessionContext.user, getType)?.let { recAddr ->

            val giveTypeExchange = LegacyExchangeRates.Table.save(giveType)
            val getTypeExchange = LegacyExchangeRates.Table.save(getType)

            val getAmount = transaction {
                giveAmount
                    .multiply(BigDecimal(giveTypeExchange?.fiatToCryptoAtomicExchangeRate))
                    .divide(
                        BigDecimal(getTypeExchange?.fiatToCryptoAtomicExchangeRate),
                        getType.roundingScale,
                        RoundingMode.HALF_UP
                    )
            }

            UserInput.promptYesNo(
                "Would you like to swap your $giveAmount ${giveType.abbrev()} to $getAmount ${getType.abbrev()}?",
                sessionContext
            )?.let {
                if (it) {
                    //todo write to ledger
                    // subtract first type, add otherAmt to ledger
                    //update status on ledger? Just send directly to cashout at the end

                    //need to provide instructions for uploading more (BTC address)
                    //todo command needs to be able to generate BTC address (right now only happens at confirm)

                    UserCreditLedger.save(
                        sessionContext.user,
                        sessionContext.role,
                        null,
                        giveAmount.abs().multiply(BigDecimal(-1)),
                        giveType,
                        statusIn = LedgerEntryStatus.AVAILABLE,
                        memoIn = TransactionMemo.SWAP.name,
                        exchangeRateFromUsdIn = giveTypeExchange,
                    )

                    UserCreditLedger.save(
                        sessionContext.user,
                        sessionContext.role,
                        null,
                        getAmount.abs(),
                        getType,
                        statusIn = LedgerEntryStatus.AVAILABLE,
                        memoIn = TransactionMemo.SWAP.name,
                        exchangeRateFromUsdIn = getTypeExchange,
                    )

                    return true
                }
            }

        } ?: run {
            val outStr =
                "We could not find a wallet address for ${getType.abbrev()}.\n\nUse ${CmdRegistry.ADDWALLET.upper()} to add " +
                        "an address and then try ${CmdRegistry.CASHOUT.upper()} again."

            sessionContext.interrupt(outStr)
            //todo notify about how there is no address for this type and to add one to cash out

        }

        return false
    }


    private suspend fun promptForValue(userGives: CryptoType, userGets: CryptoType) {

        UserInput.selectAnOption(
            sessionContext,
            amountInputOptions(),
            headerText = "Which currency would you like to calculate your exchange from?",
            headlineText = CmdRegistry.SWAP.name.uppercase(),
        )?.let { selectedRefType ->
            if (selectedRefType is SelectedEnum && selectedRefType.enum is CurrencyLegacyInterface) {
                refType = selectedRefType.enum

                val valType = if (refType is FiatType) {
                    ResponseType.CURRENCY
                } else {
                    ResponseType.DECIMAL
                }

                UserInput.promptUser(
                    promptString = "Enter value of ${refType.abbrev()} to calculate exchange from",
                    sessionContext,
                    validationType = valType,
                )?.let {
                    refValue = BigDecimal(it)

                    //todo calculate exchange here
                    //allow user to see other values

                    //todo will need to format these as well
                    var giveValue: BigDecimal = BigDecimal(0)
                    var getValue: BigDecimal = BigDecimal(0)

                    var fiatValue: BigDecimal = BigDecimal(0)
                    var fiatType: CurrencyLegacyInterface = FiatType.USD


                    //todo much better formatting needed
                    val confirmText = """
                        |Your swap:
                        | • IN:     $giveValue ${userGives.abbrev()}
                        | • OUT:    $getValue ${userGets.abbrev()}
                        |
                        | • USD:    $fiatValue ${fiatType.abbrev()}
                        |
                        | Continue swap?
                        | ${PressAgent.yesNoOptions()}
                    """.trimMargin()

                }


            }


        }

    }

    private suspend fun swapCrypto() {
        MyBalancesCmd(sessionContext).execute() //todo display USD (fiat) values for each currency

//        UserInput.selectAnOption(
//            sessionPair,
//            swappableCurrencies(),
//            headerText = "Which currency would you like to swap from?",
//            headlineText = CmdRegistry.SWAP.name.uppercase(),
//        )?.let { selectedCurrencyFrom ->
//            if (selectedCurrencyFrom is SelectedEnum && selectedCurrencyFrom.enum is CryptoType) {
//                userGives = selectedCurrencyFrom.enum
//                userGets = when (userGives) {
//                    CryptoType.BTC -> CryptoType.MOB
//                    CryptoType.MOB -> CryptoType.BTC
//                    else -> {
//                        Log.error("Invalid currency selection")
//                        return
//                    }
//                }
//
//                //todo add a confirm step here
//                sessionPair.interrupt("Your selected swap: ${userGives.abbrev()} -> ${userGets.abbrev()}")
//
//                if (userGets == CryptoType.BTC || userGives == CryptoType.BTC) {
//                    if (!isBtcWalletRunning(true)) {
//                        return
//                    }
//                }
//
//
////                val receivingAddr = fetchCryptoAddrForType(sessionPair.user, userGets) ?: run {
////                    "You will need to add a ${userGets.fullName} receiving address. Use ${CmdRegistry.ADDWALLET.commandTag()} to do this.".let {
////                        sessionPair.interrupt(it)
////                    }
////                    return
////                }
//
//
//
//                promptForValue(userGives, userGets)
//            }
//        }

    }
}