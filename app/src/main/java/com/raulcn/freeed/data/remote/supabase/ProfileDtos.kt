package com.raulcn.freeed.data.remote.supabase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
    val id: String,
    val email: String,
    val role: String,
    @SerialName("profile_status")
    val profileStatus: String,
    @SerialName("display_name")
    val displayName: String,
    val bio: String? = null
)

@Serializable
data class StudentProfileDto(
    @SerialName("profile_id")
    val profileId: String,
    @SerialName("university_name")
    val universityName: String? = null,
    @SerialName("degree_program")
    val degreeProgram: String? = null,
    val semester: Int? = null
)

@Serializable
data class CompanyProfileDto(
    @SerialName("profile_id")
    val profileId: String,
    @SerialName("business_name")
    val businessName: String,
    val industry: String? = null,
    @SerialName("contact_person_name")
    val contactPersonName: String? = null
)

