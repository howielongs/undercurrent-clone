package com.undercurrent.legacyshops.repository.entities.shop_orders


import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.promptables.dataprompts.DataPromptSequence
import com.undercurrent.legacy.types.enums.status.OrderStatus
import com.undercurrent.legacy.utils.UtilLegacy
import com.undercurrent.legacyshops.repository.entities.storefronts.*
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.VARCHAR_SIZE
import com.undercurrent.shared.view.components.ExpirationTimer
import com.undercurrent.system.context.SessionContext
import com.undercurrent.system.dbus.SignalExpirationTimer
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upperCase

object DeliveryOrders : ExposedTableWithStatus2("shop_orders") {
    val orderCode = varchar("order_code", VARCHAR_SIZE).uniqueIndex()
        .clientDefault { UtilLegacy.generateUniqueOrderCode() }

    val invoice = reference("invoice_id", Invoices)
    val customer = reference("customer_id", ShopCustomers)
    val vendor = reference("vendor_id", ShopVendors)

    val zipcode = varchar("zipcode", VARCHAR_SIZE)
    val deliveryAddress = varchar("delivery_address", VARCHAR_SIZE)
    val deliveryName = varchar("delivery_name", VARCHAR_SIZE)
    val notesToVendor = varchar("note_to_vendor", VARCHAR_SIZE)

    // see about using transforms to format on output
    var confirmedDate = datetime("confirmed_date").nullable()
    var declinedDate = datetime("declined_date").nullable()

    // should notes also be own table?
    val notesToCustomer = varchar("note_to_customer", VARCHAR_SIZE).nullable()
    val trackingNumber = varchar("tracking_number", VARCHAR_SIZE).nullable()

    fun autoConfirmEligibleOrders(): List<DeliveryOrder> {
        return Storefronts.autoConfirmStorefronts()
            .flatMap {
                transaction {
                    Log.debug("Storefront #${it.uid} eligible for auto-confirm")
                    it.unconfirmedOrders
                }
            }
            .toList()
    }

    override fun singularItem(): String {
        return "Order"
    }

    @Deprecated("get rid of usage of PromptSequence")
    suspend fun save(
        sessionContext: SessionContext,
        invoiceIn: Invoice,
        customerProfileIn: ShopCustomer,
        shopVendorIn: ShopVendor,
        promptSequence: DataPromptSequence,
        expirationTimer: ExpirationTimer = SignalExpirationTimer(sessionContext),
    ): DeliveryOrder? {
        promptSequence.promptedInputsToMap()?.let { responses ->
            //todo needs better reflection capabilities
            // also better null handling

            //todo should pull this to class for dep-injection
            expirationTimer.stopTimer()

            return save(
                invoiceIn = invoiceIn,
                customerProfileIn = customerProfileIn,
                shopVendorIn = shopVendorIn,
                zipcodeIn = responses[DeliveryOrders::zipcode.name] ?: return null,
                deliveryAddressIn = responses[DeliveryOrders::deliveryAddress.name] ?: return null,
                deliveryNameIn = responses[DeliveryOrders::deliveryName.name] ?: return null,
                notesToVendorIn = responses[DeliveryOrders::notesToVendor.name] ?: return null,
            )
        }
        return null
    }

     fun save(
        invoiceIn: Invoice,
        customerProfileIn: ShopCustomer,
        shopVendorIn: ShopVendor,
        zipcodeIn: String,
        deliveryAddressIn: String,
        deliveryNameIn: String,
        notesToVendorIn: String,
    ): DeliveryOrder? {
        val newOrder = transaction {
            DeliveryOrder.new {
                invoice = invoiceIn
                customer = customerProfileIn
                shopVendor = shopVendorIn
                zipcode = zipcodeIn
                deliveryAddress = deliveryAddressIn
                deliveryName = deliveryNameIn
                notesToVendor = notesToVendorIn
                status = OrderStatus.SUBMITTED.name
            }
        }

        //could probably have a 'referrersOn' relation?
        transaction {
            invoiceIn.order = newOrder
            customerProfileIn.moveCartItemsToOrder(newOrder).let {
                Log.debug("CHECKOUT: Transferred $it items from cart to the order")
            }
        }

        return newOrder
    }


    fun byStatus(
        status: String,
        includeExpired: Boolean = false
    ): List<DeliveryOrder> {
        return transaction {
            DeliveryOrder.find {
                DeliveryOrders.status.upperCase() eq status.uppercase()
            }.toList().filter {
                if (!includeExpired) {
                    it.isNotExpired()
                } else {
                    true
                }
            }
        }
    }



}