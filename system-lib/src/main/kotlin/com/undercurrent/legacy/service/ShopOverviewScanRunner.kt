package com.undercurrent.legacy.service

import com.undercurrent.legacy.commands.executables.scans.MatchBtcReceiveToUser
import com.undercurrent.legacy.commands.executables.scans.ScanForConfirmedOrders
import com.undercurrent.legacy.commands.executables.scans.ScanForFullyPaidOrders
import com.undercurrent.legacy.commands.executables.scans.ScanForOutboundPayments
import com.undercurrent.legacy.repository.entities.payments.MobReceivedEvents
import com.undercurrent.legacy.routing.RunConfig
import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.utils.Log
import com.undercurrent.system.context.DbusProps
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

object ShopOverviewScanRunner {
     val paymentNudgePeriodSeconds: Int = 60 // Default value, adjust as needed

    //todo pass env into here
    suspend fun scan(dbusProps: DbusProps) {
        try {
            Log.debug("PaymentNudge Seconds: $paymentNudgePeriodSeconds")
            Log.debug("MASTER SCAN: STARTING MASTER SCAN COROUTINE: $coroutineContext")

            if (RunConfig.environment != Environment.PROD) {
                MobReceivedEvents.runScans()
            }

            Log.debug("MASTER SCAN: Inside scan for PaymentNudge")

            coroutineScope {
                // Launch concurrent scans using `launch` here
                launch { ScanForConfirmedOrders.checkForConfirmedOrders() }
                launch { ScanForConfirmedOrders.autoConfirmEligibleOrders(dbusProps) }
                launch { MatchBtcReceiveToUser.migrateBtcReceivesToLedger() }
            }

            coroutineScope {
                // Launch more concurrent scans using `launch` here
                launch { ScanForFullyPaidOrders.checkForFullyPaidOrders() }
                launch { ScanForOutboundPayments.dispatchOutboundPayments(dbusProps) }
            }

            Log.debug("MASTER SCAN: FINISHING MASTER SCAN COROUTINE: $coroutineContext")
        } catch (e: Exception) {
            Log.fatal("MASTER SCAN: Failed on scans", e)
            println("MASTER SCAN: Failed on scans\n${e.stackTraceToString()}")
        }
    }
}
