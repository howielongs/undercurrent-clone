package com.undercurrent.legacyshops.nodes.vendor_nodes

import com.undercurrent.legacy.repository.entities.system.attachments.Attachments
import com.undercurrent.legacy.types.enums.AttachmentType
import com.undercurrent.legacyshops.nodes.shared_nodes.CancelNode
import com.undercurrent.legacyshops.repository.entities.shop_items.ShopProduct
import com.undercurrent.legacyshops.repository.entities.shop_items.ShopProducts
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopVendors
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.legacyshops.service.CustomerListFetcher
import com.undercurrent.legacyshops.service.StorefrontToCustomerBroadcaster
import com.undercurrent.prompting.nodes.interactive_nodes.YesNoInputNode
import com.undercurrent.shared.messages.CanSendToUser
import com.undercurrent.shared.messages.InterrupterMessageEntity
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.time.EpochNano
import com.undercurrent.shared.utils.tx
import com.undercurrent.shared.view.treenodes.InteractiveNode
import com.undercurrent.shared.view.treenodes.OutputNode
import com.undercurrent.shared.view.treenodes.TreeNode
import com.undercurrent.system.context.DbusProps
import com.undercurrent.system.context.SystemContext
import com.undercurrent.system.messaging.inbound.InboundAttachmentsFetcher
import com.undercurrent.system.messaging.outbound.Notifier
import com.undercurrent.system.repository.entities.User


