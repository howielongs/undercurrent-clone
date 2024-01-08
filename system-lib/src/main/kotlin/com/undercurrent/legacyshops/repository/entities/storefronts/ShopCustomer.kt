package com.undercurrent.legacyshops.repository.entities.storefronts

import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.data_transfer_objects.currency.FiatAmount
import com.undercurrent.legacy.dinosaurs.prompting.selectables.OptionSelector
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectableEntity
import com.undercurrent.legacy.repository.entities.payments.DepositCryptoAddress
import com.undercurrent.legacy.repository.entities.payments.DepositCryptoAddresses
import com.undercurrent.legacy.routing.RunConfig
import com.undercurrent.legacy.types.enums.CryptoType
import com.undercurrent.legacy.types.enums.ListIndexTypeOld
import com.undercurrent.legacy.types.enums.currency.CurrencyLegacyInterface
import com.undercurrent.legacy.types.enums.status.ActiveMutexStatus
import com.undercurrent.legacy.types.enums.status.OrderStatus.*
import com.undercurrent.legacy.utils.UtilLegacy
import com.undercurrent.legacyshops.repository.companions.CustomerCompanion
import com.undercurrent.legacyshops.repository.entities.shop_items.CartItem
import com.undercurrent.legacyshops.repository.entities.shop_items.CartItems
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrder
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrders
import com.undercurrent.shared.HasUserEntity
import com.undercurrent.shared.repository.dinosaurs.EntityHasStatusField
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.shared.repository.dinosaurs.entityHasStatus
import com.undercurrent.shared.types.enums.ShopRole.CUSTOMER
import com.undercurrent.shared.utils.time.unexpiredExpr
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.messaging.outbound.sendNotify
import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.repository.entities.Users
import kotlinx.coroutines.coroutineScope
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.math.RoundingMode

object ShopCustomers : ExposedTableWithStatus2("shop_customers") {
    val storefront = reference("storefront_id", Storefronts)
    val user = reference("user_id", Users)

    override fun singularItem(): String {
        return "Customer"
    }

}

