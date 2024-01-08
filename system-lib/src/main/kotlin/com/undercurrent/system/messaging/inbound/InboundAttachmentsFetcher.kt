package com.undercurrent.system.messaging.inbound

import com.undercurrent.system.repository.entities.User
import com.undercurrent.legacy.repository.entities.system.attachments.Attachments
import com.undercurrent.legacy.routing.RunConfig
import com.undercurrent.shared.messages.RoutingProps
import com.undercurrent.shared.repository.bases.RootEntity0
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.time.EpochNano
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.messaging.inbound.querybuilders.fetch.FetchQueryProvider
import com.undercurrent.system.messaging.inbound.querybuilders.fetch.LatestInboundAttachmentsQueryProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.Op
import kotlin.coroutines.cancellation.CancellationException

interface CanFetchAttachments {
    suspend fun fetchAttachmentsOrCancel(afterEpochNano: EpochNano): List<Attachments.Entity>
}

abstract class InboundFetcher<E : RootEntity0>(
    protected val maxCount: Int = RunConfig.DEFAULT_MSG_EXPIRY_SEC.toInt(),
    private val queryProvider: FetchQueryProvider
) : InboundEntityFetcher<E>, FetchQueryProvider {

    abstract override suspend fun fetchAllAfter(afterEpochNano: EpochNano): List<E>

    override fun toFetchExpr(afterEpochNano: EpochNano): Op<Boolean> {
        return queryProvider.toFetchExpr(afterEpochNano)
    }

}

/**
 * Await input of attachments from the user.
 * Start with epoch and await new attachments.
 * Also await a cancel signal.
 */
class InboundAttachmentsFetcher(
    val user: User,
    val routingProps: RoutingProps,
) : InboundFetcher<Attachments.Entity>(
    queryProvider = LatestInboundAttachmentsQueryProvider(
        user = user,
        routingProps = routingProps,
    ),
), CanFetchAttachments {

    //todo consider adding prompt outbound for this
    override suspend fun fetchAttachmentsOrCancel(afterEpochNano: EpochNano): List<Attachments.Entity> = coroutineScope {
        val inboundMessageFetcher = InboundMessageFetcher(
            user = user,
            routingProps = routingProps,
        )

        val attachmentsJob = async {
            //start up attachments fetcher
            getAttachments(afterEpochNano)
        }

        val validCancelSet = setOf("quit", "Quit", "QUIT", "q", "Q")

        val cancellationListenerJob = launch {            //start up cancellation fetcher
            /**
             * Should cancel other fetcher if valid cancel received
             */
            while (attachmentsJob.isActive) {
                inboundMessageFetcher.getRawInput(afterEpochNano)?.let {
                    if (validCancelSet.contains(it.lowercase().trim())) {
                        attachmentsJob.cancel()
                    }
                }
            }
        }

        try {
            // Wait for the attachments fetching job to complete
            attachmentsJob.await().also { attachments ->
                if (attachments.isNotEmpty()) {
                    cancellationListenerJob.cancel()  // Cancel the cancellation listener if attachments are fetched
                }
            }
        } catch (e: CancellationException) {
            "Attachments fetcher cancelled".let {
                Log.error(it, e)
            }
            listOf()
        } finally {
            cancellationListenerJob.cancel()  // Ensure to cancel the listener job in case of any termination
        }
    }

    /**
     * In parallel, await input of attachments from the user and messages for potential cancellation
     */
    private suspend fun getAttachments(afterEpochNano: EpochNano): List<Attachments.Entity> = coroutineScope {
        var count = 0

        var results: List<Attachments.Entity> = listOf()

        while ((results.isNullOrEmpty()) && count < maxCount) {
            count++

            //todo also do a fetch in parallel for cancellation signal

            val job = async {
                return@async fetchAllAfter(
                    afterEpochNano,
                )
            }

            results = job.await()

            if ((results.isNullOrEmpty()) && count < maxCount) {
                delay(RunConfig.FETCH_INPUT_DELAY_MS)
            }
        }

        results
    }

    override suspend fun fetchAllAfter(afterEpochNano: EpochNano): List<Attachments.Entity> {
        return tx {
            Attachments.Entity.find { toFetchExpr(afterEpochNano) }.toList()
        }
    }
}