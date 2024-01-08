package com.undercurrent.shared.abstractions


interface Expirable {
    fun isExpired(): Boolean
    fun isExpired(epoch: Long): Boolean
    fun isNotExpired(): Boolean
    fun isNotExpired(epoch: Long): Boolean
    fun unexpire()

    fun expire(): Boolean
    fun expire(epoch: Long): Boolean
}