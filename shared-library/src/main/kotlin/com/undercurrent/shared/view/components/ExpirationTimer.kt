package com.undercurrent.shared.view.components

const val EXPIRATION_TIMER_SECONDS = 60 * 5

interface ExpirationTimer : CanStartExpirationTimer, CanStopExpirationTimer

interface CanStartExpirationTimer {
    fun startTimer()
    fun startTimer(timeSeconds: Int)
}


interface CanStopExpirationTimer {
    fun stopTimer()
}