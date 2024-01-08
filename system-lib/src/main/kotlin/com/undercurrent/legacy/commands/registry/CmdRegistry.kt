package com.undercurrent.legacy.commands.registry

import com.undercurrent.legacy.commands.executables.*
import com.undercurrent.legacy.commands.executables.abstractcmds.remove_cmds.RemoveCryptoCmd
import com.undercurrent.legacy.commands.executables.abstractcmds.remove_cmds.RemoveSkuCmd
import com.undercurrent.legacy.commands.executables.abstractcmds.select_and_update_cmds.ConfirmCmd
import com.undercurrent.legacy.commands.executables.abstractcmds.select_and_update_cmds.MarkPaidCmd
import com.undercurrent.legacy.commands.executables.abstractcmds.select_and_update_cmds.MarkShippedCmd
import com.undercurrent.legacy.commands.executables.add.AddImage
import com.undercurrent.legacy.commands.executables.add.addcrypto.AddWalletCmd
import com.undercurrent.legacy.commands.executables.admin.CleanPrices
import com.undercurrent.legacy.commands.executables.admin.NudgeCheckout
import com.undercurrent.legacy.commands.executables.admin.NudgeConfirm
import com.undercurrent.legacy.commands.executables.currencyswaps.CashOutCmd
import com.undercurrent.legacy.commands.executables.currencyswaps.SwapCryptoCmd
import com.undercurrent.legacy.commands.executables.edit.EditSku
import com.undercurrent.legacy.commands.executables.info.*
import com.undercurrent.legacy.commands.executables.list.*
import com.undercurrent.legacy.commands.executables.remove.ClearCart
import com.undercurrent.legacy.commands.executables.remove.RemoveProduct
import com.undercurrent.legacy.commands.executables.remove.RemoveVendor
import com.undercurrent.legacy.commands.executables.scans.*
import com.undercurrent.legacy.commands.executables.select.SwitchShop
import com.undercurrent.legacy.commands.executables.shopping.BrowseMenu
import com.undercurrent.legacy.commands.executables.shopping.CheckoutCmd
import com.undercurrent.legacy.commands.executables.shopping.PdfMenuCmds
import com.undercurrent.legacy.commands.executables.stripe.EnableStripeCmd
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectableEnum
import com.undercurrent.legacy.repository.entities.payments.CryptoAddresses
import com.undercurrent.legacy.repository.entities.system.ping.Pinger
import com.undercurrent.legacy.system_start.SystemCommands
import com.undercurrent.legacyshops.nodes.admin_nodes.AddVendorNodes
import com.undercurrent.legacyshops.nodes.admin_nodes.AdminSendMsgNodes
import com.undercurrent.legacyshops.nodes.shared_nodes.FeedbackCmdNodes
import com.undercurrent.legacyshops.nodes.vendor_nodes.AddProductCmdNodes
import com.undercurrent.legacyshops.repository.entities.shop_items.SaleItems
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrders
import com.undercurrent.legacyswaps.nodes.SwapBotEntryNodes
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.types.enums.ShopRole.*
import com.undercurrent.system.context.SystemContext

typealias RunnerFuncType = (suspend (SystemContext) -> Unit)?

