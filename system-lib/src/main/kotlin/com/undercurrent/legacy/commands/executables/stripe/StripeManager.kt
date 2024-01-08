package com.undercurrent.legacy.commands.executables.stripe

import com.stripe.Stripe
import com.stripe.model.*
import com.stripe.model.checkout.SessionCollection
import com.stripe.param.PaymentIntentListParams
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomer
import org.jetbrains.exposed.sql.transactions.transaction
import com.stripe.model.PaymentLink as StripePaymentLink
import com.stripe.model.Price as StripePrice
import com.stripe.model.Product as StripeProduct
import com.stripe.model.checkout.Session as StripeCheckoutSession


class StripeManager() {
    private lateinit var apiSecretKey: String
    private lateinit var thisCustomer: ShopCustomer

    constructor(customer: ShopCustomer) : this(
    ) {
        //todo better way to handle null here?
        apiSecretKey = transaction { customer.storefront.stripeApiKey?.stripeApiSecretKey.toString() }
        thisCustomer = customer
    }

    data class PaymentPair(val stripePriceId: String, val qty: Int = 1) {
        fun toLineItem(): MutableMap<String, Any> {
            val lineItem1: MutableMap<String, Any> = HashMap()
            lineItem1["price"] = stripePriceId
            lineItem1["quantity"] = qty
            return lineItem1
        }
    }

    fun isPaymentSuccessful(paymentIntentId: String): Boolean {
        return paymentStatus(paymentIntentId) == "succeeded"
    }

    /**
     * {
     *   "id": "pi_1DsvKq2eZvKYlo2Cm4b0JlP4",
     *   "object": "payment_intent",
     *   "amount": 1099,
     *   "amount_capturable": 0,
     *   "amount_details": {
     *     "tip": {}
     *   },
     *   "amount_received": 0,
     *   "application": null,
     *   "application_fee_amount": null,
     *   "automatic_payment_methods": null,
     *   "canceled_at": null,
     *   "cancellation_reason": null,
     *   "capture_method": "automatic",
     *   "client_secret": "pi_1DsvKq2eZvKYlo2Cm4b0JlP4_secret_GFDVGuqAjHdTokVK6EEEOeqR0",
     *   "confirmation_method": "automatic",
     *   "created": 1547571860,
     *   "currency": "usd",
     *   "customer": null,
     *   "description": null,
     *   "invoice": null,
     *   "last_payment_error": null,
     *   "latest_charge": null,
     *   "livemode": false,
     *   "metadata": {},
     *   "next_action": null,
     *   "on_behalf_of": null,
     *   "payment_method": null,
     *   "payment_method_options": {},
     *   "payment_method_types": [
     *     "card"
     *   ],
     *   "processing": null,
     *   "receipt_email": null,
     *   "review": null,
     *   "setup_future_usage": null,
     *   "shipping": null,
     *   "statement_descriptor": null,
     *   "statement_descriptor_suffix": null,
     *   "status": "requires_payment_method",
     *   "transfer_data": null,
     *   "transfer_group": null
     * }
     */
    fun paymentStatus(paymentIntentId: String): String? {
        fetchPaymentIntent(paymentIntentId)?.let {
            return it.status.toString()
        }
        return null
    }


    /**
     * stripe payment_links create -d "line_items[0][price]"='price_1MSinLBDQuAhEHnEdytT7eK7' -d "line_items[0][quantity]"=2
     *
     * {
     *   "id": "plink_1MSiqpBDQuAhEHnE9N4v5HUX",
     *   "object": "payment_link",
     *   "active": true,
     *   "after_completion": {
     *     "hosted_confirmation": {
     *       "custom_message": null
     *     },
     *     "type": "hosted_confirmation"
     *   },
     *   "allow_promotion_codes": false,
     *   "application_fee_amount": null,
     *   "application_fee_percent": null,
     *   "automatic_tax": {
     *     "enabled": false
     *   },
     *   "billing_address_collection": "auto",
     *   "consent_collection": null,
     *   "currency": "usd",
     *   "custom_text": {
     *     "shipping_address": null,
     *     "submit": null
     *   },
     *   "customer_creation": "if_required",
     *   "livemode": false,
     *   "metadata": {},
     *   "on_behalf_of": null,
     *   "payment_intent_data": null,
     *   "payment_method_collection": "always",
     *   "payment_method_types": null,
     *   "phone_number_collection": {
     *     "enabled": false
     *   },
     *   "shipping_address_collection": null,
     *   "shipping_options": [],
     *   "submit_type": "auto",
     *   "subscription_data": null,
     *   "tax_id_collection": {
     *     "enabled": false
     *   },
     *   "transfer_data": null,
     *   "url": "https://buy.stripe.com/test_9AQ9Dm8wF14244o9AA"
     * }
     */
    fun createPaymentLink(
        stripePaymentPairs: List<PaymentPair>
    ): StripePaymentLink? {
        val lineItems: MutableList<Any> = ArrayList()

        stripePaymentPairs.forEach {
            lineItems.add(it.toLineItem())
        }

        val params: MutableMap<String, Any> = HashMap()
        params["line_items"] = lineItems

        return StripePaymentLink.create(params)
    }

