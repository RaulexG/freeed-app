package com.raulcn.freeed.data.repository

import com.raulcn.freeed.core.model.VisibilityLevel
import com.raulcn.freeed.data.remote.supabase.SupabaseClientProvider
import com.raulcn.freeed.domain.model.PortfolioItem
import com.raulcn.freeed.domain.model.PortfolioItemType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class PortfolioDraft(
    val itemType: PortfolioItemType,
    val title: String,
    val description: String?,
    val contribution: String?,
    val projectUrl: String?,
    val repositoryUrl: String?,
    val visibility: VisibilityLevel
)

class PortfolioRepository {

    private val client = SupabaseClientProvider.client

    suspend fun getMyPortfolio(): List<PortfolioItem> {
        val userId = client.auth.currentUserOrNull()?.id ?: return emptyList()
        val base = client.from("portfolio_items").select(columns = portfolioColumns()) {
            filter { eq("student_id", userId) }
            order(column = "created_at", order = Order.DESCENDING)
        }.decodeList<PortfolioItemDto>().map { it.toDomain() }
        return base.map { item ->
            val cover = runCatching { getCoverImageUrl(item.id) }.getOrNull()
            item.copy(coverImageUrl = cover)
        }
    }

    suspend fun getItemById(itemId: String): PortfolioItem? {
        val base = client.from("portfolio_items").select(columns = portfolioColumns()) {
            filter { eq("id", itemId) }
        }.decodeSingleOrNull<PortfolioItemDto>()?.toDomain() ?: return null
        val cover = runCatching { getCoverImageUrl(base.id) }.getOrNull()
        return base.copy(coverImageUrl = cover)
    }

    suspend fun createItem(draft: PortfolioDraft): String {
        val userId = client.auth.currentUserOrNull()?.id
            ?: error("No authenticated user found.")

        val payload = buildJsonObject {
            put("student_id", userId)
            put("item_type", draft.itemType.backendValue)
            put("title", draft.title.trim())
            putNullableString("description", draft.description)
            putNullableString("contribution", draft.contribution)
            putNullableString("project_url", draft.projectUrl)
            putNullableString("repository_url", draft.repositoryUrl)
            put("visibility", draft.visibility.backendValue)
        }

        val inserted = client.from("portfolio_items")
            .insert(payload) { select(columns = Columns.list("id")) }
            .decodeSingle<InsertedPortfolioIdDto>()
        return inserted.id
    }

    suspend fun updateItem(itemId: String, draft: PortfolioDraft) {
        val payload = buildJsonObject {
            put("item_type", draft.itemType.backendValue)
            put("title", draft.title.trim())
            putNullableString("description", draft.description)
            putNullableString("contribution", draft.contribution)
            putNullableString("project_url", draft.projectUrl)
            putNullableString("repository_url", draft.repositoryUrl)
            put("visibility", draft.visibility.backendValue)
        }

        client.from("portfolio_items").update(payload) {
            filter { eq("id", itemId) }
        }
    }

    suspend fun deleteItem(itemId: String) {
        client.from("portfolio_items").delete {
            filter { eq("id", itemId) }
        }
    }

    suspend fun linkCoverImage(itemId: String, assetId: String) {
        client.from("portfolio_assets").delete {
            filter {
                eq("portfolio_item_id", itemId)
                eq("is_cover", true)
            }
        }
        client.from("portfolio_assets").insert(
            buildJsonObject {
                put("portfolio_item_id", itemId)
                put("asset_id", assetId)
                put("is_cover", true)
                put("sort_order", 0)
            }
        )
    }

    suspend fun getCoverImageUrl(itemId: String): String? {
        val assetsRepo = AssetsRepository()
        val row = client.from("portfolio_assets").select(
            columns = Columns.list("portfolio_item_id", "asset_id", "is_cover")
        ) {
            filter {
                eq("portfolio_item_id", itemId)
                eq("is_cover", true)
            }
        }.decodeSingleOrNull<PortfolioCoverLinkDto>() ?: return null
        return assetsRepo.getAssetPublicUrl(row.assetId)
    }
}

@Serializable
private data class PortfolioCoverLinkDto(
    @SerialName("portfolio_item_id") val portfolioItemId: String,
    @SerialName("asset_id") val assetId: String,
    @SerialName("is_cover") val isCover: Boolean
)

private fun portfolioColumns(): Columns = Columns.list(
    "id",
    "student_id",
    "item_type",
    "title",
    "description",
    "contribution",
    "project_url",
    "repository_url",
    "visibility",
    "started_on",
    "completed_on"
)

private val VisibilityLevel.backendValue: String
    get() = when (this) {
        VisibilityLevel.PUBLIC -> "public"
        VisibilityLevel.PRIVATE -> "private"
    }

private fun JsonObjectBuilder.putNullableString(key: String, value: String?) {
    val trimmed = value?.trim()
    if (trimmed.isNullOrEmpty()) {
        put(key, JsonNull)
    } else {
        put(key, JsonPrimitive(trimmed))
    }
}

@Serializable
private data class PortfolioItemDto(
    val id: String,
    @SerialName("student_id")
    val studentId: String,
    @SerialName("item_type")
    val itemType: String,
    val title: String,
    val description: String? = null,
    val contribution: String? = null,
    @SerialName("project_url")
    val projectUrl: String? = null,
    @SerialName("repository_url")
    val repositoryUrl: String? = null,
    val visibility: String,
    @SerialName("started_on")
    val startedOn: String? = null,
    @SerialName("completed_on")
    val completedOn: String? = null
) {
    fun toDomain(): PortfolioItem = PortfolioItem(
        id = id,
        studentId = studentId,
        itemType = PortfolioItemType.fromBackend(itemType),
        title = title,
        description = description,
        contribution = contribution,
        projectUrl = projectUrl,
        repositoryUrl = repositoryUrl,
        visibility = visibility.toVisibility(),
        startedOn = startedOn,
        completedOn = completedOn
    )

    private fun String.toVisibility(): VisibilityLevel = when (lowercase()) {
        "private" -> VisibilityLevel.PRIVATE
        else -> VisibilityLevel.PUBLIC
    }
}

@Serializable
private data class InsertedPortfolioIdDto(val id: String)
