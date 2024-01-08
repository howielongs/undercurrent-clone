package com.undercurrent.legacy.commands.executables.list

import com.undercurrent.legacy.commands.executables.ExecutableExceptions
import com.undercurrent.legacy.commands.executables.abstractcmds.CanCheckShouldShow
import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.types.enums.ListIndexTypeOld
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.legacyshops.repository.entities.shop_items.ShopProduct
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.prompting.components.EmojiSymbol
import com.undercurrent.shared.messages.RoutingProps
import com.undercurrent.shared.types.SubjectHeader
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.context.SessionContext
import com.undercurrent.system.messaging.outbound.notifyAdmins
import com.undercurrent.system.messaging.outbound.sendInterrupt
import com.undercurrent.system.repository.entities.User
import org.jetbrains.exposed.sql.transactions.transaction

class ListProducts(sessionContext: SessionContext) : Executable(
    CmdRegistry.LISTPRODUCTS, sessionContext
), CanCheckShouldShow {

    override fun shouldShow(): Boolean {
        return tx { theseProducts.isNotEmpty() }
    }

    private val theseProducts: List<ShopProduct> by lazy {
        tx {
            thisStorefront.products
        }.ifEmpty {
            throw ExecutableExceptions.EmptyListException(
                sessionContext, "products", thisCommand.lower()
            )
        }
    }


    override val thisStorefront: Storefront by lazy {
        tx { thisShopVendor.currentStorefront } ?: throw ExecutableExceptions.StorefrontNotFoundException(
            sessionContext
        )
    }


    override suspend fun execute() {
        val storefront = tx { thisStorefront }

        displayProductsWithSkus(
            storefrontIn = storefront,
            requesterId = sessionContext.user.uid,
            displayItemlessProducts = true,
            displayAttachments = true,
            dbusProps = sessionContext.routingProps
        )
    }

    /**
     * Displays each product with attachments and SKUs
     */
    suspend fun displayProductsWithSkus(
        storefrontIn: Storefront,
        requesterId: Int,
        productsList: List<ShopProduct> = listOf(),
        displayItemlessProducts: Boolean = false,
        displayMenuAsSegments: Boolean = false,
        displayAttachments: Boolean = true,
        dbusProps: RoutingProps,
    ) {
        val thisUser = tx { User.findById(requesterId) } ?: run {

            //todo fix up logging
            "User #${requesterId} not found on " +
                    "DisplayProducts".let {
                        Log.error(it)
                        notifyAdmins(
                            msg = it,
                            subject = SubjectHeader.ERROR,
                            emoji = EmojiSymbol.AMBULANCE,
                            routingProps = dbusProps
                        )
                    }
            return
        }

        //prevent multiple queries if possible
        val theseProducts = productsList.ifEmpty {
            tx { storefrontIn.products }
        }

        with(theseProducts) {
            if (this.isEmpty()) {
                sendInterrupt(
                    user = thisUser,
                    role = ShopRole.VENDOR,
                    environment = dbusProps.environment,
                    msg = "No products found"
                )
                return
            }
            this.forEachIndexed { index, product ->
                //todo fix this huge tx cost
                with(tx { product.saleItems }) {
                    if (this?.isNotEmpty() ?: false || displayItemlessProducts) {
                        //todo fix up all theses excessive transactions
                        val productName = tx { product.name }
                        val productDetails = tx { product.details }

                        if (displayAttachments) {
                            val captionText = "${productName.uppercase()} > $productDetails"
                            val linkedAttachments =
                                tx { product.linkedAttachments }
                            linkedAttachments.forEach { attachment ->
                                attachment.send(
                                    recipientUser = thisUser,
                                    captionText = captionText,
                                    dbusPropsIn = dbusProps
                                )
                            }
                        }
                        if (displayMenuAsSegments) {
                            val saleItemsString = product.getSaleItemsListString(
                                index + 1, this,
                                indexType = ListIndexTypeOld.BULLET
                            )
                            thisUser?.interrupt(
                                saleItemsString,
                                role = dbusProps.role,
                                environment = dbusProps.environment

                            )
                            if (!dbusProps.isTestMode()) {
                                //todo this is smelly
                                Thread.sleep(1000)
                            }
                        }
                    }
                }
            }
            if (!displayMenuAsSegments) {
                if (!dbusProps.isTestMode()) {
                    //todo this is smelly
                    Thread.sleep(1000)
                }
                thisUser?.interrupt(
                    msg = selectableInventoryString(
                        storefrontIn = storefrontIn,
                        productsList = this,
                        displayItemlessProducts = displayItemlessProducts
                    ),
                    role = dbusProps.role,
                    environment = dbusProps.environment
                )
            }
        }
    }

    //todo could add this call to Customer/User (pass userID)
//todo more optimized overload of this could be with saleItems list
// slightly different approach
    private fun selectableInventoryString(
        storefrontIn: Storefront,
        productsList: List<ShopProduct> = listOf(),
        displayItemlessProducts: Boolean = false,
        indexType: ListIndexTypeOld = ListIndexTypeOld.BULLET,
    ): String {
        return transaction {
            var outString = ""


            //prevent multiple queries if possible
            val theseProducts = productsList.ifEmpty {
                storefrontIn.products
            }

            with(theseProducts) {
                if (this == null || this.isEmpty()) {
                    return@transaction PressAgent.VendorStrings.noSaleItems()
                }

                forEachIndexed { index, product ->
                    //todo writing out some JOIN statements wouldn't hurt...
                    with(transaction { product.saleItems }) {
                        if (this.isNotEmpty() || displayItemlessProducts) {
                            outString += product.getSaleItemsListString(
                                index + 1,
                                this,
                                indexType = indexType
                            )
                        }
                    }
                }
                //todo could return both string and list data
                outString
            }
        }
    }
}