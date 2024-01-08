package com.undercurrent.legacyshops.repository.entities.shop_orders


import com.undercurrent.legacy.commands.executables.ExecutableExceptions.NullLoadException
import com.undercurrent.legacy.commands.executables.stripe.StripeManager
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.data_transfer_objects.InvoiceReceipt
import com.undercurrent.legacy.data_transfer_objects.currency.FiatAmount
import com.undercurrent.legacy.repository.entities.payments.*
import com.undercurrent.legacy.repository.entities.payments.CryptoAmountLegacy.Companion.CRYPTO_CUST_FUDGE_FACTOR
import com.undercurrent.system.repository.entities.Admins
import com.undercurrent.system.repository.entities.Users
import com.undercurrent.system.messaging.outbound.notifyAdmins
import com.undercurrent.legacy.repository.repository_service.payments.accounting.LedgerTransactionBuilder
import com.undercurrent.legacy.routing.RoutingConfig
import com.undercurrent.legacy.routing.RunConfig
import com.undercurrent.legacy.service.ShopOverviewScanRunner.paymentNudgePeriodSeconds
import com.undercurrent.legacy.service.UserBalanceChecker
import com.undercurrent.legacy.service.crypto.BitcoinWalletServices
import com.undercurrent.legacy.types.enums.CryptoType
import com.undercurrent.legacy.types.enums.TransactionMemo
import com.undercurrent.legacy.types.enums.status.InvoiceStatus
import com.undercurrent.legacy.types.enums.status.LedgerEntryStatus
import com.undercurrent.legacy.types.enums.status.OrderStatus
import com.undercurrent.legacy.types.string.PressAgent.VendorStrings.paymentPendingString
import com.undercurrent.legacy.utils.UtilLegacy
import com.undercurrent.legacyshops.repository.companions.InvoiceCompanion
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomer
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopVendor
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront.Companion.feePctDecimal
import com.undercurrent.shared.formatters.UserToIdString
import com.undercurrent.shared.repository.dinosaurs.EntityHasStatusField
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.shared.repository.dinosaurs.entityHasStatus
import com.undercurrent.shared.repository.entities.SignalSms
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.VARCHAR_SIZE
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.context.DbusProps
import com.undercurrent.system.context.SessionContext
import kotlinx.coroutines.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import java.math.BigDecimal

const val ENABLE_INCOMPLETE_PAYMENT_NUDGE = false

object Invoices : ExposedTableWithStatus2("shop_order_invoices") {
    //this ensures that the wallet is never completely exhausted on admin cashout
    val ADMIN_PAYOUT_FUDGE_FACTOR = BigDecimal(0.8)

    val order = optReference("order_id", DeliveryOrders)

    //consider grouping these more tightly, at least with util data class
    val exchangeRate = reference("exchange_rate_id", LegacyExchangeRates.Table)
    val subtotalCrypto = reference("subtotal_amount_id", CryptoAmountsLegacy)
    val feesCrypto = reference("fees_amount_id", CryptoAmountsLegacy)
    val totalCrypto = reference("total_amount_id", CryptoAmountsLegacy)

    val splitFeesPerAdminAmount = reference("split_fees_amount_id", CryptoAmountsLegacy)

    val feePercentDecimal = varchar("fee_pct", VARCHAR_SIZE).clientDefault { feePctDecimal.toString() }
    val raw = varchar("raw", VARCHAR_SIZE).clientDefault { "" }
    val receipt = varchar("receipt", VARCHAR_SIZE).clientDefault { "" }

    var lastNudgedTimestamp = long("last_nudged_timestamp").nullable()
}

class Invoice(id: EntityID<Int>) : ExposedEntityWithStatus2(id, Invoices), EntityHasStatusField {
    companion object : InvoiceCompanion()

    var order by DeliveryOrder optionalReferencedOn Invoices.order

    var exchangeRate by LegacyExchangeRates.Entity referencedOn Invoices.exchangeRate

