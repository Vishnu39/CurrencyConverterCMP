package com.vish.currencyconverter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import kotlin.reflect.KClass

class ConverterViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        val repository = NetworkConverterRepository(CurrencyApi(createHttpClient()))
        @Suppress("UNCHECKED_CAST")
        return ConverterViewModel(repository) as T
    }
}