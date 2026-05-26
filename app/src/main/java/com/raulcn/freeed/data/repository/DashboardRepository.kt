package com.raulcn.freeed.data.repository

import com.raulcn.freeed.core.model.UserRole
import com.raulcn.freeed.data.remote.supabase.SupabaseClientProvider
import com.raulcn.freeed.domain.model.Category
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class DashboardStats(
    val servicesCount: Int = 0,
    val portfolioCount: Int = 0,
    val requestsCount: Int = 0
)

class DashboardRepository {

    private val client = SupabaseClientProvider.client

    suspend fun getCategories(): List<Category> {
        return client.from("categories").select(
            columns = Columns.list("id", "name", "slug", "description", "is_active", "sort_order")
        ).decodeList<CategoryDto>()
            .filter { it.isActive }
            .sortedBy { it.sortOrder }
            .map { dto ->
                Category(
                    id = dto.id,
                    name = dto.name,
                    slug = dto.slug,
                    description = dto.description
                )
            }
    }

    suspend fun getCurrentStats(role: UserRole): DashboardStats {
        val userId = client.auth.currentUserOrNull()?.id ?: return DashboardStats()

        return when (role) {
            UserRole.STUDENT -> DashboardStats(
                servicesCount = client.from("services").select(columns = Columns.list("id")) {
                    filter {
                        eq("student_id", userId)
                    }
                }.decodeList<IdRowDto>().size,
                portfolioCount = client.from("portfolio_items").select(columns = Columns.list("id")) {
                    filter {
                        eq("student_id", userId)
                    }
                }.decodeList<IdRowDto>().size,
                requestsCount = client.from("service_requests").select(columns = Columns.list("id")) {
                    filter {
                        eq("student_id", userId)
                    }
                }.decodeList<IdRowDto>().size
            )

            UserRole.COMPANY -> DashboardStats(
                requestsCount = client.from("service_requests").select(columns = Columns.list("id")) {
                    filter {
                        eq("company_id", userId)
                    }
                }.decodeList<IdRowDto>().size
            )

            UserRole.ADMIN -> DashboardStats()
        }
    }
}

@Serializable
private data class CategoryDto(
    val id: String,
    val name: String,
    val slug: String,
    val description: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("sort_order")
    val sortOrder: Int = 100
)

@Serializable
private data class IdRowDto(
    val id: String
)
