package com.undercurrent.legacy.repository.repository_service.payments.accounting

import com.undercurrent.legacy.repository.entities.payments.UserCreditLedger
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrder
import com.undercurrent.shared.utils.tx

class LedgerEntryFetcher {

    fun fetchByOrder(order: DeliveryOrder): List<UserCreditLedger.Entity> {
        return tx {
            val invoice = order.invoice
            UserCreditLedger.Entity.find {
                UserCreditLedger.Table.invoice eq invoice.id
            }.filter { it.isNotExpired() }.toList()
        }
    }

}