package com.vish.currencyconverter

interface ConverterRepository{
    suspend fun loadCurrencies(): List<CurrencyOptions>

    suspend fun loadRates(sourceCode: String): CurrencyRates

    fun close()


}
class NetworkConverterRepository(
    private val api: CurrencyApi,
) : ConverterRepository {
    override suspend fun loadCurrencies(): List<CurrencyOptions> = api.fetchCurrencies()

    override suspend fun loadRates(sourceCode: String): CurrencyRates= api.fetchRates(sourceCode)
    override  fun close() {
        api.close()
    }

}