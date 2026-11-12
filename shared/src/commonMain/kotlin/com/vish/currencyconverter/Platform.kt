package com.vish.currencyconverter

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform