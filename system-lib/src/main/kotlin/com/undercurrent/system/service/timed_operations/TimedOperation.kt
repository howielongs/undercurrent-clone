package com.undercurrent.system.service.timed_operations

import com.undercurrent.shared.messages.InterrupterMessageEntity
import com.undercurrent.shared.messages.CanSendToUser
import com.undercurrent.shared.messages.UserOutputProvider
import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.time.ElapsedTimer
import kotlinx.coroutines.coroutineScope

open class TimedOperation(
    val environment: Environment,
    private val label: String,
    private val outputProvider: UserOutputProvider<InterrupterMessageEntity>,
    val operationFunc: () -> Unit,
) : CanRunWithTimer, CanSendToUser<InterrupterMessageEntity> {

    override fun sendOutput(message: String): InterrupterMessageEntity? {
        Log.info(message)
       return  outputProvider.sendOutput(message)
    }

    private val timer: ElapsedTimer by lazy {
        ElapsedTimer()
    }

    override suspend fun start() = coroutineScope {
        timer.start()

        operationFunc()

        timer.stop()
        val resultStr = "$label took: ${timer.diffSeconds()} seconds"
        sendOutput(resultStr)
        return@coroutineScope
    }
}