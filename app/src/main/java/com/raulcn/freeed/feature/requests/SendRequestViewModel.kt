package com.raulcn.freeed.feature.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulcn.freeed.data.repository.BrowseRepository
import com.raulcn.freeed.data.repository.NewRequestDraft
import com.raulcn.freeed.data.repository.RequestsRepository
import com.raulcn.freeed.domain.model.Service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SendRequestUiState(
    val service: Service? = null,
    val isLoadingService: Boolean = false,
    val isSubmitting: Boolean = false,
    val title: String = "",
    val message: String = "",
    val budget: String = "",
    val deadline: String = "",
    val errorMessage: String? = null,
    val sentSuccessfully: Boolean = false
)

class SendRequestViewModel(
    private val browseRepository: BrowseRepository = BrowseRepository(),
    private val requestsRepository: RequestsRepository = RequestsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SendRequestUiState())
    val uiState: StateFlow<SendRequestUiState> = _uiState.asStateFlow()

    fun load(serviceId: String) {
        if (_uiState.value.isLoadingService || _uiState.value.service?.id == serviceId) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingService = true, errorMessage = null) }
            runCatching { browseRepository.getPublishedServiceById(serviceId) }
                .onSuccess { service ->
                    _uiState.update {
                        it.copy(
                            service = service,
                            isLoadingService = false,
                            errorMessage = if (service == null) "Este servicio ya no esta disponible." else null,
                            title = if (service != null && it.title.isBlank()) {
                                "Solicitud para: ${service.title}"
                            } else it.title
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoadingService = false,
                            errorMessage = throwable.message ?: "No se pudo cargar el servicio."
                        )
                    }
                }
        }
    }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value, errorMessage = null) }
    fun onMessageChange(value: String) = _uiState.update { it.copy(message = value, errorMessage = null) }
    fun onBudgetChange(value: String) {
        val sanitized = value.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(budget = sanitized, errorMessage = null) }
    }
    fun onDeadlineChange(value: String) = _uiState.update { it.copy(deadline = value, errorMessage = null) }

    fun submit() {
        val state = _uiState.value
        val service = state.service ?: return
        val validation = validate(state)
        if (validation != null) {
            _uiState.update { it.copy(errorMessage = validation) }
            return
        }

        val draft = NewRequestDraft(
            serviceId = service.id,
            studentId = service.studentId,
            title = state.title.trim(),
            message = state.message.trim(),
            proposedBudget = state.budget.toDoubleOrNull(),
            desiredDeadline = state.deadline.takeIf { it.isNotBlank() }
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            runCatching { requestsRepository.createRequest(draft) }
                .onSuccess {
                    _uiState.update { it.copy(isSubmitting = false, sentSuccessfully = true) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = throwable.message ?: "No se pudo enviar la solicitud."
                        )
                    }
                }
        }
    }

    private fun validate(state: SendRequestUiState): String? {
        val title = state.title.trim()
        if (title.length < 5) return "El titulo debe tener al menos 5 caracteres."
        if (title.length > 140) return "El titulo no puede exceder 140 caracteres."
        if (state.message.trim().length < 20) return "Cuenta brevemente que necesitas (minimo 20 caracteres)."
        if (state.budget.isNotBlank()) {
            val amount = state.budget.toDoubleOrNull()
            if (amount == null || amount < 0) return "Ingresa un presupuesto valido."
        }
        if (state.deadline.isNotBlank() && !isLikelyDate(state.deadline)) {
            return "La fecha debe estar en formato AAAA-MM-DD."
        }
        return null
    }

    private fun isLikelyDate(value: String): Boolean {
        return value.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))
    }
}
