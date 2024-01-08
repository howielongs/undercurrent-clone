package com.undercurrent.legacy.repository.entities.payments


import com.undercurrent.legacy.repository.abstractions.BaseEventsTable
import com.undercurrent.system.repository.entities.Admins
import com.undercurrent.legacy.repository.repository_service.payments.crypto.DefaultMobAccount
import com.undercurrent.legacy.routing.RunConfig
import com.undercurrent.shared.repository.database.loadDatabase
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.VARCHAR_SIZE
import com.undercurrent.shared.utils.tx
import org.jetbrains.exposed.sql.transactions.transaction

object MobReceivedEvents : BaseEventsTable("mobilecoin_received_events") {
    val mobAmount = varchar("mob_amount", VARCHAR_SIZE).clientDefault { "0" }
    val pmobAmount = long("pmob_amount").clientDefault { 0L }

    val senderSms = varchar("sender_sms", VARCHAR_SIZE)

    //probably don't need all of these, but helpful to keep for now and just pare down later
    val recipientSms = varchar("recipient_sms", VARCHAR_SIZE).nullable()
    val receiptBytes = varchar("receipt_bytes", VARCHAR_SIZE).nullable()
    val receiptB64 = varchar("receipt_b64", VARCHAR_SIZE).nullable()
    val receiptNote = varchar("receipt_note", VARCHAR_SIZE).nullable()

    val receiptJson = varchar("receipt_json", VARCHAR_SIZE).nullable()
    val receiverRequestJson = varchar("receiver_request_json", VARCHAR_SIZE).nullable()

    // allow checks in round-robin to prevent being deadlocked by a particularly slow transaction
    suspend fun runScans() {
        Log.debug("Running scans for MOB receive events")
        try {
            val thisDefaultMobAccount = DefaultMobAccount().load() ?: run {
                //perhaps throw an exception instead
                "Error fetching or importing default MOB account".let { Admins.notifyError(it) }
                return
            }

            tx { unprocessedMobReceiptEvents }.forEach {
                it.parseReceiptAndAddToLedger(thisDefaultMobAccount)
            }
            Log.debug("Done running scans for MOB (for now)")
        } catch (e: Exception) {
            Admins.notifyError("Caught exception while running MobReceivedEvents.runScans()", e)
        }
    }


    fun save(
        senderSmsIn: String = "",
        recipientSmsIn: String = "",
        receiptBytesIn: String = "",
        receiptB64In: String = "",
        receiptNoteIn: String = "",
        receiverRequestJsonIn: String = "",
    ): MobReceivedEvent? {
        loadDatabase(RunConfig.environment, false)
        return tx {
            MobReceivedEvent.new {
                senderSms = senderSmsIn
                recipientSms = recipientSmsIn
                receiptBytes = receiptBytesIn
                receiptB64 = receiptB64In
                receiptNote = receiptNoteIn
                receiverRequestJson = receiverRequestJsonIn
            }
        }
    }


    private var unprocessedMobReceiptEvents: List<MobReceivedEvent> = listOf()
        get() {
            return transaction {
                MobReceivedEvent.find {
                    pmobAmount eq 0L
                }
            }.filter { it.isNotExpired() }.toList()
        }
}