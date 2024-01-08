package com.undercurrent.legacyshops.nodes.customer_nodes

import com.undercurrent.prompting.nodes.interactive_nodes.YesNoInputNode
import com.undercurrent.shared.messages.InterrupterMessageEntity
import com.undercurrent.shared.messages.UserInputProvider
import com.undercurrent.shared.messages.UserOutputProvider
import com.undercurrent.shared.view.treenodes.InteractiveNode
import com.undercurrent.shared.view.treenodes.OutputNode
import com.undercurrent.shared.view.treenodes.TreeNode


/**
 * NextCartActionNode:
 * - User has options to:
 *      a) View cart
 *      b) Clear cart
 *      c) Return to main menu
 *
 * - User selects option
 * - if (a) -> ViewCartNode
 *  - Cart contents displayed
 *      Ex:
 *-----––------------------------------------------------
 * YOUR CART (2 items)
 * –––––––––––––––––––––––––––––––––––––
 *
 * 1. The Leopard Gecko
 * Price: $0.25
 * Quantity: 1
 * Details: This is a cute leopard gecko. He's a gecko, that looks like a leopard. Kind of.
 *
 * 2. The Leopard Gecko
 * Price: $0.25
 * Quantity: 1
 * Details: This is a cute leopard gecko. He's a gecko, that looks like a leopard. Kind of.
 *-----––------------------------------------------------
 *
 *  - Cart subtotals displayed (subtotal, fees, total)
 *      Ex:
 *-----––------------------------------------------------
 * TOTALS
 * Subtotal: $0.50
 * Fees:        $0.08
 * Total:       $0.58
 *-----––------------------------------------------------
 *
 *  - User is prompted for next action (nextCartActionNode)
 * - if (b) -> ClearableCartYesNoNode
 *  - Cart contents displayed
 *  - Cart subtotals displayed (subtotal, fees, total)
 *  - User is prompted to confirm clearing cart (yes/no): "Are you sure you want to clear your cart?"
 *
 *     - if (yes) -> SuccessClearCartNode
 *     - Cart is cleared
 *     - Displays "Looks like your cart is empty. Why not start shopping and add some items?"
 *     - Perform prompt to verify cart is empty
 *     - User is taken to ShopEntryNode
 *
 *     if (no) -> DeclinedClearCartNode
 *     - Displays "Ok! No changes have been made."
 *     - User is taken to nextCartActionNode
 * - if (c) -> MainMenuNode
 */

data class CartItemObj(val name: String, val price: Double, val quantity: Int, val details: String)

//todo rework these into individual function nodes

class NextCartActionNode(
    inputProvider: UserInputProvider,
    outputProvider: UserOutputProvider<InterrupterMessageEntity>,
    private val cartDataProvider: () -> MutableList<CartItemObj>,
) : InteractiveNode(inputProvider, outputProvider) {

    override suspend fun next(): TreeNode? {
        val menuOptions = """
            Please select an option:
            a) View cart
            b) Clear cart
            c) Return to main menu
        """.trimIndent()

        //todo impl this properly
        val selectedOption = "a"
//        val selectedOption = MenuOptionInputUtil(inputProvider, outputProvider, setOf("a", "b", "c")).getInput(menuOptions)

        return when (selectedOption) {
            "a" -> ViewCartNode(inputProvider, outputProvider, cartDataProvider)
            "b" -> ClearableCartYesNoNode(inputProvider, outputProvider, cartDataProvider)
            "c" -> MainMenuNode(inputProvider, outputProvider)
            else -> null
        }
    }
}
class ViewCartNode(
    inputProvider: UserInputProvider,
    outputProvider: UserOutputProvider<InterrupterMessageEntity>,
    private val cartDataProvider: () -> MutableList<CartItemObj>,
) : InteractiveNode(inputProvider, outputProvider) {

    override suspend fun next(): TreeNode? {
        val cartData = cartDataProvider()
        val cartDisplay = buildString {
            append("YOUR CART (${cartData.size} items)\n")
            append("–––––––––––––––––––––––––––––––––––––\n")
            cartData.forEachIndexed { index, item ->
                append("${index + 1}. ${item.name}\n")
                append("Price: $${item.price}\n")
                append("Quantity: ${item.quantity}\n")
                append("Details: ${item.details}\n")
                append("\n")
            }
        }
        sendOutput(cartDisplay)

        val totalsDisplay = buildString {
            val subtotal = cartData.sumByDouble { it.price * it.quantity }
            val fees = subtotal * 0.16 // Example fee calculation
            val total = subtotal + fees
            append("TOTALS\n")
            append("Subtotal: $${String.format("%.2f", subtotal)}\n")
            append("Fees: $${String.format("%.2f", fees)}\n")
            append("Total: $${String.format("%.2f", total)}\n")
        }
        sendOutput(totalsDisplay)

        return NextCartActionNode(inputProvider, outputProvider, cartDataProvider)
    }
}

class ClearableCartYesNoNode(
    inputProvider: UserInputProvider,
    outputProvider: UserOutputProvider<InterrupterMessageEntity>,
    private val cartDataProvider: () -> MutableList<CartItemObj>,
) : YesNoInputNode(inputProvider, outputProvider) {

    override suspend fun next(): TreeNode? {
        return fetchInput("Are you sure you want to clear your cart?")?.let {
            return when (it) {
                true -> SuccessClearCartNode(
                    inputProvider = inputProvider,
                    outputProvider = outputProvider,
                    cartDataProvider = cartDataProvider
                )
                false -> DeclinedClearCartNode(
                    inputProvider = inputProvider,
                    outputProvider = outputProvider,
                    cartDataProvider = cartDataProvider
                )
            }
        } ?: run {
            sendOutput("Invalid choice. Please try again.")
            null
        }
    }
}

class SuccessClearCartNode(
    private val inputProvider: UserInputProvider,
    outputProvider: UserOutputProvider<InterrupterMessageEntity>,
    val cartDataProvider: () -> MutableList<CartItemObj>,
) : OutputNode(outputProvider) {

    override suspend fun next(): TreeNode? {
        cartDataProvider().clear()
        sendOutput("Looks like your cart is empty. Why not start shopping and add some items?")
        // Assuming ShopEntryNode exists
        return ShopEntryNode(inputProvider, outputProvider)
    }
}

class DeclinedClearCartNode(
    private val inputProvider: UserInputProvider,
    outputProvider: UserOutputProvider<InterrupterMessageEntity>,
    private val cartDataProvider: () -> MutableList<CartItemObj>,
    ) : OutputNode(outputProvider) {

    override suspend fun next(): TreeNode? {
        sendOutput("Ok! No changes have been made.")
        return NextCartActionNode(inputProvider, outputProvider, cartDataProvider)
    }
}

class MainMenuNode(
    inputProvider: UserInputProvider,
    outputProvider: UserOutputProvider<InterrupterMessageEntity>,
) : OutputNode(outputProvider) {

    override suspend fun next(): TreeNode? {
        sendOutput("Returning to main menu...")
        //todo impl this
        return null
    }
}

class ShopEntryNode(
    inputProvider: UserInputProvider,
    outputProvider: UserOutputProvider<InterrupterMessageEntity>,
) : InteractiveNode(inputProvider, outputProvider) {

    override suspend fun next(): TreeNode? {
        sendOutput("Moving to shop menu...")
        //todo impl this
        return null
    }
}
