package com.raulcn.freeed.feature.home

import com.raulcn.freeed.domain.model.Category
import com.raulcn.freeed.domain.model.ProfileSummary
import com.raulcn.freeed.domain.model.Service

data class HomeUiState(
    val greetingName: String = "",
    val searchQuery: String = "",
    val categories: List<Category> = emptyList(),
    val featuredServices: List<Service> = emptyList(),
    val featuredStudents: List<ProfileSummary> = emptyList(),
    val servicesCount: Int = 0,
    val portfolioCount: Int = 0,
    val requestsCount: Int = 0,
    val favoriteIds: Set<String> = emptySet(),
    val isCompany: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
