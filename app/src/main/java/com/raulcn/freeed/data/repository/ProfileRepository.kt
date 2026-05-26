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

class ProfileRepository {

    private val client = SupabaseClientProvider.client

    suspend fun getCurrentProfile(): AppUserProfile? {
        repeat(3) { attempt ->
            val user = client.auth.currentUserOrNull()
            if (user == null) {
                delay(180)
                return@repeat
            }

            val profile = client.from("profiles").select(
                columns = Columns.list("id", "email", "role", "profile_status", "display_name", "bio")
            ) {
                filter {
                    eq("id", user.id)
                }
            }.decodeSingleOrNull<ProfileDto>()

            if (profile == null) {
                delay(180)
                return@repeat
            }

            return when (profile.role.asUserRole()) {
                UserRole.STUDENT -> {
                    val studentProfile = client.from("student_profiles").select(
                        columns = Columns.list("profile_id", "university_name", "degree_program", "semester")
                    ) {
                        filter {
                            eq("profile_id", user.id)
                        }
                    }.decodeSingleOrNull<StudentProfileDto>()

                    AppUserProfile(
                        id = profile.id,
                        email = profile.email,
                        role = UserRole.STUDENT,
                        profileStatus = profile.profileStatus.asProfileStatus(),
                        displayName = profile.displayName,
                        bio = profile.bio,
                        universityName = studentProfile?.universityName,
                        degreeProgram = studentProfile?.degreeProgram,
                        semester = studentProfile?.semester
                    )
                }

                UserRole.COMPANY -> {
                    val companyProfile = client.from("company_profiles").select(
                        columns = Columns.list("profile_id", "business_name", "industry", "contact_person_name")
                    ) {
                        filter {
                            eq("profile_id", user.id)
                        }
                    }.decodeSingleOrNull<CompanyProfileDto>()

                    AppUserProfile(
                        id = profile.id,
                        email = profile.email,
                        role = UserRole.COMPANY,
                        profileStatus = profile.profileStatus.asProfileStatus(),
                        displayName = profile.displayName,
                        bio = profile.bio,
                        businessName = companyProfile?.businessName,
                        industry = companyProfile?.industry,
                        contactPersonName = companyProfile?.contactPersonName
                    )
                }

                UserRole.ADMIN -> AppUserProfile(
                    id = profile.id,
                    email = profile.email,
                    role = UserRole.ADMIN,
                    profileStatus = profile.profileStatus.asProfileStatus(),
                    displayName = profile.displayName,
                    bio = profile.bio
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
        bio: String
    ) {
        val userId = requireCurrentUserId()

        client.from("profiles").update(
            mapOf(
                "display_name" to displayName.trim(),
                "bio" to bio.trim()
            )
        ) {
            filter {
                eq("id", userId)
            }
        }

        client.from("student_profiles").update(
            mapOf(
                "university_name" to universityName.trim(),
                "degree_program" to degreeProgram.trim(),
                "semester" to semester
            )
        ) {
            filter {
                eq("profile_id", userId)
            }
        }
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
            mapOf(
                "display_name" to displayName.trim(),
                "bio" to description.trim()
            )
        ) {
            filter {
                eq("id", userId)
            }
        }

        client.from("company_profiles").update(
            mapOf(
                "business_name" to businessName.trim(),
                "industry" to industry.trim(),
                "contact_person_name" to contactPersonName.trim(),
                "description" to description.trim()
            )
        ) {
            filter {
                eq("profile_id", userId)
            }
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
