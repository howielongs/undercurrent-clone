package com.undercurrent.shared.experimental


object CustomerBrowseStates {
    sealed class State : FsmState {
        object Entry : State()
        object BrowseProducts : State()
        object ViewCart : State()
        object EmptyCart : State()
        object Checkout : State()
    }

    sealed class Event : FsmEvent {
        object Start : Event()
        object SelectProduct : Event()
        object ViewCartEvent : Event()
        object CheckoutEvent : Event()
        object CartIsEmpty : Event()
    }
}


class ShopCustomerBrowseFsm(
    private var currentState: CustomerBrowseStates.State = CustomerBrowseStates.State.Entry
) : BaseFsm<CustomerBrowseStates.State, CustomerBrowseStates.Event>(currentState) {

    private val cart = mutableListOf<String>()

    fun addProductToCart(product: String) {
        cart.add(product)
    }

    override fun handleEvent(event: CustomerBrowseStates.Event) {
        currentState = when (currentState) {
            is CustomerBrowseStates.State.Entry -> {
                when (event) {
                    is CustomerBrowseStates.Event.Start -> CustomerBrowseStates.State.BrowseProducts
                    else -> currentState
                }
            }

            is CustomerBrowseStates.State.BrowseProducts -> {
                when (event) {
                    is CustomerBrowseStates.Event.SelectProduct -> {
                        addProductToCart("ExampleProduct")
                        CustomerBrowseStates.State.BrowseProducts
                    }

                    is CustomerBrowseStates.Event.ViewCartEvent -> {
                        if (cart.isEmpty()) CustomerBrowseStates.State.EmptyCart else CustomerBrowseStates.State.ViewCart
                    }

                    else -> currentState
                }
            }

            is CustomerBrowseStates.State.ViewCart -> {
                when (event) {
                    is CustomerBrowseStates.Event.CheckoutEvent -> CustomerBrowseStates.State.Checkout
                    else -> currentState
                }
            }

            is CustomerBrowseStates.State.EmptyCart -> {
                when (event) {
                    is CustomerBrowseStates.Event.Start -> CustomerBrowseStates.State.BrowseProducts
                    else -> currentState
                }
            }

            is CustomerBrowseStates.State.Checkout -> currentState // End CustomerBrowseState or can loop back based on other conditions

        }
    }

    override fun display(): String {
        return when (currentState) {
            is CustomerBrowseStates.State.Entry -> "Welcome to the shop!"
            is CustomerBrowseStates.State.BrowseProducts -> "Browse products"
            is CustomerBrowseStates.State.ViewCart -> "View cart"
            is CustomerBrowseStates.State.EmptyCart -> "Empty cart"
            is CustomerBrowseStates.State.Checkout -> "Checkout"
        }
    }
}

private fun sampleUsage() {
    val chatbot = ShopCustomerBrowseFsm()

    println(chatbot.display()) // Welcome message
    chatbot.handleEvent(CustomerBrowseStates.Event.Start)
    println(chatbot.display()) // Product browsing

    chatbot.handleEvent(CustomerBrowseStates.Event.SelectProduct)
    chatbot.handleEvent(CustomerBrowseStates.Event.ViewCartEvent)
    println(chatbot.display()) // View cart with products

    chatbot.handleEvent(CustomerBrowseStates.Event.CheckoutEvent)
    println(chatbot.display()) // Checkout
}