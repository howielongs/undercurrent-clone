package com.undercurrent.swaps.views.customer

import com.undercurrent.swaps.views.banker_views.SelectCryptoPrompt
import com.undercurrent.prompting.views.menubuilding.SelectableOption
import com.undercurrent.legacyswaps.types.SwappableCrypto
import com.undercurrent.testutils.TestAssertUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CustomerSelectCryptoCurrencyViewTest {

    @BeforeEach
    fun setUp() {
    }

    private fun promptSelectionOfCryptoCurrencies(): SelectCryptoPrompt {
        val prompt = SelectCryptoPrompt("Please choose the cryptocurrency you're swapping from:")

        val result = prompt.toString()

        TestAssertUtils().assertContains(
            result, listOf(
                "Please choose the cryptocurrency you're swapping from:",
                "-[A] Bitcoin",
                "-[B] MobileCoin",
            )
        )
        return prompt
    }


    private fun selectCurrency(
        prompt: SelectCryptoPrompt,
        selectionHandle: String,
        vararg expectedList: String,
        notExpectedList: List<String> = listOf(),
    ): SelectableOption<String, SwappableCrypto>? {
        println("\n-> $selectionHandle\n")
        with(prompt.selectOption(selectionHandle)) {
            TestAssertUtils().assertContains(
                this.toString(), listOf(*expectedList),
            )
            TestAssertUtils().assertDoesntContain(
                this.toString(), notExpectedList
            )
            return this
        }
    }

    @Test
    fun `customer selects bitcoin from list`() {
        val prompt = promptSelectionOfCryptoCurrencies()

        val selection1 = selectCurrency(
            prompt = prompt,
            selectionHandle = "A",
            "Bitcoin (BTC)",
            notExpectedList = listOf(
                "MobileCoin",
            )
        )

        val selection2 = selectCurrency(
            prompt = prompt,
            selectionHandle = "B",
            "MobileCoin",
            notExpectedList = listOf(
                "Bitcoin",
            )
        )


    }
}