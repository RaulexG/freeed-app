package com.raulcn.freeed.feature.onboarding

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulcn.freeed.data.repository.AssetKind
import com.raulcn.freeed.data.repository.AssetAccessScope
import com.raulcn.freeed.data.repository.AssetsRepository
import com.raulcn.freeed.data.repository.ProfileRepository
import com.raulcn.freeed.data.repository.SkillOption
import com.raulcn.freeed.data.repository.SkillsRepository
import com.raulcn.freeed.domain.model.AppUserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentSetupFormUiState(
    val isInitialized: Boolean = false,
    val displayName: String = "",
    val universityName: String = "",
    val degreeProgram: String = "",
    val semester: String = "",
    val bio: String = "",
    val avatarUrl: String? = null,
    val pickedAvatarUri: Uri? = null,
    val availableSkills: List<SkillOption> = emptyList(),
    val selectedSkillIds: Set<String> = emptySet(),
    val skillsQuery: String = "",
    val newSkillName: String = "",
    val isLoadingSkills: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

data class CompanySetupFormUiState(
    val isInitialized: Boolean = false,
    val displayName: String = "",
    val businessName: String = "",
    val industry: String = "",
    val contactPersonName: String = "",
    val description: String = "",
    val logoUrl: String? = null,
    val pickedLogoUri: Uri? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class StudentProfileSetupViewModel(
    private val profileRepository: ProfileRepository = ProfileRepository(),
    private val assetsRepository: AssetsRepository = AssetsRepository(),
    private val skillsRepository: SkillsRepository = SkillsRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(StudentSetupFormUiState())
    val uiState: StateFlow<StudentSetupFormUiState> = _uiState.asStateFlow()

    fun prefill(profile: AppUserProfile?) {
        _uiState.update {
            it.copy(
                isInitialized = true,
                displayName = profile?.displayName.orEmpty(),
                universityName = profile?.universityName.orEmpty(),
                degreeProgram = profile?.degreeProgram.orEmpty(),
                semester = profile?.semester?.toString().orEmpty(),
                bio = profile?.bio.orEmpty(),
                avatarUrl = profile?.avatarUrl
            )
        }
        loadSkills()
    }

    fun onDisplayNameChange(value: String) = _uiState.update { it.copy(displayName = value, errorMessage = null) }
    fun onUniversityNameChange(value: String) = _uiState.update { it.copy(universityName = value, errorMessage = null) }
    fun onDegreeProgramChange(value: String) = _uiState.update { it.copy(degreeProgram = value, errorMessage = null) }
    fun onSemesterChange(value: String) = _uiState.update { it.copy(semester = value, errorMessage = null) }
    fun onBioChange(value: String) = _uiState.update { it.copy(bio = value, errorMessage = null) }
    fun onAvatarPicked(uri: Uri?) = _uiState.update { it.copy(pickedAvatarUri = uri, errorMessage = null) }
    fun onSkillsQueryChange(value: String) = _uiState.update { it.copy(skillsQuery = value) }
    fun onNewSkillNameChange(value: String) = _uiState.update { it.copy(newSkillName = value, errorMessage = null) }
    fun onToggleSkill(skillId: String) {
        _uiState.update { state ->
            val next = if (state.selectedSkillIds.contains(skillId)) {
                state.selectedSkillIds - skillId
            } else {
                state.selectedSkillIds + skillId
            }
            state.copy(selectedSkillIds = next, errorMessage = null)
        }
    }

    private fun loadSkills() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSkills = true) }
            val catalog = runCatching { skillsRepository.getActiveSkills() }.getOrDefault(emptyList())
            val currentUserSkills = runCatching { skillsRepository.getCurrentUserSkills() }.getOrDefault(emptyList())
            _uiState.update {
                it.copy(
                    availableSkills = catalog,
                    selectedSkillIds = currentUserSkills.map { skill -> skill.id }.toSet(),
                    isLoadingSkills = false
                )
            }
        }
    }

    fun addCustomSkill() {
        val name = _uiState.value.newSkillName.trim()
        if (name.length < 2) {
            _uiState.update { it.copy(errorMessage = "Escribe al menos 2 caracteres para la skill.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSkills = true, errorMessage = null) }
            runCatching {
                val id = skillsRepository.getOrCreateSkillIdByName(name)
                id
            }.onSuccess { skillId ->
                val catalog = runCatching { skillsRepository.getActiveSkills() }.getOrDefault(emptyList())
                _uiState.update {
                    it.copy(
                        availableSkills = catalog,
                        selectedSkillIds = it.selectedSkillIds + skillId,
                        newSkillName = "",
                        isLoadingSkills = false
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoadingSkills = false,
                        errorMessage = throwable.message ?: "No se pudo agregar la skill."
                    )
                }
            }
        }
    }

    fun save(contentResolver: ContentResolver, onSuccess: () -> Unit) {
        val state = _uiState.value
        val semesterValue = state.semester.toIntOrNull()
        if (state.displayName.isBlank() || state.universityName.isBlank() || state.degreeProgram.isBlank() || semesterValue == null || state.bio.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Completa todos los campos correctamente.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                profileRepository.completeStudentProfile(
                    displayName = state.displayName,
                    universityName = state.universityName,
                    degreeProgram = state.degreeProgram,
                    semester = semesterValue,
                    bio = state.bio,
                    skillIds = state.selectedSkillIds.toList()
                )

                state.pickedAvatarUri?.let { uri ->
                    val uploaded = assetsRepository.uploadImage(
                        contentResolver = contentResolver,
                        uri = uri,
                        kind = AssetKind.PROFILE_AVATAR,
                        accessScope = AssetAccessScope.PUBLIC_READ,
                        subPath = "profiles"
                    )
                    profileRepository.updateAvatar(uploaded.assetId)
                }
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
                onSuccess()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = throwable.message ?: "No se pudo guardar el perfil."
                    )
                }
            }
        }
    }
}

