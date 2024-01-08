package com.undercurrent.legacy.service.crypto.mobilecoin.requests

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.undercurrent.legacy.service.crypto.mobilecoin.MobService
import java.math.BigDecimal

sealed interface JsonRequestInterface {
    suspend fun run(): JsonObject?
}

abstract class MobileCoinApiRequest(
    methodStr: String,
) :
    MobJsonRequestBuilder(methodStr = methodStr),
    JsonRequestInterface {
    override suspend fun run(): JsonObject? {
        return MobService().req(build())
    }

}

data class ImportAccount(
    private val mnemonic: MobMnemonicText = MobileCoinDefaultValues.mnemonic(),
    private val name: MobAcctNameText = MobileCoinDefaultValues.name(),
) : MobileCoinApiRequest(
    methodStr = "import_account",
) {
    override val builderBodyFunc: (obj: JsonObject) -> JsonObject
        get() = { obj: JsonObject ->
            obj.apply {
                add("params", JsonObject().apply {
                    addProperty("mnemonic", mnemonic.cleanedValue)
                    addProperty("key_derivation_version", "2")
                    addProperty("name", name.cleanedValue)
                })
            }
        }
}


data class CheckB58Type(private val b58Code: String) :
    MobileCoinApiRequest(methodStr = "check_b58_type") {
    override val builderBodyFunc: (obj: JsonObject) -> JsonObject
        get() = { obj: JsonObject ->
            obj.apply {
                add("params", JsonObject().apply {
                    addProperty("b58_code", b58Code)
                })
            }
        }
}

data class VerifyAddress(private val address: String) :
    MobileCoinApiRequest(methodStr = "verify_address") {
    override val builderBodyFunc: (obj: JsonObject) -> JsonObject
        get() = { obj: JsonObject ->
            obj.apply {
                add("params", JsonObject().apply {
                    addProperty("address", address)
                })
            }
        }
}

object GetAllAccounts : MobileCoinApiRequest(methodStr = "get_all_accounts") {
    override val builderBodyFunc: (obj: JsonObject) -> JsonObject
        get() = { obj: JsonObject ->
            obj
        }
}

data class BuildAndSubmitTx(
    private val accountId: String,
    private val recipientAddress: String,
    private val pMobValue: BigDecimal
) : MobileCoinApiRequest(
    methodStr = "build_and_submit_transaction",
) {
    override val builderBodyFunc: (obj: JsonObject) -> JsonObject
        get() = { obj: JsonObject ->
            obj.apply {
                add("params", JsonObject().apply {
                    addProperty("account_id", accountId)
                    addProperty("recipient_public_address", recipientAddress)
                    addProperty("value_pmob", pMobValue.toLong().toString())
                })
            }
        }
}


data class CheckReceiverReceiptStatus(
    private val recipientAddress: String,
    private val receiverReceiptJson: JsonElement,
) : MobileCoinApiRequest(
    methodStr = "check_receiver_receipt_status",
) {
    override val builderBodyFunc: (obj: JsonObject) -> JsonObject
        get() = { obj: JsonObject ->
            obj.apply {
                add("params", JsonObject().apply {
                    addProperty("address", recipientAddress)
                    add("receiver_receipt", receiverReceiptJson)
                })
            }
        }
}