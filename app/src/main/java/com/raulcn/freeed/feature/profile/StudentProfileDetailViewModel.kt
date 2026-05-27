package com.raulcn.freeed.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulcn.freeed.data.repository.BrowseRepository
import com.raulcn.freeed.domain.model.PortfolioItem
import com.raulcn.freeed.domain.model.PublicStudentProfile
import com.raulcn.freeed.domain.model.Service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentProfileDetailUiState(
    val profile: PublicStudentProfile? = null,
    val services: List<Service> = emptyList(),
    val portfolio: List<PortfolioItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class StudentProfileDetailViewModel(
    private val browseRepository: BrowseRepository = BrowseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentProfileDetailUiState())
    val uiState: StateFlow<StudentProfileDetailUiState> = _uiState.asStateFlow()

    fun load(profileId: String) {
        if (_uiState.value.isLoading || _uiState.value.profile?.id == profileId) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val profileResult = runCatching { browseRepository.getStudentPublicProfile(profileId) }
            val servicesResult = runCatching { browseRepository.getServicesByStudent(profileId) }
            val portfolioResult = runCatching { browseRepository.getPortfolioByStudent(profileId) }
            val firstError = profileResult.exceptionOrNull()?.message
                ?: servicesResult.exceptionOrNull()?.message
                ?: portfolioResult.exceptionOrNull()?.message
            _uiState.update {
                it.copy(
                    profile = profileResult.getOrNull(),
                    services = servicesResult.getOrDefault(emptyList()),
                    portfolio = portfolioResult.getOrDefault(emptyList()),
                    isLoading = false,
                    errorMessage = firstError
                        ?: if (profileResult.getOrNull() == null) "No encontramos este perfil." else null
                )
            }
        }
    }
}
