package com.raulcn.freeed.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulcn.freeed.data.repository.ProfileRepository
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
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class StudentProfileSetupViewModel(
    private val profileRepository: ProfileRepository = ProfileRepository()
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
                bio = profile?.bio.orEmpty()
            )
        }
    }

    fun onDisplayNameChange(value: String) = _uiState.update { it.copy(displayName = value, errorMessage = null) }
    fun onUniversityNameChange(value: String) = _uiState.update { it.copy(universityName = value, errorMessage = null) }
    fun onDegreeProgramChange(value: String) = _uiState.update { it.copy(degreeProgram = value, errorMessage = null) }
    fun onSemesterChange(value: String) = _uiState.update { it.copy(semester = value, errorMessage = null) }
    fun onBioChange(value: String) = _uiState.update { it.copy(bio = value, errorMessage = null) }

    fun save(onSuccess: () -> Unit) {
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
                    bio = state.bio
                )
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
    private val profileRepository: ProfileRepository = ProfileRepository()
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
                description = profile?.bio.orEmpty()
            )
        }
    }

    fun onDisplayNameChange(value: String) = _uiState.update { it.copy(displayName = value, errorMessage = null) }
    fun onBusinessNameChange(value: String) = _uiState.update { it.copy(businessName = value, errorMessage = null) }
    fun onIndustryChange(value: String) = _uiState.update { it.copy(industry = value, errorMessage = null) }
    fun onContactPersonNameChange(value: String) = _uiState.update { it.copy(contactPersonName = value, errorMessage = null) }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value, errorMessage = null) }

    fun save(onSuccess: () -> Unit) {
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

