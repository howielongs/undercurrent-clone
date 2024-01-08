package com.undercurrent.shops.repository.proto_versions

import com.undercurrent.shared.SystemUserNew
import com.undercurrent.shared.SystemUsersNew
import com.undercurrent.shared.repository.bases.RootEntity0
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.bases.RootTable0
import com.undercurrent.shared.repository.entities.JoinCodeEntity
import com.undercurrent.shared.repository.entities.JoinCodeTable
import com.undercurrent.shared.utils.VARCHAR_SIZE
import com.undercurrent.shared.utils.tx
import com.undercurrent.shops.repository.proto_versions.companions.*
import com.undercurrent.shops.types.wrappers.JoinCodeValue
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column

sealed class ProtoShopBotEntity(id: EntityID<Int>, table: ProtoShopBotTable) : RootEntity0(id, table)

sealed class ProtoShopBotTable(
    tableName: String,
) : RootTable0("Z_NEW_$tableName")

object ProtoShopCustomers : ProtoShopBotTable("shop_customers") {
    val user = reference("user_id", SystemUsersNew)
    val storefront = reference("storefront_id", ProtoStorefronts)
}

class ProtoShopCustomer(id: EntityID<Int>) : ProtoShopBotEntity(id, ProtoShopCustomers) {
    var user by SystemUserNew referencedOn ProtoShopCustomers.user
    var storefront by ProtoStorefront referencedOn ProtoShopCustomers.storefront

    companion object : ProtoShopCustomerCompanion<ProtoShopCustomer>()
}

object ProtoShopJoinCodeBursts : ProtoShopBotTable("shop_join_code_burst") {
    val storefront = reference("storefront_id", ProtoStorefronts)
    val initiatorUser = reference("initiator_user_id", SystemUsersNew)
    val quantity = integer("qty")
}

class ProtoShopJoinCodeBurst(id: EntityID<Int>) : ProtoShopBotEntity(id, ProtoShopJoinCodeBursts) {

    var storefront by ProtoStorefront referencedOn ProtoShopJoinCodeBursts.storefront
    var initiatorUser by SystemUserNew referencedOn ProtoShopJoinCodeBursts.initiatorUser

    var quantity by ProtoShopJoinCodeBursts.quantity

    val joinCodes by ProtoShopJoinCode optionalReferrersOn (ProtoShopJoinCodesNew.burstEvent)

    companion object : RootEntityCompanion0<ProtoShopJoinCodeBurst>(ProtoShopJoinCodeBursts) {
        fun save(
            storefrontIn: ProtoStorefront,
            initiatorUserIn: SystemUserNew,
            quantityIn: Int,
        ): ProtoShopJoinCodeBurst? {
            return tx {
                new {
                    storefront = storefrontIn
                    initiatorUser = initiatorUserIn
                    quantity = quantityIn
                }
            }
        }
    }
}


object ProtoShopJoinCodesNew : ProtoShopBotTable("shop_join_codes"), JoinCodeTable {
    val storefront = reference("storefront_id", ProtoStorefronts)
    override val parent: Column<EntityID<Int>?> = optReference("parent_code_id", ProtoShopJoinCodesNew)
    override val code = varchar("code", VARCHAR_SIZE).uniqueIndex()
    val burstEvent = optReference("burst_event_id", ProtoShopJoinCodeBursts)
}

class ProtoShopJoinCode(id: EntityID<Int>) : ProtoShopBotEntity(id, ProtoShopJoinCodesNew),
    JoinCodeEntity<JoinCodeValue> {
    var storefront by ProtoStorefront referencedOn (ProtoShopJoinCodesNew.storefront)
    var parent: ProtoShopJoinCode? by ProtoShopJoinCode optionalReferencedOn (ProtoShopJoinCodesNew.parent)

    override var code: JoinCodeValue by ProtoShopJoinCodesNew.code.transform(
        toColumn = { it.value }, toReal = { JoinCodeValue(it) }
    )
    var burstEvent by ProtoShopJoinCodeBurst optionalReferencedOn (ProtoShopJoinCodesNew.burstEvent)


    val usages by ProtoShopJoinCodeUsage referrersOn ProtoShopJoinCodeUsages.joinCode

    companion object : ProtoShopJoinCodeCompanion<ProtoShopJoinCode>()
}


