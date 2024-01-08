package com.undercurrent.legacy.system_start

import kotlinx.coroutines.*

class ScheduledCoroutineJob {
    fun startCoroutineJob(workerFunction: suspend () -> Unit, delayMs: Long = 0, periodMs: Long = 1000) {
        val coroutineScope = CoroutineScope(Dispatchers.IO) // Create a CoroutineScope

        val job = coroutineScope.launch {
            delay(delayMs)
            while (isActive) {
                workerFunction()
                delay(periodMs)
            }
        }

        // Add a shutdown hook to cancel the job when the application exits
        Runtime.getRuntime().addShutdownHook(Thread {
            runBlocking {
                job.cancelAndJoin()
            }
        })
    }
}