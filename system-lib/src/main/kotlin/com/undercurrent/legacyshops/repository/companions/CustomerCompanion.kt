package com.undercurrent.legacyshops.repository.companions

import com.undercurrent.legacy.types.enums.status.ActiveMutexStatus
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomer
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomers
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.repository.entities.User
import org.jetbrains.exposed.sql.transactions.transaction

open class CustomerCompanion : RootEntityCompanion0<ShopCustomer>(ShopCustomers) {

//    fun getCartContentsString(
//        cartContents: List<CartItem>,
//        receiptFooter: String? = null
//    ): String {
//        with(cartContents) {
//            if (this.isEmpty()) {
//                return CartItems.emptyListString()
//            }
//
//            val result = with(this.map { SelectableEntity(it) }) {
//                if (this.isEmpty()) {
//                    null
//                } else {
//                    OptionSelector(
//                        this,
//                        headerText = "YOUR CART:",
//                        indexType = ListIndexTypeOld.BULLET,
//                        isSelectable = false,
//                        footerText = receiptFooter ?: generateReceiptFooter(),
//                        headlineText = null,
//                    ).let {
//                        it.promptString
//                    }
//                }
//            }
//
//            result?.let { return it }
//                ?: return CartItems.emptyListString()
//        }
//    }

    //todo add tests and ensure you understand where this is all coming from
//    fun generateReceiptFooter(
//        subtotalAmount: FiatAmount? = null,
//        feesAmount: FiatAmount? = null,
//        totalAmount: FiatAmount? = null,
//    ): String {
//        val subtotal: FiatAmount = subtotalAmount ?: CartItems.getSubtotal(this) ?: FiatAmount(0)
//        val fees: FiatAmount = feesAmount ?: subtotal.multiply(transaction { storefront.feePercentDecimal() })
//        val total: FiatAmount = totalAmount ?: subtotal.add(fees)
//
//
//        return """
//            |TOTALS
//            |Subtotal: ${"\t"} ${subtotal.prettyFiat(withSymbol = true)}
//            |Fees: ${"\t\t\t"} ${fees.prettyFiat(withSymbol = true)}
//            |Total: ${"\t\t\t"} ${total.prettyFiat(withSymbol = true)}
//        """.trimMargin()
//    }



    fun save(
        userIn: User,
        storefrontIn: Storefront,
        statusIn: ActiveMutexStatus = ActiveMutexStatus.CURRENT
    ): ShopCustomer {
        //todo get away from using status on customer profiles
        return transaction {
            userIn.customerProfiles
                .filter { it.status == ActiveMutexStatus.CURRENT.name }
                .forEach { it.status = ActiveMutexStatus.ACTIVE.name }

            new {
                user = userIn
                storefront = storefrontIn
                status = statusIn.name
            }
        }
    }

    private fun noShopsFoundStr(): String {
        return "No shops found. \nEnter a vendor's join code at any point to browse that shop`s inventory"
    }

    //todo make better selectable list for /links
    fun activeStorefrontsToString(user: User): String {
        var outString = "Vendor shops you're linked to:\n"

        //todo see about making this suspendedTransaction
        tx { user.activeCustomerProfiles }
            .ifEmpty { return noShopsFoundStr() }
            ?.forEach { customer ->
                //todo try to make async/suspend

                tx {
                    val storefrontSummary =
                        customer.switchShopOutLineString()

                    outString += when (customer.status) {
                        ActiveMutexStatus.CURRENT.toString(), "" -> {
                            "\t-> $storefrontSummary"
                        }

                        else -> {
                            "\t   $storefrontSummary"
                        }
                    }
                }

                outString += "\n"
            }

        outString += "\nEnter join code at any point to browse that shop`s inventory.\n\n"
        return outString
//        return PressAgent.hintText(
//            outString,
//            CmdRegistry.MENU,
//        )
    }

}