object ProtoShopJoinCodeUsages : ProtoShopBotTable("shop_join_code_usages") {
    val user = reference("user_id", SystemUsersNew)
    val joinCode = reference("join_code_id", ProtoShopJoinCodesNew)
}

class ProtoShopJoinCodeUsage(id: EntityID<Int>) : ProtoShopBotEntity(id, ProtoShopJoinCodeUsages) {
    var user by SystemUserNew referencedOn (ProtoShopJoinCodeUsages.user)
    var shopJoinCode by ProtoShopJoinCode referencedOn (ProtoShopJoinCodeUsages.joinCode)


    companion object : RootEntityCompanion0<ProtoShopJoinCodeUsage>(ProtoShopJoinCodeUsages)
}


object ProtoShopOrders : ProtoShopBotTable("shop_orders") {
    val customer = reference("customer_id", ProtoShopCustomers)
}

class ProtoShopOrder(id: EntityID<Int>) : ProtoShopBotEntity(id, ProtoShopOrders) {
    var customer by ProtoShopCustomer referencedOn ProtoShopOrders.customer

    companion object : RootEntityCompanion0<ProtoShopOrder>(ProtoShopOrders) {
        fun create(customerIn: ProtoShopCustomer): ProtoShopOrder {
            return tx {
                ProtoShopOrder.new {
                    customer = customerIn
                }
            }
        }
    }
}


object ProtoShopOrderItems : ProtoShopBotTable("shop_order_items") {
    val customer = ProtoShopOrderItems.reference("customer_id", ProtoShopCustomers)
    val order = ProtoShopOrderItems.reference("order_id", ProtoShopOrders)
    val sku = ProtoShopOrderItems.reference("sku_id", ProtoShopSKUs)

    val qty = ProtoShopOrderItems.integer("qty")

}

class ProtoShopOrderItem(id: EntityID<Int>) : ProtoShopBotEntity(id, ProtoShopOrderItems) {
    var customer by ProtoShopCustomer referencedOn ProtoShopOrderItems.customer
    var order by ProtoShopOrder referencedOn ProtoShopOrderItems.order
    var sku by ProtoShopSKU referencedOn ProtoShopOrderItems.sku
    var qty by ProtoShopOrderItems.qty

    companion object : RootEntityCompanion0<ProtoShopOrderItem>(ProtoShopOrderItems) {
        fun create(
            customerIn: ProtoShopCustomer,
            orderIn: ProtoShopOrder,
            skuIn: ProtoShopSKU,
            qtyIn: Int
        ): ProtoShopOrderItem {
            return tx {
                ProtoShopOrderItem.new {
                    customer = customerIn
                    order = orderIn
                    sku = skuIn
                    qty = qtyIn
                }
            }
        }
    }
}


object ProtoShopProductsNew : ProtoShopBotTable("shop_products") {
    val storefront = reference("storefront_id", ProtoStorefronts)
    val name = varchar("name", VARCHAR_SIZE)
    val details = varchar("details", VARCHAR_SIZE)
}

class ProtoShopProduct(id: EntityID<Int>) : ProtoShopBotEntity(id, ProtoShopProductsNew) {
    var storefront by ProtoStorefront referencedOn ProtoShopProductsNew.storefront
    var name by ProtoShopProductsNew.name
    var details by ProtoShopProductsNew.details

    private val allSaleItems by ProtoShopSaleItem referrersOn ProtoShopSaleItems.product

    var saleItems: List<ProtoShopSaleItem> = listOf()
        get() {
            return tx {
                allSaleItems.toList().filter { it.isNotExpired() }
            }
        }

    var joinCodes: List<ProtoShopJoinCode> = listOf()
        get() {
            return tx {
                storefront.allJoinCodes.filter { it.isNotExpired() }.toList()
            }
        }


    companion object : ProtoShopProductCompanion<ProtoShopProduct>()

}


object ProtoShopSaleItems : ProtoShopBotTable("shop_sale_items") {
    val product = reference("product_id", ProtoShopProductsNew)

