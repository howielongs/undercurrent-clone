package com.undercurrent.legacyshops.nodes.admin_nodes

import com.undercurrent.legacyshops.nodes.ShopAppOperationNode
import com.undercurrent.system.context.SystemContext


sealed class AbstractShopAdminNode(
    context: SystemContext,
) : ShopAppOperationNode(
    context
)