package com.undercurrent.shared.view.components

import com.undercurrent.shared.repository.entities.SignalSms

interface CanSendTypingIndicator {
    fun sendTypingIndicator(recipientHumanAddr: SignalSms)
}

interface CanCancelTypingIndicator {
    fun cancelTypingIndicator()
}

interface HasTypingIndicator : CanSendTypingIndicator, CanCancelTypingIndicator