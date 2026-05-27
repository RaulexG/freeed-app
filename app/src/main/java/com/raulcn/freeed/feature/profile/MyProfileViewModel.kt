package com.raulcn.freeed.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulcn.freeed.data.repository.PortfolioRepository
import com.raulcn.freeed.data.repository.ServicesRepository
import com.raulcn.freeed.domain.model.PortfolioItem
import com.raulcn.freeed.domain.model.Service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyProfileTabsUiState(
    val services: List<Service> = emptyList(),
    val portfolio: List<PortfolioItem> = emptyList(),
    val isLoadingServices: Boolean = false,
    val isLoadingPortfolio: Boolean = false,
    val errorMessage: String? = null,
    val loaded: Boolean = false
)

class MyProfileViewModel(
    private val servicesRepository: ServicesRepository = ServicesRepository(),
    private val portfolioRepository: PortfolioRepository = PortfolioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyProfileTabsUiState())
    val uiState: StateFlow<MyProfileTabsUiState> = _uiState.asStateFlow()

    fun load() {
        if (_uiState.value.loaded || _uiState.value.isLoadingServices) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingServices = true,
                    isLoadingPortfolio = true,
                    errorMessage = null
                )
            }
            val servicesResult = runCatching { servicesRepository.getMyServices() }
            val portfolioResult = runCatching { portfolioRepository.getMyPortfolio() }
            val firstError = servicesResult.exceptionOrNull()?.message
                ?: portfolioResult.exceptionOrNull()?.message
            _uiState.update {
                it.copy(
                    services = servicesResult.getOrDefault(emptyList()),
                    portfolio = portfolioResult.getOrDefault(emptyList()),
                    isLoadingServices = false,
                    isLoadingPortfolio = false,
                    errorMessage = firstError,
                    loaded = true
                )
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(loaded = false) }
        load()
    }
}
