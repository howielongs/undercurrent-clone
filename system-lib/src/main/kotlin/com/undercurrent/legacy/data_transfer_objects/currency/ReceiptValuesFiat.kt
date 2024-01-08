package com.undercurrent.legacy.data_transfer_objects.currency

data class ReceiptValuesFiat(
    var subtotal: FiatAmount,
    var fees: FiatAmount,
    var total: FiatAmount,
)