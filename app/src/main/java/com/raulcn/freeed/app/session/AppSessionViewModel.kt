package com.raulcn.freeed.app.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulcn.freeed.core.model.ProfileStatus
import com.raulcn.freeed.core.model.UserRole
import com.raulcn.freeed.data.repository.AuthRepository
import com.raulcn.freeed.data.repository.ProfileRepository
import com.raulcn.freeed.data.remote.supabase.SupabaseConfig
import com.raulcn.freeed.domain.model.AppUserProfile
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SessionRouteTarget {
    data object Login : SessionRouteTarget
    data object Home : SessionRouteTarget
    data object StudentSetup : SessionRouteTarget
    data object CompanySetup : SessionRouteTarget
}

data class AppSessionUiState(
    val isConfigurationMissing: Boolean = !SupabaseConfig.isConfigured,
    val isLoading: Boolean = SupabaseConfig.isConfigured,
    val hasActiveSession: Boolean = false,
    val routeTarget: SessionRouteTarget? = null,
    val profile: AppUserProfile? = null,
    val errorMessage: String? = null
)

class AppSessionViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppSessionUiState())
    val uiState: StateFlow<AppSessionUiState> = _uiState.asStateFlow()

    private val _oneShotMessage = MutableSharedFlow<String>()
    val oneShotMessage: SharedFlow<String> = _oneShotMessage

    init {
        if (!_uiState.value.isConfigurationMissing) {
            observeSession()
            viewModelScope.launch {
                runCatching { authRepository.restoreSession() }
                    .onFailure { throwable ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            routeTarget = SessionRouteTarget.Login,
                            errorMessage = throwable.message
                        )
                    }
            }
        }
    }

    fun refreshUserContext() {
        if (_uiState.value.isConfigurationMissing) return
        viewModelScope.launch {
            loadCurrentProfile()
        }
    }

    fun signOut() {
        viewModelScope.launch {
            runCatching { authRepository.signOut() }
                .onFailure {
                    _oneShotMessage.emit(it.message ?: "No se pudo cerrar sesion.")
                }
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            authRepository.sessionStatus.collect { status ->
                when (status) {
                    SessionStatus.Initializing -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    }

                    is SessionStatus.NotAuthenticated -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            hasActiveSession = false,
                            routeTarget = SessionRouteTarget.Home,
                            profile = null
                        )
                    }

                    is SessionStatus.RefreshFailure -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            hasActiveSession = false,
                            routeTarget = SessionRouteTarget.Login,
                            profile = null
                        )
                    }

                    is SessionStatus.Authenticated -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            hasActiveSession = true,
                            routeTarget = null,
                            errorMessage = null
                        )
                        loadCurrentProfile()
                    }
                }
            }
        }
    }

    private suspend fun loadCurrentProfile() {
        runCatching { profileRepository.getCurrentProfile() }
            .onSuccess { profile ->
                val target = when {
                    profile == null -> SessionRouteTarget.Home
                    profile.profileStatus == ProfileStatus.ONBOARDING && profile.role == UserRole.STUDENT -> {
                        SessionRouteTarget.StudentSetup
                    }

                    profile.profileStatus == ProfileStatus.ONBOARDING && profile.role == UserRole.COMPANY -> {
                        SessionRouteTarget.CompanySetup
                    }

                    else -> SessionRouteTarget.Home
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hasActiveSession = true,
                    routeTarget = target,
                    profile = profile,
                    errorMessage = null
                )
            }
            .onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hasActiveSession = true,
                    routeTarget = SessionRouteTarget.Home,
                    errorMessage = throwable.message
                )
            }
    }
}
