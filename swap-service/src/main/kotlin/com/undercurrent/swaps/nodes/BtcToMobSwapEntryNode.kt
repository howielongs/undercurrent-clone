package com.undercurrent.swaps.nodes

//class BtcToMobSwapEntryNode(
//    private val inputProvider: UserInputProvider,
//    outputProvider: UserOutputProvider<InterrupterMessageEntity>,
//) : OutputNode(outputProvider) {
//    override suspend fun next(): TreeNode? {
//        outputProvider.sendOutput("Welcome to Undercurrent`s Swap Bot!")
//        return SwapDirectionSelectionNode(inputProvider, outputProvider)
//    }
//}
//
//data class SwapSelection<T>(
//    val index: T,
//    val srcCurrency: SupportedSwapCurrency,
//    val destCurrency: SupportedSwapCurrency,
//    val lineText: String,
//)
//
//class SwapDirectionSelectionNode(
//    inputProvider: UserInputProvider,
//    outputProvider: UserOutputProvider<InterrupterMessageEntity>,
//) : MenuSelectNode<String>(inputProvider, outputProvider) {
//    lateinit var srcCurrency: SupportedSwapCurrency
//    lateinit var destCurrency: SupportedSwapCurrency
//
//    override suspend fun next(): TreeNode? {
//
//        //todo need to populate with items for each currency and what they can swap to
//
//        //header text, followed by string of list (abc)
//        fetchInput("What would you like to swap?")
//
//
//        //todo get these from the fetch
//
//        return null
////        return CurrencyStatusOverviewNode(
////            srcCurrency,
////            destCurrency,
////            inputProvider,
////            outputProvider,
////        )
//    }
//
//}

/**
 * The current exchange rate is:
 * 1 BTC = 48,465 MOB ($26,298.20 USD)
 *
 * How would you like to proceed?
 * – [A] Provide an amount to swap
 * – [B] Calculate your exchange rate
 */
//class CurrencyStatusOverviewNode(
//    val srcCurrency: SupportedSwapCurrency,
//    val destCurrency: SupportedSwapCurrency,
//    inputProvider: UserInputProvider,
//    outputProvider: UserOutputProvider<InterrupterMessageEntity>,
//    val exchangeRateProvider: ExchangeRateProvider,
//) : MenuSelectNode<String>(inputProvider, outputProvider) {
//    override suspend fun next(): TreeNode? {
//        sendOutput("The current exchange rate is:")
//        return null
//    }
//
//}