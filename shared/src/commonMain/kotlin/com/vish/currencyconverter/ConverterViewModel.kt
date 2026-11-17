package com.vish.currencyconverter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.code


data class ConverterUiState(
    val isLoading: Boolean = true,
    val ratesLoading: Boolean = false,
    val errorMessage: String? = null,
    val currencies: List<CurrencyOptions> = emptyList(),
    val fromCode: String ="eur",
    val toCode: String = "inr",
    val amountText: String ="1",
    val resultText: String ="",
    val rateLine: String ="",
    val rateDate: String? = null
)
class ConverterViewModel(private val repository: ConverterRepository): ViewModel() {

    private var loadJobs: Job? = null
    private var rates: Map<String, Double> = emptyMap()

    private val _state = MutableStateFlow(ConverterUiState())
    val state : StateFlow<ConverterUiState> = _state.asStateFlow()
    init {
        refresh()
    }

    private fun showError(exception: Exception) {
        rates = emptyMap()
        val detail = exception.message?.take(160)?.trim().orEmpty()
        val message = if (detail.isEmpty()) "Could not load rates." else "Could not load rates. $detail"
       /* _state.update {
            it.copy(isLoading = false, ratesLoading = false,errorMessage = message)
        }*/
    }

    private fun refresh() {
        loadJobs?.cancel()
        loadJobs = viewModelScope.launch {
            _state.update { converterUiState ->
                converterUiState.copy(
                    isLoading = converterUiState.currencies.isEmpty(),
                    ratesLoading = true,
                    errorMessage = null,
                )
            }
            try {
                val currencies = repository.loadCurrencies()
                ensureActive()
                if (currencies.isEmpty()){
                    showError(IllegalStateException("The currency list was empty."))
                    return@launch
                }
                val from = _state.value.fromCode.takeIf { code -> currencies.any { it.code==code } }?:
                currencies.first().code

                val to = _state.value.toCode.takeIf { code -> currencies.any { it.code == code } && code != from }
                    ?: currencies.first { it.code != from }.code

                _state.update { it.copy(currencies=currencies, fromCode = from, toCode = to) }
                val loaded = repository.loadRates(from)
                ensureActive()
                if (_state.value.fromCode != from) return@launch
                rates = loaded.perUnit
                _state.update { converterUiState ->
                    withResult(
                        converterUiState.copy(
                            isLoading = false,
                            ratesLoading = false,
                            rateDate = loaded.date,
                            errorMessage = null
                        )
                    )
                }



            }
            catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                showError(error)
            }
        }
    }

    private fun loadRates(sourceCode: String){
        loadJobs?.cancel()
        loadJobs = viewModelScope.launch{
            try {
                val loaded = repository.loadRates(sourceCode)
                ensureActive()
                if (_state.value.fromCode!=sourceCode)return@launch

                rates = loaded.perUnit

                _state.update { converterUiState ->
                    withResult(
                        converterUiState.copy(
                            isLoading = false,
                            ratesLoading = false,
                            rateDate = loaded.date,
                            errorMessage = null
                        )
                    )
                }


            }
            catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (_state.value.fromCode == sourceCode)
                    showError(error)
            }
        }
    }

    private fun withResult(state: ConverterUiState): ConverterUiState {
        if (state.isLoading || state.ratesLoading){
            return state.copy(resultText = "", rateLine = "")
        }
        val amount = parseAmount(state.amountText)

        val rate = rates[state.toCode]
        //meant total amount × per-unit rate
        val result = when {
            amount == null -> ""
            state.fromCode == state.toCode -> formatResult(amount)
            rate == null -> ""
            else -> formatResult(convert(amount, rate))
        }
        //logical rate per unit
        val rateLine = when {
            state.fromCode == state.toCode -> "1 ${state.fromCode.uppercase()} = 1.00 ${state.toCode.uppercase()}"
            rate == null -> ""
            else -> "1 ${state.fromCode.uppercase()} = ${formatResult(rate)} ${state.toCode.uppercase()}"
        }
        return state.copy(resultText=result, rateLine = rateLine)
    }

    fun onAmountChange(text: String) {
        if (!isValidAmountInput(text)) return
        _state.update { converterUiStae ->
            withResult(converterUiStae.copy(amountText = text))
        }
    }

    fun onFromSelected(code: String) {
        if (code == _state.value.fromCode) return
        _state.update { converterUiState ->
            converterUiState.copy(fromCode = code, ratesLoading = true, errorMessage = null, resultText = "", rateLine = "")
        }
        loadRates(code)
    }

    fun onToSelected(code: String) {
        if (code == _state.value.toCode) return
        _state.update { converterUiState ->
            withResult(converterUiState.copy(toCode = code))
        }
    }

    fun retry() {
        refresh()
    }

    override fun onCleared() {
        loadJobs?.cancel()
        repository.close()
    }
    fun swap() {
        val current = _state.value
        val newFrom = current.toCode
        _state.update {
            current.copy(
                fromCode = newFrom,
                toCode = current.fromCode,
                ratesLoading = true,
                errorMessage = null,
                resultText = "",
                rateLine = "",
            )
        }
        loadRates(newFrom)
    }

    //showError needed done
    //show option for swapping done


    //pagination needed
}