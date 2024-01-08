package com.undercurrent.shared.utils.time

import java.math.BigDecimal
import java.math.RoundingMode

class ElapsedTimer {

    private var startEpoch: Long = 0L
    private var endEpoch: Long = 0L

    fun start() {
        startEpoch = SystemEpochNanoProvider.getEpochNano()
    }

    fun stop() {
        endEpoch = SystemEpochNanoProvider.getEpochNano()
    }

    private fun diff(): Long {
        return endEpoch - startEpoch
    }

    fun diffSeconds(): BigDecimal {
        return BigDecimal(diff()).divide(
            BigDecimal(1000000000), 6,
            RoundingMode.HALF_UP
        )
    }


}