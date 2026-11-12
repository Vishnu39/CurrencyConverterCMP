package com.vish.currencyconverter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vish.currencyconverter.theme.AppTheme
import org.jetbrains.compose.resources.painterResource

import currencyconverter.shared.generated.resources.Res
import currencyconverter.shared.generated.resources.compose_multiplatform

@Composable
@Preview
fun App() {
    AppTheme {
        val platformOwner = LocalViewModelStoreOwner.current
        if (platformOwner != null) {
            ConverterContent(platformOwner)
        } else {
            ConverterContent(rememberAppViewModelStoreOwner())
        }
    }
}

@Composable
private fun ConverterContent(owner: ViewModelStoreOwner) {
    val viewModel: ConverterViewModel = viewModel(
        viewModelStoreOwner = owner,
        factory = ConverterViewModelFactory(),
    )
    ConverterScreen(viewModel)
}

@Composable
private fun rememberAppViewModelStoreOwner(): ViewModelStoreOwner {
    val owner = remember {
        object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
    }
    DisposableEffect(owner) {
        onDispose { owner.viewModelStore.clear() }
    }
    return owner
}