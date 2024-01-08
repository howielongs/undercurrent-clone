package com.undercurrent.legacy.types.enums.status

enum class OrderStatus {
    NEW,
    SUBMITTED,
    CONFIRMED,
    AWAITING_PAYMENT,

    AWAITING_SHIPMENT,
    SHIPPED,
    DELIVERED,

    DECLINED,
    CANCELED
}