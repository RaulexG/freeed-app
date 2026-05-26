package com.raulcn.freeed.feature.portfolio

import com.raulcn.freeed.domain.model.PortfolioItem

data class PortfolioUiState(
    val items: List<PortfolioItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

