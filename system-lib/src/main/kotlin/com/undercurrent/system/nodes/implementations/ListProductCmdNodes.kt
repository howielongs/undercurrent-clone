package com.undercurrent.system.nodes.implementations

import com.undercurrent.legacy.repository.entities.system.attachments.AttachmentLinks
import com.undercurrent.legacy.types.enums.AttachmentType
import com.undercurrent.legacyshops.repository.entities.shop_items.ShopProduct
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopVendors
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.prompting.nodes.interactive_nodes.OperationNode
import com.undercurrent.shared.utils.tx
import com.undercurrent.shared.view.treenodes.TreeNode
import com.undercurrent.system.context.SystemContext

class ListProductCmdNodes(
    context: SystemContext,
) : OperationNode<SystemContext>(
    context = context,
) {

    override suspend fun next(): TreeNode? {
        return listProducts()
    }

    private fun fetchStorefront(): Storefront? {
        return tx {
            return@tx ShopVendors.fetchBySms(context.userSms.value)?.allStorefronts?.firstOrNull()
        }
    }

    //todo impl this and add interfaces
    private fun fetchProducts(): List<ShopProduct> {
        return tx {
            return@tx fetchStorefront()?.products?.toList() ?: emptyList()
        }
    }

    private fun fetchAttachmentLinksForProducts(): List<ShopProduct> {
        return tx {
            return@tx fetchStorefront()?.products?.toList() ?: emptyList()
        }
    }

    suspend fun listProducts(): TreeNode? {

        fetchProducts().listIterator().forEach { product ->
            sendOutput("${product.name.uppercase()} > ${product.details}\n")
            tx {
                product.linkedAttachments
            }.forEach { attachment ->
                // TODO Check if this works
                val imageLink = AttachmentLinks.fetchByTypeAndParentId(
                    AttachmentType.PRODUCT_IMAGE,
                    tx { product.fetchId() }
                )
                // TODO Still not sure how to show images
            }
        }
        return null // This is the Dead End
    }
}