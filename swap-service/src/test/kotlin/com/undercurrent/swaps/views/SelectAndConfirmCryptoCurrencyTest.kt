package com.undercurrent.swaps.views

import com.undercurrent.prompting.views.menubuilding.SelectYesNoPrompt
import com.undercurrent.swaps.views.banker_views.SelectCryptoPrompt
import com.undercurrent.testutils.TestAssertUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SelectAndConfirmCryptoCurrencyTest {

    @BeforeEach
    fun setUp() {
    }
    @Test
    fun `select invalid options`() {

    }

    @Test
    fun `select btc first, don't confirm, then select MOB and confirm`() {

    }

    @Test
    fun `confirm selection of bitcoin with single combined prompt call`() {

    }
    @Test
    fun `confirm selection of bitcoin with two prompt calls`() {
        val selectedCrypto = with(SelectCryptoPrompt()) {
            TestAssertUtils().assertContains(
                this.toString(), listOf(
                    "Select a crypto currency:",
                    "-[A] Bitcoin",
                    "-[B] MobileCoin",
                )
            )
            TestAssertUtils().assertDoesntContain(
                this.toString(), listOf(
                    "Please choose the cryptocurrency you're swapping from",
                    "::",
                )
            )

            val selectedCryptoOption = selectOption("A")
            TestAssertUtils().assertContains(
                selectedCryptoOption.toString(), listOf(
                    "-[A] Bitcoin",
                )
            )

            selectedCryptoOption?.item
        }

        SelectYesNoPrompt("Are you sure you want to select ${selectedCrypto?.label}?").let {
            TestAssertUtils().assertContains(
                it.toString(), listOf(
                    "Are you sure you want to select Bitcoin?",
                    "-[Y] YES",
                    "-[N] NO",
                )
            )

            it.selectOption("Y").let {
                TestAssertUtils().assertContains(
                    it.toString(), listOf(
                        "-[Y] YES",
                    )
                )
            }
        }

    }
}