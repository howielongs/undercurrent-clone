package com.undercurrent.legacyshops.repository.entities.storefronts


import com.undercurrent.legacy.commands.registry.BaseCommand
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.commands.registry.CmdRegistry.MENU
import com.undercurrent.legacy.commands.registry.TopCommand
import com.undercurrent.legacy.repository.entities.payments.StripeApiKeys
import com.undercurrent.legacy.repository.entities.system.attachments.AttachmentLinks
import com.undercurrent.legacy.routing.RunConfig
import com.undercurrent.legacy.types.enums.JoinCodeType
import com.undercurrent.legacy.types.enums.StorefrontPrefType
import com.undercurrent.legacy.types.enums.StripeKeyType
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.legacy.utils.LastActive
import com.undercurrent.legacy.utils.UtilLegacy
import com.undercurrent.legacy.utils.fetchLastActiveStr
import com.undercurrent.legacyshops.repository.companions.StorefrontCompanion
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCode
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCodes
import com.undercurrent.legacyshops.repository.entities.shop_items.ShopProduct
import com.undercurrent.legacyshops.repository.entities.shop_items.ShopProducts
import com.undercurrent.legacyshops.repository.entities.shop_items.SaleItem
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrder
import com.undercurrent.prompting.components.EmojiSymbol
import com.undercurrent.shared.HasUserEntity
import com.undercurrent.shared.formatters.UserToIdString
import com.undercurrent.shared.messages.CanNotifyCreated
import com.undercurrent.shared.repository.dinosaurs.EntityHasStatusField
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.repository.dinosaurs.entityHasStatus
import com.undercurrent.shared.types.SubjectHeader
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.time.SystemEpochNanoProvider
import com.undercurrent.shared.utils.time.unexpiredExpr
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.context.SessionContext
import com.undercurrent.system.messaging.outbound.notifyAdmins
import com.undercurrent.system.repository.entities.Admins
import com.undercurrent.system.repository.entities.User
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.math.RoundingMode

