package com.raulcn.freeed.data.repository

import com.raulcn.freeed.data.remote.supabase.ProfileSkillRowDto
import com.raulcn.freeed.data.remote.supabase.SkillCatalogDto
import com.raulcn.freeed.data.remote.supabase.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class SkillOption(
    val id: String,
    val name: String,
    val slug: String
)

class SkillsRepository {

    private val client = SupabaseClientProvider.client

    suspend fun getActiveSkills(): List<SkillOption> {
        return client.from("skills").select(
            columns = Columns.list("id", "name", "slug")
        ) {
            filter { eq("is_active", true) }
            order(column = "name", order = Order.ASCENDING)
        }.decodeList<SkillCatalogDto>().map {
            SkillOption(id = it.id, name = it.name, slug = it.slug)
        }
    }

    suspend fun getCurrentUserSkillNames(): List<String> {
        val userId = client.auth.currentUserOrNull()?.id ?: return emptyList()
        return client.from("profile_skills").select(
            columns = Columns.raw("profile_id,skill_id,skills(name)")
        ) {
            filter { eq("profile_id", userId) }
        }.decodeList<ProfileSkillRowDto>()
            .mapNotNull { it.skills?.name?.takeIf(String::isNotBlank) }
            .distinct()
            .sorted()
    }

    suspend fun getCurrentUserSkills(): List<SkillOption> {
        val userId = client.auth.currentUserOrNull()?.id ?: return emptyList()
        return client.from("profile_skills").select(
            columns = Columns.raw("profile_id,skill_id,skills(id,name,slug)")
        ) {
            filter { eq("profile_id", userId) }
        }.decodeList<ProfileSkillRowDto>()
            .mapNotNull { row ->
                val skill = row.skills ?: return@mapNotNull null
                val skillId = skill.id?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val skillName = skill.name?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val skillSlug = skill.slug?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                SkillOption(
                    id = skillId,
                    name = skillName,
                    slug = skillSlug
                )
            }
            .distinctBy { it.id }
            .sortedBy { it.name.lowercase() }
    }

    suspend fun replaceCurrentUserSkills(skillIds: List<String>) {
        val userId = client.auth.currentUserOrNull()?.id
            ?: error("No authenticated user found.")

        client.from("profile_skills").delete {
            filter { eq("profile_id", userId) }
        }

        val distinctIds = skillIds.distinct()
        if (distinctIds.isEmpty()) return

        val payload = distinctIds.map { skillId ->
            buildJsonObject {
                put("profile_id", userId)
                put("skill_id", skillId)
            }
        }
        client.from("profile_skills").insert(payload)
    }

    suspend fun getOrCreateSkillIdByName(rawName: String): String {
        val normalizedName = rawName.trim()
        require(normalizedName.length >= 2) { "La skill debe tener al menos 2 caracteres." }
        val existing = client.from("skills").select(
            columns = Columns.list("id", "name", "slug")
        ) {
            filter { ilike("name", normalizedName) }
        }.decodeList<SkillCatalogDto>().firstOrNull { it.name.equals(normalizedName, ignoreCase = true) }
        if (existing != null) return existing.id

        val slug = normalizedName
            .lowercase()
            .replace(Regex("[^a-z0-9\\s-]"), "")
            .trim()
            .replace(Regex("\\s+"), "-")
            .replace(Regex("-+"), "-")
            .ifBlank { "skill-${System.currentTimeMillis()}" }

        return runCatching {
            val inserted = client.from("skills")
                .insert(
                    buildJsonObject {
                        put("name", normalizedName)
                        put("slug", slug)
                        put("is_active", true)
                    }
                ) {
                    select(columns = Columns.list("id"))
                }.decodeSingle<InsertedSkillIdDto>()
            inserted.id
        }.getOrElse { insertError ->
            val maybeCreated = runCatching {
                client.from("skills").select(
                    columns = Columns.list("id", "name", "slug")
                ) {
                    filter { ilike("name", normalizedName) }
                }.decodeList<SkillCatalogDto>().firstOrNull { it.name.equals(normalizedName, ignoreCase = true) }
            }.getOrNull()
            if (maybeCreated != null) return maybeCreated.id

            val message = insertError.message.orEmpty().lowercase()
            if (
                message.contains("row-level security") ||
                message.contains("new row violates row-level security policy") ||
                message.contains("permission denied")
            ) {
                error("No tienes permisos para crear skills nuevas. Actualiza politicas de Supabase y vuelve a intentar.")
            }
            throw insertError
        }
    }
}

@kotlinx.serialization.Serializable
private data class InsertedSkillIdDto(val id: String)
