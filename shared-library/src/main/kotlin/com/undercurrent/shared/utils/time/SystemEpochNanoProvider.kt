package com.undercurrent.shared.utils.time

import com.undercurrent.shared.utils.Log
import java.time.Instant

@Deprecated("Get rid of this intermediate wrapper")
inline class EpochNanoVal(val value: Long)

class EpochNano(
    private val valueIn: Long? = null,
    private val nowFunc: () -> Long = { DefaultEpochProvider().getEpochNano().value }
) {
    val value: Long = valueIn ?: nowFunc()

    fun now(): EpochNano {
        return EpochNano(valueIn = nowFunc())
    }

    fun secInFuture(secInFuture: Long): EpochNano {
        return EpochNano(valueIn = value + (1000000000L * secInFuture))
    }

    fun msInFuture(msInFuture: Long): EpochNano {
        return EpochNano(valueIn = value + (1000000L * msInFuture))
    }
}


//convert to using EpochNano wherever possible (also for timestamps in messages with transformers)
interface EpochProvider {
    fun getEpochNanoLong(): Long
    fun getEpochNano(): EpochNanoVal
    fun epochNano(secInFuture: Long): Long
    fun getEpochNanoSecInFuture(nanoSecInFuture: Long): EpochNanoVal
    fun forceEpochToNano(epoch: Long): EpochNanoVal?
}


class DefaultEpochProvider : SystemEpochNanoProvider()

open class SystemEpochNanoProvider : EpochProvider {

    companion object {
        fun getEpochNano(nsInFuture: Long = 0L): Long {
            with(Instant.now()) {
                return this.epochSecond * (1000000000) + this.nano + nsInFuture
            }
        }

        private fun forceEpochToNano(epochString: String?): Long? {
            epochString?.let {
                var outEpochStr = it
                val diff = 19 - epochString.length
                if (diff > 15) {
                    return null
                }
                if (diff > 0) {
                    for (i in 0 until diff) {
                        outEpochStr += "0"
                    }
                }
                return try {
                    val longEpoch = outEpochStr.toLong()
                    if (longEpoch > 0L) {
                        return longEpoch
                    } else {
                        return null
                    }
                } catch (e: Exception) {
                    Log.trace("Hit weird long conversion error for $epochString")
                    null
                }
            } ?: return null
        }

    }

    override fun getEpochNano(): EpochNanoVal {
        return EpochNanoVal(getEpochNanoLong())
    }

    override fun getEpochNanoLong(): Long {
        return SystemEpochNanoProvider.getEpochNano()
    }

    override fun getEpochNanoSecInFuture(nanoSecInFuture: Long): EpochNanoVal {
        return EpochNanoVal(getEpochNano(nanoSecInFuture))
    }

    override fun epochNano(secInFuture: Long): Long {
        return getEpochNano(1000000000L * secInFuture)
    }

    override fun forceEpochToNano(epoch: Long): EpochNanoVal? {
        forceEpochToNano(epoch.toString())?.let {
            return EpochNanoVal(it)
        } ?: return null
    }

}