    //todo consider making these more generic than payments? Perhaps have FiatInvoice as well?
    var subtotalCrypto by CryptoAmountLegacy referencedOn Invoices.subtotalCrypto
    var feesCrypto by CryptoAmountLegacy referencedOn Invoices.feesCrypto
    var totalCrypto by CryptoAmountLegacy referencedOn Invoices.totalCrypto

    var splitFeesPerAdminAmount by CryptoAmountLegacy referencedOn Invoices.splitFeesPerAdminAmount

    var feePercentDecimal by Invoices.feePercentDecimal
    var raw by Invoices.raw
    var receipt by Invoices.receipt

    var lastNudgedTimestamp by Invoices.lastNudgedTimestamp

    var depositAddress: DepositCryptoAddress? = null
        get() {
            return tx { customerProfile?.depositAddress(cryptoType ?: CryptoType.BTC) }
        }

    val ledgerEntries by UserCreditLedger.Entity optionalReferrersOn UserCreditLedger.Table.invoice

    override fun hasStatus(status: String): Boolean {
        return entityHasStatus(status)
    }

    override fun expire(epoch: Long): Boolean {
        super.expire(epoch)

        tx {
            subtotalCrypto.expire()
            totalCrypto.expire()
            feesCrypto.expire()
            splitFeesPerAdminAmount.expire()
        }

        return true
    }

    private fun setStatusAwaitingPayment() {
        tx {
            status = InvoiceStatus.AWAITING_CUSTOMER_PAYMENT.name
            order?.status = OrderStatus.AWAITING_PAYMENT.name
            val orderStr = order?.let { UserToIdString.toIdStr(it) }

            Log.debug(
                "$orderStr\n\nUpdating order and corresponding " +
                        "Invoice status to ${OrderStatus.AWAITING_PAYMENT.name} "
            )
        }
    }


    // put event listeners on these status updates?
    private fun updateAfterReceivedFull() {
        tx {
            status = InvoiceStatus.RECEIVED_FULL_FROM_CUSTOMER.name
            order?.status = OrderStatus.AWAITING_SHIPMENT.name
        }


        //todo update creditLedger PENDING status to AVAILABLE
        tx {
            UserCreditLedger.Entity.find {
                UserCreditLedger.Table.invoice eq this@Invoice.id and (UserCreditLedger.Table.status eq LedgerEntryStatus.PENDING.name)
            }.toList()
        }.filter { it.isNotExpired() }
            .forEach {
                tx { it.status = LedgerEntryStatus.AVAILABLE.name }
            }

    }

    private fun isTimeToNudge(): Boolean {
        val nudgeEpoch = tx { lastNudgedTimestamp } ?: run {
            tx { lastNudgedTimestamp = UtilLegacy.getEpoch() }
            return true
        }

        val diff = UtilLegacy.getEpoch() - nudgeEpoch
        return (diff >= paymentNudgePeriodSeconds * 1000000000)
    }

    private suspend fun updateMoreNeeded(diffAmt: BigDecimal, forceIncompletePaymentNudge: Boolean = false) {
        tx {
            status = InvoiceStatus.AWAITING_CUSTOMER_PAYMENT.name
            order?.status = OrderStatus.AWAITING_PAYMENT.name
        }

        if (forceIncompletePaymentNudge || (ENABLE_INCOMPLETE_PAYMENT_NUDGE && isTimeToNudge())) {
            notifyMoreNeeded(diffAmt)
        }
    }

    //todo may want this more generic (CurrencyType instead)
    var cryptoType: CryptoType? = null
        get() = tx { exchangeRate.cryptoTypeEnum }

    private suspend fun notifyMoreNeeded(diffAmt: BigDecimal) {
        //todo this will be handled differently depending on currency type
        tx { lastNudgedTimestamp = UtilLegacy.getEpoch() }

        val thisOrder = tx { order }
        val thisOrderCode = tx { order?.orderCode }
        val thisCustomer = tx { customerProfile }

        val currencyType = tx { cryptoType }

        //todo remove hardcoding to just BTC here...
        """
        |Order $thisOrderCode
        |
        |Incomplete payment. Customer notified to send remaining:
        |${diffAmt} ${currencyType?.abbrev() ?: CryptoType.BTC.abbrev()}
        """.trimMargin().let { notifyAdmins(it) }

        thisOrder?.let {
            thisCustomer?.let {
                it.nudgeSendPayment(
                    order = thisOrder,
                    negativeAmount = diffAmt,
                    currencyInterface = currencyType ?: CryptoType.BTC
                )
            }
        }
    }

