package com.raulcn.freeed.domain.model

import com.raulcn.freeed.core.model.ProfileStatus
import com.raulcn.freeed.core.model.UserRole

data class AppUserProfile(
    val id: String,
    val email: String,
    val role: UserRole,
    val profileStatus: ProfileStatus,
    val displayName: String,
    val bio: String?,
    val universityName: String? = null,
    val degreeProgram: String? = null,
    val semester: Int? = null,
    val businessName: String? = null,
    val industry: String? = null,
    val contactPersonName: String? = null
)

