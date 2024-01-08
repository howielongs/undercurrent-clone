package com.undercurrent.legacy.commands.executables.abstractcmds.remove_cmds

import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomer
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopVendor
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefronts
import com.undercurrent.shared.utils.time.unexpiredExpr
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.repository.entities.User
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and

interface HasStorefront {
    val thisStorefront: Storefront
}

interface CanFetchStorefront<T> {
    fun fetchStorefront(): Storefront?
    fun fetchStorefront(entity: T): Storefront?
}

interface CanFetchStorefronts {
    fun fetchStorefronts(vendor: ShopVendor): List<Storefront>
}

class StorefrontFetcher : CanFetchStorefronts {

    override fun fetchStorefronts(vendor: ShopVendor): List<Storefront> {
        val storefrontsFetchQuery = Storefronts.vendor eq vendor.id and unexpiredExpr(Storefronts)

        return tx {
            Storefront.find { storefrontsFetchQuery }.toList()
        }

    }
}

interface HasBackingStorefrontField<T> : HasStorefront, CanFetchStorefront<T>

interface HasCustomerField {
    val customer: ShopCustomer
}

interface CanFetchCustomer<T> {
    fun fetchCustomer(entity: T): ShopCustomer?
}

interface HasVendorField {
    val thisVendor: ShopVendor
}

interface HasVendorUserField {
    val thisVendorUser: User
}

interface HasCustomerUserField {
    val customerUser: User
}

interface CanFetchVendor<T> {
    fun fetchVendor(): ShopVendor?
    fun fetchVendor(entity: T): ShopVendor?
}


interface HasBackingVendorField<T> : HasVendorField, CanFetchVendor<T>



//interface CanFetchPendingOrders<F> {
//    suspend fun fetchPendingOrders(from: F): List<DeliveryOrder>
//}

//class PendingOrdersFromVendorFetcher : CanFetchPendingOrders<ShopVendor> {
//
//    override suspend fun fetchPendingOrders(from: ShopVendor): List<DeliveryOrder> {
//        val ordersFromCustomerFetcher = PendingOrdersFromCustomerFetcher()
//        var outOrders = ArrayList<DeliveryOrder>()
//        for (memberShip in from.fetchActiveCustomers()) {
//            outOrders.addAll(ordersFromCustomerFetcher.fetchPendingOrders(memberShip))
//        }
//        return tx { outOrders.sortedByDescending { it.uid } }
//    }
//}

//class PendingOrdersFromCustomerFetcher : CanFetchPendingOrders<CustomerProfile> {
//
//    override suspend fun fetchPendingOrders(from: CustomerProfile): List<DeliveryOrder> {
//
//        val statusMatcher = StatusMatchChecker()
//
//
//        return transaction {
//            from.allOrders.filter {
//                it.isNotExpired() &&
//                        statusMatcher.statusDoesNotMatchAny(
//                            it,
//                            OrderStatus.CANCELED,
//                            OrderStatus.DECLINED,
//                            OrderStatus.SHIPPED,
//                            OrderStatus.NEW
//                        )
//            }
//        }
//    }
//}


//interface CanFetchPendingInvoices<F> {
//    fun fetchPendingInvoices(from: F): List<Invoice>
//}

//class PendingInvoicesFetcher : CanFetchPendingInvoices<ShopVendor> {
//    override fun fetchPendingInvoices(from: ShopVendor): List<Invoice> {
//        return tx {
//            from.pendingInvoices
//        }
//    }
//}

//todo impl this
//class StorefrontProductsDisplayer(
//    val storefront: Storefront,
//    val dbusProps: DbusProps
//) {
//    suspend fun displayProducts() {
//        displayProductsWithSkus(
//            requesterId = tx { storefront.user.uid },
//            displayItemlessProducts = true,
//            displayAttachments = true,
//            dbusProps = dbusProps,
//        )
//    }

//    /**
//     * Displays each product with attachments and SKUs
//     */
//    suspend fun displayProductsWithSkus(
//        requesterId: Int,
//        productsList: List<Product> = listOf(),
//        displayItemlessProducts: Boolean = false,
//        displayMenuAsSegments: Boolean = false,
//        displayAttachments: Boolean = true,
//        dbusProps: RoutingProps,
//    ) {
//        with(storefront) {
//            val thisUser = tx { User.findById(requesterId) } ?: run {
//                "User #${requesterId} not found on " +
//                        "DisplayProducts".let {
//                            Log.error(it)
//                            notifyAdmins(
//                                msg = it,
//                                subject = SubjectHeader.ERROR,
//                                emoji = EmojiSymbol.AMBULANCE,
//                                routingProps = dbusProps
//                            )
//                        }
//                return
//            }
//
//            //prevent multiple queries if possible
//            val theseProducts = productsList.ifEmpty {
//                tx { products }
//            }
//
//            with(theseProducts) {
//                if (this.isEmpty()) {
//                    sendInterrupt(
//                        user = thisUser,
//                        role = Rloe.VENDOR,
//                        environment = dbusProps.environment,
//                        msg = "No products found"
//                    )
//                    return
//                }
//                this.forEachIndexed { index, product ->
//                    with(tx { product.saleItems }) {
//                        if (this?.isNotEmpty() ?: false || displayItemlessProducts) {
//                            //todo fix up all theses excessive transactions
//                            val productName = tx { product.name }
//                            val productDetails = tx { product.details }
//
//                            if (displayAttachments) {
//                                val captionText = "${productName.uppercase()} > $productDetails"
//                                val linkedAttachments =
//                                    tx { product.linkedAttachments }
//                                linkedAttachments.forEach { attachment ->
//                                    attachment.send(
//                                        recipientUser = thisUser,
//                                        captionText = captionText
//                                    )
//                                }
//                            }
//                            if (displayMenuAsSegments) {
//                                val saleItemsString = product.getSaleItemsListString(
//                                    index + 1, this,
//                                    indexType = com.undercurrent.legacy.types.enums.ListIndexTypeOld.BULLET
//                                )
//                                thisUser?.interrupt(
//                                    saleItemsString
//                                )
//                                if (!com.undercurrent.legacy.routing.RunConfig.isTestMode) {
//                                    //todo this is smelly
//                                    java.lang.Thread.sleep(1000)
//                                }
//                            }
//                        }
//                    }
//                }
//                if (!displayMenuAsSegments) {
//                    if (!RunConfig.isTestMode) {
//                        //todo this is smelly
//                        Thread.sleep(1000)
//                    }
//                    thisUser?.interrupt(
//                        selectableInventoryString(this, displayItemlessProducts)
//                    )
//                }
//            }
//        }
//    }
//}