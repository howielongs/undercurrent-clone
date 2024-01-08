package com.undercurrent.legacyshops.repository.entities.storefronts

import com.undercurrent.legacy.commands.executables.abstractcmds.remove_cmds.CanFetchCryptoAddresses
import com.undercurrent.legacy.commands.executables.abstractcmds.remove_cmds.UserCryptoAddressFetcher
import com.undercurrent.legacy.repository.entities.payments.CryptoAddress
import com.undercurrent.legacy.repository.entities.payments.CryptoAddresses
import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.repository.entities.Users
import com.undercurrent.legacy.repository.entities.system.attachments.AttachmentLinks
import com.undercurrent.legacy.routing.RunConfig
import com.undercurrent.legacy.types.enums.AttachmentType
import com.undercurrent.legacy.types.enums.status.ActiveMutexStatus
import com.undercurrent.legacy.types.enums.status.OrderStatus
import com.undercurrent.legacy.utils.fetchLastActiveStr
import com.undercurrent.legacyshops.repository.companions.ShopVendorCompanion
import com.undercurrent.legacyshops.repository.entities.shop_orders.Invoice
import com.undercurrent.legacyshops.repository.entities.shop_items.ShopProduct
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrder
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrders
import com.undercurrent.shared.repository.dinosaurs.*
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.VARCHAR_SIZE
import com.undercurrent.shared.utils.time.unexpiredExpr
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.messaging.outbound.sendNotify
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Each User can only have one Vendor, but a Vendor can have multiple storefronts (Same with Customers)
 */
object ShopVendors : ExposedTableWithStatus2("shop_vendors"), HasSingularLabel, HasPluralLabel {

    val user = reference("user_id", Users)
    val nickname = varchar("nickname", VARCHAR_SIZE)

    override fun singularItem(): String {
        return "Vendor"
    }

    fun fetchBySms(sms: String): ShopVendor? {
        return transaction { Users.fetchBySms(sms)?.shopVendor }
    }


}

//@Deprecated("Legacy version. Replace with newer service version")
open class ShopVendor(id: EntityID<Int>) : ExposedEntityWithStatus2(id, ShopVendors),
    CanFetchCryptoAddresses<CryptoAddress> {
    companion object : ShopVendorCompanion()

    var nickname by ShopVendors.nickname
    var user by User referencedOn ShopVendors.user

    val deliveryOrders by DeliveryOrder referrersOn (DeliveryOrders.vendor)

    var pdfMenus: List<AttachmentLinks.Entity> = listOf()
        get() {
            return AttachmentLinks.fetchByType(
                transaction { user.uid },
                ShopRole.VENDOR,
                AttachmentType.SHOP_MENU,
            )
        }

    @Deprecated("Use separate class for this")
    fun broadcastToActiveCustomers(msg: String) {
        transaction { activeStorefronts }.let {
            it.forEach { storefront ->
                storefront.broadcastToActiveCustomers(msg)
            }
        }
    }

    private val storefrontsFetchQuery = Storefronts.vendor eq this@ShopVendor.id and unexpiredExpr(Storefronts)

    var allStorefronts: List<Storefront> = listOf()
        get() {
            return transaction {
                Storefront.find { storefrontsFetchQuery }.toList()
            }
        }

    var activeCustomerCountInt: Int = 0
        get() {
            return fetchActiveCustomers().count()
        }

    var products: List<ShopProduct> = listOf()
        get() {
            return transaction { currentStorefront?.products ?: listOf() }
        }

    var payableOrders: List<DeliveryOrder> = listOf()
        get() {
            return transaction {
                pendingOrders.filter {
                    it.status == OrderStatus.AWAITING_PAYMENT.toString()
                }
            }
        }

    var shippableOrders: List<DeliveryOrder> = listOf()
        get() {
            return transaction {
                pendingOrders.filter {
                    it.status == OrderStatus.AWAITING_SHIPMENT.toString()
                }
            }
        }

    fun fetchPendingOrders(): List<DeliveryOrder> {
        return transaction {
            var outOrders = ArrayList<DeliveryOrder>()
            for (memberShip in fetchActiveCustomers()) {
                outOrders.addAll(memberShip.pendingOrders)
            }
            outOrders.sortedByDescending { it.uid }
        }
    }

    private var pendingOrders: List<DeliveryOrder> = listOf()
        get() {
            return fetchPendingOrders()
        }


    //todo run through this as test and see what the deal might be
    private fun fetchActiveCustomers(): MutableList<ShopCustomer> {
        val customerProfiles = mutableListOf<ShopCustomer>()
        transaction { activeStorefronts }.forEach { storefront ->
            transaction { storefront.activeCustomerProfiles }?.let {
                customerProfiles.addAll(it)
            }
        }
        return customerProfiles
    }

    var activeStorefronts: List<Storefront> = listOf()
        get() {
            return transaction {
                allStorefronts.filter {
                    entityHasStatus(ActiveMutexStatus.CURRENT.name) || entityHasStatus(ActiveMutexStatus.ACTIVE.name)
                }
            }
        }

    private val cryptoAddrFetcher: CanFetchCryptoAddresses<CryptoAddress> = UserCryptoAddressFetcher()

    override fun fetchCryptoAddresses(thisUser: User): List<CryptoAddress> {
        return cryptoAddrFetcher.fetchCryptoAddresses(thisUser)
    }

    //todo replace this with address fetcher
    var cryptoAddresses: List<CryptoAddress> = listOf()
        get() {
            return tx {
                CryptoAddress.find {
                    CryptoAddresses.user eq user.id and (unexpiredExpr(CryptoAddresses))
                }.toList()
            }
        }

    var currentStorefront: Storefront? = null
        get() {
            // consider that 'singleOrNull' may be desired to indicate an issue with more than one current customer
            return transaction { allStorefronts.firstOrNull { it.entityHasStatus(ActiveMutexStatus.CURRENT.name) } }
        }

    fun fetchPendingInvoices(): List<Invoice> {
        val pendingOrders = fetchPendingOrders()
        return tx { pendingOrders.map { it.invoice }.sortedByDescending { it.uid } }
    }

    var pendingInvoices: List<Invoice> = listOf()
        get() = transaction { pendingOrders.map { it.invoice }.sortedByDescending { it.uid } }


    var joinCode: String? = null
        get() {
            return transaction { currentStorefront?.joinCode }
        }

    @Deprecated("Use separate class for this")
    fun toUserAndNameTag(withHeader: Boolean = true): String {
        val header = if (withHeader) {
            "Vendor: "
        } else {
            ""
        }
        return transaction { "${header}User #${user.uid} - $nickname" }
    }

    @Deprecated("Use separate class for this")
    fun notify(msg: String) {
        tx { user }?.let {
            sendNotify(it, ShopRole.VENDOR, RunConfig.environment, msg)
        }
    }

    //todo add header and fix up code in lower levels from here

    override fun toString(): String {
        var thisUser = transaction { user }

        val sms = transaction { thisUser.smsNumber }
        var tagString = ""
        val customers = fetchActiveCustomers()
        val thisJoinCode = transaction { joinCode }

        val thisNameTag = transaction { nickname }


        //todo replace with takeIf{ }
        if (thisNameTag != null && thisNameTag != "") {
            tagString = "($thisNameTag)"
        }


        var appendStr = if (thisUser != null) {
            "\t${fetchLastActiveStr()}\n"
        } else {
            ""
        }

        //todo will want to separate by more storefronts (not needed right now)
        //right now this will display the joincode for the current storefront
        return "${sms}: $thisJoinCode $tagString\n" +
                "\tCustomers: ${customers.size}\n" +
                appendStr

    }

}

