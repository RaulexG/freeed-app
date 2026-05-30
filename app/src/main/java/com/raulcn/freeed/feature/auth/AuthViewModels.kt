package com.raulcn.freeed.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulcn.freeed.core.model.UserRole
import com.raulcn.freeed.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginFormUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class RegisterFormUiState(
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val selectedRole: UserRole? = null,
    val isLoading: Boolean = false,
    val message: String? = null,
    val isSuccessMessage: Boolean = false,
    val awaitingEmailConfirmation: Boolean = false,
    val confirmationEmail: String? = null
)

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginFormUiState())
    val uiState: StateFlow<LoginFormUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }

    fun signIn(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingresa correo y contrasena.") }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email.trim()).matches()) {
            _uiState.update { it.copy(errorMessage = "Ingresa un correo valido.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                authRepository.signIn(
                    email = state.email,
                    password = state.password
                )
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "No se pudo iniciar sesion."
                    )
                }
            }
        }
    }
}

class RegisterViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterFormUiState())
    val uiState: StateFlow<RegisterFormUiState> = _uiState.asStateFlow()

    fun onDisplayNameChange(value: String) = _uiState.update { it.copy(displayName = value, message = null) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, message = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, message = null) }
    fun onConfirmPasswordChange(value: String) = _uiState.update { it.copy(confirmPassword = value, message = null) }
    fun onRoleSelected(role: UserRole) = _uiState.update { it.copy(selectedRole = role, message = null) }

    fun register(onFinished: () -> Unit) {
        val state = _uiState.value
        val validationMessage = validate(state)
        if (validationMessage != null) {
            _uiState.update { it.copy(message = validationMessage, isSuccessMessage = false) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            runCatching {
                authRepository.signUp(
                    displayName = state.displayName,
                    email = state.email,
                    password = state.password,
                    role = state.selectedRole ?: UserRole.STUDENT
                )
            }.onSuccess { signedIn ->
                if (signedIn) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = "Cuenta creada correctamente.",
                            isSuccessMessage = true,
                            awaitingEmailConfirmation = false
                        )
                    }
                    onFinished()
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = null,
                            isSuccessMessage = true,
                            awaitingEmailConfirmation = true,
                            confirmationEmail = state.email.trim()
                        )
                    }
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = throwable.message ?: "No se pudo crear la cuenta.",
                        isSuccessMessage = false
                    )
                }
            }
        }
    }

    private fun validate(state: RegisterFormUiState): String? {
        if (state.displayName.isBlank()) return "Ingresa tu nombre."
        if (state.displayName.trim().length > 100) return "El nombre no puede tener mas de 100 caracteres."
        if (state.email.isBlank()) return "Ingresa tu correo."
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email.trim()).matches()) return "Ingresa un correo valido."
        if (state.password.length < 8) return "La contrasena debe tener al menos 8 caracteres."
        if (state.password != state.confirmPassword) return "Las contrasenas no coinciden."
        if (state.selectedRole == null) return "Selecciona tu tipo de cuenta."
        return null
    }
}

