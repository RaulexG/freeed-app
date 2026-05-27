package com.raulcn.freeed.feature.home

import com.raulcn.freeed.core.model.ServiceModality
import com.raulcn.freeed.domain.model.Category
import com.raulcn.freeed.domain.model.Service

data class ExploreUiState(
    val services: List<Service> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val errorMessage: String? = null,
    val query: String = "",
    val skillQuery: String = "",
    val universityQuery: String = "",
    val degreeQuery: String = "",
    val semesterQuery: String = "",
    val selectedCategoryId: String? = null,
    val selectedModality: ServiceModality? = null,
    val favoriteIds: Set<String> = emptySet(),
    val isCompany: Boolean = false
) {
    val hasActiveFilters: Boolean
        get() = query.isNotBlank() ||
            skillQuery.isNotBlank() ||
            universityQuery.isNotBlank() ||
            degreeQuery.isNotBlank() ||
            semesterQuery.isNotBlank() ||
            selectedCategoryId != null ||
            selectedModality != null
}
