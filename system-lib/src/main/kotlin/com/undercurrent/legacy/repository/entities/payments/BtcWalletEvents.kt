package com.undercurrent.legacy.repository.entities.payments

import com.undercurrent.system.messaging.outbound.notifyAdmins
import com.undercurrent.legacy.types.enums.CryptoType
import com.undercurrent.legacy.utils.UtilLegacy
import com.undercurrent.system.types.WalletEventEntity
import com.undercurrent.system.types.WalletMemo
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.shared.messages.CanNotifyCreated
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.utils.VARCHAR_SIZE
import org.bitcoinj.wallet.Wallet
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal


class BtcWalletEvents : WalletEventEntity {

    override fun save(wallet: Wallet?, rawIn: String?, memoIn: WalletMemo?) {
        transaction {
            Entity.new {
                balanceSat = wallet?.balance?.toSat() ?: 0L
                memo = memoIn?.name
                raw = rawIn
            }
        }
    }

    object Table : ExposedTableWithStatus2("bitcoin_wallet_events") {
        var balanceSat = long("balance_sat").clientDefault { 0L }
        val raw = varchar("raw", VARCHAR_SIZE).nullable()
        val memo = varchar("memo", VARCHAR_SIZE).nullable()

        
    }

    class Entity(id: EntityID<Int>) : ExposedEntityWithStatus2(id, Table), CanNotifyCreated {
        companion object : RootEntityCompanion0<Entity>(Table)

        var balanceSat by Table.balanceSat
        var raw by Table.raw
        var memo by Table.memo

        override fun notifyCreated() {
            notifyAdmins(this.toString(), "WALLET_SNAPSHOT")
        }

        override fun toString(): String {
            val usdToBtcRate = CryptoType.BTC.getFiatToCryptoExchangeRate() ?: return "Could not parse exchange rate"

            //todo may need to fix up if 0L balance keeps getting returned
            return """|${UtilLegacy.isoToDatetime(createdDate.toString())}
                |USD->BTC: $$usdToBtcRate
            |Balance: $balanceSat sat ($${UtilLegacy.atomicToFiat(BigDecimal(balanceSat), usdToBtcRate)})
        """.trimMargin()
        }

    }

}