    fun createStripeProductAndPrice(
        name: String,
        description: String? = null,
        unitAmountCents: Int,
        currency: String = "usd",
    ): StripePrice? {
        //todo enhance this to also fetch existing items
        createStripeProduct(name, description)?.let { stripeProduct ->
            createStripePrice(unitAmountCents, currency, stripeProduct.id)?.let { stripePrice ->
                return stripePrice
            }
        }
        return null
    }

    /**
     * stripe prices create --unit-amount=1251 --currency=usd --product=prod_ND93ByaIyJDLfY
     *
     * {
     *   "id": "price_1MSinLBDQuAhEHnEdytT7eK7",
     *   "object": "price",
     *   "active": true,
     *   "billing_scheme": "per_unit",
     *   "created": 1674314151,
     *   "currency": "usd",
     *   "custom_unit_amount": null,
     *   "livemode": false,
     *   "lookup_key": null,
     *   "metadata": {},
     *   "nickname": null,
     *   "product": "prod_ND93ByaIyJDLfY",
     *   "recurring": null,
     *   "tax_behavior": "unspecified",
     *   "tiers_mode": null,
     *   "transform_quantity": null,
     *   "type": "one_time",
     *   "unit_amount": 1251,
     *   "unit_amount_decimal": "1251"
     * }
     */
    private fun createStripePrice(
        unitAmountCents: Int,
        currency: String = "usd",
        stripeProductId: String,
    ): StripePrice? {
        Stripe.apiKey = apiSecretKey

        //todo create method to create product and price together

        val params: MutableMap<String, Any> = HashMap()
        params["unit_amount"] = unitAmountCents
        params["currency"] = currency
        params["product"] = stripeProductId

        return StripePrice.create(params)
    }

    /**
     * stripe products create --name="Medium T-Shirt" --description="Plain white t-shirt"
     *
     * data response:
     * {
     *   "id": "prod_ND93ByaIyJDLfY",
     *   "object": "product",
     *   "active": true,
     *   "attributes": [],
     *   "created": 1674314025,
     *   "default_price": null,
     *   "description": "Plain white t-shirt",
     *   "images": [],
     *   "livemode": false,
     *   "metadata": {},
     *   "name": "Medium T-Shirt",
     *   "package_dimensions": null,
     *   "shippable": null,
     *   "statement_descriptor": null,
     *   "tax_code": null,
     *   "type": "service",
     *   "unit_label": null,
     *   "updated": 1674314025,
     *   "url": null
     * }
     */
    private fun createStripeProduct(
        name: String,
        description: String? = null,
    ): StripeProduct {
        Stripe.apiKey = apiSecretKey

        val params: MutableMap<String, Any> = HashMap()
        params["name"] = name
        description?.let {
            params["description"] = it
        }

        return StripeProduct.create(params)
    }

    fun fetchPaymentIntent(intentId: String): PaymentIntent? {
        Stripe.apiKey = apiSecretKey

        return PaymentIntent.retrieve(
            intentId
        )
    }

    fun isPaid(paymentLinkId: String): Boolean? {
        listCheckoutSessions()?.data?.let { stripeSessions ->
            stripeSessions.toList().singleOrNull {
                it.paymentLink.toString() == paymentLinkId
            }?.let { stripeCheckoutSession ->
                return stripeCheckoutSession.paymentStatus == "paid"
            }
        }
        return null
    }

    fun fetchDataFromCheckoutSession(paymentLinkId: String): Pair<String, String>? {
        listCheckoutSessions()?.data?.let { stripeSessions ->
            stripeSessions.toList().singleOrNull {
                it.paymentLink.toString() == paymentLinkId
            }?.let { stripeCheckoutSession ->
                return Pair(stripeCheckoutSession.id, stripeCheckoutSession.paymentIntent)
            }
        }
        return null
    }

    fun listCheckoutSessions(): SessionCollection? {
        Stripe.apiKey = apiSecretKey

        val params: MutableMap<String, Any> = HashMap()

        return StripeCheckoutSession.list(params)
    }

    //todo wrap each of these in coroutines
    fun populatePaymentData(pLinkId: String): StripePaymentLink? {
        Stripe.apiKey = apiSecretKey

        return StripePaymentLink.retrieve(pLinkId)

    }


    fun listPaymentIntents(): PaymentIntentCollection? {
        Stripe.apiKey = apiSecretKey

        val params = PaymentIntentListParams
            .builder()
            .setLimit(5)
            .build()

        return PaymentIntent.list(params)
    }

}