class ShopCustomer(id: EntityID<Int>) : ExposedEntityWithStatus2(id, ShopCustomers), EntityHasStatusField,
    HasUserEntity<User> {
    companion object : CustomerCompanion()

    override var user by User referencedOn ShopCustomers.user
    var storefront by Storefront referencedOn ShopCustomers.storefront

    private val allOrders by DeliveryOrder referrersOn DeliveryOrders.customer

    override fun hasStatus(status: String): Boolean {
        return entityHasStatus(status)
    }


    fun depositAddress(currencyInterface: CurrencyLegacyInterface): DepositCryptoAddress? {
        return transaction {
            DepositCryptoAddress.find { DepositCryptoAddresses.user eq user.id }
                .filter { it.isNotExpired() && it.cryptoType.toString() == currencyInterface.abbrev() }
                .maxByOrNull { it.id }
        }
    }


    suspend fun nudgeSendPayment(
        order: DeliveryOrder,
        negativeAmount: BigDecimal,
        currencyInterface: CurrencyLegacyInterface
    ) = coroutineScope {

        // this will be different depending on currency
        tx { order.invoice.lastNudgedTimestamp = UtilLegacy.getEpoch() }

        val outAmt = UtilLegacy.roundBigDecimal(
            negativeAmount.abs(),
            currencyInterface = currencyInterface
        )

        //todo should pull this block out
        when (currencyInterface) {
            CryptoType.BTC -> {
                depositAddress(currencyInterface)?.let {
                    tx { it.address }?.let {
                        notify(
                            tx {
                                "Order ${order.orderCode}\n\n" +
                                        "USE THE NEXT MESSAGES TO COMPLETE PAYMENT: " +
                                        "\n\nFIRST: COPY & PASTE payment address into " +
                                        "${currencyInterface.fullName} wallet app of your choice, " +
                                        "such as Cash App or Coinbase.\n" +
                                        "\nSECOND: COPY & PASTE ${currencyInterface.fullName} into your wallet as the amount to send (do not use USD).\n\n" +
                                        "Use `${CmdRegistry.OPENORDERS.upper()}` to see details of your order(s)."
                            }
                        )
                        //add enough of a pause here...
                        notify("$it")
                        notify("$outAmt")
                    }
                }
            }

            CryptoType.MOB -> {
                val roundedAmt = outAmt.divide(
                    BigDecimal(1),
                    2,
                    RoundingMode.CEILING
                )

                notify(
                    "Please send ${roundedAmt} MOB to me by simply attaching it to a message and sending."
                )

            }

            CryptoType.STRIPE -> {
                tx { order.stripePaymentUrl() }?.let {
                    notify(
                        "Thank you for your order.\n\n" +
                                "Your confirmation code is: ${tx { order.orderCode }}\n\n" +
                                "We will process this order for you shortly. You will receive a tracking number when your order has shipped."
                    )
                    notify(
                        "TO PAY: " +
                                "\n\n $it" +
                                "\n\nUse `${CmdRegistry.OPENORDERS.upper()}` to see details of your order(s)."
                    )
                }
            }

            else -> {}
        }
    }


    //todo pull method out
    fun makeCurrent() {
        tx {
            user.customerProfiles
                .filter { it.status == ActiveMutexStatus.CURRENT.name }
                .forEach { it.status = ActiveMutexStatus.ACTIVE.name }
            this@ShopCustomer.status = ActiveMutexStatus.CURRENT.name
        }
    }


    var statusEnum: ActiveMutexStatus? = null
        get() {
            return tx {
                ActiveMutexStatus.values().forEach {
                    if (it.name == this@ShopCustomer.status) {
                        return@tx it
                    }
                }
                return@tx null
            }
        }


    var displayName: String? = null
        get() {
            return transaction { storefront?.displayName }
        }

    var joinCode: String? = null
        get() {
            return transaction { storefront?.joinCode }
        }

    var shopVendor: ShopVendor? = null
        get() {
            return transaction {
                storefront?.vendor?.takeIf {
                    it.isNotExpired()
                }
            }
        }


    var pendingOrders: List<DeliveryOrder> = listOf()
        get() {
            return transaction {
                allOrders.filter {
                    it.isNotExpired() &&
                            it.statusDoesNotMatchAny(
                                CANCELED,
                                DECLINED,
                                SHIPPED,
                                NEW,
                            )
                }
            }
        }

    fun switchShopOutLineString(): String {
        return transaction {
            val currentStr = if (status.uppercase() == ActiveMutexStatus.CURRENT.name.uppercase()) {
                " (CURRENT)"
            } else {
                ""
            }
            //consider also adding date joined the shop
            "$displayName - $joinCode$currentStr"
        }
    }

    var unconfirmedOrders: List<DeliveryOrder> = listOf()
        get() {
            return transaction {
                allOrders.filter {
                    it.entityHasStatus(SUBMITTED.name)
                }
            }
        }


    var cartSubtotal: FiatAmount? = null
        get() = transaction { CartItems.getSubtotal(user) }


    var cartContents: List<CartItem> = listOf()
        get() {
            return transaction {
                CartItem.find {
                    CartItems.customer eq this@ShopCustomer.id.value and (
                            CartItems.quantity greater 0 and (CartItems.deliveryOrder eq null) and unexpiredExpr(
                                CartItems
                            )
                            )
                }.toList()
            }
        }


    fun moveCartItemsToOrder(newOrder: DeliveryOrder): Int {
        return transaction {
            var count = 0

            cartContents.forEach {
                it.deliveryOrder = newOrder
                count++
            }
            count
        }
    }

    data class Cart(
        val items: List<CartItem> = listOf(),
        val receiptText: String = CartItems.emptyListString()
    ) {
        fun isEmpty(): Boolean {
            return items.isNullOrEmpty()
        }

        fun isNotEmpty(): Boolean {
            return !isEmpty()
        }
    }

    var cart: Cart = Cart()
        get() {
            return getCartObject()
        }

    private fun getCartObject(): Cart {
        return transaction {
            with(cartContents) {
                Cart(this, getCartContentsString(this))
            }
        }
    }

    fun getCartContentsString(
        cartContents: List<CartItem>,
        receiptFooter: String? = null
    ): String {
        with(cartContents) {
            if (this.isEmpty()) {
                return CartItems.emptyListString()
            }

            val selectableCartItems = cartContents.map { SelectableEntity(it) }

            val result: String? = if (selectableCartItems.isEmpty()) {
                null
            } else {
                OptionSelector(
                    options = selectableCartItems,
                    headerText = "YOUR CART:",
                    indexType = ListIndexTypeOld.BULLET,
                    isSelectable = false,
                    footerText = receiptFooter ?: generateReceiptFooter(),
                    headlineText = null,
                ).let {
                    it.promptString
                }
            }

            result?.let { return it }
                ?: return CartItems.emptyListString()
        }
    }

    fun generateReceiptFooter(
        subtotalAmount: FiatAmount? = null,
        feesAmount: FiatAmount? = null,
        totalAmount: FiatAmount? = null,
    ): String {
        val subtotal: FiatAmount = subtotalAmount ?: CartItems.getSubtotal(this) ?: FiatAmount(0)
        val fees: FiatAmount = feesAmount ?: subtotal.multiply(transaction { storefront.feePercentDecimal() })
        val total: FiatAmount = totalAmount ?: subtotal.add(fees)


        return """
            |TOTALS
            |Subtotal: ${"\t"} ${subtotal.prettyFiat(withSymbol = true)}
            |Fees: ${"\t\t\t"} ${fees.prettyFiat(withSymbol = true)}
            |Total: ${"\t\t\t"} ${total.prettyFiat(withSymbol = true)}
        """.trimMargin()
    }


    fun toUserIdString(): String {
        return transaction { "Customer: User #${user.uid}" }
    }

    @Deprecated("Impl like helper")
    fun infoString(): String {
        return transaction { "${storefront?.displayName} (Join code: $joinCode)" }
    }

    fun notify(customerMsg: String) {
        sendNotify(
            user = transaction {
                this@ShopCustomer.user
            },
            role = CUSTOMER,
            environment = RunConfig.environment,
            msg = customerMsg
        )
    }

}

