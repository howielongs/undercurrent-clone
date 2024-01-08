package com.undercurrent.legacy.repository.entities.payments


import com.undercurrent.legacy.repository.abstractions.BaseEventsEntity
import com.undercurrent.legacy.repository.abstractions.BaseEventsTable
import com.undercurrent.legacy.types.enums.CryptoType
import com.undercurrent.legacy.types.enums.currency.CurrencyLegacyInterface
import com.undercurrent.legacy.types.string.BulletString
import com.undercurrent.legacy.utils.UtilLegacy
import com.undercurrent.shared.messages.CanNotifyCreated
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.VARCHAR_SIZE
import com.undercurrent.system.messaging.outbound.notifyAdmins
import com.undercurrent.system.repository.entities.User
import org.bitcoinj.core.Transaction
import org.bitcoinj.wallet.Wallet
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

object BtcReceivedEvents {
    fun save(amountStr: String, txStringIn: String): Entity? {
        if (txStringIn.contains("USER_PAYMENT")) {
            notifyAdmins("Received USER_PAYMENT BTC event: $amountStr\n\n$txStringIn")
            return null
        }
        return transaction {
            Entity.new {
                amount = amountStr.split(" ")[0]
                type = CryptoType.BTC.name
                raw = txStringIn
            }
        }
    }

    fun save(wallet: Wallet?, tx: Transaction): Entity? {
        return save(tx.getValueSentToMe(wallet).toFriendlyString(), tx.toString())
    }


    object Table : BaseEventsTable("bitcoin_receive_events") {
        val amount = varchar("amount", VARCHAR_SIZE)
        var receivingAddressStr = varchar("rec_addr_str", VARCHAR_SIZE).nullable()
    }

    class Entity(id: EntityID<Int>) : BaseEventsEntity(id, Table), CanNotifyCreated {
        companion object : RootEntityCompanion0<Entity>(Table)

        //todo figure out how to get this to ExchangeRate (or just share with Invoice)
        var amount by Table.amount
        var receivingAddressStr by Table.receivingAddressStr

        fun matchAndAddToLedger(): User? {
            return updateToMatchedDepositAddress()?.let { depositAddr ->
                UserCreditLedger.saveReceivedAmount(depositAddr, transaction { amount })?.let {
                    this@Entity.expire()
                }
                transaction { depositAddr.user }
            }
        }

        private fun updateToMatchedDepositAddress(): DepositCryptoAddress? {
            return matchReceivingAddr(transaction { raw })?.let {
                transaction {
                    this@Entity.receivingAddressStr = it.address
                    it
                }
            } ?: null
        }

        private fun matchReceivingAddr(
            txString: String
        ): DepositCryptoAddress? {
            val outputsList = txString.split("out  ").drop(1)
            val outputsString = outputsList.joinToString("\n • ")

            //consider also allowing expired addresses (if user uses old address to send amount)
            val receivingAddrKeys =
                transaction {
                    //todo ensure to filter only for receive events that happened after receiver addr created
                    DepositCryptoAddress.all()
                        .sortedByDescending { it.id }
                }

            outputsList.forEach { lineHash ->
                //todo should only match with addresses created before this event
                val matches = receivingAddrKeys
                    .filter { it.isNotExpired() && lineHash.contains(it.address) }

                when (matches.count()) {
                    0 -> {
                        //continue parsing through
                    }

                    1 -> {
                        return matches.first()
                    }

                    else -> {
                        notifyAdmins("Multiple matches found for receiving address: $lineHash")
                        return matches.first()
                    }
                }
            }

            transaction {
                Log.error("Msg 1/2\n\nCould not parse receiving address for transaction outputs:\n\n$outputsString")
                Log.error("Msg 2/2\n\nFull transaction:\n\n$txString\n")
            }
            return null
        }


        private fun notifyAdminsBtcReceived(
            thisAmount: String,
            currencyInterface: CurrencyLegacyInterface = CryptoType.BTC
        ) {
            BulletString().apply {
                add("BTC Received")
                add("Amount", thisAmount + " ${currencyInterface.abbrev()}")
                add("Address", transaction { receivingAddressStr })
                line()
                add(transaction { raw })

                Log.debug(outString, sourceClass = BtcReceivedEvents::class.java.simpleName)
                notifyAdmins(outString, subject = "BTC_RECEIVED")
            }
        }


        override fun notifyCreated() {
            val thisAmount = transaction { amount }
            val roundedAmount = UtilLegacy.roundBigDecimal(BigDecimal(thisAmount), CryptoType.BTC).toString()

            notifyAdminsBtcReceived(roundedAmount)

            this.matchAndAddToLedger()?.let {
                it.notify(
                    "We just received $roundedAmount BTC from you!",
                    ShopRole.CUSTOMER
                )
            }
        }


    }
}
