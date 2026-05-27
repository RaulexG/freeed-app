package com.raulcn.freeed.data.repository

import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap
import com.raulcn.freeed.data.remote.supabase.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlin.time.Duration.Companion.hours
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

enum class AssetKind(val backendValue: String) {
    PROFILE_AVATAR("profile_avatar"),
    COMPANY_LOGO("company_logo"),
    STUDENT_CV("student_cv"),
    PORTFOLIO_IMAGE("portfolio_image"),
    PORTFOLIO_DOCUMENT("portfolio_document"),
    SERVICE_IMAGE("service_image"),
    REQUEST_ATTACHMENT("request_attachment")
}

enum class AssetAccessScope(val backendValue: String) {
    PUBLIC_READ("public_read"),
    OWNER_ONLY("owner_only"),
    REQUEST_PARTICIPANTS("request_participants")
}

data class UploadedAsset(
    val assetId: String,
    val publicUrl: String?,
    val objectPath: String,
    val bucketId: String
)

class AssetsRepository {

    private val client = SupabaseClientProvider.client

    suspend fun uploadImage(
        contentResolver: ContentResolver,
        uri: Uri,
        kind: AssetKind,
        accessScope: AssetAccessScope,
        subPath: String
    ): UploadedAsset {
        val userId = client.auth.currentUserOrNull()?.id
            ?: error("No authenticated user found.")

        val bucketId = when (accessScope) {
            AssetAccessScope.PUBLIC_READ -> "public-media"
            else -> "private-documents"
        }

        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
        val fileName = "${System.currentTimeMillis()}.$extension"
        val objectPath = "$userId/${subPath.trim('/')}/$fileName"

        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("No se pudo leer el archivo seleccionado.")

        client.storage.from(bucketId).upload(path = objectPath, data = bytes) {
            upsert = true
        }

        val publicUrl = if (bucketId == "public-media") {
            client.storage.from(bucketId).publicUrl(objectPath)
        } else {
            null
        }

        val payload = buildJsonObject {
            put("owner_id", userId)
            put("bucket_id", bucketId)
            put("object_path", objectPath)
            put("file_name", fileName)
            put("mime_type", mimeType)
            put("file_size_bytes", bytes.size.toLong())
            put("asset_kind", kind.backendValue)
            put("access_scope", accessScope.backendValue)
        }

        val inserted = client.from("assets")
            .insert(payload) { select(columns = Columns.list("id")) }
            .decodeSingle<InsertedAssetIdDto>()

        return UploadedAsset(
            assetId = inserted.id,
            publicUrl = publicUrl,
            objectPath = objectPath,
            bucketId = bucketId
        )
    }

    suspend fun getAssetPublicUrl(assetId: String): String? {
        val row = client.from("assets").select(
            columns = Columns.list("bucket_id", "object_path")
        ) {
            filter { eq("id", assetId) }
        }.decodeSingleOrNull<AssetLookupDto>() ?: return null

        if (row.bucketId == "public-media") {
            return client.storage.from(row.bucketId).publicUrl(row.objectPath)
        }

        return runCatching {
            client.storage.from(row.bucketId).createSignedUrl(
                path = row.objectPath,
                expiresIn = 24.hours
            )
        }.getOrNull()
    }

    suspend fun findLatestPublicAssetUrl(ownerId: String, kind: AssetKind): String? {
        val row = client.from("assets").select(
            columns = Columns.list("bucket_id", "object_path", "asset_kind")
        ) {
            filter {
                eq("owner_id", ownerId)
                eq("asset_kind", kind.backendValue)
                eq("access_scope", AssetAccessScope.PUBLIC_READ.backendValue)
            }
            order(column = "created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            limit(1)
        }.decodeSingleOrNull<AssetLookupKindDto>() ?: return null

        return if (row.bucketId == "public-media") {
            client.storage.from(row.bucketId).publicUrl(row.objectPath)
        } else {
            runCatching {
                client.storage.from(row.bucketId).createSignedUrl(
                    path = row.objectPath,
                    expiresIn = 24.hours
                )
            }.getOrNull()
        }
    }
}

@Suppress("unused")
private fun JsonObjectBuilder.putNullableString(key: String, value: String?) {
    val trimmed = value?.trim()
    if (trimmed.isNullOrEmpty()) {
        put(key, JsonNull)
    } else {
        put(key, JsonPrimitive(trimmed))
    }
}

@Serializable
private data class InsertedAssetIdDto(val id: String)

@Serializable
private data class AssetLookupDto(
    @SerialName("bucket_id")
    val bucketId: String,
    @SerialName("object_path")
    val objectPath: String
)

@Serializable
private data class AssetLookupKindDto(
    @SerialName("bucket_id")
    val bucketId: String,
    @SerialName("object_path")
    val objectPath: String,
    @SerialName("asset_kind")
    val assetKind: String
)
