package com.undercurrent.shared

interface CanStartCommand {
    suspend fun startNewCommand(cmd: Any)
}