// Requires MAJOR cleanup (acting as a god class right now)
class Storefront(id: EntityID<Int>) : ExposedEntityWithStatus2(id, Storefronts), EntityHasStatusField,
    CanNotifyCreated, HasUserEntity<User> {

    companion object : StorefrontCompanion();

    override fun hasStatus(status: String): Boolean {
        return entityHasStatus(status)
    }


    //pull these from Vendor
    var displayName by Storefronts.displayName
    var welcomeMsg by Storefronts.welcomeMsg

    //todo make this pass-through and store to JoinCodes table
    var joinCode by Storefronts.joinCode

    var vendor by ShopVendor referencedOn (Storefronts.vendor)

//    val allJoinCodes by JoinCodes.Entity referrersOn JoinCodes.Table.storefront

//    var products: List<Product> = listOf()
//        get() {
//            return com.undercurrent.shared.utils.tx {
//                allProducts.toList().filter { it.isNotExpired() }
//            }
//        }

    var joinCodes: List<JoinCode> = listOf()
        get() {
            return tx {
                JoinCode.find {
                    JoinCodes.entityId eq this@Storefront.id.value and (
                            JoinCodes.entityType.lowerCase() eq JoinCodeType.STOREFRONT.name.lowercase())
                }.filter { it.isNotExpired() }.toList()
            }
        }


    private val allProducts by ShopProduct referrersOn ShopProducts.storefront

    override val user: User
        get() {
            return tx {
                vendor.user
            }
        }

    fun feePercentDecimal(): BigDecimal {
        return feePercentFull().divide(BigDecimal("100"), 2, RoundingMode.UP)
    }

    fun feePercentFull(): BigDecimal {
        //todo needs to match storefront id
        return StorefrontPrefs.fetchValue(this@Storefront, StorefrontPrefType.FEE_PERCENT)
            ?.let {
                try {
                    BigDecimal(it.value).divide(BigDecimal("1"), 2, RoundingMode.UP)
                } catch (e: Exception) {
                    Log.error("Unable to parse FEE_PERCENT from storefront #${this@Storefront.uid}")
                    feePctFull
                }
            } ?: feePctFull
    }

    fun toFeePercentStr(): String {
        return adminString() + "\tAdmin fees: ${feePercentFull()}%\n"
    }

    private fun theseStorefrontPrefs(): List<StorefrontPrefs.Entity> {
        return transaction {
            return@transaction StorefrontPrefs.Entity.find {
                StorefrontPrefs.Table.storefront eq this@Storefront.uid
            }.filter { it.isNotExpired() }.toList()
        }
    }

    fun hasAutoConfirm(): Boolean {
        return transaction {
            theseStorefrontPrefs()
                .firstOrNull { it.key == StorefrontPrefType.AUTOCONFIRM }?.value == true.toString()
        }
    }


    var pdfMenus: List<AttachmentLinks.Entity> = listOf()
        get() {
            return transaction { vendor?.pdfMenus }?.toList() ?: listOf()
        }

    //todo can probably condense down all these stripe key getters
    fun fetchStripeKey(keyType: StripeKeyType = StripeKeyType.TEST): StripeApiKeys.Entity? {
        return transaction {
            StripeApiKeys.Entity.find {
                StripeApiKeys.Table.storefront eq this@Storefront.id and (
                        StripeApiKeys.Table.type eq keyType.name)
            }.firstOrNull { it.isNotExpired() }
        }
    }

    fun isEnabledForStripe(): Boolean {
        return stripeApiKey != null
    }


    var stripeApiKey: StripeApiKeys.Entity? = null
        get() {
            return transaction {
                StripeApiKeys.Entity.find {
                    StripeApiKeys.Table.storefront eq this@Storefront.id
                }.singleOrNull { it.isNotExpired() }
            }
        }


    override fun notifyCreated() {
        val thisJoinCode = transaction { joinCode }
        val thisNameTag = transaction { vendor.nickname }

        val outString = PressAgent.hintText(
            "Welcome! Your vendor account is ready to use.\n" +
                    "\nYour unique vendor code is ${thisJoinCode}.",
            TopCommand.START,
            CmdRegistry.ADDWALLET,
            CmdRegistry.ADDPRODUCT,
        )

        notify(outString)
        //todo execute START menu from here... (perhaps insert Inbound message?)

        val adminStr = """
            |New vendor account created: ${UserToIdString.toIdStr(vendor.user)}
            |Join code: $thisJoinCode
            |Name tag: $thisNameTag
        """.trimMargin()

        Log.debug(adminStr)
        notifyAdmins(
            msg = adminStr,
            env = RunConfig.environment,
            subject = SubjectHeader.NEW_VENDOR,
            emoji = EmojiSymbol.GREEN_CHECK_MARK
        )
    }


    //4 usages remaining...
    @Deprecated("Use separate class for this")
    fun notify(msg: String) {
        transaction { vendor }.let { it.notify(msg) }
    }


    //todo again, use backRef
    private var linkedCustomers: List<ShopCustomer> = listOf()
        get() {
            return transaction {
                ShopCustomer.find {
                    ShopCustomers.storefront eq this@Storefront.id and unexpiredExpr(ShopCustomers)
                }
                    .toList()
            }
        }


//todo potentially link to join codes (have as object ref)

    var headerName: String = ""
        get() {
            return transaction {
                if (displayName == "") {
                    joinCode
                } else {
                    displayName
                }
            }
        }


    var activeCustomerProfiles: List<ShopCustomer> = listOf()
        get() {
            return linkedCustomers?.filter { it.isNotExpired() }?.toList() ?: listOf()
        }

    var activeCustomerCount: Int = 0
        get() = activeCustomerProfiles.count()


    var unconfirmedOrders: List<DeliveryOrder> = listOf()
        get() {
            return activeCustomerProfiles.flatMap { transaction { it.unconfirmedOrders } }
        }


    var products: List<ShopProduct> = listOf()
        get() {
            return transaction { allProducts }?.filter { it.isNotExpired() } ?: listOf()
        }

    //todo experiment with .flatmap or .flatten
    var saleItems: List<SaleItem> = listOf()
        get() {
            var items = mutableListOf<SaleItem>()
            transaction { allProducts }?.forEach {
                items.addAll(transaction { it.saleItems })
            }
            return items
        }

    fun broadcastToActiveCustomers(
        msg: String = "New item added to my store. " +
                "Use ${MENU.withSlash()} to check it out!"
    ) {
        val customers = transaction { activeCustomerProfiles }
        val thisVendor = transaction { vendor }
        val displayName = transaction { displayName }
        val joinCode = transaction { joinCode }

        customers.forEach { customer ->
            val contentStr = """
            |``$msg``
            |
            |Shop: $displayName
            | • Date joined: ${transaction { UtilLegacy.formatDbDate(customer.createdDate) }}
            | • Join code: $joinCode
        """.trimMargin()

            PressAgent.wrappedHint(
                "Your vendor says:",
                contentStr,
                CmdRegistry.CHECKOUT,
                CmdRegistry.MENU,
                CmdRegistry.FEEDBACK,
                CmdRegistry.MYINFO,
            ).let { customer.notify(it) }
        }

        thisVendor.notify("Message sent to your customers:\n\n``$msg``")
    }

    //todo consider doing suspend to ensure all are sent before menu displayed
//todo also consider doing attachment parse when gathering nested products prompt
    @Deprecated("Pull this out")
    suspend fun sendAllProductAttachments(
        recipientUserId: Int,
        productsList: List<ShopProduct> = emptyList(),
        displayItemlessProducts: Boolean = false,
        command: BaseCommand? = null,
        sessionContext: SessionContext
    ) {
        val thisUser = tx { User.findById(recipientUserId) } ?: run {
            Admins.notifyError(
                "User #${recipientUserId} not found on " +
                        "DisplayProducts"
            )
            return
        }
        //prevent multiple queries if possible
        val theseProducts = productsList.ifEmpty {
            products
        }

        with(theseProducts) {
            if (this.isEmpty()) {
                return
            }
            this.forEachIndexed { _, product ->
                with(product.saleItems) {
                    if (this?.isNotEmpty() ?: false || displayItemlessProducts) {
                        val captionText = "${product.name.uppercase()} > ${product.details}"
                        tx {
                            product.linkedAttachments
                        }.forEach { attachment ->
                            attachment.send(
                                recipientUser = thisUser,
                                captionText = captionText,
                                currentOperationTag = command?.lower() ?: "",
                                dbusPropsIn = sessionContext.routingProps
                            )
                        }
                    }
                }
            }
        }


    }



    fun userActivityMap(): String {
        return tx {
            val epoch = SystemEpochNanoProvider.getEpochNano()

            activeCustomerProfiles
                .map { LastActive(it.user, startEpoch = epoch) }
                .sortedBy { it.nanoSec }
                .joinToString("\n • ")
                { it.toString() }
        }
    }

    private fun adminString(): String {
        val thisVendorProfile = transaction { vendor }
        var thisUser = transaction { thisVendorProfile.user }

        val sms = transaction { thisUser.smsNumber }
        var tagString = ""
        val customers = activeCustomerProfiles
        val thisJoinCode = transaction { joinCode }

        val thisNameTag = transaction { thisVendorProfile.nickname }
        val thisDisplayName = transaction { displayName }


        //todo replace with takeIf{ }
        if (thisNameTag != null && thisNameTag != "") {
            tagString = "($thisNameTag, $thisDisplayName)"
        }

        var appendStr = if (thisUser != null) {
            "\t${fetchLastActiveStr()}\n"
        } else {
            ""
        }

        //todo will want to separate by more storefronts (not needed right now)
        //right now this will display the joincode for the current storefront
        return "${sms}: $thisJoinCode $tagString\n" +
                "\tCustomers: ${customers.size}\n" +
                appendStr
    }

    //todo very unclear where this is being called from
    override fun toString(): String {
        val thisVendor = tx { vendor }
        val nickname = tx { thisVendor.nickname }
        val displayName = tx { displayName }
        val joinCode = tx { joinCode }
        val sms = tx { thisVendor.user.smsNumber }
        return "$nickname ($displayName) - $joinCode"
    }





}

