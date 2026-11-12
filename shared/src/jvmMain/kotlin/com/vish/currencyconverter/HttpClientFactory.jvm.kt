package com.vish.currencyconverter

import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java

actual fun createHttpClient(): HttpClient = HttpClient(Java) {
    applyDefaults()
}