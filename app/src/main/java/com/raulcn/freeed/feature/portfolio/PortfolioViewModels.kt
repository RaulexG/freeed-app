package com.raulcn.freeed.feature.portfolio

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulcn.freeed.core.model.VisibilityLevel
import com.raulcn.freeed.data.repository.AssetAccessScope
import com.raulcn.freeed.data.repository.AssetKind
import com.raulcn.freeed.data.repository.AssetsRepository
import com.raulcn.freeed.data.repository.PortfolioDraft
import com.raulcn.freeed.data.repository.PortfolioRepository
import com.raulcn.freeed.domain.model.PortfolioItemType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MyPortfolioViewModel(
    private val portfolioRepository: PortfolioRepository = PortfolioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyPortfolioUiState())
    val uiState: StateFlow<MyPortfolioUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { portfolioRepository.getMyPortfolio() }
                .onSuccess { items ->
                    _uiState.update { it.copy(items = items, isLoading = false) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "No se pudo cargar tu portafolio."
                        )
                    }
                }
        }
    }

    fun delete(itemId: String) {
        if (_uiState.value.pendingDeleteId != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(pendingDeleteId = itemId, errorMessage = null) }
            runCatching { portfolioRepository.deleteItem(itemId) }
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            pendingDeleteId = null,
                            items = state.items.filterNot { it.id == itemId }
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            pendingDeleteId = null,
                            errorMessage = throwable.message ?: "No se pudo eliminar el item."
                        )
                    }
                }
        }
    }
}

class PortfolioEditorViewModel(
    private val portfolioRepository: PortfolioRepository = PortfolioRepository(),
    private val assetsRepository: AssetsRepository = AssetsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PortfolioEditorUiState())
    val uiState: StateFlow<PortfolioEditorUiState> = _uiState.asStateFlow()

    fun initialize(itemIdArg: String?) {
        if (_uiState.value.isInitialized) return
        val editingId = itemIdArg?.takeIf { it.isNotBlank() && it != NEW_PORTFOLIO_ARG }

        _uiState.update {
            it.copy(
                itemId = editingId,
                isInitialized = true,
                isLoadingItem = editingId != null
            )
        }

        if (editingId == null) return

        viewModelScope.launch {
            runCatching { portfolioRepository.getItemById(editingId) }
                .onSuccess { item ->
                    if (item == null) {
                        _uiState.update {
                            it.copy(
                                isLoadingItem = false,
                                errorMessage = "No encontramos este elemento."
                            )
                        }
                        return@onSuccess
                    }
                    val coverUrl = runCatching { portfolioRepository.getCoverImageUrl(item.id) }.getOrNull()
                    _uiState.update {
                        it.copy(
                            isLoadingItem = false,
                            itemType = item.itemType,
                            title = item.title,
                            description = item.description.orEmpty(),
                            contribution = item.contribution.orEmpty(),
                            projectUrl = item.projectUrl.orEmpty(),
                            repositoryUrl = item.repositoryUrl.orEmpty(),
                            visibility = item.visibility,
                            currentCoverUrl = coverUrl
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoadingItem = false,
                            errorMessage = throwable.message ?: "No se pudo cargar el elemento."
                        )
                    }
                }
        }
    }

    fun onTypeChange(type: PortfolioItemType) = _uiState.update { it.copy(itemType = type, errorMessage = null) }
    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value, errorMessage = null) }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value, errorMessage = null) }
    fun onContributionChange(value: String) = _uiState.update { it.copy(contribution = value, errorMessage = null) }
    fun onProjectUrlChange(value: String) = _uiState.update { it.copy(projectUrl = value, errorMessage = null) }
    fun onRepositoryUrlChange(value: String) = _uiState.update { it.copy(repositoryUrl = value, errorMessage = null) }
    fun onVisibilityChange(value: VisibilityLevel) = _uiState.update { it.copy(visibility = value, errorMessage = null) }
    fun onCoverPicked(uri: Uri?) = _uiState.update { it.copy(pickedCoverUri = uri, errorMessage = null) }

    fun save(contentResolver: ContentResolver, onSaved: () -> Unit) {
        val state = _uiState.value
        val validation = validate(state)
        if (validation != null) {
            _uiState.update { it.copy(errorMessage = validation) }
            return
        }

        val draft = PortfolioDraft(
            itemType = state.itemType,
            title = state.title.trim(),
            description = state.description.takeIf { it.isNotBlank() },
            contribution = state.contribution.takeIf { it.isNotBlank() },
            projectUrl = state.projectUrl.takeIf { it.isNotBlank() },
            repositoryUrl = state.repositoryUrl.takeIf { it.isNotBlank() },
            visibility = state.visibility
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                val resultId = if (state.isEditing) {
                    portfolioRepository.updateItem(state.itemId!!, draft)
                    state.itemId
                } else {
                    portfolioRepository.createItem(draft)
                }
                state.pickedCoverUri?.let { uri ->
                    val uploaded = assetsRepository.uploadImage(
                        contentResolver = contentResolver,
                        uri = uri,
                        kind = AssetKind.PORTFOLIO_IMAGE,
                        accessScope = AssetAccessScope.PUBLIC_READ,
                        subPath = "portfolio/$resultId"
                    )
                    portfolioRepository.linkCoverImage(resultId, uploaded.assetId)
                }
                resultId
            }.onSuccess { newId ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        savedSuccessfully = true,
                        itemId = newId
                    )
                }
                onSaved()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = throwable.message ?: "No se pudo guardar."
                    )
                }
            }
        }
    }

    private fun validate(state: PortfolioEditorUiState): String? {
        val title = state.title.trim()
        if (title.length < 3) return "El titulo debe tener al menos 3 caracteres."
        if (title.length > 140) return "El titulo no puede exceder 140 caracteres."
        listOf(state.projectUrl, state.repositoryUrl).forEach { url ->
            if (url.isNotBlank() && !isLikelyUrl(url)) {
                return "Revisa los enlaces: deben empezar con http:// o https://."
            }
        }
        return null
    }

    private fun isLikelyUrl(value: String): Boolean {
        val trimmed = value.trim()
        return trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)
    }

    companion object {
        const val NEW_PORTFOLIO_ARG = "new"
    }
}

class PortfolioDetailViewModel(
    private val portfolioRepository: PortfolioRepository = PortfolioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PortfolioDetailUiState())
    val uiState: StateFlow<PortfolioDetailUiState> = _uiState.asStateFlow()

    fun load(itemId: String) {
        if (_uiState.value.isLoading || _uiState.value.item?.id == itemId) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val item = portfolioRepository.getItemById(itemId)
                val cover = runCatching { portfolioRepository.getCoverImageUrl(itemId) }.getOrNull()
                item to cover
            }
                .onSuccess { (item, cover) ->
                    _uiState.update {
                        it.copy(
                            item = item,
                            coverUrl = cover,
                            isLoading = false,
                            errorMessage = if (item == null) "No encontramos este elemento." else null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "No se pudo cargar el elemento."
                        )
                    }
                }
        }
    }
}
