package com.undercurrent.legacy.service.csvutils.csv_handlers

import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrder
import com.undercurrent.legacy.service.csvutils.csv_linewriters.DeliveryOrderCsvLineWriter
import com.undercurrent.system.context.SessionContext

class DeliveryOrdersCsvHandler<T : DeliveryOrder>(
    orders: List<T>, sessionContext: SessionContext
) : CsvHandler<T>(
    items = orders,
    headers = "Order Code\tName\tAddress\tAddress Line 2\tCity\tState\tZipcode\tCountry\tOrder Items\tPounds\tLength\tWidth\tHeight",
    csvLineWriterFactory = { order -> DeliveryOrderCsvLineWriter(order) }, sessionContext
)