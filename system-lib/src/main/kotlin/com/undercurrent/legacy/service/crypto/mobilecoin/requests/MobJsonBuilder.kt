package com.undercurrent.legacy.service.crypto.mobilecoin.requests

import com.google.gson.JsonObject
import org.bouncycastle.util.encoders.Hex


data class ReceiverReceipt(
    private val pubKey: String,
    private val confirmation: String,
    private val receipt: com.undercurrent.legacy.types.protos.MobileCoinAPI.Receipt,
) : JsonRequestBuilderInterface {
    fun build(): JsonObject {
        return JsonObject().apply {
            addProperty("object", "receiver_receipt")
            addProperty("public_key", pubKey)
            addProperty("confirmation", confirmation)
            addProperty("tombstone_block", receipt.tombstoneBlock.toString())
            add("amount", JsonObject().apply {
                addProperty("object", "amount")
                addProperty(
                    "commitment",
                    Hex.toHexString(receipt.maskedAmountV2.commitment.data.toByteArray())
                )
                addProperty(
                    "masked_value",
                    receipt.maskedAmountV2.maskedValue.toULong().toString()
                )
                addProperty(
                    "masked_token_id",
                    Hex.toHexString(receipt.maskedAmountV2.maskedTokenId.toByteArray())
                )
                addProperty(
                    "version",
                    "V2"
                )
            })
        }
    }

    override val builderBodyFunc: (obj: JsonObject) -> JsonObject
        get() = { JsonObject() }
}


