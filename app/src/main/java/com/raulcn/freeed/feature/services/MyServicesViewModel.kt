package com.raulcn.freeed.feature.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulcn.freeed.core.model.ServiceStatus
import com.raulcn.freeed.data.repository.ServicesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MyServicesViewModel(
    private val servicesRepository: ServicesRepository = ServicesRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyServicesUiState())
    val uiState: StateFlow<MyServicesUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { servicesRepository.getMyServices() }
                .onSuccess { services ->
                    _uiState.update { it.copy(services = services, isLoading = false) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "No se pudo cargar tu catalogo."
                        )
                    }
                }
        }
    }

    fun publish(serviceId: String) = changeStatus(serviceId, ServiceStatus.PUBLISHED)
    fun pause(serviceId: String) = changeStatus(serviceId, ServiceStatus.PAUSED)

    private fun changeStatus(serviceId: String, status: ServiceStatus) {
        if (_uiState.value.pendingStatusServiceId != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(pendingStatusServiceId = serviceId, errorMessage = null) }
            runCatching { servicesRepository.updateStatus(serviceId, status) }
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            pendingStatusServiceId = null,
                            services = state.services.map {
                                if (it.id == serviceId) it.copy(status = status) else it
                            }
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            pendingStatusServiceId = null,
                            errorMessage = throwable.message ?: "No se pudo actualizar el estado."
                        )
                    }
                }
        }
    }
}
