package com.undercurrent.legacy.commands.registry.lifecycle.nodes

import com.undercurrent.legacyshops.nodes.customer_nodes.*
import com.undercurrent.testutils.MockInputProvider
import com.undercurrent.testutils.MockOutputProvider
import com.undercurrent.testutils.TestIOFormatterProvider
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

// still need to fix up string usage
class CartActionTests {
    private val formatter = TestIOFormatterProvider()
    private val mockInputProvider = MockInputProvider(formatter)
    private val mockOutputProvider = MockOutputProvider(formatter)
    private val mockCartDataProvider: () -> MutableList<CartItemObj> = { mutableListOf() }

    @Test
    fun `NextCartActionNode - selects view cart option`() = runBlocking {
        mockInputProvider.setInput("a")
        val node = NextCartActionNode(mockInputProvider, mockOutputProvider, mockCartDataProvider)
        assertTrue(node.next() is ViewCartNode)
    }

    @Test
    fun `ViewCartNode - displays cart items`() = runBlocking {
        val cartItems = mutableListOf(
            CartItemObj("Item1", 10.0, 2, "Details1"),
            CartItemObj("Item2", 5.0, 1, "Details2")
        )
        val mockCartDataProvider: () -> MutableList<CartItemObj> = { cartItems }
        val node = ViewCartNode(mockInputProvider, mockOutputProvider, mockCartDataProvider)
        node.next()
        assertTrue(mockOutputProvider.getOutput().contains("YOUR CART (2 items)"))
    }

    @Test
    fun `ClearableCartYesNoNode - confirms cart clearing`() = runBlocking {
        mockInputProvider.setInput("yes")
        val node = ClearableCartYesNoNode(mockInputProvider, mockOutputProvider, mockCartDataProvider)
        assertTrue(node.next() is SuccessClearCartNode)
    }

    @Test
    fun `ClearableCartYesNoNode - declines cart clearing`() = runBlocking {
        mockInputProvider.setInput("no")
        val node = ClearableCartYesNoNode(mockInputProvider, mockOutputProvider, mockCartDataProvider)
        assertTrue(node.next() is DeclinedClearCartNode)
    }

    @Test
    fun `SuccessClearCartNode - clears cart and notifies user`() = runBlocking {
        val cartItems = mutableListOf(
            CartItemObj("Item1", 10.0, 2, "Details1"),
            CartItemObj("Item2", 5.0, 1, "Details2")
        )
        val mockCartDataProvider: () -> MutableList<CartItemObj> = { cartItems }
        val node = SuccessClearCartNode(mockInputProvider, mockOutputProvider, mockCartDataProvider)
        node.next()
        assertTrue(cartItems.isEmpty()) // Ensure cart is cleared
        assertTrue(
            mockOutputProvider.getOutput()
                .contains("Looks like your cart is empty. Why not start shopping and add some items?")
        )
    }

    @Test
    fun `DeclinedClearCartNode - does not clear cart and notifies user`() = runBlocking {
        val cartItems = mutableListOf(
            CartItemObj("Item1", 10.0, 2, "Details1"),
            CartItemObj("Item2", 5.0, 1, "Details2")
        )
        val mockCartDataProvider: () -> MutableList<CartItemObj> = { cartItems }
        val node = DeclinedClearCartNode(mockInputProvider, mockOutputProvider, mockCartDataProvider)
        node.next()
        assertEquals(2, cartItems.size) // Ensure cart is not cleared
        assertTrue(mockOutputProvider.getOutput().contains("Ok! No changes have been made."))
    }

    // Additional tests can be added for MainMenuNode and ShopEntryNode
}
