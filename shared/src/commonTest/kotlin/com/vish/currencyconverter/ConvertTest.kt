package com.vish.currencyconverter

import kotlin.code
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConvertTest {
    @Test
    fun commaAndDotAreBothDecimalSeparators() {
        assertEquals(1.5, parseAmount("1,5"))
        assertEquals(1.5, parseAmount("1.5"))
        assertEquals(2.0, parseAmount("2"))
        assertNull(parseAmount(""))
        assertNull(parseAmount("1,5.0"))
        assertTrue(isValidAmountInput("1,"))
        assertFalse(isValidAmountInput("12a"))
        assertFalse(isValidAmountInput("1,2,3"))
    }

    @Test
    fun resultUsesTwoToFourDecimals() {
        assertEquals(3.0, convert(2.0, 1.5))
        assertEquals("1.20", formatResult(1.2))
        assertEquals("1.234", formatResult(1.234))
        assertEquals("1.2346", formatResult(1.23456))
        assertEquals("5.00", formatResult(convert(2.5, 2.0)))
    }

    @Test
    fun searchAndPageDoNotReturnTheWholeList() {
        val currencies = List(45) { index ->
            CurrencyOptions(code = "c$index", name = if (index == 12) "Indian Rupee" else "Other")
        }
        val matches = filterCurrencies(currencies, "rup")
        assertEquals(listOf("c12"), matches.map { it.code })
        assertEquals(30, currencyPage(currencies, page = 1).size)
        assertEquals(45, currencyPage(currencies, page = 2).size)
    }
}