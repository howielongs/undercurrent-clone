package com.undercurrent.legacy.commands.executables.scans


import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.BaseCommand
import com.undercurrent.legacy.commands.registry.CmdRegistry.*
import com.undercurrent.legacy.repository.entities.payments.BtcReceivedEvents
import com.undercurrent.legacy.repository.entities.payments.CryptoAddress
import com.undercurrent.legacy.repository.entities.payments.CryptoAddresses
import com.undercurrent.legacy.repository.entities.payments.UserCreditLedger
import com.undercurrent.legacy.types.enums.status.LedgerEntryStatus
import com.undercurrent.legacy.types.enums.status.OrderStatus
import com.undercurrent.legacyshops.repository.entities.shop_orders.Invoice.Companion.fetchAwaitingPayments
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrder
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrders
import com.undercurrent.shared.formatters.UserToIdString
import com.undercurrent.shared.messages.RoutingProps
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.time.unexpiredExpr
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.context.DbusProps
import com.undercurrent.system.context.SessionContext
import com.undercurrent.system.messaging.outbound.sendInterrupt
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

abstract sealed class ScanCommands(
    override val thisCommand: BaseCommand,
    sessionContext: SessionContext,
) : Executable(thisCommand, sessionContext)

data class ScanForOutboundPayments(
    override val sessionContext: SessionContext,
) : ScanCommands(SCAN_FOR_OUTBOUND_PAYMENTS, sessionContext) {
    companion object {
        fun dispatchOutboundPayments(dbusProps: RoutingProps) {
            transaction {
                UserCreditLedger.Entity.find {
                    UserCreditLedger.Table.status eq LedgerEntryStatus.OUTBOX.name
                }.filter { it.isNotExpired() }.toList()
            }.forEach { ledgerEntity ->
                transaction {
                    ledgerEntity.status = LedgerEntryStatus.SENT.name
                }

                //consider making this a coroutine
                val type = transaction { ledgerEntity.currencyType }
                val user = transaction { ledgerEntity.user }
                val amount = transaction { ledgerEntity.amount.replace("-", "") }
                val address = transaction {
                    CryptoAddress.find {
                        CryptoAddresses.user eq user.id and (CryptoAddresses.type eq type)
                    }.first { it.isNotExpired() }.address
                }

                "Attempting to send $amount $type to:\n" +
                        "$address".let { msg ->
                            Log.debug("${UserToIdString.toIdStr(user)} " + msg)
                            sendInterrupt(user, dbusProps.role, dbusProps.environment, msg)
                        }

                val outMsg = if (ledgerEntity.send(type, address, dbusProps)) {
                    "SUCCESS! Sent $amount $type to:\n$address "
                } else {
                    transaction {
                        ledgerEntity.status = LedgerEntryStatus.OUTBOX.name
                    }
                    "ALAS! Unable to send $amount $type.\n\nWill retry sending to:\n$address "
                }

                Log.debug("${UserToIdString.toIdStr(user)} " + outMsg)
                sendInterrupt(
                    user = user,
                    role = dbusProps.role,
                    environment = dbusProps.environment,
                    msg = outMsg
                )
            }
        }
    }

    override suspend fun execute() {
        dispatchOutboundPayments(sessionContext.routingProps)
    }
}

data class ScanForConfirmedOrders(
    override val sessionContext: SessionContext,
) : ScanCommands(SCAN_FOR_CONFIRMED_ORDERS, sessionContext) {
    companion object {
        suspend fun checkForConfirmedOrders() {
            Log.debug("Checking for confirmed orders in loop")
            tx {
                DeliveryOrder.find {
                    DeliveryOrders.status eq OrderStatus.CONFIRMED.name
                }.map { it.invoice }.toList()
            }.filter { it.isNotExpired() }.forEach {
                //consider making this a coroutine
                it.setUpPaymentReceivers()
            }
            Log.debug("Exiting method 'checkForConfirmedOrders()'")

        }

        suspend fun autoConfirmEligibleOrders(dbusProps: DbusProps) {
            DeliveryOrders.autoConfirmEligibleOrders().forEach {
                //insert default message?
                it.confirmOrder(dbusProps = dbusProps)
            }
            Log.debug("SCANS Done running auto-confirm operation")

        }

    }

    override suspend fun execute() {
        checkForConfirmedOrders()
    }
}

data class ScanForFullyPaidOrders(
    override val sessionContext: SessionContext,
) : ScanCommands(SCAN_FOR_FULLY_PAID_ORDERS, sessionContext) {
    override suspend fun execute() {
        checkForFullyPaidOrders(true)
    }

    companion object {
        suspend fun checkForFullyPaidOrders(forceIncompletePaymentNudge: Boolean = false) {
            fetchAwaitingPayments().forEach {
                Log.debug("SCANS Checking and notifying of payment status for awaiting payment invoice...")
                it.checkAndNotifyPaymentStatus(forceIncompletePaymentNudge)
            }
        }
    }
}

data class MatchBtcReceiveToUser(
    override val sessionContext: SessionContext,
) : ScanCommands(MATCH_BTC_RECEIVE_TO_USER, sessionContext) {
    override suspend fun execute() {
        migrateBtcReceivesToLedger()
    }

    companion object {
        fun migrateBtcReceivesToLedger() {
            Log.debug("Querying for unmatchedBTCReceived transactions...")
            val unmatchedBtcReceives: List<BtcReceivedEvents.Entity> = tx {
                BtcReceivedEvents.Entity.find {
                    BtcReceivedEvents.Table.receivingAddressStr eq null and unexpiredExpr(BtcReceivedEvents.Table)
                }.toList()
            }.filter {
                Log.debug("Attempting to filter unmatchedBtcReceives list")
                it.isNotExpired()
            }

            Log.debug("Got unmatched BTC Receives (size: ${unmatchedBtcReceives.count()}")

            unmatchedBtcReceives.forEach { recEvent ->
                UserToIdString.toIdStr(recEvent, "BTC Receive Event")
                val idString = UserToIdString.toIdStr(recEvent, "BTC Receive Event")
                Log.debug("Attempting to match $idString to ledger")
                recEvent.matchAndAddToLedger()
            }
            Log.debug("Done checking for unmatchedBtcReceives")
        }
    }
}