class AddProductCmdNodes(
    context: SystemContext,
    private val fetchProducts: (Storefront) -> List<ShopProduct> = { storefront ->
        tx {
            ShopProduct.find {
                ShopProducts.storefront eq storefront.id
            }.toList()
        }
    },
) : AbstractShopVendorNode(context), CanSendToUser<InterrupterMessageEntity> {

    override suspend fun next(): TreeNode? {
        return startAddProduct()
    }

    private fun createProduct(
        productName: String,
        productDetails: String,
        thisUser: User,
        attachments: List<Attachments.Entity> = listOf(),
    ): ShopProduct? {
        val newProduct = tx {
            val storefrontIn = ShopVendors.fetchBySms(thisUser.smsNumber)?.allStorefronts?.firstOrNull()
            storefrontIn?.let {
                return@tx ShopProduct.new {
                    name = productName
                    details = productDetails
                    storefront = it
                }
            }
        }

        attachments.forEach {
            // might need to do this inside above transaction block?
            it.createLink(
                parentEntity = newProduct,
                attachmentTypeIn = AttachmentType.PRODUCT_IMAGE,
            )
        }

        return newProduct
    }


    suspend fun startAddProduct(): TreeNode? {
        //todo make this a MenuSelectType
        //todo Option B should be hidden if there are no existing Products

        return textInputNode("Select the letter of a command to run:" + "\nA. Add a new product" + "\nB. Add a new SKU to an existing product" + "\nC. Cancel current operation",
            ifSuccess = {
                when (it.lowercase()) {
                    "a" -> enterNameFunc()
                    "b" -> AddSkuCmdNodes(context).SelectProductToAddSku().next()
                    "c" -> CancelNode(context).next()
                    else -> {
                        sendOutput("Invalid choice. Please try again.")
                        this
                    }
                }
            })
    }


    suspend fun enterNameFunc(): TreeNode? {
        return textInputNode("Enter name for product:", ifSuccess = { enterDetails(it) })
    }

    suspend fun enterDetails(productName: String): TreeNode? {
        return textInputNode("Enter product details:",
            ifSuccess = { decideAttachments(productName, it) })
    }

    // actually impl this option
    suspend fun decideAttachments(productName: String, productDetails: String): TreeNode? {
        return yesNoNode("Would you like to attach any images to this product?",
            ifYes = { UploadAttachments(productName, productDetails).next() },
            ifNo = { confirmNewProduct(productName, productDetails, listOf()) })
    }

    inner class UploadAttachments(
        private val productName: String,
        private val productDetails: String,
    ) : InteractiveNode(interactors) {
        override suspend fun next(): TreeNode? {
            val now = EpochNano()

            sendOutput("Please upload any images you would like to attach to this product (send `q` to cancel)")

            //todo wrap this a little more nicely
            val attachmentsProvider = InboundAttachmentsFetcher(
                user = context.user,
                routingProps = DbusProps(
                    roleIn = ShopRole.VENDOR, envIn = context.environment
                ),
            )

            val attachments: List<Attachments.Entity> = attachmentsProvider.fetchAttachmentsOrCancel(now)

            attachments.let {
                if (it.isNotEmpty()) {
                    sendOutput("Attachments received: ${it.size}")
                } else {
                    sendOutput("No attachments received.")
                }
            }

            return confirmNewProduct(productName, productDetails, attachments)
        }
    }


    suspend fun confirmNewProduct(
        productName: String,
        productDetails: String,
        attachments: List<Attachments.Entity> = listOf()
    ): TreeNode? {

        //todo impl attachments on confirm message

        val attachmentsStr = if (attachments.isNotEmpty()) {
//            val attachmentStrs = attachments.map { it.caption }
//            "\n\n • Attachments: ${attachmentStrs.joinToString(", ")}"
            "\n • Attachments: ${attachments.size}"
        } else {
            ""
        }

        val productCreateStr = """
                |New product to create:
                | • Product: $productName
                | • Details: $productDetails$attachmentsStr
                | 
                | Save?
            """.trimMargin()

        return yesNoNode(productCreateStr,
            ifYes = { CreateProductNode(productName, productDetails, attachments).next() },
            ifNo = { ProductNotSaved(productName).next() })
    }

    //todo perhaps have separate attachment linkage node (SRP)...
    inner class CreateProductNode(
        private val productName: String,
        private val productDetails: String,
        private val attachments: List<Attachments.Entity> = listOf(),
    ) : OutputNode(interactors) {
        override suspend fun next(): TreeNode? {
            createProduct(
                productName = productName, productDetails = productDetails, thisUser = context.user, attachments
            )?.let {
                sendOutput("Product created: $productName")
                return DecideToAddSkuToProduct(it)
            }
            sendOutput("Error creating product: $productName")
            return ProductNotSaved(productName)
        }
    }

    inner class ProductNotSaved(
        private val productName: String,
    ) : OutputNode(interactors) {
        override suspend fun next(): TreeNode? {
            sendOutput("Product not saved: $productName")
            return null
        }
    }

    inner class DecideToAddSkuToProduct(
        val product: ShopProduct, private val articleStr: String = "a"
    ) : YesNoInputNode(interactors) {
        override suspend fun next(): TreeNode? {
            return fetchInput("Would you like to add $articleStr SKU to this product?")?.let {
                if (it) {
                    AddSkuCmdNodes(context).EnterPriceForSku(product)
                } else {
                    sendOutput("You decided not to add a new SKU.")
                    DecideToNotifyCustomers()
                }
            }
        }
    }


    inner class DecideToNotifyCustomers : YesNoInputNode(inputProvider, nodeInterrupter) {
        override suspend fun next(): TreeNode? {
            return fetchInput("Would you like to notify your customers that you have updated your inventory?")?.let {
                if (it) {
                    inputMsgToCustomers()
                } else {
                    sendOutput("Customers not notified.")
                    //todo do something here to finish up flow
                    null
                }
            }
        }
    }

    fun inputMsgToCustomers(): TreeNode? {
        val prompt = """
            |Please input your message to your customers
            |(e.g.: "New item added to my store. Go check it out!"):
        """.trimMargin()

        return textInputNode(prompt, ifSuccess = { confirmMsgToCustomers(it) })
    }

    private fun confirmMsgToCustomers(msg: String): TreeNode? {
        val prompt = """
            |You entered:
            |
            |``$msg``
            |
            |Send?
        """.trimMargin()

        return yesNoNode(prompt, ifYes = { NotifyCustomers(msg).next() }, ifNo = {
            sendOutput("Nothing sent to customers.")
            null
        })
    }


    //todo perhaps pull this into own class?

    inner class NotifyCustomers(
        private val msg: String = "New item added",
    ) : OutputNode(interactors) {

        override suspend fun next(): TreeNode? {

            thisStorefront?.let { storefront ->
                val routingProps = DbusProps(
                    roleIn = ShopRole.CUSTOMER, envIn = context.environment
                )
                val broadcaster = StorefrontToCustomerBroadcaster(
                    thisStorefront = storefront,
                    customerFetcher = CustomerListFetcher(storefront),
                    customerNotifier = {
                        Notifier(userEntity = it, routingProps = routingProps)
                    },
                )
                val count = broadcaster.broadcast(msg)

                sendOutput("Successfully notified $count of your customers:\n\n``$msg``")
                return null
            }

            sendOutput("Sorry, we're unable to send message to customers...")
            logger.error(
                "Unable to send message to customers..." + "\n\nmsg: $msg" + "\n\nstorefront: ${thisStorefront}\n" + "Unable to fetch storefront for User #${context.user.id}"
            )
            return null
        }
    }
}


