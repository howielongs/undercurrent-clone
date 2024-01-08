package com.undercurrent.legacy.service.csvutils.csv_linewriters

import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrder
import com.undercurrent.system.repository.entities.Admins
import com.undercurrent.system.repository.entities.ZipCodeLookup
import com.undercurrent.shared.utils.ctx


class DeliveryOrderCsvLineWriter<T : DeliveryOrder>(val order: T) : CsvLineWriter<T>(order) {
    override suspend fun write(): String {
        val outStr = StringBuilder()

        ctx {
            val zipcode = order.zipcode
            val orderCode = order.orderCode
            val deliveryName = order.deliveryName
            val deliveryAddress = order.deliveryAddress
            val receiptStr = order.receipt.toString()

            val zipCodeLookup = ZipCodeLookup.fetch(zipcode)
            val cityStr = zipCodeLookup?.city ?: ""
            val stateAbbrStr = zipCodeLookup?.stateAbbr ?: ""

            if (zipCodeLookup == null) {
                Admins.notifyError("Unable to load city/state for $zipcode CSV out")
            }

            outStr.appendValue(orderCode)
                .appendValue(deliveryName)
                .appendValue(deliveryAddress)
                .appendValue("")
                .appendValue(cityStr)
                .appendValue(stateAbbrStr)
                .appendValue(zipcode)
                .appendValue("")
                .appendValue(receiptStr)
                .appendValue("")
                .appendValue("")
                .appendValue("")
                .appendValue("")
        }
        return outStr.toString()
    }
}


