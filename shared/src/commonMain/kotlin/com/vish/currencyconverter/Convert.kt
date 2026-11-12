package com.vish.currencyconverter

import kotlin.math.abs
import kotlin.math.round

data class CurrencyOptions(val code: String, val name: String )


data class CurrencyRates(val date: String,val perUnit: Map<String,Double>)
const val CurrencyPageSize = 30

fun filterCurrencies(currencies: List<CurrencyOptions>, query: String): List<CurrencyOptions> {
    val needle = query.trim().lowercase()
    if (needle.isEmpty()) return currencies
    return currencies.filter { currency ->
        currency.code.contains(needle) || currency.name.lowercase().contains(needle)
    }
}

fun currencyPage(currencies: List<CurrencyOptions>, page: Int): List<CurrencyOptions> {
    val count = page.coerceAtLeast(1) * CurrencyPageSize
    return currencies.take(count)
}

fun parseAmount(text: String): Double? {
    if (text.isEmpty() || text == "." || text == "," || !isValidAmountInput(text)) return null
    return text.replace(',', '.').toDoubleOrNull()
}

fun isValidAmountInput(text: String): Boolean {
    if (text.length > 16) return false
    var separatorSeen = false
    for (character in text) {
        when {
            character.isDigit() -> Unit
            character == '.' || character == ',' -> {
                if (separatorSeen) return false
                separatorSeen = true
            }
            else -> return false
        }
    }
    return true
}

fun convert(amount: Double, rate: Double): Double = amount * rate

fun formatResult(value: Double): String{
    if (!value.isFinite() || abs(value) > 9.0e14) return ""
    val negative = value < 0.0
    val scaled = round(abs(value) * 10_000.0).toLong()
    val whole = scaled / 10_000
    val fraction = (scaled % 10_000).toString().padStart(4, '0')
    val decimals = fraction.dropLastWhile { it == '0' }.padEnd(2, '0')
    val body = "$whole.$decimals"
    return if (negative && scaled != 0L) "-$body" else body

}
