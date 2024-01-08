package com.undercurrent.legacy.routing
//
//import com.undercurrent.shared.utils.Util.isMacOs
//import com.undercurrent.shared.types.enums.Environment
//import com.undercurrent.shared.types.enums.Role
//import com.undercurrent.legacy.routing.RunConfig
//
//
////Todo: duplicate this and save as RoutingConfig.kt (ensure not tracked by git)
//object RoutingConfigTemplate {
//    const val defaultTestMobAddr = ""
//
//
//    fun fetchPaymentAdmins(): Pair<String, String> {
//        return if (RunConfig.isTestMode) {
//            Pair("+12623333333", "+12624444444")
//        } else {
//            //todo replace with real numbers
//            Pair("+12623333333", "+12624444444")
//        }
//    }
//
//    fun getAdminSmsNumbers(environment: Environment): HashMap<String, Role> {
//        return when (environment) {
//            Environment.TEST -> {
//                hashMapOf(
//                    "+12623333333" to Rloe.ADMIN,
//                )
//            }
//
//            //todo replace with real numbers
//            Environment.DEV -> {
//                if (isMacOs()) {
//                    hashMapOf(
//                    )
//                } else {
//                    hashMapOf(
//                    )
//                }
//            }
//
//            else -> {
//                hashMapOf(
//                )
//            }
//        }
//    }
//
//    @Deprecated("Make use of function in MobileCoinDefaultValues")
//    fun getMnemonic(environment: Environment): String {
//        return when (environment) {
//            else -> ""
//        }
//    }
//
////    fun getSmsRoutes(): ArrayList<MessageRouting.SmsRoute> {
////        var smsRouteArrayList = arrayListOf(
////            MessageRouting.SmsRoute("+19999999999", EnvMode.TEST, Rloe.ADMIN),
////            MessageRouting.SmsRoute("+18888888888", EnvMode.TEST, Rloe.VENDOR),
////            MessageRouting.SmsRoute("+17777777777", EnvMode.TEST, Rloe.CUSTOMER),
////        )
////
////        return smsRouteArrayList
////    }
//
//
//}