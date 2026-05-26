package com.raulcn.freeed.feature.auth

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val rememberSession: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val selectedRole: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

