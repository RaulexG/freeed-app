package com.raulcn.freeed.feature.splash

data class SplashUiState(
    val isLoading: Boolean = true,
    val nextRoute: String? = null,
    val errorMessage: String? = null
)

