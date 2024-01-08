package com.undercurrent.legacy.types.enums

enum class StripeKeyType(val validationType: ResponseType) {
    LIVE(
        validationType = ResponseType.STRIPE_SECRET_LIVE_KEY
    ),
    TEST(
        validationType = ResponseType.STRIPE_SECRET_TEST_KEY
    )
}