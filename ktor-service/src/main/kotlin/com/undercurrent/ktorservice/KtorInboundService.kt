package com.undercurrent.ktorservice

import com.google.gson.GsonBuilder
import com.undercurrent.shared.repository.database.ProductionDatabase
import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.system.repository.entities.messages.InboundMessage
class KtorInboundService {
//    val fullValidNums = listOf("+12625637281", "+12625637291", "+16235637292")

    fun saveInbound(msg: String) {
        // Initialize your database connection
        val databaseStarter = ProductionDatabase(
            Environment.DEV,
            shouldRunMigrations = false
        )
        databaseStarter.db

        // Create a Gson instance with custom serializers and deserializers
        val gson = GsonBuilder()
            .registerTypeAdapter(InboundMessage::class.java, InboundMessageSerializer)
            .registerTypeAdapter(InboundMessage::class.java, InboundMessageDeserializer)
            .create()

        // Deserialize JSON to an InboundMessage
        val inboundMessageData = gson.fromJson(msg, InboundMessage::class.java)


        // Perform the database transaction to save the InboundMessage
//        tx {
//            try {
//                val newMsg = InboundMessage.new {
//                    createdDate = Util.getCurrentUtcDateTime()
//                    updatedDate = Util.getCurrentUtcDateTime()
//                    expiryEpoch = null
//                    body = inboundMessageData.body
//                    senderSms = inboundMessageData.senderSms
//                    receiverSms = inboundMessageData.receiverSms
//                    timestamp = inboundMessageData.timestamp
//                    dbusPath = inboundMessageData.dbusPath
//                    uuid = inboundMessageData.uuid
//                    readAtDate = inboundMessageData.readAtDate
//                }
//
//                // Commit the transaction
//                "New msg saved: $newMsg".let {
//                    println(it)
//                    Log.debug(it)
//                }
//            } catch (e: Exception) {
//                Log.error("Unable to save message: ${e.message}")
//            }
//        }
    }
}