    suspend fun checkAndNotifyPaymentStatus(forceIncompletePaymentNudge: Boolean = false) {
        Log.debug("SCANS Starting checkAndNotifyPaymentStatus")
        when (tx { cryptoType }) {
            CryptoType.STRIPE -> {
                Log.debug("SCANS Checking Stripe invoice...")
                //todo need safeguarding in scans for nested txs
                /**
                 * Check for payment intent
                 * If one exists, fetch from Stripe api and check status
                 * If "succeeded", update ledger accordingly and notify received full
                 * If not, follow "updateMoreNeeded" lifecycle
                 */
                val thisOrder = tx { order } ?: return

                //todo can more efficiently query and write data directly to object
                thisOrder.stripePaymentLinksObject()?.let { linksObj ->
                    tx { customerProfile }?.let { customer ->
                        StripeManager(customer).isPaid(
                            tx { linksObj.paymentLink }
                        )?.let {
                            if (it) {
                                Log.debug("Stripe payment is paid for Order #${thisOrder.uid}, will attempt to write to ledger")
                                val amountIn =
                                    tx { roundedTotalFudgeCrypto } ?: run {
                                        "Could not load total amount for order to balance ledger".let { msg ->
                                            Admins.notifyError(msg)
                                            return
                                        }
                                    }
                                val userIn = tx { customer.user }

                                Log.debug("SCANS CHECK_AND_NOTIFY_PAYMENT_STATUS: Writing to ledger...")

                                UserCreditLedger.save(
                                    userIn = userIn,
                                    roleIn = ShopRole.CUSTOMER,
                                    amountIn = amountIn,
                                    currencyInterfaceIn = CryptoType.STRIPE,
                                    statusIn = LedgerEntryStatus.RECEIVED,
                                    invoiceIn = this@Invoice,
                                    memoIn = TransactionMemo.FROM_CUSTOMER.toString(),
                                )

                                Log.debug("SCANS Finished writing to Ledger, will attempt to write to 'updateAfterReceivedFull()'")
                                updateAfterReceivedFull()
                                Log.debug("SCANS Finished updateAfterReceivedFull(), will attempt notifyAudienceFullReceived()")
                                notifyAudienceFullReceived()

                            }
                        }
                    }
                }
            }

            else -> {
                Log.debug("SCANS Getting customer account balance...")
                customerAccountBalance()?.let { diff ->
                    if (diff >= BigDecimal(0)) {
                        Log.debug("SCANS Finished writing to Ledger, will attempt to write to 'updateAfterReceivedFull()'")
                        updateAfterReceivedFull()
                        Log.debug("SCANS Finished updateAfterReceivedFull(), will attempt notifyAudienceFullReceived()")
                        notifyAudienceFullReceived()
                    } else {
                        updateMoreNeeded(diff, forceIncompletePaymentNudge)
                    }
                }

            }
        }
    }

