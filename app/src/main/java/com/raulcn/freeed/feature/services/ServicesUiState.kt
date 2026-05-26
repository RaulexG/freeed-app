package com.raulcn.freeed.feature.services

import com.raulcn.freeed.domain.model.Category
import com.raulcn.freeed.domain.model.Service

data class MyServicesUiState(
    val services: List<Service> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class ServiceEditorUiState(
    val serviceId: String? = null,
    val title: String = "",
    val shortDescription: String = "",
    val description: String = "",
    val selectedCategory: Category? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

