package com.undercurrent.system.service.timed_operations

interface CanRunWithTimer {
    suspend fun start()
}