package com.raulcn.freeed.feature.profile

import com.raulcn.freeed.domain.model.PortfolioItem
import com.raulcn.freeed.domain.model.Service
import com.raulcn.freeed.domain.model.StudentProfile

data class StudentProfileUiState(
    val profile: StudentProfile? = null,
    val portfolioItems: List<PortfolioItem> = emptyList(),
    val services: List<Service> = emptyList(),
    val isOwnProfile: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

