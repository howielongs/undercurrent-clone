package com.undercurrent.system.messaging.inbound

import com.undercurrent.system.repository.entities.User
import com.undercurrent.legacy.routing.RunConfig
import com.undercurrent.shared.messages.RoutingProps
import com.undercurrent.shared.messages.UserInputProvider
import com.undercurrent.shared.repository.bases.RootEntity0
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.utils.Util
import com.undercurrent.shared.utils.time.EpochNano
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.repository.entities.messages.InboundMessage
import com.undercurrent.system.messaging.inbound.querybuilders.fetch.LatestInboundMessageQueryProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

interface CanFilterFetchResults<E : RootEntity0> {
    fun filterFetchResults(results: List<E>): List<E>
}

interface InboundEntityFetcher<E : RootEntity0> {
    suspend fun fetchAllAfter(afterEpochNano: EpochNano): List<E>
}



//todo test in place of RawInputProvider
class InboundMessageFetcher(
    user: User,
    routingProps: RoutingProps,
) : InboundFetcher<InboundMessage>(
    queryProvider = LatestInboundMessageQueryProvider(
        user = user,
        routingProps = routingProps,
    )
), UserInputProvider {

    //look into hiding this inside UserInputProvider
    override suspend fun fetchAllAfter(afterEpochNano: EpochNano): List<InboundMessage> {
        val thisItem: RootEntityCompanion0<InboundMessage> = InboundMessage.Companion

        return tx {
            thisItem.find { toFetchExpr(afterEpochNano) }.toList()
        }
    }
    //todo SMELLY

    override suspend fun getRawInput(afterEpoch: EpochNano): String? = coroutineScope {
        var textInput: String? = null
        var count = 0

        while ((textInput.isNullOrEmpty()) && count < maxCount) {
            count++

            val job = async {
                val results = fetchAllAfter(afterEpoch)

                return@async if (results.isNotEmpty()) {
                    //AFTER results action
                val dateNow = Util.getCurrentUtcDateTime()
                    textInput = tx {
                        val result = results.minByOrNull { it.timestamp }
                        result?.readAtDate = dateNow
                        result?.body
                    }
                    textInput
                } else {
                    null
                }
            }

            textInput = job.await()

            if ((textInput.isNullOrEmpty()) && count < maxCount) {
                delay(RunConfig.FETCH_INPUT_DELAY_MS)
            }
        }

        textInput?.let {
            it
        }
    }
}