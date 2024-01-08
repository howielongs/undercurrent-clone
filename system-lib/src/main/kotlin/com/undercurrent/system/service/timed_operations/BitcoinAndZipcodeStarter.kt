package com.undercurrent.system.service.timed_operations

import com.undercurrent.legacy.service.crypto.BitcoinWalletServices
import com.undercurrent.shared.types.SubjectHeader
import com.undercurrent.prompting.components.EmojiSymbol
import com.undercurrent.shared.messages.InterrupterMessageEntity
import com.undercurrent.shared.messages.CanSendToUser
import com.undercurrent.shared.messages.UserOutputProvider
import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.newScope
import kotlinx.coroutines.*
import java.math.BigDecimal

@Deprecated("This needs major fixup before it can be used")
class TimedZipImport(
    environment: Environment,
    outputProvider: UserOutputProvider<InterrupterMessageEntity>,
) : TimedOperation(label = "Zip import",
    environment = environment,
    outputProvider = outputProvider,
    operationFunc = {
//        ZipCodeCsvImporter().runImport()
    })

@Deprecated("This needs major fixup before it can be used")
class TimedBtcStartup(
    environment: Environment,
    outputProvider: UserOutputProvider<InterrupterMessageEntity>,
) : TimedOperation(label = "Starting BTC wallet",
    environment = environment,
    outputProvider = outputProvider, operationFunc = {
        BitcoinWalletServices.startWallet(environment)
    })

interface CanSendFormattedOutput {
    fun sendFormattedOutput(messageBody: String, subject: SubjectHeader?, emojiStatus: EmojiSymbol?)
}

@Deprecated("Should separate this out")
class BitcoinAndZipcodeStarter(
    val environment: Environment,
    private val adminOutputProvider: UserOutputProvider<InterrupterMessageEntity>,
    val adminNotificationFormatter: (String, SubjectHeader?, EmojiSymbol?) -> String,
    val formattedOutputProvider: (
        String, SubjectHeader?, EmojiSymbol?
    ) -> Unit = { s1: String, s2: SubjectHeader?, e1: EmojiSymbol? ->
        adminOutputProvider.sendOutput(adminNotificationFormatter(s1, s2, e1))
    },
) : CanSendToUser<InterrupterMessageEntity>, CanSendFormattedOutput {

    override fun sendFormattedOutput(
        messageBody: String, subject: SubjectHeader?, emojiStatus: EmojiSymbol?
    ) {
        formattedOutputProvider(
            messageBody, subject ?: SubjectHeader.BROADCAST, emojiStatus ?: EmojiSymbol.WARNING
        )
    }

    override fun sendOutput(msgBody: String): InterrupterMessageEntity? {
        return adminOutputProvider.sendOutput(msgBody)
    }

    suspend fun start() {
        val scope = newScope("BitcoinAndZipcodeStarter")
        try {
            scope.launch {
                sendFormattedOutput(
                    "Starting Bitcoin wallet in the background. Please wait...",
                    SubjectHeader.START_WALLET,
                    EmojiSymbol.CLOCK130
                )

                //BitcoinWalletServices.startWallet(environment)
                TimedBtcStartup(environment = environment, outputProvider = adminOutputProvider).start()

                BitcoinWalletServices.getWallet()?.let { wallet ->
                    var balanceSat = BigDecimal(wallet.balance.toSat())

                    //todo should convert this into USD

                    "Current BTC balance: $balanceSat sat".let {
                        sendFormattedOutput(it, SubjectHeader.BROADCAST, EmojiSymbol.MONEYBAG)
                    }
                }

                sendFormattedOutput(
                    messageBody = "Wallet started. Carry on with your life.",
                    subject = SubjectHeader.BROADCAST,
                    emojiStatus = EmojiSymbol.SUCCESS
                )
            }

            scope.launch {
                //todo this is showing up multiple times...
                sendFormattedOutput(
                    "Importing zip codes from CSV in the background...", SubjectHeader.IMPORT, EmojiSymbol.CLOCK130
                )

                TimedZipImport(environment = environment, outputProvider = adminOutputProvider).start()
            }


        } catch (e: IllegalStateException) {
            Log.error("Error starting wallet", e, this::class.java.simpleName)
//            Admins.interrupt(
//                "Wallet unable to start. Try again soon.", SubjectHeader.START_WALLET, Emoji.WARNING
//            )
        }
    }


}