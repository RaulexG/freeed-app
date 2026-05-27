package com.raulcn.freeed.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulcn.freeed.core.model.UserRole
import com.raulcn.freeed.data.repository.BrowseRepository
import com.raulcn.freeed.data.repository.DashboardRepository
import com.raulcn.freeed.data.repository.FavoritesRepository
import com.raulcn.freeed.domain.model.AppUserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val dashboardRepository: DashboardRepository = DashboardRepository(),
    private val browseRepository: BrowseRepository = BrowseRepository(),
    private val favoritesRepository: FavoritesRepository = FavoritesRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun load(profile: AppUserProfile?) {
        if (_uiState.value.isLoading || _uiState.value.categories.isNotEmpty()) return

        val isCompany = profile?.role == UserRole.COMPANY

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    greetingName = profile?.displayName?.substringBefore(" ")?.ifBlank { profile.displayName } ?: "FreeEd",
                    isLoading = true,
                    errorMessage = null,
                    isCompany = isCompany
                )
            }

            runCatching {
                val categories = dashboardRepository.getCategories()
                val services = browseRepository.getPublishedServices().take(3)
                val stats = profile?.let { dashboardRepository.getCurrentStats(it.role) }
                val favoriteIds = if (isCompany) favoritesRepository.getFavoriteServiceIds() else emptySet()
                LoadResult(categories, services, stats, favoriteIds)
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        categories = result.categories,
                        featuredServices = result.services,
                        servicesCount = result.stats?.servicesCount ?: result.services.size,
                        portfolioCount = result.stats?.portfolioCount ?: 0,
                        requestsCount = result.stats?.requestsCount ?: 0,
                        favoriteIds = result.favoriteIds,
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

    fun toggleFavorite(serviceId: String) {
        val state = _uiState.value
        if (!state.isCompany) return
        val isFavorite = state.favoriteIds.contains(serviceId)
        _uiState.update {
            it.copy(favoriteIds = if (isFavorite) it.favoriteIds - serviceId else it.favoriteIds + serviceId)
        }
        viewModelScope.launch {
            runCatching {
                if (isFavorite) favoritesRepository.removeFavorite(serviceId)
                else favoritesRepository.addFavorite(serviceId)
            }.onFailure {
                _uiState.update { current ->
                    current.copy(
                        favoriteIds = if (isFavorite) current.favoriteIds + serviceId else current.favoriteIds - serviceId,
                        errorMessage = it.message ?: "No se pudo actualizar favoritos."
                    )
                }
            }
        }
    }

    private data class LoadResult(
        val categories: List<com.raulcn.freeed.domain.model.Category>,
        val services: List<com.raulcn.freeed.domain.model.Service>,
        val stats: com.raulcn.freeed.data.repository.DashboardStats?,
        val favoriteIds: Set<String>
    )
}
