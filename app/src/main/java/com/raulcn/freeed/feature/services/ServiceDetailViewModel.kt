package com.raulcn.freeed.feature.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulcn.freeed.data.repository.BrowseRepository
import com.raulcn.freeed.domain.model.Service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ServiceDetailUiState(
    val service: Service? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ServiceDetailViewModel(
    private val browseRepository: BrowseRepository = BrowseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServiceDetailUiState())
    val uiState: StateFlow<ServiceDetailUiState> = _uiState.asStateFlow()

    fun load(serviceId: String) {
        if (_uiState.value.isLoading || _uiState.value.service?.id == serviceId) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                browseRepository.getPublishedServiceById(serviceId)
            }.onSuccess { service ->
                _uiState.update {
                    it.copy(
                        service = service,
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
}
