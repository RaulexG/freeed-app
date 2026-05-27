package com.raulcn.freeed.domain.model

import com.raulcn.freeed.core.model.ProfileStatus
import com.raulcn.freeed.core.model.ServiceModality
import com.raulcn.freeed.core.model.UserRole
import com.raulcn.freeed.core.model.VisibilityLevel

data class ProfileSummary(
    val id: String,
    val role: UserRole,
    val status: ProfileStatus,
    val visibility: VisibilityLevel,
    val displayName: String,
    val headline: String?,
    val bio: String?,
    val avatarUrl: String?
)

data class StudentProfile(
    val profile: ProfileSummary,
    val universityName: String?,
    val degreeProgram: String?,
    val semester: Int?,
    val city: String?,
    val preferredWorkModality: ServiceModality?,
    val skills: List<String> = emptyList()
)

data class CompanyProfile(
    val profile: ProfileSummary,
    val businessName: String,
    val industry: String?,
    val contactPersonName: String?,
    val isVerified: Boolean
)

data class PublicStudentProfile(
    val id: String,
    val displayName: String,
    val bio: String?,
    val universityName: String?,
    val degreeProgram: String?,
    val semester: Int?,
    val avatarUrl: String? = null,
    val skills: List<String> = emptyList()
)

data class PublicCompanyProfile(
    val id: String,
    val displayName: String,
    val businessName: String,
    val industry: String?,
    val contactPersonName: String?,
    val description: String?,
    val websiteUrl: String?,
    val logoUrl: String? = null
)
