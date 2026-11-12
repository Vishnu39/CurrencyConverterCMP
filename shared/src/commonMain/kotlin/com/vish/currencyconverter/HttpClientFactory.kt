package com.vish.currencyconverter

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout

expect fun createHttpClient(): HttpClient

fun HttpClientConfig<*>.applyDefaults(){
    expectSuccess=true


    install(HttpTimeout){
        connectTimeoutMillis = 10_000
        requestTimeoutMillis = 15_000

    }
}