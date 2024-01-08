package com.undercurrent.legacyshops.repository.entities.shop_items


import com.undercurrent.legacy.data_transfer_objects.currency.FiatAmount
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrders
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomer
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomers
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.shared.utils.VARCHAR_SIZE
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.repository.entities.User
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal

object CartItems : ExposedTableWithStatus2("shop_cart_items") {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    val deliveryOrder = optReference("order_id", DeliveryOrders).default(null)
    val saleItem = reference("sku_id", SaleItems)
    val customer = reference("customer_id", ShopCustomers)

    val notes = varchar("notes", VARCHAR_SIZE)
    val quantity = integer("quantity").clientDefault { 0 }

    fun getSubtotal(customerProfile: ShopCustomer): FiatAmount? {
        return transaction {
            var subtotal = BigDecimal(0)
            customerProfile.cartContents.forEach {
                val price = it.saleItem?.price
                subtotal = subtotal.add(
                    BigDecimal(it.quantity)
                        .multiply(BigDecimal(price))
                )
            }
            FiatAmount(subtotal)
        }
    }

    fun getCartItems(customerProfile: ShopCustomer): List<CartItem> {
        return transaction {
            CartItem.find { customer eq customerProfile.id }
                .toList()
                .filter { it.isNotExpired() }
        }
    }

    fun getCartItems(user: User): List<CartItem>? {
        user.currentCustomerProfile?.let {
            return getCartItems(it)
        } ?: run {
            //preferable to return null so that we are aware of an issue and not just an empty cart
            logger.warn("Cannot find customer for user ${tx { user.fetchId() }} cartItems were asked for")
            return null
        }
    }

    //todo will need to check if this works, or if better to have nullable DeliveryOrder ref
    fun fetchWithoutOrder(): List<CartItem> {
        return transaction {
            CartItem.find { deliveryOrder eq null }
                .toList()
                .filter { it.isNotExpired() }
        }
    }

    //todo clean up transaction calls here
    fun getSubtotal(user: User): FiatAmount? {
        user.currentCustomerProfile?.let { shopCustomer ->
            getSubtotal(shopCustomer)?.let {
                return it
            }
        }
        logger.warn("${this.javaClass.simpleName}: Cannot find customer for user ${tx { user.fetchId() }} subtotal were asked for")
//        throw IllegalStateException("Cannot find customer for user ${tx { user.fetchId() }} subtotal were asked for")
        //preferable to return null so that we are aware of an issue and not just an empty cart

        return null
    }

    fun emptyListString(): String {
        return PressAgent.customerEmptyCart()
    }


}