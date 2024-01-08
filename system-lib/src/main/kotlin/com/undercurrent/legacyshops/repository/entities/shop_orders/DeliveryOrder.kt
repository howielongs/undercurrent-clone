package com.undercurrent.legacyshops.repository.entities.shop_orders


import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.repository.entities.payments.DepositCryptoAddress
import com.undercurrent.legacy.repository.entities.payments.StripePaymentLinks
import com.undercurrent.legacy.routing.RunConfig
import com.undercurrent.legacy.types.enums.CryptoType
import com.undercurrent.legacy.types.enums.status.InvoiceStatus
import com.undercurrent.legacy.types.enums.status.OrderStatus
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.legacy.utils.TimeAndDateProvider
import com.undercurrent.legacy.utils.UtilLegacy
import com.undercurrent.legacyshops.repository.entities.shop_items.CartItem
import com.undercurrent.legacyshops.repository.entities.shop_items.CartItems
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomer
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopVendor
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.shared.messages.CanNotifyCreated
import com.undercurrent.shared.messages.RoutingProps
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.dinosaurs.EntityHasStatusField
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.repository.dinosaurs.entityHasStatus
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.Util.currentUtc
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.context.DbusProps
import com.undercurrent.system.context.SessionContext
import com.undercurrent.system.messaging.outbound.AdminsNotifyProvider
import com.undercurrent.system.messaging.outbound.sendInterrupt
import com.undercurrent.system.messaging.outbound.sendNotify
import com.undercurrent.system.repository.entities.User
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.exposed.dao.id.EntityID
import java.math.BigDecimal
import java.time.LocalDateTime


open class ShopOrderCompanion : RootEntityCompanion0<DeliveryOrder>(DeliveryOrders) {
    //todo need to also display this with full receipt
    fun displayLongString(order: DeliveryOrder, role: AppRole): String {
        return tx {
            val thisInvoice = order.invoice
            thisInvoice.displayInvoice(role, order)
        }
    }

}

