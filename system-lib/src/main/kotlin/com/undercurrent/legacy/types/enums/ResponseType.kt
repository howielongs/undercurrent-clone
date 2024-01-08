package com.undercurrent.legacy.types.enums

import com.undercurrent.system.context.SystemContext
import com.undercurrent.legacy.dinosaurs.prompting.InputValidator
import kotlin.reflect.KFunction2

enum class ResponseType(
    val validateFunction: KFunction2<String, SystemContext?, String?> =
        InputValidator::validateString,
) {
    SMS(InputValidator::validatePhoneNumber),

    CRYPTO_ADDRESS(InputValidator::validateBitcoinAddress),
    BTC_ADDRESS(InputValidator::validateBitcoinAddress),
    MOB_ADDRESS(InputValidator::validateMobileCoinAddress),

    BOOLEAN,
    STRING,

    //todo need to enforce upper and lower bounds
    INT(InputValidator::validateInt),
    POSITIVE_INT(InputValidator::validatePositiveInt),
    FEE_PERCENT(InputValidator::validateFeePercent),

    //todo add validation for this
    DECIMAL(),

    //will support individual currency types in future
    CURRENCY(InputValidator::validateCurrency),
    YESNO(InputValidator::validateYesNo),
    STRIPE_SECRET_TEST_KEY(InputValidator::validateStripeSecretTestKey),
    STRIPE_SECRET_LIVE_KEY(InputValidator::validateStripeSecretLiveKey),
    ZIPCODE,
    INDEX,
}