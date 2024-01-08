package com.undercurrent.swaps.views

import com.undercurrent.legacyswaps.types.SupportedSwapCurrency

object SwapCustomerStrings {
    val welcomeStr: String = """Welcome to Undercurrent’s Swap-bot!""".trimMargin()

    val infoStr: String = """|With our service, you can send Bitcoin (BTC) 
        |to address [UCbtc address User 1] at any time and 
        |receive Mobilecoin (MOB) directly in this chat.""".trimMargin()

    val info2Str: String = """|Once you upload your Bitcoin (BTC) wallet, 
        |you can send Mobilecoin (MOB) directly in this chat at 
        |any time to receive Bitcoin (BTC) to your wallet.""".trimMargin()


    object AddWalletPrompts {

        val addWalletPrompt: String = """|In order to swap crypto, you must add BTC and MOB
        |wallet addresses. Would you like to proceed?""".trimMargin()

        fun inputAddressPrompt(type: SupportedSwapCurrency): String {
            return "Please input address to your $type wallet:"
        }

        val walletAnytimePrompt: String = "Okay, you can add a wallet at any time. "

        fun addressConfirm(addressStr: String): String {
            return """|You entered:
                |
                |$addressStr
                |
                |Add address?""".trimMargin()
        }

        fun addAddressSuccess(type: SupportedSwapCurrency): String {
            return """|Success! We've added your ${type.abbrev} address""".trimMargin()
        }

        fun noChangesMade(type: SupportedSwapCurrency): String {
            return """|No worries, no changes have been made to ${type.abbrev} address""".trimMargin()
        }
    }

    object PerformSwap {


        val topSwapPrompt: String = """|What would you like to swap?
        |– [A] BTC -> MOB
        |– [B] MOB -> BTC""".trimMargin()


        fun exchangeSummaryString(
            mobRateForEachBtc: String, usdRateForEachBtc: String
        ): String {
            return """|The current exchange rate is:
            |1 BTC = $mobRateForEachBtc MOB (${'$'}$usdRateForEachBtc USD)
            |
            |How would you like to proceed?
            |– [A] Provide an amount to swap
            |– [B] Calculate your exchange rate""".trimMargin()
        }


        //todo should give option for viewing as satoshi vs btc decimals
        fun howMuchToReceive(type: SupportedSwapCurrency): String {
            return """How much ${type.abbrev} would you like to receive?"""
        }

        //todo should give option for viewing as satoshi vs btc decimals
        fun howMuchToSend(type: SupportedSwapCurrency): String {
            return """How much ${type.abbrev} would you like to send?"""
        }

        //todo will need builders for this
        //todo pull out vars
        /**
         * """|Your exchange rate is:
         *             |0.00199310 BTC = 100 MOB (${'$'}54.54 USD)
         *             |
         *             |
         *             |Swap Details:
         *             |Bitcoin
         *             |From: 100
         *             |To: 50 BTC (${'$'}30 USD)
         *             |
         *             |Mobilecoin
         *             |From: 50
         *             |To: 100 MOB (${'$'}80 USD)
         *             |
         *             |Total USD Swapped
         *             |${'$'}52.14 USD
         *             |
         *             |Note: actual values may change due to market volatility."""
         */
        fun swapPreSummaryString(
            amtBtc: String,
            amtMob: String,
            amtUsd: String,
        ): String {

            return """|Your exchange rate is:
            |$amtBtc BTC = $amtMob MOB (${'$'}$amtUsd USD)
            |
            |
            |${swapDetailsStr(amtBtc, amtMob, amtUsd)}""".trimMargin()
        }

        fun swapDetailsStr(
            amtBtc: String,
            amtMob: String,
            amtUsd: String,
        ): String {

            return """|Swap Details:
            |Bitcoin
            |From: 100
            |To: 50 BTC (${'$'}30 USD)
            |
            |Mobilecoin
            |From: 50
            |To: $amtMob MOB (${'$'}80 USD)
            |
            |Total USD Swapped
            |${'$'}$amtUsd USD
            |
            |Note: actual values may change due to market volatility.""".trimMargin()
        }


        val proceedWithSwapYesNo = """|Would you like to proceed with this swap?
        |– [A] Yes
        |– [B] No (cancel and start over)""".trimMargin()

        fun pleaseSendAmts(amtMob: String, amtUsd: String, mobAddr: String): String {
            return """|Please send $amtMob MOB (${'$'}$amtUsd USD) to the address below:
            |$mobAddr
            |
            |All you have to do is COPY & PASTE that address into your BTC wallet.
            |
            |Please send the amount within the next hour, or this swap will expire. """.trimMargin()
        }

        val swapSuccess = "Your swap was a success!"

        val performAnother = """|Would you like to perform another swap?
        |– [A] Yes
        |– [B] No (cancel)""".trimMargin()

    }
}