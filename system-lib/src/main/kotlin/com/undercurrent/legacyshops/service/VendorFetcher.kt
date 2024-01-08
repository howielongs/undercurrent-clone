package com.undercurrent.legacyshops.service

import com.undercurrent.legacyshops.repository.entities.storefronts.ShopVendor
import org.jetbrains.exposed.sql.transactions.transaction

class VendorFetcher {
    companion object {
        fun fetchAllVendors(): List<ShopVendor> {
            return transaction { ShopVendor.all().filter { it.isNotExpired() } }
        }
    }
}