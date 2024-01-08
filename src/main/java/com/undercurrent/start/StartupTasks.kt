package com.undercurrent.start

import com.undercurrent.system.messaging.outbound.notifyAdmins
import com.undercurrent.legacy.repository.entities.system.ping.Pinger
import com.undercurrent.legacy.routing.RoutingConfig
import com.undercurrent.legacy.routing.RunConfig.MESSAGE_DISPATCH_SPACING_MS
import com.undercurrent.legacy.service.ShopOverviewScanRunner
import com.undercurrent.legacy.service.crypto.BitcoinWalletServices
import com.undercurrent.legacy.system_start.ScheduledCoroutineJob
import com.undercurrent.legacy.utils.encryption.SystemUserManagement
import com.undercurrent.shared.types.SubjectHeader
import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.Log
import com.undercurrent.system.context.DbusProps
import com.undercurrent.system.service.csv_import.ZipCodeCsvImporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

internal class StartupTasks(
    private val dbusProps: DbusProps,
    private val disableBtcWalletUsage: Boolean = false,
    private val delaySec: Int
) {
    suspend fun launchStartupTasks() {
        coroutineScope {
            launch(Dispatchers.IO) {
                ScheduledCoroutineJob().startCoroutineJob(
                    workerFunction = {
                        OutboxDispatcher(dbusProps).dispatch()
                    },
                    delayMs = delaySec.toLong() * 1000L,
                    periodMs = MESSAGE_DISPATCH_SPACING_MS
                )
            }

            launch(Dispatchers.IO) {
                if (dbusProps.role == ShopRole.ADMIN) {
                    val adminSmsNumbers = RoutingConfig.getAdminSmsNumbers(dbusProps.environment).keys.toList()
                    //todo need to beef this up to include checks for removed admins as well
                    with(SystemUserManagement()) {
                        adminSmsNumbers.forEach {
                            addDefaultUser(
                                smsToSet = it,
                                roleToSet = ShopRole.ADMIN,
                            )
                        }
                    }
                }
            }

            launch {
                if (dbusProps.role == ShopRole.VENDOR && !disableBtcWalletUsage) {
                    when (dbusProps.environment) {
                        Environment.PROD -> {
                            Log.info("Running LIVE transactions (Bitcoin mainnet)") //, "LIVE_BTC")
                        }

                        else -> {
                            notifyAdmins("Running TEST transactions (Bitcoin testnet)", "TEST_BTC")
                        }
                    }

                    BitcoinWalletServices.startWallet(dbusProps.environment)

//                Pinger().pingAllAdmins()
//                    sendFormattedOutput(
//                        messageBody = "Wallet started. Carry on with your life.",
//                        subject = SubjectHeader.BROADCAST,
//                        emojiStatus = Emoji.SUCCESS
//                    )
//                    sendFormattedOutput(
//                        messageBody = "Wallet started. Carry on with your life.",
//                        subject = SubjectHeader.BROADCAST,
//                        emojiStatus = Emoji.SUCCESS
//                    )


                    //todo this is being received by Vendors
                    notifyAdmins("Wallet started. Carry on with your life.", SubjectHeader.BROADCAST.name)

                    //display balance of wallet
                }
            }


            launch(Dispatchers.IO) {
                if (dbusProps.role == ShopRole.VENDOR) {
                    val shopScans = ScheduledCoroutineJob()

                    shopScans.startCoroutineJob(workerFunction = {
                        ShopOverviewScanRunner.scan(dbusProps)
                    }, delayMs = 10_000 + delaySec.toLong() * 1000L, periodMs = 120_000)
                }
            }

            //handle ZipCode import
            launch(Dispatchers.IO) {
                if (dbusProps.role == ShopRole.VENDOR) {
//                    "Starting zip import from Main...".let {
//                        Log.info(it)
//                        println(it)
//                    }

                    ZipCodeCsvImporter().runImport()

                    //todo provide some admin notifs

//                    "Done with zip import from Main...".let {
//                        Log.info(it)
//                        println(it)
//                    }
                }
            }

            launch(Dispatchers.IO) {
                if (dbusProps.role == ShopRole.CUSTOMER) {
                    Pinger(dbusProps).pingAllAdmins()
                }
            }

            launch(Dispatchers.IO) {
                if (dbusProps.role == ShopRole.VENDOR) {
                    // dispatcher to fetch exchange rates every few minutes
                }
            }
        }
    }


}