    private fun notifyAudienceFullReceived() {
        val thisOrderCode = tx { order?.orderCode }
        val thisCustomer = tx { customerProfile }
        val thisVendor = tx { shopVendor }


        //todo improve formatting for these
        val roundedFeeSplitCrypto = tx { roundedFeeSplitCrypto } ?: BigDecimal(0)
        val roundedSubtotalCrypto = tx { roundedSubtotalCrypto } ?: BigDecimal(0)

        val currencyAbbrev = tx { cryptoType }?.abbrev()

        //todo add vendor and customer identifiers for admin out
        """
        |Order $thisOrderCode
        |
        |Received full payment from customer.
        |
        |Vendor will soon provide a tracking number to the customer.
        |
        |${roundedFeeSplitCrypto} $currencyAbbrev will soon be available to you.
             """.trimMargin().let { notifyAdmins(it) }

        //todo use CASHOUT to withdraw your balance
        //todo need to separate AVAILABLE from VERIFIED
        """
        |Order $thisOrderCode
        |
        |Received full payment from customer.
        |
        |Your account has been credited $roundedSubtotalCrypto $currencyAbbrev
        |
        |
        |
        |Use ${CmdRegistry.MARKSHIPPED.upper()} to apply tracking info and notify customer of shipment.
            """.trimMargin().let { thisVendor?.notify(it) }

        """
        |Order $thisOrderCode
        |
        |Full payment received!
        |
        |Your vendor will provide you with a tracking number once your 
        |order is shipped.
             """.trimMargin().let { thisCustomer?.notify(it) }
    }

    fun isFullyPaid(): Boolean {
        return customerAccountBalance()?.let {
            it >= BigDecimal(0)
        } == true
    }

    fun customerAccountBalance(): BigDecimal {
        Log.debug("Checking customer account balance")

        return tx {
            this@Invoice.cryptoType?.let {
                Log.debug("Checking customer's ${it.name} balance")
                customerProfile?.user?.let { user ->
                    UserBalanceChecker(user).checkBalance(it)
                }
            }
        } ?: tx { totalCrypto.toCustomerFudgeCrypto(true) }
    }

    /**
     * Do null check on receivingAddress
     * Generate based on payments type (may not be a receiving address depending on payments type)
     */
    private fun addReceivingAddress(
        cryptoType: CryptoType
    ): DepositCryptoAddress? {
        if (cryptoType == CryptoType.BTC) {
            if (!BitcoinWalletServices.isWalletRunning()) {
//                "Bitcoin wallet has not yet been initialized. Please try again in a few minutes.".let {
//                    sessionPair.interrupt(
//                        it,
//                        Rloe.VENDOR
//                    )
//                }
//                Admins.notifyError("addReceivingAddress: Vendor attempting to confirm an order, but BTC wallet is not yet up. Attempting to start...")

//                BitcoinWalletServices.startWallet() //todo retry adding fresh send address when wallet is finally up

                return null
            }

            return BitcoinWalletServices.getFreshSendAddress()?.let {
                tx { this@Invoice.customerProfile?.user }?.let { customerUser ->
                    DepositCryptoAddresses.save(customerUser, cryptoType, it)
                }
            } ?: null
        }
        return null
    }

    suspend fun setUpPaymentReceivers() {
        val thisVendorUser = tx { shopVendor?.user } ?: run {
            Admins.notifyError("${UserToIdString.toIdStr(this@Invoice)}\n\nUnable to load vendor for CONFIRM:setUpPaymentReceivers")
            return
        }

        val vendorDbusProps = DbusProps(roleIn = ShopRole.VENDOR, envIn = RunConfig.environment)
        setUpPaymentReceivers(SessionContext(thisVendorUser, vendorDbusProps))
    }

    private suspend fun generateStripeUrl(thisCustomer: ShopCustomer): StripePaymentLinks.Entity? {
        val stripe = StripeManager(thisCustomer)
        tx { order?.cartItems?.toList() }
            ?.filter { it.isNotExpired() }
            ?.let { cartItems ->
                cartItems.mapNotNull { cartItem ->
                    cartItem.toStripePrice(stripe)?.let { stripePrice ->
                        tx {
                            stripePrice.priceStripeId
                        }?.let { priceId ->
                            StripeManager.PaymentPair(
                                priceId,
                                tx { cartItem.quantity }
                            )
                        }
                    }
                }.toList().let {
                    val scope = CoroutineScope(
                        Dispatchers.IO +
                                CoroutineName("create payment link")
                    )
                    val job = scope.launch {
                        stripe.createPaymentLink(it)?.let { newPayLink ->
                            tx { order }?.let { thisOrder ->
                                return@launch tx {
                                    StripePaymentLinks.Entity.new {
                                        deliveryOrder = thisOrder
                                        paymentUrl = newPayLink.url
                                        paymentLink = newPayLink.id
                                    }
                                }
                            }
                        }
                    }
                    job.join()
                }
            }
        return null
    }

