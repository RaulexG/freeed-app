package com.raulcn.freeed.feature.services

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulcn.freeed.core.model.ServiceModality
import com.raulcn.freeed.core.model.ServiceStatus
import com.raulcn.freeed.data.repository.AssetAccessScope
import com.raulcn.freeed.data.repository.AssetKind
import com.raulcn.freeed.data.repository.AssetsRepository
import com.raulcn.freeed.data.repository.DashboardRepository
import com.raulcn.freeed.data.repository.ServiceDraft
import com.raulcn.freeed.data.repository.ServicePriceType
import com.raulcn.freeed.data.repository.ServicesRepository
import com.raulcn.freeed.domain.model.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ServiceEditorViewModel(
    private val servicesRepository: ServicesRepository = ServicesRepository(),
    private val dashboardRepository: DashboardRepository = DashboardRepository(),
    private val assetsRepository: AssetsRepository = AssetsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServiceEditorUiState())
    val uiState: StateFlow<ServiceEditorUiState> = _uiState.asStateFlow()

    fun initialize(serviceIdArg: String?) {
        if (_uiState.value.isInitialized) return
        val editingId = serviceIdArg?.takeIf { it.isNotBlank() && it != NEW_SERVICE_ARG }

        _uiState.update {
            it.copy(
                serviceId = editingId,
                isInitialized = true,
                isLoadingCategories = true,
                isLoadingService = editingId != null
            )
        }

        viewModelScope.launch {
            runCatching { dashboardRepository.getCategories() }
                .onSuccess { categories ->
                    _uiState.update {
                        it.copy(
                            categories = categories,
                            isLoadingCategories = false,
                            selectedCategory = it.selectedCategory
                                ?: categories.firstOrNull().takeIf { _ -> editingId == null }
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoadingCategories = false,
                            errorMessage = throwable.message ?: "No se pudieron cargar las categorias."
                        )
                    }
                }

            if (editingId != null) loadExistingService(editingId)
        }
    }

    private suspend fun loadExistingService(serviceId: String) {
        runCatching { servicesRepository.getServiceById(serviceId) }
            .onSuccess { service ->
                if (service == null) {
                    _uiState.update {
                        it.copy(
                            isLoadingService = false,
                            errorMessage = "No encontramos este servicio."
                        )
                    }
                    return
                }
                val categories = _uiState.value.categories
                val matchingCategory = categories.firstOrNull { it.id == service.categoryId }
                    ?: Category(
                        id = service.categoryId,
                        name = "Categoria",
                        slug = "",
                        description = null
                    )

                val currentImage = runCatching { servicesRepository.getPrimaryImageUrl(serviceId) }.getOrNull()

                _uiState.update {
                    it.copy(
                        isLoadingService = false,
                        title = service.title,
                        shortDescription = service.shortDescription.orEmpty(),
                        description = service.description,
                        selectedCategory = matchingCategory,
                        modality = service.modality,
                        priceType = derivePriceType(service.priceLabel),
                        priceAmount = extractAmount(service.priceLabel),
                        status = service.status,
                        currentImageUrl = currentImage
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

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value, errorMessage = null) }
    fun onShortDescriptionChange(value: String) =
        _uiState.update { it.copy(shortDescription = value, errorMessage = null) }

    fun onDescriptionChange(value: String) =
        _uiState.update { it.copy(description = value, errorMessage = null) }

    fun onCategorySelected(category: Category) =
        _uiState.update { it.copy(selectedCategory = category, errorMessage = null) }

    fun onModalityChange(modality: ServiceModality) =
        _uiState.update { it.copy(modality = modality, errorMessage = null) }

    fun onPriceTypeChange(type: ServicePriceType) =
        _uiState.update {
            it.copy(
                priceType = type,
                priceAmount = if (type == ServicePriceType.FIXED || type == ServicePriceType.HOURLY) it.priceAmount else "",
                errorMessage = null
            )
        }

    fun onPriceAmountChange(value: String) {
        val sanitized = value.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(priceAmount = sanitized, errorMessage = null) }
    }

    fun onImagePicked(uri: Uri?) = _uiState.update { it.copy(pickedImageUri = uri, errorMessage = null) }

    fun save(contentResolver: ContentResolver, publish: Boolean, onSaved: () -> Unit) {
        val state = _uiState.value
        val validation = validate(state, publish)
        if (validation != null) {
            _uiState.update { it.copy(errorMessage = validation) }
            return
        }

        val targetStatus = when {
            publish -> ServiceStatus.PUBLISHED
            state.isEditing -> state.status
            else -> ServiceStatus.DRAFT
        }

        val draft = ServiceDraft(
            title = state.title.trim(),
            categoryId = state.selectedCategory?.id ?: return,
            shortDescription = state.shortDescription.takeIf { it.isNotBlank() },
            description = state.description.trim(),
            modality = state.modality,
            priceType = state.priceType,
            priceAmount = if (state.requiresPriceAmount) state.priceAmount.toDoubleOrNull() else null,
            status = targetStatus
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                val resultId = if (state.isEditing) {
                    servicesRepository.updateService(state.serviceId!!, draft)
                    state.serviceId
                } else {
                    servicesRepository.createService(draft)
                }
                state.pickedImageUri?.let { uri ->
                    val uploaded = assetsRepository.uploadImage(
                        contentResolver = contentResolver,
                        uri = uri,
                        kind = AssetKind.SERVICE_IMAGE,
                        accessScope = AssetAccessScope.PUBLIC_READ,
                        subPath = "services/$resultId"
                    )
                    servicesRepository.linkPrimaryImage(resultId, uploaded.assetId)
                }
                resultId
            }.onSuccess { newId ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        savedSuccessfully = true,
                        serviceId = newId,
                        status = targetStatus
                    )
                }
                onSaved()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = throwable.message ?: "No se pudo guardar el servicio."
                    )
                }
            }
        }
    }

    private fun validate(state: ServiceEditorUiState, publish: Boolean): String? {
        if (state.title.trim().length < 5) return "El titulo debe tener al menos 5 caracteres."
        if (state.title.trim().length > 120) return "El titulo no puede exceder 120 caracteres."
        if (state.shortDescription.length > 180) return "El resumen no puede exceder 180 caracteres."
        if (state.description.isBlank()) return "Agrega una descripcion."
        if (state.selectedCategory == null) return "Selecciona una categoria."
        if (state.requiresPriceAmount) {
            val amount = state.priceAmount.toDoubleOrNull()
            if (amount == null || amount <= 0.0) return "Ingresa un precio valido."
        }
        if (publish && state.description.length < 30) {
            return "La descripcion debe tener al menos 30 caracteres para publicar."
        }
        return null
    }

    private fun derivePriceType(label: String?): ServicePriceType {
        val normalized = label?.lowercase().orEmpty()
        return when {
            "sin costo" in normalized -> ServicePriceType.FREE
            "convenir" in normalized -> ServicePriceType.CUSTOM
            "/hora" in normalized -> ServicePriceType.HOURLY
            else -> ServicePriceType.FIXED
        }
    }

    private fun extractAmount(label: String?): String {
        if (label.isNullOrBlank()) return ""
        val digits = label.filter { it.isDigit() || it == '.' }
        return digits.trimEnd('.')
    }

    companion object {
        const val NEW_SERVICE_ARG = "new"
    }
}
