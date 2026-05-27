package com.raulcn.freeed.feature.services

import android.net.Uri
import com.raulcn.freeed.core.model.ServiceModality
import com.raulcn.freeed.core.model.ServiceStatus
import com.raulcn.freeed.data.repository.ServicePriceType
import com.raulcn.freeed.domain.model.Category
import com.raulcn.freeed.domain.model.Service

data class MyServicesUiState(
    val services: List<Service> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val pendingStatusServiceId: String? = null
)

data class ServiceEditorUiState(
    val serviceId: String? = null,
    val isInitialized: Boolean = false,
    val isLoadingService: Boolean = false,
    val isLoadingCategories: Boolean = false,
    val isSaving: Boolean = false,
    val title: String = "",
    val shortDescription: String = "",
    val description: String = "",
    val selectedCategory: Category? = null,
    val categories: List<Category> = emptyList(),
    val modality: ServiceModality = ServiceModality.REMOTE,
    val priceType: ServicePriceType = ServicePriceType.FIXED,
    val priceAmount: String = "",
    val status: ServiceStatus = ServiceStatus.DRAFT,
    val currentImageUrl: String? = null,
    val pickedImageUri: Uri? = null,
    val errorMessage: String? = null,
    val savedSuccessfully: Boolean = false
) {
    val isEditing: Boolean
        get() = serviceId != null

    val requiresPriceAmount: Boolean
        get() = priceType == ServicePriceType.FIXED || priceType == ServicePriceType.HOURLY
}
