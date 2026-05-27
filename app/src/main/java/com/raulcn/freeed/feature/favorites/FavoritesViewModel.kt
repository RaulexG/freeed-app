package com.raulcn.freeed.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulcn.freeed.data.repository.FavoritesRepository
import com.raulcn.freeed.domain.model.Service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val items: List<Service> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val pendingRemoveId: String? = null
)

class FavoritesViewModel(
    private val favoritesRepository: FavoritesRepository = FavoritesRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { favoritesRepository.getFavoriteServices() }
                .onSuccess { items ->
                    _uiState.update { it.copy(items = items, isLoading = false) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "No se pudieron cargar tus favoritos."
                        )
                    }
                }
        }
    }

    fun remove(serviceId: String) {
        if (_uiState.value.pendingRemoveId != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(pendingRemoveId = serviceId, errorMessage = null) }
            runCatching { favoritesRepository.removeFavorite(serviceId) }
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            pendingRemoveId = null,
                            items = state.items.filterNot { it.id == serviceId }
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            pendingRemoveId = null,
                            errorMessage = throwable.message ?: "No se pudo eliminar el favorito."
                        )
                    }
                }
        }
    }
}