enum class CmdRegistry(
    override val hint: String = "",
    override val permissions: Set<AppRole> = setOf(ADMIN),
    override val commandGroup: Set<TopCommand> = setOf(),
    override val priority: Int = 50,
    override val prompt: String? = null,
    override val callback: CallbackType = null,
    override val displayAs: String = "",
    override val handlerClass: HandlerClassType = null,
    override val entityClass: ExposedTableWithStatus2? = null,
    override val simpleHelp: String? = null,
    override val runnerFunc: RunnerFuncType = null
) : UserCommand {
    STARTSWAP(
        //will be a "flat" command: will do a fetch for permissions and not look at current session sms/dbuspath, etc.
        hint = "Enter swapbot product lifecycle",
        permissions = setOf(ADMIN),
        commandGroup = setOf(TopCommand.START, TopCommand.ADVANCED, TopCommand.SWAPBOT, TopCommand.DEMOS),
        runnerFunc = { SwapBotEntryNodes(it).execute() },
    ),


    GEN_MANY_CODES(
        "Generate many join codes",
        setOf(ADMIN),
        commandGroup = setOf(TopCommand.ADVANCED),
        handlerClass = GenerateManyJoinCodes::class.java,
    ),


    MYCODES(
        "View join codes for this shop (will generate CSV if large number)",
        setOf(VENDOR),
        commandGroup = setOf(TopCommand.START),
        handlerClass = ViewCodesCmd::class.java,
    ),

    IMPORT_ZIPS(
        "Import CSV of zipcodes to database",
        setOf(ADMIN),
        commandGroup = setOf(TopCommand.ADVANCED),
        handlerClass = ImportZipcodesCmd::class.java,
    ),

    //    BTC_TO_MOB(
//        "Swap BTC for MOB",
//        setOf(ADMIN, CUSTOMER, VENDOR),
//        commandGroup = setOf(TopCommand.CRYPTO, TopCommand.START, TopCommand.WALLET, TopCommand.BANKING),
//        priority = -4,
//        handlerClass = BtcToMobCmd::class.java
//    ),
//    MOB_TO_BTC(
//        "Swap MOB for BTC",
//        setOf(ADMIN, CUSTOMER, VENDOR),
//        commandGroup = setOf(TopCommand.CRYPTO, TopCommand.START, TopCommand.WALLET, TopCommand.BANKING),
//        priority = -4,
//        handlerClass = MobToBtcCmd::class.java
//    ),
//    VENDOR_SETUP(
//        "Get prompted for preferences as a vendor when a storefront is created",
//        setOf(VENDOR),
//        commandGroup = setOf(TopCommand.START),
//        priority = 40,
//        handlerClass = VendorSetupCmd::class.java
//
//    ),

    //todo should have threshold for this
    //also log when this happens
    //nest inside setup wizard
    AUTOCONFIRM(
        "Set/unset auto-confirm for your orders",
        setOf(VENDOR),
        commandGroup = setOf(TopCommand.START, TopCommand.ORDERS),
        priority = 40,
        handlerClass = SetAutoConfirmCmd::class.java
    ),

    EDITFEES(
        "Set fees to be collected by system (and then split to admins)",
        setOf(ADMIN),
        commandGroup = setOf(TopCommand.START, TopCommand.ORDERS, TopCommand.ADVANCED),
        handlerClass = SetStorefrontFeesCmd::class.java

    ),


    CSV_REPORT(
        "Export orders as csv",
        setOf(VENDOR),
        commandGroup = setOf(TopCommand.ORDERS),
        handlerClass = ExportCsvOrderReportsCmd::class.java

    ),

    SWAP(
        "Swap cryptocurrencies",
        setOf(VENDOR),
        commandGroup = setOf(),
        priority = 400,
        handlerClass = SwapCryptoCmd::class.java

    ),

    //todo have list of orders by status
    //todo can do similar for listing users, vendors, etc. (by time last used)
    //todo also include count of items in each bucket
    OPENORDERS(
        "List open confirmed orders",
        setOf(ADMIN, CUSTOMER, VENDOR),
        commandGroup = setOf(TopCommand.ORDERS),
        priority = 5,
        handlerClass = ListDeliveryOrders::class.java,
    ),

    MARKPAID(
        hint = "Mark an order as paid",
        permissions = setOf(ADMIN),
        commandGroup = setOf(TopCommand.SHOPS, TopCommand.ORDERS, TopCommand.ADVANCED),
        handlerClass = MarkPaidCmd::class.java,
    ),

    ENABLE_STRIPE(
        hint = "Select a vendor to allow Stripe payments",
        permissions = setOf(ADMIN),
        commandGroup = setOf(TopCommand.MOB, TopCommand.WALLET, TopCommand.ADVANCED),
        handlerClass = EnableStripeCmd::class.java,
    ),
    MOB_IMPORT_DEFAULT(
        "Ensure default MOB account is imported for environment",
        setOf(ADMIN),
        commandGroup = setOf(TopCommand.MOB, TopCommand.CRYPTO, TopCommand.ADVANCED, TopCommand.SCAN),
        handlerClass = EnsureDefaultMobAccountImported::class.java,
    ),
    UPLOADMENU(
        "Upload a PDF menu (will have option to overwrite/replace existing)",
        setOf(VENDOR),
        commandGroup = setOf(TopCommand.PDFMENU), //todo add this to addtoProduct?
        priority = 4,
        callback = PdfMenuCmds::uploadMenu,
    ),

    //todo come back and allow customer to view menu after prompt on MENU
    VIEWPDFMENU(
        "View current PDF menu",
        setOf(VENDOR, CUSTOMER),
        commandGroup = setOf(TopCommand.PDFMENU),
        priority = 3,
        callback = PdfMenuCmds::viewMenu,
    ),
    REMOVEMENU(
        "Remove PDF menu from the system",
        setOf(VENDOR),
        commandGroup = setOf(TopCommand.PDFMENU),
        priority = 5,
        callback = PdfMenuCmds::removeMenu,
    ),
    CASHOUT(
        "Cash out balance of selected currency",
        setOf(ADMIN, VENDOR),
        commandGroup = setOf(TopCommand.BANKING, TopCommand.WALLET, TopCommand.CRYPTO, TopCommand.ADVANCED),
        priority = 3,
        handlerClass = CashOutCmd::class.java,
    ),
    BALANCES(
        "Check your current account balance in each currency type",
        setOf(ADMIN, VENDOR, CUSTOMER),
        commandGroup = setOf(TopCommand.WALLET),
        handlerClass = MyBalancesCmd::class.java,
    ),
    SCAN_FOR_OUTBOUND_PAYMENTS(
        "Scan for payments that are set to be sent",
        setOf(ADMIN),
        commandGroup = setOf(TopCommand.SCAN, TopCommand.NUDGE, TopCommand.ADVANCED),
        handlerClass = ScanForOutboundPayments::class.java,
    ),

    //todo eventually put scans into their own class
    SCAN_FOR_CONFIRMED_ORDERS(
        "Scan for confirmed orders and ensure they have " + "corresponding receivers set up",
        setOf(ADMIN),
        commandGroup = setOf(TopCommand.SCAN, TopCommand.NUDGE, TopCommand.ADVANCED),
        handlerClass = ScanForConfirmedOrders::class.java,
    ),
    MATCH_BTC_RECEIVE_TO_USER(
        "Scan through received BTC events and if not receiver has been matched, attempt to match and add to ledger",
        setOf(ADMIN),
        commandGroup = setOf(TopCommand.SCAN, TopCommand.NUDGE, TopCommand.ADVANCED),
        handlerClass = MatchBtcReceiveToUser::class.java,
    ),

    //    SCAN_FOR_PAID_IN_STRIPE(
//        "Check Stripe via API and check if new receive events have occurred",
//        setOf(ADMIN),
//        commandGroup = setOf(TopCommand.SCAN, TopCommand.ADVANCED),
////        handlerClass = ScanForConfirmedOrders::class.java,
//    ),
    SCAN_FOR_FULLY_PAID_ORDERS(
        "If order is fully paid, update status of Invoice and Order to reflect this",
        setOf(ADMIN),
        commandGroup = setOf(TopCommand.SCAN, TopCommand.NUDGE, TopCommand.ADVANCED),
        handlerClass = ScanForFullyPaidOrders::class.java,
    ),
    SCAN_FOR_AVAILABLE_BTC(
        "Check wallet balance to see if newly received amount is ready to disperse to vendors",
        setOf(ADMIN),
        commandGroup = setOf(TopCommand.SCAN, TopCommand.NUDGE, TopCommand.ADVANCED),
        handlerClass = ScanForConfirmedOrders::class.java,
    ),
    SEND(
        "Send message to particular user.",
        //todo cleanup handler class and allow for vendor as well
        setOf(ADMIN),
        commandGroup = setOf(TopCommand.LISTCMDS, TopCommand.START),
//        handlerClass = SendMessage::class.java,
        runnerFunc = {
            AdminSendMsgNodes(it).execute()
        }

    ),
    FEEDBACK(
        "Provide feedback to admins or ask for help",
        setOf(ADMIN, VENDOR, CUSTOMER),
        commandGroup = setOf(TopCommand.LISTCMDS, TopCommand.START),
        priority = 99,
        runnerFunc = {
            com.undercurrent.legacyshops.nodes.shared_nodes.FeedbackCmdNodes(it).FeedbackEntryNode(
                fromString = com.undercurrent.legacy.utils.CustomerMembershipToString(it).generateString()
            ).execute()
        }
    ),
    VIEWIMAGES(
        "View images stored in your gallery",
        setOf(ADMIN, VENDOR),
        priority = 10,
        commandGroup = setOf(TopCommand.IMAGES),
        handlerClass = ListImageAttachments::class.java,
    ),

    CANCEL(
        "Cancel current operation",
        setOf(ADMIN, VENDOR, CUSTOMER),
        commandGroup = TopCommand.values().toSet(),
        priority = 1000,
        callback = SystemCommands::cancelCmd
    ),
    FINISH(
        "Finish current operation",
        setOf(ADMIN, VENDOR, CUSTOMER),
        commandGroup = setOf(TopCommand.INTERNAL),
        priority = 999,
        callback = SystemCommands::finishCmd
    ),
    HEALTH(
        "Display last runtime for background tasks",
        setOf(ADMIN),
        commandGroup = setOf(TopCommand.ADVANCED),
        handlerClass = HealthCmd::class.java,
    ),
    MIGRATEIMAGES(
        "Migrate attachments to their correct environment's subdirectory on the server",
        setOf(ADMIN),
        commandGroup = setOf(TopCommand.ADVANCED, TopCommand.IMAGES),
        handlerClass = MigrateAttachments::class.java,
    ),
    NUDGECONFIRM(
        "Nudge vendors to confirm submitted orders",
        setOf(ADMIN),
        commandGroup = setOf(TopCommand.ADVANCED, TopCommand.ORDERS, TopCommand.NUDGE),
        handlerClass = NudgeConfirm::class.java,
    ),
    NUDGECHECKOUT(
        "Prompt anyone with items in their cart to check out",
        setOf(ADMIN),
        commandGroup = setOf(TopCommand.ADVANCED, TopCommand.CART, TopCommand.NUDGE),
        handlerClass = NudgeCheckout::class.java,
    ),
    PING("Get notified on available channels",
        setOf(ADMIN, VENDOR, CUSTOMER),
        commandGroup = setOf(TopCommand.LISTCMDS),
        runnerFunc = {
            Pinger(dbusProps = it.routingProps).pingAllRolesForUser(user = it.user)
        }),
    WELCOME(
        "View welcome message and helpful hints",
        setOf(VENDOR, CUSTOMER),
        commandGroup = setOf(TopCommand.LISTCMDS),
        callback = SystemCommands::welcomeCmd
    ),
    TERMS(
        "Display current Terms of Service",
        setOf(ADMIN, CUSTOMER, VENDOR),
        commandGroup = setOf(TopCommand.LISTCMDS),
        handlerClass = ViewTerms::class.java,

        ),
    EDIT_DISPLAY_NAME(
        //todo impl this
        "Edit display name customers will see",
        setOf(VENDOR),
        commandGroup = setOf(TopCommand.START, TopCommand.EDIT),
        handlerClass = EditStorefrontDisplayNameCmd::class.java,
    ),
    EDIT_WELCOME_MSG(
        //todo impl this
        "Edit welcome message new customers will see",
        setOf(VENDOR),
        commandGroup = setOf(TopCommand.START, TopCommand.EDIT),
        handlerClass = EditWelcomeMsgCmd::class.java,
    ),
    REFER(
        //todo impl this
        "Generate a joinCode to share",
//            setOf(CUSTOMER, VENDOR, ADMIN),
        setOf(VENDOR),
        commandGroup = setOf(TopCommand.START),
        handlerClass = ReferCmd::class.java,
    ),
    SWITCHSHOP(
        "View shops you've joined",
        setOf(CUSTOMER),
        commandGroup = setOf(TopCommand.SHOPS),
        handlerClass = SwitchShop::class.java,
    ),
    LISTSHOPS(
        "View shops you've joined",
        setOf(CUSTOMER),
        commandGroup = setOf(TopCommand.SHOPS),
        handlerClass = ListShops::class.java,
    ),
    LISTUSERS(
        "List users on the system",
        setOf(ADMIN),
        commandGroup = setOf(TopCommand.USERS),
        handlerClass = ListUsers::class.java,
    ),

    RMUSER(
        "Remove user from the system",
        setOf(ADMIN),
        commandGroup = setOf(TopCommand.USERS),
    ),

    MARKSHIPPED(
        "Mark order as shipped",
        setOf(VENDOR),
        commandGroup = setOf(TopCommand.ORDERS),
        priority = 8,
        handlerClass = MarkShippedCmd::class.java,
        entityClass = DeliveryOrders,
    ),

    RMCRYPTO(
        "Remove a payments wallet address",
        setOf(ADMIN, VENDOR, CUSTOMER),
        commandGroup = setOf(TopCommand.CRYPTO, TopCommand.WALLET),
        handlerClass = RemoveCryptoCmd::class.java,
        entityClass = CryptoAddresses,
        //may be able to combine into AddWalletNodes
    ),
    ADDWALLET(
        "Add wallet data to receive payments",
        setOf(ADMIN, VENDOR, CUSTOMER),
        commandGroup = setOf(TopCommand.CRYPTO, TopCommand.WALLET),
        handlerClass = AddWalletCmd::class.java,
//        runnerFunc = { AddCryptoWalletNodes(it).execute() },
    ),
    MOBILECOIN(
        "Get info on how to get started with MobileCoin",
        setOf(ADMIN, VENDOR, CUSTOMER),
        commandGroup = setOf(TopCommand.LISTCMDS, TopCommand.CRYPTO, TopCommand.WALLET),
        handlerClass = MobileCoinInfo::class.java,

        ),
    BITCOIN(
        "Get info on how to get started with Bitcoin wallets",
        setOf(ADMIN, VENDOR, CUSTOMER),
        commandGroup = setOf(TopCommand.LISTCMDS, TopCommand.CRYPTO, TopCommand.WALLET),
        handlerClass = BitcoinInfo::class.java,

        ),
    MYINFO("Show user info",
        setOf(ADMIN, VENDOR, CUSTOMER),
        commandGroup = setOf(TopCommand.LISTCMDS),
        runnerFunc = { MyInfoCmd(it).execute() }),
    ADDVENDOR("Add a new vendor and send registration",
        setOf(ADMIN),
        commandGroup = setOf(TopCommand.VENDORS),
        runnerFunc = { AddVendorNodes(it).execute() }),
    RMVENDOR(
        "Remove vendor permanently",
        setOf(ADMIN),
        commandGroup = setOf(TopCommand.VENDORS),
        handlerClass = RemoveVendor::class.java,
    ),

    LISTVENDORS(
        "View all yer vendors",
        setOf(ADMIN),
        commandGroup = setOf(TopCommand.VENDORS),
        displayAs = "listvendors",
        handlerClass = ListVendors::class.java,
    ),
    REMOVESKU(
        "Remove product SKU from your inventory",
        setOf(VENDOR),
        commandGroup = setOf(TopCommand.PRODUCTS),
        handlerClass = RemoveSkuCmd::class.java,
        entityClass = SaleItems,
    ),
    RMPRODUCT(
        "Remove product (and all its SKUs) from your inventory",
        setOf(VENDOR),
        commandGroup = setOf(TopCommand.PRODUCTS),
        handlerClass = RemoveProduct::class.java,
    ),

    ADDPRODUCT("Add a product to your inventory",
        setOf(VENDOR),
        commandGroup = setOf(TopCommand.PRODUCTS),
        priority = 4,
        runnerFunc = { AddProductCmdNodes(it).execute() }),
    ADDIMAGE(
        "Add/replace an image on a product in your inventory",
        setOf(VENDOR),
        commandGroup = setOf(TopCommand.PRODUCTS),
        priority = 5,
        handlerClass = AddImage::class.java,
    ),
    LISTPRODUCTS(
        "List inventory of products",
        setOf(VENDOR),
        commandGroup = setOf(TopCommand.PRODUCTS),
        handlerClass = ListProducts::class.java,

        ),
    EDITPRICE(
        "Edit the price of a SKU",
        setOf(VENDOR),
        handlerClass = EditSku::class.java,
        commandGroup = setOf(TopCommand.PRODUCTS, TopCommand.EDIT),
        priority = 100,
    ),
    CLEANPRICES(
        "Clean price formats (4-21-2022)",
        setOf(ADMIN),
        commandGroup = setOf(TopCommand.ADVANCED),
        handlerClass = CleanPrices::class.java,
    ),
    HOME(
        "Return home and cancel all current operations",
        setOf(ADMIN, VENDOR, CUSTOMER),
        commandGroup = setOf(TopCommand.START),
        callback = SystemCommands::homeCmd
    ),
    MENU(
        "View items available to add to your cart",
        setOf(CUSTOMER),
        commandGroup = setOf(TopCommand.CART, TopCommand.START),
        priority = 1,
        handlerClass = BrowseMenu::class.java,
        simpleHelp = "View menu",
    ),
    CLEARCART(
        "Remove all items from customer's cart",
        setOf(CUSTOMER),
        commandGroup = setOf(TopCommand.CART),
        priority = 10,
        handlerClass = ClearCart::class.java,
    ),

    VIEWCART(
        "View contents of shopping cart",
        setOf(CUSTOMER),
        commandGroup = setOf(TopCommand.CART),
        priority = 5,
        handlerClass = ListCartItems::class.java,
        simpleHelp = "View cart",
    ),
    SCAN_BLOCKCHAIN(
        "Scan blockchain for currently-open orders",
        setOf(),
        commandGroup = setOf(TopCommand.NUDGE, TopCommand.CRYPTO),
        priority = 4,
//        handlerClass = ConfirmCmd::class.java,
//        runnerFunc = { ConfirmOrderNodes(it).execute() },
    ),
    CONFIRM(
        "Confirm orders submitted by customers",
        setOf(VENDOR),
        commandGroup = setOf(TopCommand.ORDERS),
        priority = 4,
        handlerClass = ConfirmCmd::class.java,
        entityClass = DeliveryOrders,
//        runnerFunc = { ConfirmOrderNodes(it).execute() },
    ),
    CHECKOUT(
        "Submit purchase of items",
        setOf(CUSTOMER),
        commandGroup = setOf(TopCommand.CART),
        priority = 7,
        handlerClass = CheckoutCmd::class.java,
    );

    override fun selectable(): SelectableEnum {
        return SelectableEnum(
            promptText = this.name.capitalize(), enumValue = this
        )
    }

    override fun lower(): String {
        return this.name.lowercase()
    }

    override fun upper(): String {
        return this.name.uppercase()
    }

    override fun withSlash(): String {
        return "/" + this.name.lowercase()
    }

    override fun parseToString(): String {
        return this.name.lowercase()
    }
}