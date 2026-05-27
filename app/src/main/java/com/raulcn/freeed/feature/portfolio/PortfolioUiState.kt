package com.raulcn.freeed.feature.portfolio

import android.net.Uri
import com.raulcn.freeed.core.model.VisibilityLevel
import com.raulcn.freeed.domain.model.PortfolioItem
import com.raulcn.freeed.domain.model.PortfolioItemType

data class MyPortfolioUiState(
    val items: List<PortfolioItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val pendingDeleteId: String? = null
)

data class PortfolioEditorUiState(
    val itemId: String? = null,
    val isInitialized: Boolean = false,
    val isLoadingItem: Boolean = false,
    val isSaving: Boolean = false,
    val itemType: PortfolioItemType = PortfolioItemType.PROJECT,
    val title: String = "",
    val description: String = "",
    val contribution: String = "",
    val projectUrl: String = "",
    val repositoryUrl: String = "",
    val visibility: VisibilityLevel = VisibilityLevel.PUBLIC,
    val currentCoverUrl: String? = null,
    val pickedCoverUri: Uri? = null,
    val errorMessage: String? = null,
    val savedSuccessfully: Boolean = false
) {
    val isEditing: Boolean
        get() = itemId != null
}

data class PortfolioDetailUiState(
    val item: PortfolioItem? = null,
    val coverUrl: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
