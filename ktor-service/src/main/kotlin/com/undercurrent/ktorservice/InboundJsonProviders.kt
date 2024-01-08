package com.undercurrent.ktorservice

import com.google.gson.*
import com.undercurrent.legacy.routing.RunConfig
import com.undercurrent.shared.utils.Util
import com.undercurrent.shared.utils.time.EpochNano
import com.undercurrent.shared.utils.time.SystemEpochNanoProvider
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.repository.entities.messages.InboundMessage
import java.lang.reflect.Type

object InboundMessageSerializer : JsonSerializer<InboundMessage> {
    override fun serialize(
        src: InboundMessage,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val json = JsonObject()
        json.addProperty("id", src.id.value)
        json.addProperty("createdDate", src.createdDate.toString())
        json.addProperty("updatedDate", src.updatedDate.toString())
        json.addProperty("expiryEpoch", src.expiryEpoch)
        json.addProperty("body", src.body)
        json.addProperty("senderSms", src.senderSms)
        json.addProperty("receiverSms", src.receiverSms)
        json.addProperty("dbusPath", src.dbusPath)
        json.addProperty("timestamp", src.timestamp)
        json.addProperty("uuid", src.uuid)
        json.addProperty("readAt", src.readAtDate?.toString())
        return json
    }
}

object InboundMessageDeserializer : JsonDeserializer<InboundMessage> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): InboundMessage {
        val jsonObject = json.asJsonObject
//        val expiryEpoch = jsonObject["expiryEpoch"]?.asLong ?: 0
        val body = jsonObject["body"].asString
        val senderSms = jsonObject["sender_sms"].asString
        val receiverSms = jsonObject["receiver_sms"].asString
        val dbusPath = jsonObject["dbus_path"].asString
//        val timestamp = jsonObject["timestamp"]?.asLong ?: 0
        val uuid = jsonObject["uuid"]?.asString
//        val readAt = jsonObject["read_at"]?.asString

        // Create an InboundMessage object with the parsed data
        return tx {
            InboundMessage.new {
                createdDate = Util.getCurrentUtcDateTime()
                updatedDate = Util.getCurrentUtcDateTime()
                this.expiryEpoch = SystemEpochNanoProvider().epochNano(RunConfig.DEFAULT_MSG_EXPIRY_SEC)
                this.body = body
                this.senderSms = senderSms
                this.receiverSms = receiverSms
                this.dbusPath = dbusPath
                this.timestamp = EpochNano().value
                this.uuid = uuid
                this.readAtDate = null
            }
        }
    }
}

