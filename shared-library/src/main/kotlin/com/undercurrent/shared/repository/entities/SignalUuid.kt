package com.undercurrent.shared.repository.entities

interface SignalAddress
interface SignalUserAddress : SignalAddress

typealias BotSms = SignalSms
typealias HumanAddrType = SignalSms


inline class SignalUuid(val value: String) : SignalUserAddress

inline class SignalSms(val value: String) : SignalUserAddress {
    override fun toString(): String {
        return "$value"
    }
}



inline class SignalGroupId(val value: String) : SignalAddress

inline class Sms(val value: String?)


