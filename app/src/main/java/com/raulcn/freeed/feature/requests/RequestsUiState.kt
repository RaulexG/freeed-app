package com.raulcn.freeed.feature.requests

import com.raulcn.freeed.domain.model.ServiceRequest

data class RequestsUiState(
    val sent: List<ServiceRequest> = emptyList(),
    val received: List<ServiceRequest> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

