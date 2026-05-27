package com.raulcn.freeed.data.repository

import com.raulcn.freeed.core.model.ServiceModality
import com.raulcn.freeed.core.model.ServiceStatus
import com.raulcn.freeed.data.remote.supabase.SupabaseClientProvider
import com.raulcn.freeed.domain.model.Service
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class FavoritesRepository {

    private val client = SupabaseClientProvider.client

    /**
     * Returns the service IDs the current company has saved as favorites.
     * Returns an empty set for guests or non-companies (RLS would do it anyway).
     */
    suspend fun getFavoriteServiceIds(): Set<String> {
        val userId = client.auth.currentUserOrNull()?.id ?: return emptySet()
        return client.from("favorite_services").select(
            columns = Columns.list("service_id")
        ) {
            filter { eq("company_id", userId) }
        }.decodeList<FavoriteIdRowDto>().map { it.serviceId }.toSet()
    }

    /**
     * Returns the favorite services as full domain objects, ordered by date saved (desc).
     */
    suspend fun getFavoriteServices(): List<Service> {
        val userId = client.auth.currentUserOrNull()?.id ?: return emptyList()
        val base = client.from("favorite_services").select(
            columns = Columns.raw(
                "company_id,service_id,created_at," +
                    "services!favorite_services_service_id_fkey(id,student_id,category_id,title," +
                    "short_description,description,service_status,modality,price_type," +
                    "price_amount,currency_code,tags)"
            )
        ) {
            filter { eq("company_id", userId) }
            order(column = "created_at", order = Order.DESCENDING)
        }.decodeList<FavoriteWithServiceDto>().mapNotNull { it.services?.toService() }
        val servicesRepo = ServicesRepository()
        return base.map { service ->
            val image = runCatching { servicesRepo.getPrimaryImageUrl(service.id) }.getOrNull()
            service.copy(imageUrl = image)
        }
    }

    suspend fun addFavorite(serviceId: String) {
        val userId = client.auth.currentUserOrNull()?.id
            ?: error("No authenticated user found.")
        client.from("favorite_services").insert(
            buildJsonObject {
                put("company_id", userId)
                put("service_id", serviceId)
            }
        )
    }

    suspend fun removeFavorite(serviceId: String) {
        val userId = client.auth.currentUserOrNull()?.id
            ?: error("No authenticated user found.")
        client.from("favorite_services").delete {
            filter {
                eq("company_id", userId)
                eq("service_id", serviceId)
            }
        }
    }
}

@Serializable
private data class FavoriteIdRowDto(
    @SerialName("service_id")
    val serviceId: String
)

@Serializable
private data class FavoriteWithServiceDto(
    val services: EmbeddedServiceDto? = null
)

@Serializable
private data class EmbeddedServiceDto(
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
    fun toService(): Service = Service(
        id = id,
        studentId = studentId,
        categoryId = categoryId,
        title = title,
        shortDescription = shortDescription,
        description = description,
        status = serviceStatus.toServiceStatus(),
        modality = modality.toServiceModality(),
        priceLabel = priceLabel(),
        tags = tags
    )

    private fun priceLabel(): String = when (priceType?.lowercase()) {
        "free" -> "Sin costo"
        "custom" -> "A convenir"
        "hourly" -> if (priceAmount != null) {
            "${currencyCode ?: "MXN"} ${priceAmount.formatMoney()}/hora"
        } else "Por hora"
        else -> if (priceAmount != null) {
            "${currencyCode ?: "MXN"} ${priceAmount.formatMoney()}"
        } else "Precio a convenir"
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
