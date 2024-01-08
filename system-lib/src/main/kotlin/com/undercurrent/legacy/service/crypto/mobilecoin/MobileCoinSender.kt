package com.undercurrent.legacy.service.crypto.mobilecoin


import com.undercurrent.system.messaging.outbound.notifyAdmins
import com.undercurrent.legacy.repository.repository_service.payments.crypto.DefaultMobAccount
import com.undercurrent.legacy.service.crypto.mobilecoin.requests.BuildAndSubmitTx
import com.undercurrent.shared.utils.Log
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.math.RoundingMode

interface CryptoSenderInterface {
    suspend fun send()
}

class MobileCoinSender(
    val amountMob: BigDecimal,
    val recipientAddress: String,
    val memo: String
) : CryptoSenderInterface {

    override suspend fun send() {
        val amountPmob = amountMob.scaleByPowerOfTen(12).divide(BigDecimal(1), 0, RoundingMode.HALF_UP)
        Log.debug("Sending $amountPmob pMOB to address\n\t$recipientAddress\n\tMemo: $memo")

        DefaultMobAccount().load()?.let { defaultAcct ->
            BuildAndSubmitTx(
                accountId = transaction { defaultAcct.accountId },
                recipientAddress = recipientAddress,
                pMobValue = amountPmob
            ).run()?.let {
                saveEvent()
                //todo notify other users when this occurs
                notifyRoles()
                return
            }
        }
        Log.error("Returning null from MobAccounts.fetchAccountByName")
    }

    private fun saveEvent() {
        //todo impl this

//                MobWalletEventOld(
//                    reqJson = txJson.toString(),
//                    responseJson = response.toString(),
//                    pMobAmount = amountPmob.toString(),
//                    recipientAddress = recipientAddress,
//                    userId = user?.uid ?: 0,
//                    currency = CryptoType.MOB.name,
//                    status = "",
//                    memo = memo,
//                ).create()

    }

    private fun notifyRoles() {
        Log.debug("Job returned for $amountMob MOB to $recipientAddress")
        notifyAdmins("Forwarded $amountMob MOB to $recipientAddress")
        //todo impl this

        //                recipientUser?.let {
//                    recipientRole?.let { role ->
//                        when (role) {
//                            Rloe.VENDOR -> {
//                                it.notify(
//                                    "Success! Received $amountMob MOB from customer!\n\n(Go check your wallet...)",
//                                    Rloe.VENDOR
//                                )
//                            }
//
//                            Rloe.CUSTOMER -> {}
//                            Rloe.ADMIN -> {}
//                            else -> {}
//                        }
//                    }
//
//                }

    }

}