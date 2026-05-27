package com.raulcn.freeed.feature.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulcn.freeed.core.model.UserRole
import com.raulcn.freeed.data.repository.BrowseRepository
import com.raulcn.freeed.data.repository.FavoritesRepository
import com.raulcn.freeed.data.repository.ServicesRepository
import com.raulcn.freeed.domain.model.AppUserProfile
import com.raulcn.freeed.domain.model.Service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ServiceDetailUiState(
    val service: Service? = null,
    val imageUrl: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isFavorite: Boolean = false,
    val isCompany: Boolean = false
)

class ServiceDetailViewModel(
    private val browseRepository: BrowseRepository = BrowseRepository(),
    private val servicesRepository: ServicesRepository = ServicesRepository(),
    private val favoritesRepository: FavoritesRepository = FavoritesRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServiceDetailUiState())
    val uiState: StateFlow<ServiceDetailUiState> = _uiState.asStateFlow()

    fun load(serviceId: String, sessionProfile: AppUserProfile?) {
        if (_uiState.value.isLoading || _uiState.value.service?.id == serviceId) return
        val isCompany = sessionProfile?.role == UserRole.COMPANY

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isCompany = isCompany) }
            runCatching {
                val service = browseRepository.getPublishedServiceById(serviceId)
                    ?: servicesRepository.getServiceById(serviceId)
                val imageUrl = runCatching { servicesRepository.getPrimaryImageUrl(serviceId) }.getOrNull()
                val favoriteIds = if (isCompany) {
                    runCatching { favoritesRepository.getFavoriteServiceIds() }.getOrDefault(emptySet())
                } else emptySet()
                Triple(service, imageUrl, favoriteIds.contains(serviceId))
            }.onSuccess { (service, imageUrl, isFavorite) ->
                _uiState.update {
                    it.copy(
                        service = service,
                        imageUrl = imageUrl,
                        isFavorite = isFavorite,
                        isLoading = false,
                        errorMessage = if (service == null) "No encontramos este servicio." else null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "No se pudo cargar el servicio."
                    )
                }
            }
        }
    }

    fun toggleFavorite() {
        val state = _uiState.value
        val service = state.service ?: return
        if (!state.isCompany) return
        val newValue = !state.isFavorite
        _uiState.update { it.copy(isFavorite = newValue) }
        viewModelScope.launch {
            runCatching {
                if (newValue) favoritesRepository.addFavorite(service.id)
                else favoritesRepository.removeFavorite(service.id)
            }.onFailure {
                _uiState.update { current ->
                    current.copy(
                        isFavorite = !newValue,
                        errorMessage = it.message ?: "No se pudo actualizar favoritos."
                    )
                }
            }
        }
    }
}