    suspend fun setUpPaymentReceivers(sessionContext: SessionContext) = coroutineScope {

        //todo change this to PaymentMethod, then CurrencyType, then CryptoType
        val thisCryptoType =
            tx { cryptoType } ?: throw NullLoadException(sessionContext, "payments type", "CONFIRM")

        if (thisCryptoType == CryptoType.BTC) {
            if (!BitcoinWalletServices.isWalletRunning()) {
//                "Bitcoin wallet has not yet been initialized. Please try again in a few minutes.".let {
//                    sessionPair.interrupt(
//                        it,
//                        Rloe.VENDOR
//                    )
//                }
//                Admins.notifyError("setUpPaymentReceivers: Vendor attempting to confirm an order, but BTC wallet is not yet up. Attempting to start...")
//                BitcoinWalletServices.startWallet()
                return@coroutineScope
            } else {
                addReceivingAddress(thisCryptoType)
            }
        }


        val thisOrder =
            tx { order } ?: throw NullLoadException(sessionContext, "Order", "CONFIRM")
        val thisVendor =
            tx { shopVendor } ?: throw NullLoadException(sessionContext, "vendor", "CONFIRM")
        val thisCustomer =
            tx { customerProfile } ?: throw NullLoadException(
                sessionContext,
                "customer",
                "CONFIRM"
            )
        tx { thisCustomer.user }

        val totalAmt =
            tx { roundedTotalCrypto } ?: throw NullLoadException(
                sessionContext,
                "payments total",
                "CONFIRM"
            )
        val totalFudgeRoundedAmt =
            tx { roundedTotalFudgeCrypto } ?: throw NullLoadException(
                sessionContext,
                "payments total",
                "CONFIRM"
            )
        val subtotalRoundedAmt =
            tx { roundedSubtotalCrypto } ?: throw NullLoadException(
                sessionContext,
                "payments subtotal",
                "CONFIRM"
            )
        val feeSplitUnroundedAmt =
            tx { splitFeesPerAdminAmount.toMacro() }


//        val adminSms1 = System.getProperty("admin_sms_1")
//        val adminSms2 = System.getProperty("adminSms2")

        val adminSms1 = RoutingConfig.adminsPair.first
        val adminSms2 = RoutingConfig.adminsPair.second

        var adminUser1 = tx { Users.fetchBySms(SignalSms(adminSms1)) }
            ?: throw Exception("admin user 1 unable to load")

        var adminUser2 = tx { Users.fetchBySms(SignalSms(adminSms2)) }
            ?: throw Exception("admin user 2 unable to load")


        //todo need to generate payment link
        //for intermediate objects (price and product), fetchOrCreate
        if (thisCryptoType == CryptoType.STRIPE) {
            generateStripeUrl(thisCustomer)
        }

        LedgerTransactionBuilder(
            currencyInterface = thisCryptoType,
            invoice = this@Invoice,
            feeSplitAmt = feeSplitUnroundedAmt,
            totalFudgeAmt = totalFudgeRoundedAmt,
            subtotalAmt = subtotalRoundedAmt,
            thisVendor = thisVendor,
            thisCustomer = thisCustomer,
            exchangeRateFromUsd = tx { exchangeRate }
        ).addAdmins(adminUser1, adminUser2)
            .addVendor()
            .addCustomer()

        //todo these ought to be expired along with Invoice
        //todo cascade as children?

        val vendorString = tx {
            """Order: ${thisOrder.orderCode}
                    |Status: ${thisOrder.status}
                    |   
                    |${paymentPendingString()}
                    |   
                    |You will receive: $subtotalRoundedAmt ${thisCryptoType.abbrev()}
                """.trimMargin()
        }

        //todo add cryptoKey and admin percents
        val adminStr = tx {
            val orderStr = UserToIdString.toIdStr(thisOrder)
            """$orderStr
                    |   ${thisVendor.toUserAndNameTag()}
                    |   ${thisCustomer.toUserIdString()}
                    |   
                    |   Status: ${thisOrder.status}
                    |   
                    |   FROM
                    |   Customer: $totalFudgeRoundedAmt
                    |   (Rounded up from $totalAmt)
                    |   
                    |   TO
                    |   Vendor: $subtotalRoundedAmt
                    |   Each Admin: $feeSplitUnroundedAmt
                    |   
                    |   Payment from customer pending...
                """.trimMargin()
        }

        sessionContext.interrupt(vendorString)
        notifyAdmins(adminStr)

        with(thisCustomer) {
            nudgeSendPayment(
                order = thisOrder,
                negativeAmount = customerAccountBalance(),
                currencyInterface = thisCryptoType
            )
        }

        setStatusAwaitingPayment()

    }


