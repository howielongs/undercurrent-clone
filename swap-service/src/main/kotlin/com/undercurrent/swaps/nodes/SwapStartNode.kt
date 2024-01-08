package com.undercurrent.swaps.nodes

//interface CanFetchBtcAddr<T : ProtoUserEntity> {
//    fun fetchBtcAddr(user: T): BtcAddress?
//}
//
//interface CanFetchMobAddr<T : ProtoUserEntity> {
//    fun fetchMobAddr(user: T): String?
//}
//
//class SwapStartNode<U : User>(
//    context: SystemContext,
//    notifyAdminsFunc: (String) -> Unit,
//    val btcAddrFetcher: (U) -> BtcAddress? = { null },
//    val mobAddrFetcher: (U) -> String? = { null },
//) : BaseCommandNode(
//    context,
//    notifyAdminsFunc = notifyAdminsFunc
//), CanFetchBtcAddr<U> {
//    override suspend fun next(): TreeNode? {
//        return SwapWelcomeNode()
//    }
//
//    var thisBtcAddr: BtcAddress? = null
//    var thisMobAddr: String? = null
//
//    override fun fetchBtcAddr(user: U): BtcAddress? {
//        return thisBtcAddr ?: btcAddrFetcher(user)
//    }
//
//    /**
//     * Send welcome message for entering swap interface
//     *
//     * Check each wallet type and prompt for adding if not present
//     * Resume back to this node after each wallet is added
//     *  (take into consideration if AddWallet operation is canceled, etc.)
//     */
//    inner class SwapWelcomeNode : InteractiveNode(inputProvider, outputProvider) {
//        override suspend fun next(): TreeNode? {
//            sendOutput(SwapCustomerStrings.welcomeStr)
//
//
//
//
//
//            return null
//        }
//    }
//
////    inner class PromptAddBtcWallet : OutputNode(outputProvider) {
////
////        override suspend fun next(): TreeNode? {
////            //todo need to resume nodes after this
////            thisBtcAddr = fetchBtcAddr(thisUser)
////
////            //todo need to limit the number of times this occurs
////            if(thisBtcAddr == null) {
////                //todo need to be able to cancel out of this
////
////                PromptAddBtcWallet().executeAll()
////                thisBtcAddr = fetchBtcAddr(thisUser)
////            }
////
////
////            //todo also do check for MOB            //todo change up return node
////            return null
////        }
////
////    }
//
//    inner class PromptAddMobWallet : OutputNode(outputProvider) {
//        override suspend fun next(): TreeNode? {
//            TODO("Not yet implemented")
//        }
//    }
//
//    inner class AddCryptoWalletNode() : OutputNode(outputProvider) {
//        override suspend fun next(): TreeNode? {
//            TODO("Not yet implemented")
//        }
//    }
//
//}