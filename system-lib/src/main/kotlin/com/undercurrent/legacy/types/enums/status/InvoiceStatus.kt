package com.undercurrent.legacy.types.enums.status

//likely will morph into OrderStatus
enum class InvoiceStatus {
    CONFIRMED,
    AWAITING_CUSTOMER_PAYMENT,
    RECEIVED_FULL_FROM_CUSTOMER,
    DISPERSAL_PENDING,

    DECLINED_BY_VENDOR,
    CANCELED,
}