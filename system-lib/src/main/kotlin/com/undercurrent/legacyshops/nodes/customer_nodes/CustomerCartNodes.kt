package com.undercurrent.legacyshops.nodes.customer_nodes

import com.undercurrent.legacy.commands.executables.shopping.BrowseMenu
import com.undercurrent.legacy.commands.executables.shopping.CheckoutCmd
import com.undercurrent.legacy.commands.registry.BaseCommand
import com.undercurrent.legacy.commands.registry.CmdRegistry.*
import com.undercurrent.legacy.data_transfer_objects.currency.FiatAmount
import com.undercurrent.legacyshops.nodes.shared_nodes.CancelNode
import com.undercurrent.legacyshops.repository.entities.shop_items.CartItem
import com.undercurrent.legacyshops.repository.entities.shop_items.CartItems
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront.Companion.feePctDecimal
import com.undercurrent.legacyshops.service.CartItemPresenter
import com.undercurrent.legacyshops.service.CartItemStringPresenter
import com.undercurrent.shared.view.treenodes.TreeNode
import com.undercurrent.system.context.SessionContext
import com.undercurrent.system.context.SystemContext
import com.undercurrent.system.repository.entities.User
import java.math.BigDecimal

interface CustomerCartNodeFlow {
    suspend fun selectCartOperation(): TreeNode?

    suspend fun displayCartContentsNode(): TreeNode?

    suspend fun confirmClearCartNode(cartContentsString: String): TreeNode?
    suspend fun performClearCartNode(): TreeNode?


}


//            YOUR CART:
//            • The Leopard Gecko (1 gecko) $0.25 x 1 ..... $0.25
//            Details: This is a cute leopard gecko. He's a gecko, that looks like a leopard. Kind of.
//            ......
//            TOTALS
//            Subtotal: $0.50
//            Fees:     $0.08
//            Total:     $0.58
class CustomerCartNodes(
    context: SystemContext,
) : AbstractShopCustomerNode(context = context), CustomerCartNodeFlow, CartItemPresenter {

    //todo fix this up and ensure it's tested
    private val buildTotalsStringFunc: (User) -> String = {
        val subtotal = CartItems.getSubtotal(it)

        //todo unsure if zero is the best idea here...
        val fees = feeAmtFetcherFunc(subtotal?.amount ?: BigDecimal.ZERO)
        val total = fees.addSafely(subtotal)
        if (subtotal != null) {
            //todo add tests for this
            "\n\nTOTALS:\nSubtotal: $${subtotal.amount}\nFees: $${fees}\nTotal: $${total}\n"
        } else {
            ""
        }
    }

    private val fetchCartItemsFunc: (User) -> List<CartItem> = {
        //todo impl this
//        CartItems.getCartItems(context.user) ?: emptyList()
        emptyList()
    }

    @Deprecated("Replace with nodal impl")
    private val callCheckoutNode: suspend (SessionContext) -> TreeNode? = {
        CheckoutCmd(it).execute()
        null
    }

    @Deprecated("Replace with nodal impl")
    private val callMenuNode: suspend (SessionContext) -> TreeNode? = {
        BrowseMenu(it).execute()
        null
    }

    private val feeAmtFetcherFunc: (BigDecimal) -> FiatAmount = {
        val subtotalAmt = it
        val feeAmt = feePctDecimal.multiply(subtotalAmt)
        FiatAmount(feeAmt)
    }

    override suspend fun next(): TreeNode? {
        return selectCartOperation()
    }

    private val commandToLineStringFunc: (BaseCommand) -> String = {
        "${it.lower()} - ${it.hint}"
    }

    override suspend fun selectCartOperation(): TreeNode? {
        //todo improve this to not have to type out hint text
        val cmdOptions = listOf(
            MENU,
            VIEWCART,
            CHECKOUT,
            CLEARCART,
            CANCEL,
        )

        return menuSelectNode(
            options = cmdOptions.map { commandToLineStringFunc(it) },
            "Select the letter of a command to run:",
            ifSuccess = { i, _ ->
                when (i) {
                    0 -> {
                        callMenuNode(context)
                    }

                    1 -> {
                        displayCartContentsNode()
                    }

                    2 -> {
                        callCheckoutNode(context)
                    }

                    3 -> {

                        //todo fix this up
                        confirmClearCartNode("")
                    }

                    4 -> {
                        CancelNode(context).next()
                    }

                    else -> {
                        sendOutput("Invalid selection. Please try again.")
                        selectCartOperation()
                    }
                }
            },

            )
    }

    /**
     * Display contents of cart, then ask if user wants to clear cart
     */
    override suspend fun confirmClearCartNode(cartContentsString: String): TreeNode? {
        sendOutput(cartContentsString)

        sendTypingIndicatorWithDelay()

        return yesNoNode(
            "Are you sure you want to clear your cart?",
            ifYes = {
                performClearCartNode()
            },
            ifNo = {
                selectCartOperation()
            })
    }

    override suspend fun performClearCartNode(): TreeNode? {
        TODO("Not yet implemented")
    }

    override fun present(cartItem: CartItem): String {
        return CartItemStringPresenter().present(cartItem)
    }

    private val generateCartContentsString: () -> String = {
        val cartPrompt = StringBuilder()
        cartPrompt.append("YOUR CART:\n")

        fetchCartItemsFunc(context.user).forEach {
            cartPrompt.append(present(it))
        }

        val totalsString = buildTotalsStringFunc(context.user)
        if (totalsString != null && totalsString != "") {
            cartPrompt.append(totalsString)
        }
        cartPrompt.toString()
    }

    override suspend fun displayCartContentsNode(): TreeNode? {
        sendTypingIndicator()
        val contentsStr = generateCartContentsString()

        sendOutput(contentsStr)

        return null
    }


}