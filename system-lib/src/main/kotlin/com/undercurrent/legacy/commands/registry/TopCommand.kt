package com.undercurrent.legacy.commands.registry



import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectableEnum
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.types.enums.ShopRole

enum class TopCommand(
    override val hint: String,
    override val permissions: Set<AppRole>,
    override val prompt: String? = null,
    val isHiddenFromListing: Boolean = false,
    override val priority: Int = 10,
    override val displayAs: String = "",
    override val callback: CallbackType = null,
    override val handlerClass: HandlerClassType = null,
    override val entityClass: ExposedTableWithStatus2? = null,
    override val simpleHelp: String? = null,
    override val runnerFunc: RunnerFuncType = null,

    ) : BaseCommand {
    ALL(
        "Show all commands available to user",
        setOf(ShopRole.ADMIN, ShopRole.VENDOR, ShopRole.CUSTOMER),
        isHiddenFromListing = true,
    ),
    AIRDROP(
        "Create and manage currency distribution at events",
        setOf(),
        priority = 3,
    ),
    PDFMENU(
        "View and manage shop menus",
        setOf(ShopRole.VENDOR),
//        isHiddenFromListing = true,
    ),
    SWAPBOT(
        "Swap bot commands",
        setOf(ShopRole.ADMIN),
        priority = 1,
    ),
    DEMOS(
        "Demos for proof-of-concepts",
        setOf(ShopRole.ADMIN),
        priority = 3,
    ),
    SCAN(
        "Perform scan action. Most of these are already running in the background, but can be triggered manually from here",
        setOf(ShopRole.ADMIN),
        priority = 100,
    ),
    MIGRATE(
        "Migrate data",
        setOf(ShopRole.ADMIN),
        priority = 10,
    ),
    BANKING(
        "Manage payment lifecycle",
        setOf(ShopRole.ADMIN),
        priority = 2,
    ),
    NUDGE(
        "*nudge-nudge, wink-wink*",
        setOf(ShopRole.ADMIN),
        priority = 2,
    ),
    INTERNAL(
        "Internal commands for data operations",
        setOf(ShopRole.ADMIN, ShopRole.VENDOR, ShopRole.CUSTOMER),
        isHiddenFromListing = true,
    ),
//    STOREFRONT_WIZARD(
//        "Collection of commands for ",
//        setOf(Rloe.ADMIN, Rloe.VENDOR, Rloe.CUSTOMER),
//        priority = 1,
//    ),
    START(
        "List all commands with choices to select",
    setOf(ShopRole.ADMIN, ShopRole.VENDOR, ShopRole.CUSTOMER),
        priority = 1,
    ),
    LISTCMDS(
        "Show tips and user info",
        setOf(ShopRole.ADMIN, ShopRole.VENDOR, ShopRole.CUSTOMER),
        priority = 999,
        isHiddenFromListing = true,
    ),
    EDIT(
        "Edit various data",
        setOf(ShopRole.ADMIN, ShopRole.VENDOR, ShopRole.CUSTOMER),
        priority = 50,
        isHiddenFromListing = true,
    ),
    MOB(
        "Handle MobileCoin wallet and transactions",
        setOf(ShopRole.ADMIN),
        priority = 50,
    ),
    CRYPTO(
        "Learn about Bitcoin and MobileCoin wallets",
        setOf(ShopRole.ADMIN, ShopRole.VENDOR, ShopRole.CUSTOMER),
        priority = 50,
    ),
    HEADSTART(
        "Set up default storefront/users for testing",
        setOf(ShopRole.ADMIN),
        priority = 100,
    ),
    SHOPS(
        "Shops menu",
        setOf(ShopRole.CUSTOMER),
    ),
    VENDORS(
        "System vendors",
        setOf(ShopRole.ADMIN),
    ),
    ORDERS(
        "Orders submitted",
        setOf(ShopRole.ADMIN, ShopRole.VENDOR, ShopRole.CUSTOMER),
        simpleHelp = "View submitted orders",
    ),
    ADVANCED(
        "Display advanced commands to admins",
        setOf(ShopRole.ADMIN),
        priority = 900,
    ),
    USERS(
        "Users in the system",
        setOf(ShopRole.ADMIN),
    ),
    CART(
        "Customer cart",
        setOf(ShopRole.CUSTOMER),
    ),
    PRODUCTS(
        "Vendor products",
        setOf(ShopRole.VENDOR),
        simpleHelp = "Manage your products",
    ),
    IMAGES(
        "Manage images uploaded to system",
        setOf(ShopRole.ADMIN),
    ),
    WALLET(
        "Display wallet status to admins",
        setOf(ShopRole.ADMIN),
        isHiddenFromListing = true,
    ),
    SHARE(
        "Shareable shop code to send to customers",
        setOf(ShopRole.ADMIN, ShopRole.VENDOR),
    ), ;

    override fun selectable(): SelectableEnum {
        return SelectableEnum(
            promptText = this.name.capitalize(),
            enumValue = this
        )
    }

    override fun lower(): String {
        return this.name.lowercase()
    }

    override fun withSlash(): String {
        return "/" + this.name.lowercase()
    }

    override fun parseToString(): String {
        return this.name.lowercase()
    }

    override fun upper(): String {
        return this.name.uppercase()
    }
}