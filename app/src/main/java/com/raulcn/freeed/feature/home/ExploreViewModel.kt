package com.raulcn.freeed.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulcn.freeed.core.model.ServiceModality
import com.raulcn.freeed.core.model.UserRole
import com.raulcn.freeed.data.repository.BrowseRepository
import com.raulcn.freeed.data.repository.FavoritesRepository
import com.raulcn.freeed.domain.model.AppUserProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExploreViewModel(
    private val browseRepository: BrowseRepository = BrowseRepository(),
    private val favoritesRepository: FavoritesRepository = FavoritesRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun load(sessionProfile: AppUserProfile?) {
        if (_uiState.value.isLoading) return
        val isCompany = sessionProfile?.role == UserRole.COMPANY
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isCompany = isCompany) }
            val categoriesResult = runCatching { browseRepository.getActiveCategories() }
            val servicesResult = runCatching { browseRepository.searchPublishedServices() }
            val favoritesResult = if (isCompany) {
                runCatching { favoritesRepository.getFavoriteServiceIds() }
            } else null
            val firstError = categoriesResult.exceptionOrNull()?.message
                ?: servicesResult.exceptionOrNull()?.message
            _uiState.update {
                it.copy(
                    categories = categoriesResult.getOrDefault(emptyList()),
                    services = servicesResult.getOrDefault(emptyList()),
                    favoriteIds = favoritesResult?.getOrDefault(emptySet()) ?: emptySet(),
                    isLoading = false,
                    errorMessage = firstError
                )
            }
        }
    }

    fun toggleFavorite(serviceId: String) {
        val state = _uiState.value
        if (!state.isCompany) return
        val isFavorite = state.favoriteIds.contains(serviceId)
        // Optimistic update
        _uiState.update {
            it.copy(favoriteIds = if (isFavorite) it.favoriteIds - serviceId else it.favoriteIds + serviceId)
        }
        viewModelScope.launch {
            runCatching {
                if (isFavorite) favoritesRepository.removeFavorite(serviceId)
                else favoritesRepository.addFavorite(serviceId)
            }.onFailure {
                // Revert on error
                _uiState.update { current ->
                    current.copy(
                        favoriteIds = if (isFavorite) current.favoriteIds + serviceId else current.favoriteIds - serviceId,
                        errorMessage = it.message ?: "No se pudo actualizar favoritos."
                    )
                }
            }
        }
    }

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value) }
        scheduleSearch()
    }

    fun onSkillQueryChange(value: String) {
        _uiState.update { it.copy(skillQuery = value) }
        scheduleSearch()
    }

    fun onUniversityQueryChange(value: String) {
        _uiState.update { it.copy(universityQuery = value) }
        scheduleSearch()
    }

    fun onDegreeQueryChange(value: String) {
        _uiState.update { it.copy(degreeQuery = value) }
        scheduleSearch()
    }

    fun onSemesterQueryChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(2)
        _uiState.update { it.copy(semesterQuery = digits) }
        scheduleSearch()
    }

    fun onCategoryToggle(categoryId: String?) {
        _uiState.update {
            it.copy(selectedCategoryId = if (it.selectedCategoryId == categoryId) null else categoryId)
        }
        runSearchImmediate()
    }

    fun onModalityToggle(modality: ServiceModality) {
        _uiState.update {
            it.copy(selectedModality = if (it.selectedModality == modality) null else modality)
        }
        runSearchImmediate()
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                query = "",
                skillQuery = "",
                universityQuery = "",
                degreeQuery = "",
                semesterQuery = "",
                selectedCategoryId = null,
                selectedModality = null
            )
        }
        runSearchImmediate()
    }

    private fun scheduleSearch() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            performSearch()
        }
    }

    private fun runSearchImmediate() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch { performSearch() }
    }

    private suspend fun performSearch() {
        val state = _uiState.value
        _uiState.update { it.copy(isSearching = true, errorMessage = null) }
        runCatching {
            browseRepository.searchPublishedServicesByTalent(
                query = state.query.takeIf { it.isNotBlank() },
                categoryId = state.selectedCategoryId,
                modality = state.selectedModality,
                skillQuery = state.skillQuery.takeIf { it.isNotBlank() },
                universityQuery = state.universityQuery.takeIf { it.isNotBlank() },
                degreeQuery = state.degreeQuery.takeIf { it.isNotBlank() },
                semester = state.semesterQuery.toIntOrNull()
            )
        }.onSuccess { services ->
            _uiState.update { it.copy(services = services, isSearching = false) }
        }.onFailure { throwable ->
            _uiState.update {
                it.copy(
                    isSearching = false,
                    errorMessage = throwable.message ?: "No se pudo buscar."
                )
            }
        }
    }
}
