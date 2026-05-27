package com.raulcn.freeed.feature.requests

import com.raulcn.freeed.domain.model.ServiceRequest

data class ReceivedRequestsUiState(
    val items: List<ServiceRequest> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class SentRequestsUiState(
    val items: List<ServiceRequest> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class RequestDetailUiState(
    val request: ServiceRequest? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isUpdatingStatus: Boolean = false
)
