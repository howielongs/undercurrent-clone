package com.undercurrent.shared.abstractions

// may want to better enforce type safety on crypto addr strings
interface CryptoAddressEntity {
    var typeLabel: String?
}