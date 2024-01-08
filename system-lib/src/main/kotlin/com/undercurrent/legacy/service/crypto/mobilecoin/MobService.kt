package com.undercurrent.legacy.service.crypto.mobilecoin


import com.google.gson.JsonObject
import com.undercurrent.shared.utils.Log
import com.undercurrent.system.repository.entities.Admins
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.*

sealed interface JsonServiceRequestInterface {
    suspend fun req(jsonReq: JsonObject): JsonObject?
}

abstract class BaseJsonSvcRequester : JsonServiceRequestInterface {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            gson()
        }
    }

    abstract fun badStrings(): Set<String>


    //todo have this recover and somehow restore ledger (more of an atomic process)

    //todo check for:
    //{
    //    "method": "check_receiver_receipt_status",
    //    "result": {
    //        "receipt_transaction_status": "TransactionPending",
    //        "txo": null
    //    },
    //    "jsonrpc": "2.0",
    //    "id": 1
    //}
    private fun checkForBadResponse(response: HttpResponse?, e: Exception?): Int {
        var count = 0
        e?.let {
            Admins.notifyError("Request caught exception: " + it.stackTraceToString())
            count++
        }

        badStrings()
            .filter { response.toString().contains(it) }
            .toList()
            .let {
                count += it.count()
                if (count > 1) {
                    Admins.notifyError(
                        "Got $count errors on request\n\n" +
                                "Errant strings:\n${it.joinToString(",\n")}\n\n" +
                                "Response: ${response.toString()}"
                    )
                }
            }
        return count
    }

    suspend fun request(data: JsonObject): HttpResponse {
        val response: HttpResponse = client.post("http://127.0.0.1:9090/wallet") {
            contentType(ContentType.Application.Json)
            setBody(data)
        }
        return response
    }

    override suspend fun req(
        jsonReq: JsonObject,
    ): JsonObject? {
        return CoroutineScope(
            Dispatchers.IO +
                    CoroutineName("json req for MOB")
        ).async(start = CoroutineStart.LAZY) {
            var response: HttpResponse? = null
            try {
//                Log.debug("Sending request:\n${jsonReq.toString()}")
                Log.debug("Sending request...")
                response = request(jsonReq)
                checkForBadResponse(response = response, e = null)
                return@async response.body<JsonObject>()["result"].asJsonObject
            } catch (e: Exception) {
                checkForBadResponse(response = response, e)
                return@async null
            }
        }.await()?.let {
            it
        } ?: null
    }

}

class MobService : BaseJsonSvcRequester() {

    override fun badStrings(): Set<String> {
        return setOf(
            "ParseIntError",
            "server_error",
            "InternalError",
            "\" error \":{",
            "Invalid Amount",
            "Could not parse",
            "InvalidDigit"
        )
    }


}