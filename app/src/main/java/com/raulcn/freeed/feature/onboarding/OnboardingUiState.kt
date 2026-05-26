package com.raulcn.freeed.feature.onboarding

import com.raulcn.freeed.core.model.UserRole

data class RoleSelectionUiState(
    val selectedRole: UserRole? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class StudentProfileSetupUiState(
    val displayName: String = "",
    val universityName: String = "",
    val degreeProgram: String = "",
    val semester: String = "",
    val bio: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

data class CompanyProfileSetupUiState(
    val businessName: String = "",
    val industry: String = "",
    val contactPersonName: String = "",
    val description: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

