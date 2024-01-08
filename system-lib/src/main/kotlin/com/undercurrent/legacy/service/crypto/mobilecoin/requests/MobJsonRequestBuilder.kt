package com.undercurrent.legacy.service.crypto.mobilecoin.requests

import com.google.gson.JsonObject
import com.undercurrent.shared.utils.Log

sealed interface JsonRequestBuilderInterface {
    val builderBodyFunc: (obj: JsonObject) -> JsonObject

}

abstract class MobJsonRequestBuilder(
    private val methodStr: String,
) : JsonRequestBuilderInterface {

    fun build(): JsonObject {
        with(propertyFooter(builderBodyFunc(methodHeader(JsonObject())))) {
//            Log.debug("MOB JSON BUILT: \n$this\n\n")
            Log.debug("MOB JSON BUILT\n\n")
            return this
        }
    }

    private fun methodHeader(obj: JsonObject): JsonObject {
        return obj.apply {
            addProperty("method", methodStr)
        }
    }

    private fun propertyFooter(obj: JsonObject): JsonObject {
        return obj.apply {
            addProperty("jsonrpc", "2.0")
            addProperty("id", 1)
        }
    }
}