package com.undercurrent.shared.utils

import com.undercurrent.shared.abstractions.EntityWithExpiry
import com.undercurrent.shared.utils.time.DefaultEpochProvider
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

const val VARCHAR_SIZE = 10000

var systemLocale: Locale = Locale.getDefault()

var TEST_MODE = false
var FLYWAY_ENABLED = true
var PROMPT_RETRIES = 5

fun String.cleanOutboundMsg(): String {
    return trim().replace("'", "`")
}

fun String.asAbcHandle(): String {
    return trim().replace(" ", "").lowercase()
}


fun String.cleanInboundCommand(): String {
    return trim().replace("/", "")
        .replace(" ", "").uppercase()
}

fun String.cleanJoinCode(): String {
    return cleanInboundCommand()
}

fun <K, V> LinkedHashMap<K, V>.ordinalOfKey(key: K): Int? {
    var index = 0 // Start with an index of 0
    for (entry in this.entries) {
        if (entry.key == key) {
            return index // Return the current index if the key matches
        }
        index++ // Increment the index for each iteration
    }
    return null // Return null if the key is not found
}


/**
 * If expiryEpoch is null, then it is not expired
 * If expiryEpoch is greater than now, then it is not expired
 */
//fun filterOutExpiredItem(item: EntityWithExpiry): Boolean {
//    return unexpiredEntityExpr(item)
//}

val unexpiredEntityExpr: (EntityWithExpiry) -> Boolean =
    { item: EntityWithExpiry ->
        item.expiryEpoch?.let {
            val now = DefaultEpochProvider().getEpochNanoLong()
            it > now
        } ?: true
    }

//inline fun <T : EntityWithExpiry> T.isUnexpiredEntity(): Boolean {
//    return unexpiredEntityExpr(this)
//}

inline fun <T : EntityWithExpiry> Iterable<T>.filterOutExpired(): List<T> {
    return filterTo(ArrayList<T>(), unexpiredEntityExpr)
}


object Util {
    fun isMacOs(): Boolean {
        return System.getProperty("os.name").contains("Mac OS X")
    }

    @Deprecated("Favorable to use EpochHandler method")
    fun getEpoch(): Long {
        with(Instant.now()) {
            return this.epochSecond * (1000000000) + this.nano
        }
    }

    fun currentUtc(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)


//    fun getInstantNow(): String? {
//        return DateTimeFormatter.ISO_INSTANT.format(Instant.now())
//    }

    fun getCurrentUtcDateTime(): LocalDateTime {
        return LocalDateTime.now(ZoneOffset.UTC)
    }
}

fun newScope(name: String) = CoroutineScope(Dispatchers.IO + CoroutineName(name))

/**
 *
 */
//fun <T> tryTx(
//    operationString: String = "",
//    maxRetries: Int = 5,
//    statement: Transaction.() -> T
//): T? {
//    var retries = 0
//
//    return transaction {
//        while (retries < maxRetries) {
//            try {
//                return@transaction statement()
//            } catch (e: Exception) {
//                retries += 1
//                if (retries >= maxRetries) {
//                    "Attempt #$retries/$maxRetries --> Retry errors exhausted: $operationString\n\t${e.message}\n".let {
//                        println(it)
//                        Log.error(it)
//                    }
//                    return@transaction null
//                } else {
//                    "Attempt #$retries/$maxRetries --> Error performing transaction: $operationString\n" +
//                            "\t${e.message}\n".let {
//                                println(it)
//                                Log.error(it)
//                            }
//                }
//            }
//        }
//        return@transaction null
//    }
//}


/**
 * USE THIS WHEN IN DOUBT
 */
fun <T> tx(
    statement: Transaction.() -> T
): T {
    return transaction {
        statement()
    }
}

/**
 * Use this if there is a need to specify which database to use
 */
fun <T> tx(
    db: Database,
    statement: Transaction.() -> T
): T {
    return transaction(db) {
        statement()
    }
}

/**
 * Prioritize this over stx, as after the transaction completes, the previous context is restored
 */
suspend fun <T> ctx(
    statement: suspend Transaction.() -> T
): T = withContext(Dispatchers.IO) {
    newSuspendedTransaction {
        statement()
    }
}

/**
 * Prioritize this over stx, as after the transaction completes, the previous context is restored
 */
suspend fun <T> ctx(
    db: Database,
    statement: suspend Transaction.() -> T
): T = withContext(Dispatchers.IO) {
    newSuspendedTransaction(db = db) {
        statement()
    }
}

/**
 * Sets the new context and does not restore the previous context upon transaction completion
 */
suspend fun <T> stx(
    statement: suspend Transaction.() -> T
): T {
    return newSuspendedTransaction(Dispatchers.IO) {
        statement()
    }
}

/**
 * This is used with .await() to get the result of the transaction after it completes
 */
suspend fun <T> astx(
    statement: suspend Transaction.() -> T
): Deferred<T> {
    return suspendedTransactionAsync(Dispatchers.IO) {
        statement()
    }
}
