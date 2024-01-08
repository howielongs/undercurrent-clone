package com.undercurrent.legacy.types.enums

import com.undercurrent.legacyshops.repository.entities.shop_items.ShopProduct
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.types.enums.AppRole


enum class AttachmentType(
    val linkableGroup: Set<AppRole> = setOf(),
    val consumerRoles: Set<AppRole> = setOf(ShopRole.ADMIN, ShopRole.VENDOR, ShopRole.CUSTOMER),
    val displayName: String,
    val parentClass: Class<*>? = null,
)   {
    SHOP_MENU(
        linkableGroup = setOf(ShopRole.VENDOR),
        consumerRoles = setOf(ShopRole.CUSTOMER),
        displayName = "Image/PDF of storefront menu",
    ),

    PRODUCT_IMAGE(
        linkableGroup = setOf(ShopRole.VENDOR),
        displayName = "Product in your inventory",
        parentClass = ShopProduct::class.java,
    ),
    CSV_REPORT(
        displayName = "CSV reports for transactions",
    ),
    CUSTOMER_WELCOME(
        displayName = "Welcome message to customers",
        linkableGroup = setOf(ShopRole.ADMIN)
    ),
    VENDOR_WELCOME(
        displayName = "Welcome message to vendors",
        linkableGroup = setOf(ShopRole.ADMIN)
    ),
    //todo add MOB mobilecoin here?
    MOBILECOIN(
        displayName = "MobileCoin info page",
        linkableGroup = setOf(ShopRole.ADMIN)
    ),
    BITCOIN(
        displayName = "Bitcoin info page",
        linkableGroup = setOf(ShopRole.ADMIN)
    ),
}