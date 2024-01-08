package com.undercurrent.legacyshops.nodes.customer_nodes

import com.undercurrent.legacyshops.nodes.ShopAppOperationNode
import com.undercurrent.system.context.SystemContext

sealed class AbstractShopCustomerNode(
    context: SystemContext,
) : ShopAppOperationNode(
    context
)