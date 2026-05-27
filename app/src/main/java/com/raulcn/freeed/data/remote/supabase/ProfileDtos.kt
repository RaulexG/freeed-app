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
    val bio: String? = null,
    @SerialName("avatar_asset_id")
    val avatarAssetId: String? = null
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
    val contactPersonName: String? = null,
    val description: String? = null,
    @SerialName("website_url")
    val websiteUrl: String? = null,
    @SerialName("logo_asset_id")
    val logoAssetId: String? = null
)

@Serializable
data class SkillCatalogDto(
    val id: String,
    val name: String,
    val slug: String
)

@Serializable
data class ProfileSkillRowDto(
    @SerialName("profile_id")
    val profileId: String,
    @SerialName("skill_id")
    val skillId: String,
    val skills: SkillNameDto? = null
)

@Serializable
data class SkillNameDto(
    val id: String? = null,
    val slug: String? = null,
    val name: String? = null
)
