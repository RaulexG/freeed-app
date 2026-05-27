package com.raulcn.freeed.feature.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulcn.freeed.core.model.RequestStatus
import com.raulcn.freeed.data.repository.RequestsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReceivedRequestsViewModel(
    private val requestsRepository: RequestsRepository = RequestsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceivedRequestsUiState())
    val uiState: StateFlow<ReceivedRequestsUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { requestsRepository.getReceivedRequests() }
                .onSuccess { items ->
                    _uiState.update { it.copy(items = items, isLoading = false) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "No se pudieron cargar las solicitudes."
                        )
                    }
                }
        }
    }
}

class SentRequestsViewModel(
    private val requestsRepository: RequestsRepository = RequestsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SentRequestsUiState())
    val uiState: StateFlow<SentRequestsUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { requestsRepository.getSentRequests() }
                .onSuccess { items ->
                    _uiState.update { it.copy(items = items, isLoading = false) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "No se pudieron cargar tus solicitudes."
                        )
                    }
                }
        }
    }
}

class RequestDetailViewModel(
    private val requestsRepository: RequestsRepository = RequestsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestDetailUiState())
    val uiState: StateFlow<RequestDetailUiState> = _uiState.asStateFlow()

    fun load(requestId: String) {
        if (_uiState.value.isLoading || _uiState.value.request?.id == requestId) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { requestsRepository.getRequestById(requestId) }
                .onSuccess { request ->
                    _uiState.update {
                        it.copy(
                            request = request,
                            isLoading = false,
                            errorMessage = if (request == null) "No encontramos esta solicitud." else null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "No se pudo cargar la solicitud."
                        )
                    }
                }
        }
    }

    fun accept() = transitionTo(RequestStatus.ACCEPTED)
    fun reject(reason: String?) = transitionTo(RequestStatus.REJECTED, rejectionReason = reason)
    fun markInProgress() = transitionTo(RequestStatus.IN_PROGRESS)
    fun markCompleted() = transitionTo(RequestStatus.COMPLETED)
    fun cancel(reason: String?) = transitionTo(RequestStatus.CANCELLED, cancelledReason = reason)

    private fun transitionTo(
        newStatus: RequestStatus,
        rejectionReason: String? = null,
        cancelledReason: String? = null
    ) {
        val current = _uiState.value.request ?: return
        if (_uiState.value.isUpdatingStatus) return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingStatus = true, errorMessage = null) }
            runCatching {
                requestsRepository.updateStatus(
                    requestId = current.id,
                    newStatus = newStatus,
                    rejectionReason = rejectionReason,
                    cancelledReason = cancelledReason
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isUpdatingStatus = false,
                        request = current.copy(
                            status = newStatus,
                            rejectionReason = when (newStatus) {
                                RequestStatus.REJECTED -> rejectionReason
                                else -> null
                            },
                            cancelledReason = when (newStatus) {
                                RequestStatus.CANCELLED -> cancelledReason
                                else -> null
                            }
                        )
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isUpdatingStatus = false,
                        errorMessage = throwable.message ?: "No se pudo actualizar la solicitud."
                    )
                }
            }
        }
    }
}
