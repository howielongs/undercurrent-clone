package com.undercurrent.swaps.views.banker_views

import com.undercurrent.shared.SystemUserNew
import com.undercurrent.shared.types.enums.AppRole


/**
 * Start implementing the lifecycle from the swap-service module
 * Set up decoupled Exposed database
 *
 */
class SwapBotStart(
    val user: SystemUserNew,
    val role: AppRole,
    val environment: AppRole,
    val swapDbName: String = "swap-$environment.db"
    //more context here
) {
//    fun start() {
//        when (environment) {
//            DEV, QA, LIVE, LOCAL, TEST -> {
//                sessionPair.interrupt("Starting up swap interface...")
//
//            }
//
//            PROD -> {
//                Log.debug("Swap not ready for PROD yet.")
//                return
//            }
//        }
//
//
//    }

}