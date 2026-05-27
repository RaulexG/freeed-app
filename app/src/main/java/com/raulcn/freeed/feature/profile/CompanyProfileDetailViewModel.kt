package com.raulcn.freeed.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulcn.freeed.data.repository.BrowseRepository
import com.raulcn.freeed.domain.model.PublicCompanyProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CompanyProfileDetailUiState(
    val profile: PublicCompanyProfile? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class CompanyProfileDetailViewModel(
    private val browseRepository: BrowseRepository = BrowseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompanyProfileDetailUiState())
    val uiState: StateFlow<CompanyProfileDetailUiState> = _uiState.asStateFlow()

    fun load(profileId: String) {
        if (_uiState.value.isLoading || _uiState.value.profile?.id == profileId) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { browseRepository.getCompanyPublicProfile(profileId) }
                .onSuccess { profile ->
                    _uiState.update {
                        it.copy(
                            profile = profile,
                            isLoading = false,
                            errorMessage = if (profile == null) "No encontramos esta empresa." else null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "No se pudo cargar el perfil de empresa."
                        )
                    }
                }
        }
    }
}
