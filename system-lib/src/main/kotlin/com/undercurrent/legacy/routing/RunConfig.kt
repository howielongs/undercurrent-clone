package com.undercurrent.legacy.routing

import com.undercurrent.shared.types.enums.Environment

object RunConfig {

    const val MESSAGE_DISPATCH_SPACING_MS: Long = 1500L
    const val INBOUND_JOB_LOOP_DELAY: Long = 1000L

    const val DEFAULT_MSG_EXPIRY_SEC = 120L
    const val MSG_EXPIRY_NANO_SEC = DEFAULT_MSG_EXPIRY_SEC * 1000000000L

    const val NOTIFY_MSG_DELAY_BETWEEN = 1000L
    const val FETCH_INPUT_DELAY_MS = 500L

//    const val ZIP_DATABASE_FILE = "zipcodes.db"


    var version: String = ""

    var environment: Environment = Environment.TEST

    val isTestMode: Boolean by lazy {
        setOf(Environment.TEST).contains(environment)
    }

    var expirationTimerOn = true


}
