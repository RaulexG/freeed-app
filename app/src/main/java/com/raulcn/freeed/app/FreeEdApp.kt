package com.raulcn.freeed.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raulcn.freeed.app.navigation.FreeEdNavHost
import com.raulcn.freeed.app.session.AppSessionViewModel
import com.raulcn.freeed.feature.system.ConfigurationRequiredScreen

@Composable
fun FreeEdApp() {
    val sessionViewModel: AppSessionViewModel = viewModel()
    val uiState by sessionViewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isConfigurationMissing) {
        ConfigurationRequiredScreen()
    } else {
        FreeEdNavHost(sessionViewModel = sessionViewModel)
    }
}