    var subtotal: CryptoAmountLegacy? = null
        get() {
            return tx {
                CryptoAmountLegacy.findById(subtotalCrypto.id)
            }
        }

    var total: CryptoAmountLegacy? = null
        get() {
            return tx {
                CryptoAmountLegacy.findById(totalCrypto.id)
            }
        }

    var fees: CryptoAmountLegacy? = null
        get() {
            return tx {
                CryptoAmountLegacy.findById(feesCrypto.id)
            }
        }

    var subtotalFiatStr: String? = null
        get() {
            return tx {
                subtotal?.prettyFiat(true)
            }
        }

    var totalFiatStr: String? = null
        get() {
            return tx {
                total?.prettyFiat(true)
            }
        }

    var feesFiatStr: String? = null
        get() {
            return tx {
                fees?.prettyFiat(true)
            }
        }

    //todo these various fields and formats are getting out of hand...
    private var roundedSubtotalCrypto: BigDecimal? = null
        get() {
            return tx { subtotalCrypto.toRoundedMacro() }
        }

    private var roundedTotalCrypto: BigDecimal? = null
        get() {
            return tx { totalCrypto.toRoundedMacro(scale = 8) }
        }

    var roundedTotalFudgeCrypto: BigDecimal? = null
        get() {
            return tx {
                totalCrypto.toRoundedMacro(
                    fudgeFactor = CRYPTO_CUST_FUDGE_FACTOR
                )
            }
        }


    private var roundedFeeSplitCrypto: BigDecimal? = null
        get() {
            return tx { splitFeesPerAdminAmount.toRoundedMacro() }
        }

//todo have better fetches/invert wrapping


    var currencyChoicePrompt: String = ""
        get() {
            return tx {
                var promptText = ""
                cryptoType?.let { type ->
                    val totalCrypto = if (RunConfig.isTestMode) {
                        //todo easier to verify subtotals in test mode
                        subtotal?.toRoundedMacroString(showLabel = true)
                    } else {
                        //todo still need to impl 'toRoundedMacroString'
                        total?.toRoundedMacroString(showLabel = true)
                    }

                    val rate = tx {
                        FiatAmount(
                            subtotal?.exchangeRate?.fiatToCryptoAtomicExchangeRate ?: "0"
                        ).formatted()
                    }

                    //todo add "time ago" string for when exchange rate was queried

                    val promptDetailsText = """
                        | • Order total:   $totalCrypto
                        | • 1 ${type.abbrev()} = $$rate 
                        | 
                    """.trimMargin()

                    promptText = "${type.fullName}\n$promptDetailsText"
                }
                promptText
            }
        }


    var shopVendor: ShopVendor? = null
        get() = tx { order?.shopVendor }

    var customerProfile: ShopCustomer? = null
        get() = tx { order?.customer }


    fun displayInvoice(role: AppRole, orderIn: DeliveryOrder? = null): String {
        return InvoiceReceipt(
            cryptoType = cryptoType ?: CryptoType.BTC,
            role = role as ShopRole,
            thisInvoice = this,
            orderIn = orderIn,
        ).toString()
    }
}
