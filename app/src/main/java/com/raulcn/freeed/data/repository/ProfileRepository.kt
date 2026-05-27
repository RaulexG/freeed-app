package com.raulcn.freeed.data.repository

import com.raulcn.freeed.core.model.ProfileStatus
import com.raulcn.freeed.core.model.UserRole
import com.raulcn.freeed.data.remote.supabase.CompanyProfileDto
import com.raulcn.freeed.data.remote.supabase.ProfileDto
import com.raulcn.freeed.data.remote.supabase.StudentProfileDto
import com.raulcn.freeed.data.remote.supabase.SupabaseClientProvider
import com.raulcn.freeed.domain.model.AppUserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.delay
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ProfileRepository(
    private val assetsRepository: AssetsRepository = AssetsRepository(),
    private val skillsRepository: SkillsRepository = SkillsRepository()
) {

    private val client = SupabaseClientProvider.client

    suspend fun getCurrentProfile(): AppUserProfile? {
        repeat(3) { attempt ->
            val user = client.auth.currentUserOrNull()
            if (user == null) {
                delay(180)
                return@repeat
            }

            val profile = client.from("profiles").select(
                columns = Columns.list("id", "email", "role", "profile_status", "display_name", "bio", "avatar_asset_id")
            ) {
                filter {
                    eq("id", user.id)
                }
            }.decodeSingleOrNull<ProfileDto>()

            if (profile == null) {
                delay(180)
                return@repeat
            }

            val avatarUrl = profile.avatarAssetId?.let { runCatching { assetsRepository.getAssetPublicUrl(it) }.getOrNull() }

            return when (profile.role.asUserRole()) {
                UserRole.STUDENT -> {
                    val studentProfile = client.from("student_profiles").select(
                        columns = Columns.list("profile_id", "university_name", "degree_program", "semester")
                    ) {
                        filter {
                            eq("profile_id", user.id)
                        }
                    }.decodeSingleOrNull<StudentProfileDto>()

                    val skills = runCatching { skillsRepository.getCurrentUserSkillNames() }
                        .getOrDefault(emptyList())

                    AppUserProfile(
                        id = profile.id,
                        email = profile.email,
                        role = UserRole.STUDENT,
                        profileStatus = profile.profileStatus.asProfileStatus(),
                        displayName = profile.displayName,
                        bio = profile.bio,
                        universityName = studentProfile?.universityName,
                        degreeProgram = studentProfile?.degreeProgram,
                        semester = studentProfile?.semester,
                        avatarUrl = avatarUrl,
                        skills = skills
                    )
                }

                UserRole.COMPANY -> {
                    val companyProfile = client.from("company_profiles").select(
                        columns = Columns.list(
                            "profile_id",
                            "business_name",
                            "industry",
                            "contact_person_name",
                            "description",
                            "website_url",
                            "logo_asset_id"
                        )
                    ) {
                        filter {
                            eq("profile_id", user.id)
                        }
                    }.decodeSingleOrNull<CompanyProfileDto>()

                    val companyLogoUrl = companyProfile?.logoAssetId?.let {
                        runCatching { assetsRepository.getAssetPublicUrl(it) }.getOrNull()
                    }

                    AppUserProfile(
                        id = profile.id,
                        email = profile.email,
                        role = UserRole.COMPANY,
                        profileStatus = profile.profileStatus.asProfileStatus(),
                        displayName = profile.displayName,
                        bio = profile.bio,
                        businessName = companyProfile?.businessName,
                        industry = companyProfile?.industry,
                        contactPersonName = companyProfile?.contactPersonName,
                        companyLogoUrl = companyLogoUrl,
                        avatarUrl = avatarUrl
                    )
                }

                UserRole.ADMIN -> AppUserProfile(
                    id = profile.id,
                    email = profile.email,
                    role = UserRole.ADMIN,
                    profileStatus = profile.profileStatus.asProfileStatus(),
                    displayName = profile.displayName,
                    bio = profile.bio,
                    avatarUrl = avatarUrl
                )
            }
        }

        return null
    }

    suspend fun completeStudentProfile(
        displayName: String,
        universityName: String,
        degreeProgram: String,
        semester: Int,
        bio: String,
        skillIds: List<String> = emptyList()
    ) {
        val userId = requireCurrentUserId()

        client.from("profiles").update(
            buildJsonObject {
                put("display_name", displayName.trim())
                put("bio", bio.trim())
            }
        ) {
            filter {
                eq("id", userId)
            }
        }

        client.from("student_profiles").update(
            buildJsonObject {
                put("university_name", universityName.trim())
                put("degree_program", degreeProgram.trim())
                put("semester", semester)
            }
        ) {
            filter {
                eq("profile_id", userId)
            }
        }

        skillsRepository.replaceCurrentUserSkills(skillIds)
    }

    suspend fun completeCompanyProfile(
        displayName: String,
        businessName: String,
        industry: String,
        contactPersonName: String,
        description: String
    ) {
        val userId = requireCurrentUserId()

        client.from("profiles").update(
            buildJsonObject {
                put("display_name", displayName.trim())
                put("bio", description.trim())
            }
        ) {
            filter {
                eq("id", userId)
            }
        }

        client.from("company_profiles").update(
            buildJsonObject {
                put("business_name", businessName.trim())
                put("industry", industry.trim())
                put("contact_person_name", contactPersonName.trim())
                put("description", description.trim())
            }
        ) {
            filter {
                eq("profile_id", userId)
            }
        }
    }

    suspend fun updateAvatar(assetId: String) {
        val userId = requireCurrentUserId()
        client.from("profiles").update(
            buildJsonObject {
                put("avatar_asset_id", assetId)
            }
        ) {
            filter { eq("id", userId) }
        }
    }

    suspend fun updateCompanyLogo(assetId: String) {
        val userId = requireCurrentUserId()
        client.from("company_profiles").update(
            buildJsonObject {
                put("logo_asset_id", assetId)
            }
        ) {
            filter { eq("profile_id", userId) }
        }
    }

    private fun requireCurrentUserId(): String {
        return client.auth.currentUserOrNull()?.id
            ?: error("No authenticated user found.")
    }
}

private fun String.asUserRole(): UserRole = when (lowercase()) {
    "student" -> UserRole.STUDENT
    "company" -> UserRole.COMPANY
    else -> UserRole.ADMIN
}

private fun String.asProfileStatus(): ProfileStatus = when (lowercase()) {
    "active" -> ProfileStatus.ACTIVE
    "suspended" -> ProfileStatus.SUSPENDED
    "archived" -> ProfileStatus.ARCHIVED
    else -> ProfileStatus.ONBOARDING
}
