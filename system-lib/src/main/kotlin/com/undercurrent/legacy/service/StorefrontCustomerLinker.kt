package com.undercurrent.legacy.service


import com.undercurrent.legacy.commands.registry.CmdRegistry.MENU
import com.undercurrent.legacy.repository.entities.system.IntroEvents
import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.messaging.outbound.notifyAdmins
import com.undercurrent.legacy.types.enums.status.ActiveMutexStatus
import com.undercurrent.legacy.utils.fetchLastActiveStr
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCodeUsages
import com.undercurrent.legacyshops.repository.entities.shop_items.ShopProduct
import com.undercurrent.legacyshops.repository.entities.shop_items.SaleItem
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomer
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopVendor
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefronts.displayName
import com.undercurrent.shared.formatters.UserToIdString
import com.undercurrent.shared.messages.CanSendToUserByRole
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.ctx
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.context.SessionContext
import org.jetbrains.exposed.sql.transactions.transaction

interface LinkableToStorefront {
    suspend fun linkUserToStorefront(joinCode: String?): Boolean
}

class StorefrontCustomerLinker(
    val context: SessionContext,
    val storefront: Storefront,
) : LinkableToStorefront, CanSendToUserByRole {
    val products: List<ShopProduct> by lazy {
        storefront.products
    }
    private val interrupter by lazy {
        context.interrupter
    }

    override fun sendOutputByRole(msgBody: String, role: AppRole) {
        interrupter.sendOutputByRole(msgBody, role)
    }

    private val saleItems: List<SaleItem> by lazy {
        //potentially pass in Products to ease this query
        storefront.saleItems
    }


    override suspend fun linkUserToStorefront(joinCode: String?): Boolean {
        val thisVendor: ShopVendor? = tx { context.user.shopVendor }
        val thisJoinCode = tx { storefront.joinCode }
        val thisWelcomeMsg = tx { storefront.welcomeMsg }


        val existingCustomer = ctx {
            Storefront.fetchActiveCustomers(storefront).singleOrNull { it.user.id == context.user.id && it.isNotExpired() }
        }

        val displayVendorWelcome = existingCustomer?.let {
            linkExistingCustomerToStorefront(
                context, it, joinCode?.trim()?.replace("/", "")
                    ?.replace(" ", "")?.uppercase()
            )
        } ?: run {
            thisVendor?.let {
                addCustomer(
                    context.user, it, joinCode?.trim()?.replace("/", "")
                        ?.replace(" ", "")?.uppercase() ?: thisJoinCode.trim().replace("/", "")
                        .replace(" ", "").uppercase()
                )
            }
            IntroEvents.Table.displayWelcomeIfUnseen(context)
            true
        }

        if (displayVendorWelcome) {
            //todo add custom message here
            // should be pulling in storefront or vendor displayName?
            // for now just have it as the storefront name

            context.interrupt(thisWelcomeMsg)
        }
        return false
    }


    private fun linkExistingCustomerToStorefront(
        sessionContext: SessionContext,
        existingCustomerProfile: ShopCustomer,
        joinCode: String?,
    ): Boolean {
        val thisJoinCode = joinCode ?: transaction { existingCustomerProfile.joinCode }
        var displayWelcome = false

        sendOutputByRole(
            //todo update statusEnum to use transform
            msgBody = when (existingCustomerProfile.statusEnum) {
                ActiveMutexStatus.INACTIVE -> {
                    "Link to this vendor`s storefront is unavailable: ${thisJoinCode}\n" +
                            "Contact your friendly neighborhood system administrator."
                }

                ActiveMutexStatus.CURRENT -> {
                    "This is already your current vendor storefront: ${thisJoinCode}\n" +
                            "Use `$MENU` to shop for available items, or `/help` for more commands."
                }

                ActiveMutexStatus.ACTIVE -> {
                    existingCustomerProfile.makeCurrent()
                    displayWelcome = true
                    "Set as current vendor storefront: ${thisJoinCode}. \n\n" +
                            "${ShopCustomer.activeStorefrontsToString(sessionContext.user)}" +
                            "Use `${MENU.lower()}` to shop for available items, or `/help` for more commands."
                }

                else -> {
                    "Invalid vendor storefront"
                }
            }, role = ShopRole.CUSTOMER
        )
        return displayWelcome
    }


    private fun addCustomer(
        thisUser: User,
        thisVendor: ShopVendor,
        joinCodeUsed: String
    ): ShopCustomer {
        val thisStorefront = storefront
        val customerCount = tx { storefront.activeCustomerCount }

        val thisCustomer = ShopCustomer.save(
            userIn = thisUser,
            storefrontIn = thisStorefront
        )

        val cleanedCode = joinCodeUsed.trim().replace("/", "")
            .replace(" ", "").uppercase()

        val joinCodeUsageEvent = JoinCodeUsages.save(thisUser, cleanedCode)?.let {
            Log.debug("Logged usage of code ${cleanedCode}")
            it
        }

        val totalUsages = tx { joinCodeUsageEvent?.fetchTotalUsages() } ?: 0

        val totalUsageStr =
            "Customers who have used this code: $totalUsages"


        thisVendor.notify(
            "New customer linked to your store using ${tx { joinCodeUsageEvent?.joinCode }}.\n" +
                    "Current customer count: ${customerCount}\n$totalUsageStr"
        )

        //todo consider using await for this?
        // could also include stats like previous orders, etc.
        // attach emoji alongside status by time
        //storefront: last order, num orders fulfilled (in time period), num products
        val adminStr = tx {
            val storeDisplayNameStr = if (thisStorefront.displayName == thisStorefront.joinCode) {
                ""
            } else {
                " - $displayName"
            }

            val cryptoList = thisVendor
                .cryptoAddresses.joinToString(", ") { it.type.uppercase() }

            var userLastActiveStr = transaction { fetchLastActiveStr() }
            var vendorLastActiveStr = transaction { fetchLastActiveStr() }

            //todo pull these into common util
            userLastActiveStr = if (userLastActiveStr == "") {
                ""
            } else {
                "\n • $userLastActiveStr"
            }

            vendorLastActiveStr = if (vendorLastActiveStr == "") {
                ""
            } else {
                "\n • $vendorLastActiveStr"
            }

            return@tx """
            |New customer added to storefront
            |
            |Customer: ${UserToIdString.toIdStr(thisUser)}
            | • Shops joined: ${thisUser.activeCustomerProfiles.count()}${userLastActiveStr}
            |
            |${thisVendor.toUserAndNameTag()}
            | • Total shops: ${thisVendor.activeStorefronts.count()}
            | • Total customers: ${thisVendor.activeCustomerCountInt}${vendorLastActiveStr}
            |
            |${UserToIdString.toIdStr(storefront)}$storeDisplayNameStr
            | • Join code: ${storefront.joinCode}
            | • Payment methods: $cryptoList
            | • Customer count: $customerCount
            | • Products: ${products.count()}
            | • SKUs: ${saleItems.count()}
            |
            |Last users active:
            |${storefront.userActivityMap()}
            """.trimMargin()
        }
        Log.debug("Linked user ${UserToIdString.toIdStr(thisUser)} and ${UserToIdString.toIdStr(storefront)}")

        thisUser.interrupt(
            msg = "Linked and set as current vendor storefront: $this\n\n" +
                    "${ShopCustomer.activeStorefrontsToString(thisUser)}",
            ShopRole.CUSTOMER,
            context.environment
        )

        adminStr.let { notifyAdmins(it) }
        return thisCustomer
    }


}

