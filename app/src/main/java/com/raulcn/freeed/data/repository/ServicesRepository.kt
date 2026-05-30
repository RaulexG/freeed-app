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
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

enum class ServicePriceType(val backendValue: String) {
    FIXED("fixed"),
    HOURLY("hourly"),
    CUSTOM("custom"),
    FREE("free");

    companion object {
        fun fromBackend(value: String?): ServicePriceType = when (value?.lowercase()) {
            "hourly" -> HOURLY
            "custom" -> CUSTOM
            "free" -> FREE
            else -> FIXED
        }
    }
}

data class ServiceDraft(
    val title: String,
    val categoryId: String,
    val shortDescription: String?,
    val description: String,
    val modality: ServiceModality,
    val priceType: ServicePriceType,
    val priceAmount: Double?,
    val status: ServiceStatus
)

class ServicesRepository {

    private val client = SupabaseClientProvider.client

    suspend fun getMyServices(): List<Service> {
        val userId = client.auth.currentUserOrNull()?.id ?: return emptyList()
        val base = client.from("services").select(
            columns = serviceColumns()
        ) {
            filter {
                eq("student_id", userId)
            }
            order(column = "created_at", order = Order.DESCENDING)
        }.decodeList<ServiceRowDto>().map { it.toService() }
        return base.map { service ->
            val image = runCatching { getPrimaryImageUrl(service.id) }.getOrNull()
            service.copy(imageUrl = image)
        }
    }

    suspend fun getServiceById(serviceId: String): Service? {
        val base = client.from("services").select(
            columns = serviceColumns()
        ) {
            filter {
                eq("id", serviceId)
            }
        }.decodeSingleOrNull<ServiceRowDto>()?.toService() ?: return null
        val image = runCatching { getPrimaryImageUrl(base.id) }.getOrNull()
        return base.copy(imageUrl = image)
    }

    suspend fun createService(draft: ServiceDraft): String {
        val userId = client.auth.currentUserOrNull()?.id
            ?: error("No authenticated user found.")

        val payload = buildJsonObject {
            put("student_id", userId)
            put("category_id", draft.categoryId)
            put("title", draft.title.trim())
            putNullableString("short_description", draft.shortDescription)
            put("description", draft.description.trim())
            put("modality", draft.modality.backendValue)
            put("price_type", draft.priceType.backendValue)
            putNullableNumber("price_amount", draft.priceAmount)
            put("currency_code", "MXN")
            put("service_status", draft.status.backendValue)
            if (draft.status == ServiceStatus.PUBLISHED) {
                put("published_at", nowIso())
            }
        }

        val inserted = client.from("services")
            .insert(payload) { select(columns = Columns.list("id")) }
            .decodeSingle<InsertedServiceIdDto>()
        return inserted.id
    }

    suspend fun updateService(serviceId: String, draft: ServiceDraft) {
        val userId = client.auth.currentUserOrNull()?.id
            ?: error("No authenticated user found.")

        val payload = buildJsonObject {
            put("category_id", draft.categoryId)
            put("title", draft.title.trim())
            putNullableString("short_description", draft.shortDescription)
            put("description", draft.description.trim())
            put("modality", draft.modality.backendValue)
            put("price_type", draft.priceType.backendValue)
            putNullableNumber("price_amount", draft.priceAmount)
            put("service_status", draft.status.backendValue)
            if (draft.status == ServiceStatus.PUBLISHED) {
                put("published_at", nowIso())
            }
        }

        client.from("services").update(payload) {
            filter {
                eq("id", serviceId)
                eq("student_id", userId)
            }
        }
    }

    suspend fun updateStatus(serviceId: String, status: ServiceStatus) {
        val userId = client.auth.currentUserOrNull()?.id
            ?: error("No authenticated user found.")

        val payload = buildJsonObject {
            put("service_status", status.backendValue)
            if (status == ServiceStatus.PUBLISHED) {
                put("published_at", nowIso())
            }
        }

        client.from("services").update(payload) {
            filter {
                eq("id", serviceId)
                eq("student_id", userId)
            }
        }
    }

    suspend fun linkPrimaryImage(serviceId: String, assetId: String) {
        // Replace the primary image: delete previous primary then insert this one.
        client.from("service_assets").delete {
            filter {
                eq("service_id", serviceId)
                eq("is_primary", true)
            }
        }

        client.from("service_assets").insert(
            buildJsonObject {
                put("service_id", serviceId)
                put("asset_id", assetId)
                put("is_primary", true)
                put("sort_order", 0)
            }
        )
    }

    suspend fun getPrimaryImageUrl(serviceId: String): String? {
        val assetsRepo = AssetsRepository()
        val row = client.from("service_assets").select(
            columns = Columns.list("service_id", "asset_id", "is_primary")
        ) {
            filter {
                eq("service_id", serviceId)
                eq("is_primary", true)
            }
        }.decodeSingleOrNull<PrimaryAssetLinkDto>() ?: return null
        return assetsRepo.getAssetPublicUrl(row.assetId)
    }
}

@kotlinx.serialization.Serializable
private data class PrimaryAssetLinkDto(
    @kotlinx.serialization.SerialName("service_id") val serviceId: String,
    @kotlinx.serialization.SerialName("asset_id") val assetId: String,
    @kotlinx.serialization.SerialName("is_primary") val isPrimary: Boolean
)

private fun serviceColumns(): Columns = Columns.list(
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

private val ServiceStatus.backendValue: String
    get() = when (this) {
        ServiceStatus.DRAFT -> "draft"
        ServiceStatus.PUBLISHED -> "published"
        ServiceStatus.PAUSED -> "paused"
        ServiceStatus.ARCHIVED -> "archived"
    }

private val ServiceModality.backendValue: String
    get() = when (this) {
        ServiceModality.REMOTE -> "remote"
        ServiceModality.HYBRID -> "hybrid"
        ServiceModality.ONSITE -> "onsite"
    }

private fun kotlinx.serialization.json.JsonObjectBuilder.putNullableString(key: String, value: String?) {
    val trimmed = value?.trim()
    if (trimmed.isNullOrEmpty()) {
        put(key, JsonNull)
    } else {
        put(key, JsonPrimitive(trimmed))
    }
}

private fun kotlinx.serialization.json.JsonObjectBuilder.putNullableNumber(key: String, value: Double?) {
    if (value == null) {
        put(key, JsonNull)
    } else {
        put(key, JsonPrimitive(value))
    }
}

private fun nowIso(): String = java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC).toString()

@Serializable
private data class ServiceRowDto(
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

@Serializable
private data class InsertedServiceIdDto(val id: String)

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