class DeliveryOrder(id: EntityID<Int>) : ExposedEntityWithStatus2(id, DeliveryOrders), EntityHasStatusField,
    CanNotifyCreated {
    companion object : ShopOrderCompanion()

    override fun hasStatus(status: String): Boolean {
        return entityHasStatus(status)
    }

    @Deprecated("Get rid of this")
    fun statusDoesNotMatchAny(vararg status: Enum<*>): Boolean {
        status.map { it.name.uppercase() }.toList().forEach {
            if (entityHasStatus(it)) {
                return false
            }
        }
        return true

    }

    var orderCode by DeliveryOrders.orderCode

    var invoice by Invoice referencedOn DeliveryOrders.invoice
    var customer by ShopCustomer referencedOn DeliveryOrders.customer
    var shopVendor by ShopVendor referencedOn DeliveryOrders.vendor

    var zipcode by DeliveryOrders.zipcode
    var deliveryAddress by DeliveryOrders.deliveryAddress
    var deliveryName by DeliveryOrders.deliveryName
    var notesToVendor by DeliveryOrders.notesToVendor

    //should notes also be own table?
    var notesToCustomer by DeliveryOrders.notesToCustomer
    var trackingNumber by DeliveryOrders.trackingNumber

    //todo consider having in own table, like pending payments events...
    var confirmedDate by DeliveryOrders.confirmedDate.transform({ LocalDateTime.parse(it) },
        { it?.let { it1 -> UtilLegacy.formatDbDatetime(it1) } })
    var declinedDate by DeliveryOrders.declinedDate.transform({ LocalDateTime.parse(it) },
        { it?.let { it1 -> UtilLegacy.formatDbDatetime(it1) } })

    suspend fun confirmOrder(notesOutString: String = "", dbusProps: RoutingProps) {
        tx {
            val thisId = this@DeliveryOrder.uid
            Log.debug("Confirming order $thisId")

            this@DeliveryOrder.status = OrderStatus.CONFIRMED.name
            this@DeliveryOrder.notesToCustomer = notesOutString
            this@DeliveryOrder.confirmedDate = currentUtc().toString()
        }
        notifyConfirmed(dbusProps)
    }

    fun declineOrder(notesOutString: String = "", dbusProps: RoutingProps) {
        tx {
            val thisId = this@DeliveryOrder.uid
            Log.debug("Declining order $thisId")

            this@DeliveryOrder.status = OrderStatus.DECLINED.name
            this@DeliveryOrder.notesToCustomer = notesOutString
            this@DeliveryOrder.declinedDate = currentUtc().toString()
        }

        notifyDeclined(dbusProps)
        expire()
    }

    fun stripePaymentLinksObject(): StripePaymentLinks.Entity? {
        Log.debug("Attempting to fetch StripePaymentLink")
        return when (val thisCryptoType = tx { cryptoType }) {
            CryptoType.STRIPE -> {
                tx {
                    StripePaymentLinks.Entity.find {
                        StripePaymentLinks.Table.deliveryOrder eq this@DeliveryOrder.id
                    }.toList()
                }.firstOrNull {
                    Log.debug("Evaluating StripePaymentLinks which match deliverOrder id")
                    it.isNotExpired()
                }
            }

            else -> {
                Log.warn("This is not a Stripe order, but rather ${thisCryptoType?.fullName}")
                null
            }
        }
    }

    fun stripePaymentUrl(): String? {
        return stripePaymentLinksObject()?.paymentUrl
    }

    val cartItems by CartItem optionalReferrersOn (CartItems.deliveryOrder)


    fun depositAddress(): DepositCryptoAddress? {
        return tx { invoice.depositAddress }
    }

    private fun feePercentDecimal(): BigDecimal {
        return tx { customer.storefront.feePercentDecimal() }
    }

    private fun feePercentFull(): BigDecimal {
        return tx {
            customer.storefront.feePercentFull()
        }
    }

    val allInvoices by Invoice optionalReferrersOn Invoices.order


    override fun expire(epoch: Long): Boolean {
        super.expire(epoch)

        tx {
            invoice.expire()
        }

        tx { allInvoices.toList() }.forEach {
            it.expire()
        }

        return true
    }

    private fun adminReceiptStr(
        verb: String = "created",
        thisCustomer: ShopCustomer = tx { customer },
        thisInvoice: Invoice = tx { invoice },
        feePctRounded: String = tx { feePercentFull().toString() },
        theseNotesToVendor: String = tx { notesToVendor },
        theseDeliveryNotes: String? = tx { notesToCustomer },
        thisVendor: ShopVendor = tx { shopVendor },
    ): String {
        val deliveryNoteStr = theseDeliveryNotes?.let {
            "\n\n • Delivery Notes: ``$theseDeliveryNotes``"
        } ?: ""
        val thisOrderCode = tx { orderCode }
        return """Order $thisOrderCode $verb
                | • ${thisVendor.toUserAndNameTag()}
                | • ${thisCustomer.toUserIdString()}
                | 
                | • Notes: ``$theseNotesToVendor``${deliveryNoteStr}
                | 
                | • Currency: ${tx { thisInvoice.cryptoType }}
                |
                | • Subtotal : ${thisInvoice.subtotalFiatStr}
                | • Fees     : ${thisInvoice.feesFiatStr} (${feePctRounded}%)
                | • Per Admin: ${tx { thisInvoice.splitFeesPerAdminAmount.prettyFiat(withSymbol = true) }} 
                | • Total    : ${thisInvoice.totalFiatStr}
            """.trimMargin()
    }

    private fun notifyAdmins(
        verb: String = "created",
        thisCustomer: ShopCustomer = tx { customer },
        thisInvoice: Invoice = tx { invoice },
        feePctRounded: String = tx { feePercentFull().toString() },
        theseNotesToVendor: String = tx { notesToVendor },
        theseDeliveryNotes: String? = tx { notesToCustomer },
        thisVendor: ShopVendor = tx { shopVendor },
        adminNotifier: AdminsNotifyProvider = AdminsNotifyProvider(envIn = RunConfig.environment),
    ) {
        adminReceiptStr(
            verb = verb,
            thisCustomer = thisCustomer,
            thisInvoice = thisInvoice,
            feePctRounded = feePctRounded,
            theseNotesToVendor = theseNotesToVendor,
            theseDeliveryNotes = theseDeliveryNotes,
            thisVendor = thisVendor
        ).let {
            adminNotifier.sendOutput(it)
        }
    }

    // potentially include type of tracking number being used
    @Deprecated("Clean this up immensely")
    override fun notifyCreated() {
        val thisEnv = RunConfig.environment

        var customerUser: User? = null
        var vendorUser: User? = null
        var thisCustomer: ShopCustomer? = null
        var thisVendor: ShopVendor? = null
        var thisStorefront: Storefront?
        var hasAutoConfirm = false

        tx {
            thisCustomer = customer
            customerUser = thisCustomer?.user
            thisStorefront = thisCustomer?.storefront
            hasAutoConfirm = thisStorefront?.hasAutoConfirm() == true
            thisVendor = shopVendor
            vendorUser = thisVendor?.user
        }

        if (thisCustomer == null || customerUser == null && vendorUser == null) {
            Log.warn("Customer or customerUser or vendorUser is null")
            return
        }

        val vendorReceipt = DeliveryOrder.displayLongString(this, ShopRole.VENDOR)

        customerUser?.let {
            val customerThanksMsg = """
            |Thank you. We are processing your request.
            |
            |Please wait a moment while we prepare your checkout information.
        """.trimMargin()

            //todo perhaps have smaller context objects just for interrupters, etc.

            sendInterrupt(user = it, role = ShopRole.CUSTOMER, environment = thisEnv, msg = customerThanksMsg)

            vendorUser?.let { it1 ->
                sendInterrupt(user = it1, role = ShopRole.VENDOR, environment = thisEnv, msg = vendorReceipt)
                sendNotify(
                    user = it1,
                    role = ShopRole.VENDOR,
                    environment = thisEnv,
                    msg = "You MUST use ${CmdRegistry.CONFIRM.name.uppercase()} to confirm " + "this order before you can receive payment."
                )
            }

            val adminNotifier: AdminsNotifyProvider = AdminsNotifyProvider(envIn = thisEnv)

            if (hasAutoConfirm) {
                adminNotifier.sendOutput("Order will be auto-confirmed...")

                // if AUTO-CONFIRM enabled, autoconfirm
                // add scanner util for auto-confirm orders

                // confirm order here...
                // should have method on DeliveryOrder for confirming directly

                return
            }


            notifyAdmins(adminNotifier = adminNotifier)

        }


    }

    private suspend fun notifyConfirmed(dbusProps: RoutingProps) {
        val scope = CoroutineScope(Dispatchers.IO + CoroutineName("notifyConfirmed"))


        val job = scope.launch {
            Log.debug("STARTING DELIVERY ORDER ASYNC NOTIFYCONFIRMED")
            val thisVendorUser = tx { this@DeliveryOrder.shopVendor.user }
//            val notes = tx { this@DeliveryOrder.notesToCustomer.toString() }

            val thisInvoice = tx {
                //todo eventually make invoice status obsolete
                invoice.status = InvoiceStatus.CONFIRMED.name
                invoice.order?.status = OrderStatus.CONFIRMED.name
                invoice
            }

            //todo add lazy loading for this and 'notifyDeclined'
            val thisCustomerProfile = tx { customer }

//            val customerMessage = CustomerStrings.orderConfirmed(
//                this@DeliveryOrder, notes
//            )

            notifyAdmins(verb = "confirmed", thisCustomer = thisCustomerProfile, thisInvoice = thisInvoice)

//            thisCustomerProfile.notify(customerMessage)

            //todo should do this on loop, in case retries needed when Wallet is not running
            //todo do this with suspend/await operation?

            //todo ultimately don't even need SessionPair here...

            val vendorDbusProps = DbusProps(roleIn = ShopRole.VENDOR, envIn = dbusProps.environment)

            thisInvoice.setUpPaymentReceivers(SessionContext(thisVendorUser, vendorDbusProps))
            Log.debug("DONE WITH DELIVERY ORDER ASYNC NOTIFYCONFIRMED")

        }
        job.join()
    }


    //should be able to fetch new notes, but just have here in case?
    @Deprecated("Clean up usage of transactions here")
    private fun notifyDeclined(dbusProps: RoutingProps) {
        var vendorUser: User? = null
        var customerUser: User? = null

        var vendorStr = ""
        var customerStr = ""
        var adminStr = ""

        tx {
            val thisInvoice = invoice
            thisInvoice.status = InvoiceStatus.DECLINED_BY_VENDOR.name

            val notes = this@DeliveryOrder.notesToCustomer.toString()

            val thisVendorProfile = shopVendor
            val thisCustomerProfile = customer

            val thisOrderCode = orderCode

            vendorUser = thisVendorProfile.user
            customerUser = thisCustomerProfile.user

            vendorStr = "$thisOrderCode declined."
            customerStr = PressAgent.CustomerStrings.orderDeclined(
                this@DeliveryOrder, notes
            )
            adminStr = """$thisOrderCode declined
                | • ${thisVendorProfile.toUserAndNameTag()}
                | • ${thisCustomerProfile.toUserIdString()}
                |
                | • Reason: ``${notes}``
            """.trimMargin()
        }


        vendorUser?.let {
            sendInterrupt(
                user = it,
                role = ShopRole.VENDOR,
                environment = dbusProps.environment,
                msg = vendorStr
            )
        }

        customerUser?.let {
            sendNotify(
                user = it,
                role = ShopRole.CUSTOMER,
                environment = dbusProps.environment,
                msg = customerStr
            )
        }

        AdminsNotifyProvider(envIn = dbusProps.environment).sendOutput(adminStr)
    }

    var ageNanoString: String = ""
        get() {
            val nanoAmt = UtilLegacy.getEpoch() - (UtilLegacy.isoToEpochNano(createdDate.toString()))
            return TimeAndDateProvider.getTimeAgoString(nanoAmt, prependValue = "")
        }

    var receipt: String? = null
        get() {
            return tx { invoice?.receipt }
        }

    var cryptoType: CryptoType? = null
        get() {
            return tx { invoice.cryptoType }
        }


}