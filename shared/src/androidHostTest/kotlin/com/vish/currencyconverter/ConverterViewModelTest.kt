package com.vish.currencyconverter

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.junit.JUnitAsserter.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class ConverterViewModelTest {

    @BeforeTest
    fun setUp(){
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    @Test
    fun changingTheTargetDoesNotFetchRatesAgain() {
        val repository = FakeCurrencyRepository()
        val viewModel = ConverterViewModel(repository)

        assertEquals(1, repository.rateRequests)
        assertEquals("100.00", viewModel.state.value.resultText)

        viewModel.onToSelected("usd")
        assertEquals(1, repository.rateRequests)
        assertEquals("2.00", viewModel.state.value.resultText)

        viewModel.onAmountChange("2,5")
        assertEquals(1, repository.rateRequests)
        assertEquals("5.00", viewModel.state.value.resultText)

        viewModel.onFromSelected("usd")
        assertEquals(2, repository.rateRequests)
        assertEquals("usd", viewModel.state.value.fromCode)
    }
    @Test
    fun whenLoadingFailsTheErrorIsShownAndLoadingStops() {
        // Arrange: the fake API fails, like both hosts being down
        val repository = FailingCurrencyRepository()

        // Act: creating the ViewModel starts refresh() right away
        val viewModel = ConverterViewModel(repository)
        val state = viewModel.state.value

        // Assert: the error message is shown and the spinner is gone
        assertNotNull(state.errorMessage)
        assertEquals("Could not load rates. Network error", state.errorMessage)
        assertFalse(state.isLoading)
        assertFalse(state.ratesLoading)
    }
    private class FailingCurrencyRepository : ConverterRepository {
        override suspend fun loadCurrencies(): List<CurrencyOptions> {
            throw IllegalStateException("Network error")
        }

        override suspend fun loadRates(sourceCode: String): CurrencyRates {
            error("Rates should not be loaded when the currency list fails")
        }

        override fun close() = Unit
    }
}

private class FakeCurrencyRepository : ConverterRepository {
    var rateRequests: Int = 0

    override suspend fun loadCurrencies(): List<CurrencyOptions> = listOf(
        CurrencyOptions("eur", "Euro"),
        CurrencyOptions("inr", "Indian Rupee"),
        CurrencyOptions("usd", "US Dollar"),
    )

    override suspend fun loadRates(sourceCode: String): CurrencyRates {
        rateRequests += 1
        val perUnit = when (sourceCode) {
            "usd" -> mapOf("usd" to 1.0, "eur" to 0.5, "inr" to 50.0)
            else -> mapOf("eur" to 1.0, "usd" to 2.0, "inr" to 100.0)
        }
        return CurrencyRates(date = "2026-11-10", perUnit = perUnit)
    }

    override fun close() = Unit
}