package com.raulcn.freeed.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulcn.freeed.data.repository.BrowseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExploreViewModel(
    private val browseRepository: BrowseRepository = BrowseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    fun load() {
        if (_uiState.value.isLoading || _uiState.value.services.isNotEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val categories = browseRepository.getActiveCategories()
                val services = browseRepository.getPublishedServices()
                categories to services
            }.onSuccess { (categories, services) ->
                _uiState.update {
                    it.copy(
                        categories = categories,
                        services = services,
                        isLoading = false
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "No se pudo cargar la exploracion."
                    )
                }
            }
        }
    }
}
