package com.undercurrent.legacy.data_transfer_objects


import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrder
import com.undercurrent.legacyshops.repository.entities.shop_orders.Invoice
import com.undercurrent.legacy.types.enums.CryptoType
import com.undercurrent.legacy.types.enums.status.OrderStatus
import com.undercurrent.legacy.types.string.BulletString
import com.undercurrent.legacy.utils.UtilLegacy
import com.undercurrent.shared.formatters.UserToIdString
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.Log
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.math.RoundingMode

class InvoiceReceipt(
    val cryptoType: CryptoType = CryptoType.BTC,
    val role: ShopRole,
    val thisInvoice: Invoice,
    var orderIn: DeliveryOrder? = null,
) {
    private fun stringNullIfNone(inString: String?): String? {
        return if (inString == null || inString == "") {
            null
        } else {
            "``$inString``"
        }
    }


    private fun adminString(thisOrder: DeliveryOrder): String {
        BulletString().apply {
            add(orderDetailsSection(thisOrder, displayId = true))

            section("ADMINS", trimHeader = true) //todo display in USD as well...
            add("Will each receive", transaction { thisInvoice.splitFeesPerAdminAmount })

            section("VENDOR: ${transaction { thisOrder.shopVendor.toUserAndNameTag(withHeader = false) }}")
            add("Will receive", transaction { thisInvoice.subtotalCrypto })

            section("CUSTOMER: ${transaction { UserToIdString.toIdStr(thisOrder.customer.user) }}")
            add("Will pay", totalCustomerWillPay())
            add("To", transaction { thisOrder.depositAddress()?.address })


            section("TOTALS", displayLine = false) //todo display also in USD
            add("Fees", transaction { thisInvoice.feesCrypto }) //todo include fee percentage
            add("Subtotal", transaction { thisInvoice.subtotalCrypto })
            add("Total", transaction { thisInvoice.totalCrypto })
            close()

            return this.outString
        }
    }

    private fun vendorString(thisOrder: DeliveryOrder): String {
        BulletString().apply {
            add(orderDetailsSection(thisOrder))
            section("EARNINGS: ${transaction { thisInvoice.subtotalCrypto.toRoundedMacroString(showLabel = true) }}")
            add(deliverySection(thisOrder))
            add(receiptSection(thisOrder))
            close()

            return this.outString
        }
    }


    //todo will want to send separate orders as separate messaging
    private fun customerString(thisOrder: DeliveryOrder): String {
        BulletString().apply {
            add(orderDetailsSection(thisOrder))
            add(sendRemainingSectionBody(thisOrder))
            add(receiptSection(thisOrder))
            add(deliverySection(thisOrder))
            add("Order total: ${totalCustomerWillPay()}")
            close()

            return this.outString
        }
    }

    private fun remainingPaymentStr(): String {
        return transaction {
            UtilLegacy.roundBigDecimal(thisInvoice.customerAccountBalance(), cryptoType).toString()
        } + " ${cryptoType.abbrev()}"
    }

    private fun totalCustomerWillPay(): String {
        return transaction {
            thisInvoice.totalCrypto.roundedCustomerFudge()
                .divide(BigDecimal("1"), 6, RoundingMode.UP).toString() + " ${cryptoType.abbrev()}"
        }
    }

    private fun orderDetailsSection(thisOrder: DeliveryOrder, displayId: Boolean = false): String {
        val idString = if (displayId) {
            " (#${thisOrder.id})"
        } else {
            ""
        }

        var remainingOutStr = when (transaction { thisOrder.status }) {
            OrderStatus.AWAITING_PAYMENT.name -> {
                with(thisOrder.invoice.customerAccountBalance()) {
                    if (this >= BigDecimal(0)) {
                        "0"
                    } else {
                        UtilLegacy.roundBigDecimal(this, currencyInterface = cryptoType).toString()
                    }
                }
            }

            OrderStatus.SUBMITTED.name, OrderStatus.NEW.name -> {
                transaction { thisOrder.invoice.roundedTotalFudgeCrypto.toString() }
            }

            else -> {
                "0"
            }

        }

        //make method that returns either 0 or positive owed amount
        remainingOutStr = if (remainingOutStr.contains("-")) {
            remainingOutStr.replace("-", "")
        } else {
            "0"
        }

        remainingOutStr += " ${cryptoType.abbrev()}"




        BulletString().apply {
            section("ORDER$idString:", trimHeader = true)
            add("Code", thisOrder.orderCode)
            add("Status", thisOrder.status)
            add("Currency", cryptoType.lineString(fullNameInParens = false))
            add("Submitted", transaction { UtilLegacy.formatDbDate(thisOrder.createdDate) })
            add("Confirmed", transaction { thisOrder.confirmedDate })
            add("Declined", transaction { thisOrder.declinedDate })
            add("Payment remaining", remainingOutStr)
            return outString
        }
    }

    private fun cleanReceiptCartText(receiptStr: String): String {
        val headerSet = setOf<String>("Your cart", "YOUR CART")
        headerSet.forEach {
            if (receiptStr.contains(it)) {
                var outStr = ""
                receiptStr.split("$it:\n").forEachIndexed { index, s ->
                    if (index >= 1) {
                        outStr += s
                    }
                }
                return outStr
            }
        }
        return receiptStr
    }

    private fun genAddressStr(thisOrder: DeliveryOrder): String {
        return """
            |  ${thisOrder.deliveryName},
            |  ${thisOrder.deliveryAddress},
            |  ${thisOrder.zipcode}
        """.trimMargin()

    }

    private fun sendRemainingSectionBody(
        thisOrder: DeliveryOrder,
        headerText: String = "TO COMPLETE PAYMENT:"
    ): String? {
        if (transaction { thisInvoice.isFullyPaid() }) {
            return null
        }
        BulletString().apply {
            section(headerText)

            var remainingAmt = with(remainingPaymentStr()) {
                if (this.contains("-")) {
                    this.replace("-", "")
                } else {
                    "0"
                }
            }

            add("Send", remainingAmt)

            //todo fix this up for other currency types (MOB)
            //probably just instead of address, say "send to this channel"...
            add("To", transaction { thisOrder.depositAddress()?.address })
            return outString
        }
    }

    private fun displayFullReceipt(thisOrder: DeliveryOrder, showTotals: Boolean = true): String {
        val receiptStr = cleanReceiptCartText(thisOrder.receipt.toString())

        return if (showTotals) {
            receiptStr
        } else {
            receiptStr.split("TOTALS")[0].trim()
        }
    }

    private fun receiptSection(thisOrder: DeliveryOrder): String {
        BulletString().apply {
            section("RECEIPT", displayLine = true)
            add(displayFullReceipt(thisOrder))
            return outString
        }
    }


    private fun deliverySection(thisOrder: DeliveryOrder): String {
        BulletString().apply {
            section("DELIVER TO:", displayLine = true)
            add(genAddressStr(thisOrder))
            line()
            add("Notes", stringNullIfNone(thisOrder.notesToVendor))
            add("Vendor comments", stringNullIfNone(thisOrder.notesToCustomer))
            add("Tracking", thisOrder.trackingNumber)
            return outString
        }
    }


    override fun toString(): String {
        return transaction {
            with(thisInvoice) {
                var thisOrder = orderIn ?: order ?: run {
                    var msg = "Customer order ${order?.uid} not found for PaymentInvoice $uid"
                    Log.debug(msg)
                    return@transaction ""
                }

                return@transaction when (role) {
                    //todo may want this to print something different if customer hasn't yet selected currency
                    //cryptoType == null is best indication that customer hasn't selected payment method
                    ShopRole.ADMIN -> adminString(thisOrder)
                    ShopRole.VENDOR -> vendorString(thisOrder)
                    ShopRole.CUSTOMER -> customerString(thisOrder)
                }
            }
        }
    }

}