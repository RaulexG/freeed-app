package com.raulcn.freeed.feature.home

import com.raulcn.freeed.domain.model.Category
import com.raulcn.freeed.domain.model.Service

data class ExploreUiState(
    val services: List<Service> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
