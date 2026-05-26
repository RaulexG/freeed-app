package com.raulcn.freeed.data.repository

import com.raulcn.freeed.core.model.ServiceModality
import com.raulcn.freeed.core.model.ServiceStatus
import com.raulcn.freeed.data.remote.supabase.SupabaseClientProvider
import com.raulcn.freeed.domain.model.Category
import com.raulcn.freeed.domain.model.Service
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class BrowseRepository {

    private val client = SupabaseClientProvider.client

    suspend fun getActiveCategories(): List<Category> {
        return client.from("categories").select(
            columns = Columns.list("id", "name", "slug", "description", "is_active")
        ).decodeList<CategoryBrowseDto>()
            .filter { it.isActive }
            .map {
                Category(
                    id = it.id,
                    name = it.name,
                    slug = it.slug,
                    description = it.description
                )
            }
    }

    suspend fun getPublishedServices(): List<Service> {
        return client.from("services").select(
            columns = Columns.list(
                "id",
                "student_id",
                "category_id",
                "title",
                "short_description",
                "description",
                "service_status",
                "modality",
                "price_type",
                "price_amount",
                "currency_code",
                "tags"
            )
        ).decodeList<ServiceBrowseDto>()
            .filter { it.serviceStatus.equals("published", ignoreCase = true) }
            .map { dto ->
                Service(
                    id = dto.id,
                    studentId = dto.studentId,
                    categoryId = dto.categoryId,
                    title = dto.title,
                    shortDescription = dto.shortDescription,
                    description = dto.description,
                    status = dto.serviceStatus.toServiceStatus(),
                    modality = dto.modality.toServiceModality(),
                    priceLabel = dto.toPriceLabel(),
                    tags = dto.tags
                )
            }
    }

    suspend fun getPublishedServiceById(serviceId: String): Service? {
        return getPublishedServices().firstOrNull { it.id == serviceId }
    }
}

@Serializable
private data class CategoryBrowseDto(
    val id: String,
    val name: String,
    val slug: String,
    val description: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true
)

@Serializable
private data class ServiceBrowseDto(
    val id: String,
    @SerialName("student_id")
    val studentId: String,
    @SerialName("category_id")
    val categoryId: String,
    val title: String,
    @SerialName("short_description")
    val shortDescription: String? = null,
    val description: String,
    @SerialName("service_status")
    val serviceStatus: String,
    val modality: String,
    @SerialName("price_type")
    val priceType: String? = null,
    @SerialName("price_amount")
    val priceAmount: Double? = null,
    @SerialName("currency_code")
    val currencyCode: String? = null,
    val tags: List<String> = emptyList()
) {
    fun toPriceLabel(): String {
        return when (priceType?.lowercase()) {
            "free" -> "Sin costo"
            "custom" -> "A convenir"
            "hourly" -> if (priceAmount != null) {
                "${currencyCode ?: "MXN"} ${priceAmount.formatMoney()}/hora"
            } else {
                "Por hora"
            }

            else -> if (priceAmount != null) {
                "${currencyCode ?: "MXN"} ${priceAmount.formatMoney()}"
            } else {
                "Precio a convenir"
            }
        }
    }
}

private fun String.toServiceStatus(): ServiceStatus = when (lowercase()) {
    "published" -> ServiceStatus.PUBLISHED
    "paused" -> ServiceStatus.PAUSED
    "archived" -> ServiceStatus.ARCHIVED
    else -> ServiceStatus.DRAFT
}

private fun String.toServiceModality(): ServiceModality = when (lowercase()) {
    "hybrid" -> ServiceModality.HYBRID
    "onsite" -> ServiceModality.ONSITE
    else -> ServiceModality.REMOTE
}

private fun Double.formatMoney(): String = if (this % 1.0 == 0.0) {
    toInt().toString()
} else {
    String.format("%.2f", this)
}
