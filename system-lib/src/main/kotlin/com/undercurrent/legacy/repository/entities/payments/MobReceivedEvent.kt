package com.undercurrent.legacy.repository.entities.payments

import com.google.gson.JsonParser
import com.undercurrent.legacy.repository.abstractions.BaseEventsEntity
import com.undercurrent.system.repository.entities.Admins
import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.repository.entities.Users
import com.undercurrent.system.messaging.outbound.notifyAdmins
import com.undercurrent.legacy.repository.repository_service.payments.crypto.DefaultMobAccount
import com.undercurrent.legacy.service.crypto.mobilecoin.requests.CheckReceiverReceiptStatus
import com.undercurrent.legacy.types.enums.CryptoType
import com.undercurrent.legacy.types.enums.TransactionMemo
import com.undercurrent.legacy.types.enums.status.LedgerEntryStatus
import com.undercurrent.legacy.utils.UtilLegacy
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.entities.SignalSms
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.tx
import kotlinx.coroutines.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

class MobReceivedEvent(id: EntityID<Int>) : BaseEventsEntity(id, MobReceivedEvents) {
    val mobReceiptCheckDelayMs: Long = 30000L

    //todo these should probably be CryptoAmount references
    var mobAmount by MobReceivedEvents.mobAmount
    var pmobAmount by MobReceivedEvents.pmobAmount

    //todo may want to adjust this to handle UUID as well
    var senderSms by MobReceivedEvents.senderSms
    var recipientSms by MobReceivedEvents.recipientSms

    var receiptBytes by MobReceivedEvents.receiptBytes
    var receiptB64 by MobReceivedEvents.receiptB64
    var receiptNote by MobReceivedEvents.receiptNote
    var receiptJson by MobReceivedEvents.receiptJson

    var receiverRequestJson by MobReceivedEvents.receiverRequestJson

    companion object : RootEntityCompanion0<MobReceivedEvent>(MobReceivedEvents) {
        // allow checks in round-robin to prevent being deadlocked by a particularly slow transaction
        suspend fun runScans() {
            Log.debug("Running scans for MOB receive events")

            val thisDefaultMobAccount = DefaultMobAccount().load() ?: run {
                //perhaps throw an exception instead
                "Error fetching or importing default MOB account".let { Admins.notifyError(it) }
                return
            }

            tx { unprocessedMobReceiptEvents }.forEach {
                it.parseReceiptAndAddToLedger(thisDefaultMobAccount)
            }
            Log.debug("Done running scans for MOB (for now)")
        }


//        fun save(
//            senderSmsIn: String = "",
//            recipientSmsIn: String = "",
//            receiptBytesIn: String = "",
//            receiptB64In: String = "",
//            receiptNoteIn: String = "",
//            receiverRequestJsonIn: String = "",
//        ): MobReceivedEvent? {
//            loadDatabase(RunConfig.environment, false)
//            return transaction {
//                MobReceivedEvent.new {
//                    senderSms = senderSmsIn
//                    recipientSms = recipientSmsIn
//                    receiptBytes = receiptBytesIn
//                    receiptB64 = receiptB64In
//                    receiptNote = receiptNoteIn
//                    receiverRequestJson = receiverRequestJsonIn
//                }
//            }
//        }


        private var unprocessedMobReceiptEvents: List<MobReceivedEvent> = listOf()
            get() {
                return transaction {
                    MobReceivedEvent.find {
                        MobReceivedEvents.pmobAmount eq 0L
                    }
                }.filter { it.isNotExpired() }.toList()
            }
    }

    //todo consider adding this linkage to the db itself
    private var senderUser: User? = null
        get() = transaction { Users.fetchBySms(SignalSms(senderSms)) }

    @Deprecated("Clean this up with SOLID approach")
    suspend fun parseReceiptAndAddToLedger(defaultMobAccount: MobAccounts.Entity) {
        try {

            //todo come back and clean this up
            val receiverRequestJsonElement =
                JsonParser.parseString(tx { receiverRequestJson })

            var recipientAddress = tx { defaultMobAccount.mainAddress }

            val scope = CoroutineScope(
                Dispatchers.IO +
                        CoroutineName("request payment pmob")
            )

            val job = scope.async(start = CoroutineStart.LAZY) {
                //todo add better timeout here
                val maxAttempts = 120
                var isSuccess = false
                var attempts = 0
                var txStatus = ""
                while (attempts < maxAttempts && !isSuccess) {
                    attempts++

                    Log.debug(
                        "Attempt #${attempts} " +
                                "Requesting data for transaction: " +
                                "${tx { receiptNote }}\n\n${receiverRequestJsonElement.toString()}\n\n"
                    )

                    CheckReceiverReceiptStatus(
                        recipientAddress = recipientAddress,
                        receiverReceiptJson = receiverRequestJsonElement
                    ).run()?.let {
                        Log.debug("CheckReceiverReceiptStatus response:\n${it}\n\n")
                        txStatus = it["receipt_transaction_status"].toString()
                        if (txStatus.contains("TransactionSuccess")) {
                            Log.debug("Found TransactionSuccess!")
                            isSuccess = true
                            return@async it
                        } else {
                            delay(mobReceiptCheckDelayMs)
                        }
                    }
                }
                return@async null
            }

            job.await()?.let {
                try {
                    //todo possible to pull these await blocks into own methods?
                    val amountPmob: BigDecimal = it["txo"].asJsonObject["value_pmob"].asBigDecimal
                    val amountMob: BigDecimal = amountPmob.scaleByPowerOfTen(-12)
                    val amountMobRounded = UtilLegacy.round(amountMob)

                    tx {
                        this@MobReceivedEvent.mobAmount = amountMob.toString()
                        this@MobReceivedEvent.pmobAmount = amountPmob.toLong()
                    }

                    notifyAdmins("Received amount: $amountMob MOB")
                    job.cancelAndJoin()

                    tx { senderUser }?.let { user ->
                        user.notify("Your payment of $amountMobRounded MOB was received!", ShopRole.CUSTOMER)

                        UserCreditLedger.save(
                            userIn = user,
                            roleIn = ShopRole.CUSTOMER,
                            invoiceIn = null,
                            amountIn = BigDecimal(tx { mobAmount }),
                            currencyInterfaceIn = CryptoType.MOB,
                            statusIn = LedgerEntryStatus.RECEIVED,
                            memoIn = TransactionMemo.FROM_CUSTOMER.name,
                        )
                    }

                    this@MobReceivedEvent.expire()
                    Log.debug("Got amount: $amountMob MOB")
                } catch (e: Exception) {
                    Admins.notifyError(
                        "Caught exception inside MobReceivedEvent.parseReceiptAndAddToLedger() job await block",
                        e
                    )
                }
            } ?: run {
                Log.debug("MOB tx check timed out, retrying shortly...")
            }
        } catch (e: Exception) {
            Admins.notifyError("Caught exception while running MobReceivedEvent.parseReceiptAndAddToLedger()", e)
        }
    }

}