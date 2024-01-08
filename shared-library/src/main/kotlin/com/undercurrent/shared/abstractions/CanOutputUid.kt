package com.undercurrent.shared.abstractions

interface CanOutputUid {
    var uid: Int
    fun fetchId(): Int
}