class CompanyProfileSetupViewModel(
    private val profileRepository: ProfileRepository = ProfileRepository(),
    private val assetsRepository: AssetsRepository = AssetsRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(CompanySetupFormUiState())
    val uiState: StateFlow<CompanySetupFormUiState> = _uiState.asStateFlow()

    fun prefill(profile: AppUserProfile?) {
        _uiState.update {
            it.copy(
                isInitialized = true,
                displayName = profile?.displayName.orEmpty(),
                businessName = profile?.businessName.orEmpty(),
                industry = profile?.industry.orEmpty(),
                contactPersonName = profile?.contactPersonName.orEmpty(),
                description = profile?.bio.orEmpty(),
                logoUrl = profile?.companyLogoUrl
            )
        }
    }

    fun onDisplayNameChange(value: String) = _uiState.update { it.copy(displayName = value, errorMessage = null) }
    fun onBusinessNameChange(value: String) = _uiState.update { it.copy(businessName = value, errorMessage = null) }
    fun onIndustryChange(value: String) = _uiState.update { it.copy(industry = value, errorMessage = null) }
    fun onContactPersonNameChange(value: String) = _uiState.update { it.copy(contactPersonName = value, errorMessage = null) }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value, errorMessage = null) }
    fun onLogoPicked(uri: Uri?) = _uiState.update { it.copy(pickedLogoUri = uri, errorMessage = null) }

    fun save(contentResolver: ContentResolver, onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.displayName.isBlank() || state.businessName.isBlank() || state.industry.isBlank() || state.contactPersonName.isBlank() || state.description.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Completa todos los campos del negocio.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                profileRepository.completeCompanyProfile(
                    displayName = state.displayName,
                    businessName = state.businessName,
                    industry = state.industry,
                    contactPersonName = state.contactPersonName,
                    description = state.description
                )

                state.pickedLogoUri?.let { uri ->
                    val uploaded = assetsRepository.uploadImage(
                        contentResolver = contentResolver,
                        uri = uri,
                        kind = AssetKind.COMPANY_LOGO,
                        accessScope = AssetAccessScope.PUBLIC_READ,
                        subPath = "companies"
                    )
                    profileRepository.updateCompanyLogo(uploaded.assetId)
                }
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
                onSuccess()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = throwable.message ?: "No se pudo guardar el perfil."
                    )
                }
            }
        }
    }
}