    val price = varchar("price", VARCHAR_SIZE).clientDefault { "0" }
    val label = varchar("label", VARCHAR_SIZE)
}

class ProtoShopSaleItem(id: EntityID<Int>) : ProtoShopBotEntity(id, ProtoShopSaleItems) {
    var product by ProtoShopProduct referencedOn ProtoShopSaleItems.product
    var price by ProtoShopSaleItems.price
    var label by ProtoShopSaleItems.label


    var name: String? = null
        get() = tx { product.name }

    var details: String? = null
        get() = tx { product.details }


    companion object : RootEntityCompanion0<ProtoShopSaleItem>(ProtoShopSaleItems) {
        fun create(productIn: ProtoShopProduct, priceIn: String, labelIn: String): ProtoShopSaleItem {
            return tx {
                new {
                    this.product = productIn
                    this.price = priceIn
                    this.label = labelIn
                }
            }
        }
    }
}


object ProtoShopSKUs : ProtoShopBotTable("shop_skus") {
    val product = reference("product_id", ProtoShopProductsNew)
    val name = varchar("name", VARCHAR_SIZE)
    val description = varchar("description", VARCHAR_SIZE)
}

class ProtoShopSKU(id: EntityID<Int>) : ProtoShopBotEntity(id, ProtoShopSKUs) {
    var product by ProtoShopProduct referencedOn ProtoShopSKUs.product
    var name by ProtoShopSKUs.name
    var description by ProtoShopSKUs.description

    companion object : RootEntityCompanion0<ProtoShopSKU>(ProtoShopSKUs) {
        fun create(productIn: ProtoShopProduct, nameIn: String, detailsIn: String): ProtoShopSKU {
            return tx {
                ProtoShopSKU.new {
                    product = productIn
                    name = nameIn
                    description = detailsIn
                }
            }
        }
    }

}


object ProtoShopVendorsNew : ProtoShopBotTable("shop_vendors") {
    val user = reference("user_id", SystemUsersNew)
    val nickname = varchar("nickname", VARCHAR_SIZE)
}

class ProtoShopVendor(id: EntityID<Int>) : ProtoShopBotEntity(id, ProtoShopVendorsNew) {
    var user by SystemUserNew referencedOn ProtoShopVendorsNew.user
    var nickname by ProtoShopVendorsNew.nickname

    companion object : ProtoShopVendorCompanion<ProtoShopVendor>()

    var sms: String? = null
        get() {
            return tx { user.signalSms }
        }

    val allStorefronts by ProtoStorefront referrersOn ProtoStorefronts.vendor
    var storefronts: List<ProtoStorefront> = listOf()
        get() {
            return tx {
                allStorefronts.toList().filter { it.isNotExpired() }
            }
        }

    var shopProducts: List<ProtoShopProduct> = listOf()
        get() {
            return tx {
                storefronts.flatMap { it.products }
            }
        }


    var deliveryOrders: List<RootEntity0> = listOf()
        get() {
            return tx {
                //todo impl this
                listOf()
            }
        }

    var ordersToConfirm: List<RootEntity0> = listOf()
        get() {
            return tx {
                //todo impl this
                listOf()
            }
        }
}


object ProtoStorefronts : ProtoShopBotTable("shop_storefronts") {
    val vendor = reference("vendor_id", ProtoShopVendorsNew)
}

class ProtoStorefront(id: EntityID<Int>) : ProtoShopBotEntity(id, ProtoStorefronts) {
    companion object : ProtoStorefrontCompanion<ProtoStorefront>()

    var vendor by ProtoShopVendor referencedOn (ProtoStorefronts.vendor)

    private val allProducts by ProtoShopProduct referrersOn ProtoShopProductsNew.storefront

    val allJoinCodes by ProtoShopJoinCode referrersOn ProtoShopJoinCodesNew.storefront

    var products: List<ProtoShopProduct> = listOf()
        get() {
            return tx {
                allProducts.toList().filter { it.isNotExpired() }
            }
        }

    var joinCodes: List<ProtoShopJoinCode> = listOf()
        get() {
            return tx {
                allJoinCodes.filter { it.isNotExpired() }.toList()
            }
        }


}

