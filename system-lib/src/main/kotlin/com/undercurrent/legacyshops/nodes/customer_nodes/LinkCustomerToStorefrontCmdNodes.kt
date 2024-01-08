package com.undercurrent.legacyshops.nodes.customer_nodes

import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.repository.entities.system.IntroEvents
import com.undercurrent.system.messaging.outbound.notifyAdmins
import com.undercurrent.legacy.service.fetchers.JoinCodeFetch
import com.undercurrent.legacy.types.enums.status.ActiveMutexStatus
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCode
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCodeUsages
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomer
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomers
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.legacyshops.repository.entities.storefronts.StorefrontFetcherByJoinCode
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.astx
import com.undercurrent.shared.utils.cleanInboundCommand
import com.undercurrent.shared.utils.tx
import com.undercurrent.shared.view.treenodes.TreeNode
import com.undercurrent.legacyshops.util.NewCustomerNotifyStringBuilders
import com.undercurrent.system.context.SystemContext
import com.undercurrent.system.command_execution.CoreNodes
import com.undercurrent.system.messaging.outbound.EntityNotifier
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and

/**
 * Pieces of this:
 * 0. Fetch joinCode entity for incoming string
 * 1. Fetching storefront to match join code (if it exists)
 * 2. Fetching existing customer to match user and storefront
 * 3. Creating new customer if none exists
 * 4. Linking customer to storefront
 * 5. Sending welcome message to customer
 * 6. Sending new customer linkage message to vendor
 * 7. Updating usage of join code (if it exists)
 */
class LinkCustomerToStorefrontCmdNodes(
    context: SystemContext,
    val body: String,
    joinCodeCleaner: (String) -> String = {
        if (it.contains("Join my shop")) {
            it.split("\"")[1]
        } else {
            it.cleanInboundCommand()
        }
    },
) : AbstractShopCustomerNode(context) {

    private val joinCodeStr: String = joinCodeCleaner(body)

    val shopNameProvider: (Storefront) -> String = {
        tx {
            it.displayName.let {
                if (it == "") {
                    it
                } else {
                    joinCodeStr
                }
            }
        }
    }

    override suspend fun next(): TreeNode? {
        return checkForJoinCode()
    }

     suspend fun checkForJoinCode(): TreeNode? {
        return JoinCodeFetch().fetch(joinCodeStr)?.let {
            checkForMatchingStorefrontForJoinCode(it)
        } ?: run {
            Log.warn("No join code found for $joinCodeStr")
            CoreNodes(body, context).unknownInputHandler()
        }
    }

     suspend fun checkForMatchingStorefrontForJoinCode(joinCodeIn: JoinCode): TreeNode? {
        return StorefrontFetcherByJoinCode().fetchByJoinCode(joinCodeIn)?.let { storefront ->
            checkForExistingCustomer(joinCodeIn, storefront)
        } ?: run {
            sendOutput("No storefront found for join code $joinCodeStr")
            null
        }
    }

     suspend fun checkForExistingCustomer(joinCodeIn: JoinCode, storefrontIn: Storefront): TreeNode? {
        return tx {
            ShopCustomer.find(
                ShopCustomers.storefront eq storefrontIn.id and (ShopCustomers.user eq context.user.id)
            ).firstOrNull()
        }?.let {
            Log.warn("You are already a customer of Shop ${shopNameProvider(storefrontIn)}")
            setStorefrontAsCurrent(it, joinCodeIn, storefrontIn)
        } ?: run {
            createNewCustomerNode(joinCodeIn, storefrontIn)
        }
    }

     suspend fun createNewCustomerNode(joinCodeIn: JoinCode, storefrontIn: Storefront): TreeNode? {
        val newCustomer = tx {
            ShopCustomer.new {
                this.user = context.user
                this.storefront = storefrontIn
            }.let {
                JoinCodeUsages.Entity.new {
                    this.joinCode = joinCodeIn
                    this.user = context.user
                }
                it
            }
        }

        sendOutput("You are now a customer of Shop ${shopNameProvider(storefrontIn)}")
        return notifyOfNewCustomerCreated(
            customer = newCustomer,
            joinCode = joinCodeIn,
            storefront = storefrontIn
        )
    }

     private suspend fun notifyOfNewCustomerCreated(
         customer: ShopCustomer,
         joinCode: JoinCode,
         storefront: Storefront
    ): TreeNode? {
        with(
            NewCustomerNotifyStringBuilders(
                context = context,
                customerIn = customer,
                joinCodeIn = joinCode,
                storefrontIn = storefront
            )
        ) {
            EntityNotifier(
                startingEntity = storefront,
                envIn = context.environment,
                roleIn = ShopRole.VENDOR
            ).notify(vendorOutString)
            notifyAdmins(msg = adminOutString, routingProps = context.routingProps)
        }

        return setStorefrontAsCurrent(customer, joinCode, storefront)
    }

    suspend fun setStorefrontAsCurrent(
        customer: ShopCustomer,
        joinCode: JoinCode,
        storefront: Storefront
    ): TreeNode? {
        val outString = tx {
            when (customer.statusEnum) {
                ActiveMutexStatus.INACTIVE -> {
                    "Link to this vendor`s storefront is unavailable: ${joinCode}\n" +
                            "Contact your friendly neighborhood system administrator."
                }

                ActiveMutexStatus.CURRENT -> {
                    "This is already your current vendor storefront: ${joinCode}\n\n" +
                            "Use `${CmdRegistry.MENU}` to shop for available items, or `/help` for more commands."
                }

                else -> {
                    customer.makeCurrent()
                    "Set as current vendor storefront: ${joinCode}. \n\n" +
                            "${ShopCustomer.activeStorefrontsToString(customer.user)}" +
                            "Use `${CmdRegistry.MENU.lower()}` to shop for available items, or `/help` for more commands."
                }
            }
        }
        sendOutput(outString)
        return displayWelcome( storefront)
    }

    suspend fun displayWelcome(storefront: Storefront): TreeNode? {
        val welcomeMsg = astx { storefront.welcomeMsg }
        IntroEvents.Table.displayWelcomeIfUnseen(context)
        sendOutput(welcomeMsg.await())
        return CoreNodes(CmdRegistry.MENU.name, context).next()
    }
}


