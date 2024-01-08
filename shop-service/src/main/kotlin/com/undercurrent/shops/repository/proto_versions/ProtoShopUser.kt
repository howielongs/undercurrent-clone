package com.undercurrent.shops.repository.proto_versions

import com.undercurrent.shared.SystemUserNew
import com.undercurrent.shared.repository.bases.RootEntity0
import com.undercurrent.shared.utils.tx

class ProtoShopUser(val user: SystemUserNew) {

    private var allShopVendors: List<ProtoShopVendor> = listOf()
        get() {
            return tx {
                ProtoShopVendor.all().filter {
                    it.user.id.value == user.id.value
                            && it.isNotExpired()
                }.toList()
            }
        }


    var deliveryOrders: List<RootEntity0> = listOf()
        get() {
            return tx {
                shopVendors.flatMap { it.deliveryOrders }.filter { it.isNotExpired() }
            }
        }

    var ordersToConfirm: List<RootEntity0> = listOf()
        get() {
            return tx {
                shopVendors.flatMap { it.ordersToConfirm }.filter { it.isNotExpired() }
            }
        }

    var shopVendor: ProtoShopVendor? = null
        get() {
            return tx {
                allShopVendors.firstOrNull { it.isNotExpired() }
            }
        }

    var shopVendors: List<ProtoShopVendor> = listOf()
        get() {
            return tx {
                allShopVendors.toList().filter { it.isNotExpired() }
            }
        }

    var saleItems: List<ProtoShopSaleItem> = listOf()
        get() {
            return tx {
                products.flatMap { it.saleItems }.filter { it.isNotExpired() }
            }
        }

    var products: List<ProtoShopProduct> = listOf()
        get() {
            return tx {
                shopVendors.flatMap { it.shopProducts }.filter { it.isNotExpired() }
            }
        }
}


