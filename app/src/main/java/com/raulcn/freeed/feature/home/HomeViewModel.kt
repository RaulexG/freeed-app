package com.raulcn.freeed.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulcn.freeed.data.repository.BrowseRepository
import com.raulcn.freeed.data.repository.DashboardRepository
import com.raulcn.freeed.domain.model.AppUserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val dashboardRepository: DashboardRepository = DashboardRepository(),
    private val browseRepository: BrowseRepository = BrowseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun load(profile: AppUserProfile?) {
        if (_uiState.value.isLoading || _uiState.value.categories.isNotEmpty()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    greetingName = profile?.displayName?.substringBefore(" ")?.ifBlank { profile.displayName } ?: "FreeEd",
                    isLoading = true,
                    errorMessage = null
                )
            }

            runCatching {
                val categories = dashboardRepository.getCategories()
                val services = browseRepository.getPublishedServices().take(3)
                val stats = profile?.let { dashboardRepository.getCurrentStats(it.role) }
                Triple(categories, services, stats)
            }.onSuccess { (categories, services, stats) ->
                _uiState.update {
                    it.copy(
                        categories = categories,
                        featuredServices = services,
                        servicesCount = stats?.servicesCount ?: services.size,
                        portfolioCount = stats?.portfolioCount ?: 0,
                        requestsCount = stats?.requestsCount ?: 0,
                        isLoading = false
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "No se pudo cargar el inicio."
                    )
                }
            }
        }
    }
}
