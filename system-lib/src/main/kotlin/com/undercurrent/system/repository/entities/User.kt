package com.undercurrent.system.repository.entities

import com.undercurrent.legacy.routing.RunConfig
import com.undercurrent.legacy.types.enums.status.ActiveMutexStatus
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrder
import com.undercurrent.legacyshops.repository.entities.shop_orders.Invoice
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomer
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomers
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopVendor
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopVendors
import com.undercurrent.shared.UserWithSms
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.dinosaurs.EntityWithLabels1
import com.undercurrent.shared.repository.dinosaurs.TableWithLabels1
import com.undercurrent.shared.repository.dinosaurs.entityHasStatus
import com.undercurrent.shared.repository.entities.SignalSms
import com.undercurrent.shared.repository.entities.UserEntity
import com.undercurrent.shared.repository.entities.UserTable
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.VARCHAR_SIZE
import com.undercurrent.shared.utils.ctx
import com.undercurrent.shared.utils.time.EpochNano
import com.undercurrent.shared.utils.time.unexpiredExpr
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.context.DbusProps
import com.undercurrent.system.messaging.outbound.sendInterrupt
import com.undercurrent.system.messaging.outbound.sendNotify
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

object Users : TableWithLabels1("system_users"), UserTable {
    override var smsNumber: Column<String> = varchar("sms", VARCHAR_SIZE).uniqueIndex()
    override val role = varchar("role", VARCHAR_SIZE).default(ShopRole.CUSTOMER.name)
    override var uuid = varchar("uuid", VARCHAR_SIZE).nullable()


    fun fetchBySms(smsNumber: String): User? {
        return fetchBySms(SignalSms(smsNumber))
    }


    fun fetchBySms(smsNumber: SignalSms): User? {
        return tx {
            User.find { Users.smsNumber eq smsNumber.value }.firstOrNull()
        }
    }


}

class User(id: EntityID<Int>) : EntityWithLabels1(id, Users), UserWithSms, UserEntity {
    companion object : RootEntityCompanion0<User>(Users)

    val admins by AdminProfile referrersOn Admins.user
    val vendors by ShopVendor referrersOn ShopVendors.user
    val customers by ShopCustomer referrersOn ShopCustomers.user


    override var smsNumber by Users.smsNumber
    override var role by Users.role.transform(toColumn = { it?.name ?: ShopRole.CUSTOMER.name },
        toReal = { it?.let { it1 -> ShopRole.valueOf(it1) } ?: ShopRole.CUSTOMER })
    override var uuid by Users.uuid

    override val userSms: SignalSms by lazy {
        SignalSms(tx { smsNumber })
    }


    var hasAdminProfile: Boolean = false
        get() {
            return transaction { thisAdminProfile?.isNotExpired() ?: false }
        }

    //    val thisAdmin: Admin by Admin backReferencedOn Admins.user
    private var thisAdminProfile: AdminProfile? = null
        get() {
            return transaction {
                admins.firstOrNull()
            }
        }


    var hasVendorProfile: Boolean = false
        get() = transaction {

            shopVendor?.isNotExpired() ?: false
        }


    //Each User can only have one Vendor, but a Vendor can have multiple storefronts
//Same with Customers
//    val thisVendor: Vendor? by Vendor backReferencedOn Vendors.user

    private suspend fun fetchShopVendors(): List<ShopVendor> {
        return ctx {
            ShopVendor.find {
                ShopVendors.user eq this@User.id and unexpiredExpr(ShopVendors)
            }.toList()
        }
    }

    suspend fun fetchVendor(): ShopVendor? {
        return fetchShopVendors().firstOrNull()
    }

    var shopVendor: ShopVendor? = null
        get() {
            val now = EpochNano()
            return tx {
                ShopVendor.find {
                    unexpiredExpr(ShopVendors, now.value) and (
                            ShopVendors.user eq this@User.id)
                }
                    .limit(1).firstOrNull()

            }
        }

    //figure out how to better do this referrers thing
//    val theseCustomers by Customer referrersOn Customers.user
    var customerProfiles: List<ShopCustomer> = listOf()
        get() {
            return transaction {
                ShopCustomer.find { ShopCustomers.user eq this@User.uid and (unexpiredExpr(ShopCustomers)) }
                    .toList()
            }
        }

    fun oneLinerString(showLastActive: Boolean): String {
        return tx {
            "User #$uid"
        }
    }

    var currentCustomerProfile: ShopCustomer? = null
        get() {
            //todo consider that 'singleOrNull' may be desired to indicate an issue with more than one current customer
            //todo throw exception if more than one?
            return transaction { customerProfiles.firstOrNull { it.entityHasStatus(ActiveMutexStatus.CURRENT.name) } }
        }

    @Deprecated("Impl like helper")
    var activeCustomerProfiles: List<ShopCustomer> = listOf()
        get() {
            return tx {
                customerProfiles.filter {
                    setOf(
                        ActiveMutexStatus.ACTIVE.name, ActiveMutexStatus.CURRENT.name
                    ).contains(it.status)
                }
            }
        }

    var pendingOrders: List<DeliveryOrder> = listOf()
        get() {
            return transaction {
                var outOrders = ArrayList<DeliveryOrder>()
                activeCustomerProfiles.forEach {
                    outOrders.addAll(it.pendingOrders)
                }
                outOrders
            }
        }

    var pendingInvoices: List<Invoice> = listOf()
        get() = transaction { pendingOrders.map { it.invoice } }


    @Deprecated("Pass in better dbus context")
    fun interrupt(msg: String, role: AppRole, environment: Environment) {
        sendInterrupt(
            user = this,
            role = role,
            environment = environment,
            msg = msg
        )
    }

    fun interrupt(msg: String, dbusProps: DbusProps) {
        sendInterrupt(
            user = this,
            role = dbusProps.role,
            environment = dbusProps.environment,
            msg = msg
        )
    }


    /**
     * Sends message after user's current conversation is finished (all other OUTBOX messaging finished)
     */
    @Deprecated("Pass in better dbus context")
    fun notify(msg: String, role: AppRole) {
        sendNotify(
            user = this,
            role = role,
            environment = RunConfig.environment,
            msg = msg
        )
    }


    override fun toString(): String {
        return """ User #$uid
        | SMS: $smsNumber
        | Role: $role
        |
        """.trimMargin()
    }
}

