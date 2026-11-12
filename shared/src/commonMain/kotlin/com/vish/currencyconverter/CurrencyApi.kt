package com.vish.currencyconverter

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.code


private const val baseUrl = "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/"

private const val pages = "https://latest.currency-api.pages.dev/v1/"

class CurrencyApi (private val client: HttpClient){
    suspend fun fetchCurrencies(): List<CurrencyOptions>{
        val root = Json.parseToJsonElement(getText("currencies.json")).jsonObject
        val currencies=root.map { (code,name) ->
            CurrencyOptions(code, name = name.jsonPrimitive.content)
        }.sortedBy { it.code }
        println("currencies ${currencies.size}")
        return currencies
        // require sorting
    }

    suspend fun fetchRates(sourceCode: String): CurrencyRates{
        val sourceCode = sourceCode.lowercase()
        val root = Json.parseToJsonElement(getText("currencies/$sourceCode.json")).jsonObject
        val date = root.getValue("date").jsonPrimitive.content

        val perUnit =root.getValue(sourceCode).jsonObject.mapValues { (_,value) ->
            value.jsonPrimitive.double
        }
        println("cur $sourceCode ,date $date proUnit $perUnit")
        return CurrencyRates(date, perUnit)
    }

    private suspend fun getText(path: String): String {
        val body = try {
            client.get(baseUrl + path).bodyAsText()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            client.get(pages + path).bodyAsText()
        }
        println("API $path ${body.length} chars: ${body.take(300)}")
        return body
    }

    fun close() {
        client.close()
    }
}