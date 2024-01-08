package com.undercurrent.legacyshops.nodes.shared_nodes

import com.undercurrent.legacyshops.nodes.ShopAppOperationNode
import com.undercurrent.system.context.SystemContext

sealed class AbstractShopSharedRoleNode(
    context: SystemContext,
) : ShopAppOperationNode(
    context
)