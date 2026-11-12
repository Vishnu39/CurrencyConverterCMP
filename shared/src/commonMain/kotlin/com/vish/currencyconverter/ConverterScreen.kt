package com.vish.currencyconverter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.code
import kotlin.text.compareTo

@Composable
fun ConverterScreen(viewModel: ConverterViewModel){
    val state by viewModel.state.collectAsState()
    val errorMessage = state.errorMessage
    when {
        state.isLoading && state.currencies.isEmpty() -> LoadingPane()
        errorMessage != null && state.currencies.isEmpty() -> ErrorPane(
            errorMessage,
            viewModel::retry
        )
        else -> ConverterForm (
            state = state,
            onAmountChange = viewModel::onAmountChange,
            onFromSelected = viewModel::onFromSelected,
            onToSelected = viewModel::onToSelected,
            onSwap = viewModel::swap,
            onRetry = viewModel::retry
            )
    }
}


@Composable
private fun LoadingPane() {
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(12.dp))
        Text("Loading currencies")
    }
}

@Composable
private fun ErrorPane(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun ConverterForm(
    state: ConverterUiState,
    onAmountChange: (String) -> Unit,
    onFromSelected: (String) -> Unit,
    onToSelected: (String) -> Unit,
    onSwap: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Convert",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        CurrencyDropdown(
            label = "From",
            selectedCode = state.fromCode,
            currencies = state.currencies,
            onSelected = onFromSelected,
        )
        Button(
            onClick = onSwap,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text("Swap")
        }
        CurrencyDropdown(
            label = "To",
            selectedCode = state.toCode,
            currencies = state.currencies,
            onSelected = onToSelected,
        )
        OutlinedTextField(
            value = state.amountText,
            onValueChange = onAmountChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Amount") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        if (state.ratesLoading) {
            CircularProgressIndicator()
        } else {
            Text(
                text = if (state.resultText.isEmpty()) {
                    "—"
                } else {
                    "${state.resultText} ${state.toCode.uppercase()}"
                },
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.SemiBold,
            )
            if (state.rateLine.isNotEmpty()) {
                Text(
                    text = state.rateLine,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (state.rateDate != null) {
                Text(
                    text = "Rates from ${state.rateDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        val errorMessage = state.errorMessage
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyDropdown(
    label: String,
    selectedCode: String,
    currencies: List<CurrencyOptions>,
    onSelected: (String) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val selected = currencies.firstOrNull { it.code == selectedCode }
    Box(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected?.let { "${it.code.uppercase()} — ${it.name}" }.orEmpty(),
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            singleLine = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = open) },
        )
        Box(
            Modifier
                .matchParentSize()
                .clickable { open = true },
        )
    }
    if (open) {
        CurrencyPickerDialog(
            label = label,
            currencies = currencies,
            onDismiss = { open = false },
            onSelected = { code ->
                onSelected(code)
                open = false
            },
        )
    }
}

@Composable
private fun CurrencyPickerDialog(
    label: String,
    currencies: List<CurrencyOptions>,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var page by remember { mutableStateOf(1) }
    val matches = remember(currencies, query) { filterCurrencies(currencies, query) }
    val visible = currencyPage(matches, page)
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = label, style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = query,
                    onValueChange = { value ->
                        query = value
                        page = 1
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search") },
                    singleLine = true,
                )
                if (matches.isEmpty()) {
                    Text("No currency matches")
                } else {
                    LazyColumn(Modifier.height(360.dp).fillMaxWidth()) {
                        items(visible, key = { it.code }) { currency ->
                            TextButton(
                                onClick = { onSelected(currency.code) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = "${currency.code.uppercase()} — ${currency.name}",
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                        if (visible.size < matches.size) {
                            item {
                                TextButton(
                                    onClick = { page += 1 },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text("Show more (${matches.size - visible.size} left)")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}