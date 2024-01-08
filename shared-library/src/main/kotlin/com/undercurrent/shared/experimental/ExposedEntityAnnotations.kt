package com.undercurrent.shared.experimental

import kotlin.reflect.KProperty

internal object AnnotationFetch {
    enum class UserResponseType {
        SMS,

        BTC_ADDRESS,
        MOB_ADDRESS,

        BOOLEAN,
        STRING,
        INT,
        POSITIVE_INT,
        FEE_PERCENT,

        DECIMAL,

        CURRENCY,
        YESNO,
        STRIPE_SECRET_TEST_KEY,
        STRIPE_SECRET_LIVE_KEY,
        ZIPCODE,
        INDEX,
    }

    annotation class DefaultPromptParams(
        val promptText: String,
        val label: String,
        val validationType: UserResponseType = UserResponseType.STRING,
    )

    fun promptText(field: KProperty<*>?): String? {
        return extractParams(field)?.promptText
    }

    fun validationType(field: KProperty<*>?): UserResponseType? {
        return extractParams(field)?.validationType
    }

    fun confirmLabel(field: KProperty<*>?): String? {
        return extractParams(field)?.label
    }

    private fun extractParams(field: KProperty<*>?): DefaultPromptParams? {
        extract(field)?.let {
            if (it is DefaultPromptParams) {
                return it
            }
        }
        return null
    }

    private fun extract(field: KProperty<*>?): Annotation? {
        return field
            ?.annotations
            ?.filterIsInstance<DefaultPromptParams>()
            ?.toList()
            ?.firstOrNull()
    }

}