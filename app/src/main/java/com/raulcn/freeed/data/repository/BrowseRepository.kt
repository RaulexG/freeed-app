package com.raulcn.freeed.data.repository

import com.raulcn.freeed.core.model.ServiceModality
import com.raulcn.freeed.core.model.ServiceStatus
import com.raulcn.freeed.core.model.VisibilityLevel
import com.raulcn.freeed.data.remote.supabase.SupabaseClientProvider
import com.raulcn.freeed.domain.model.Category
import com.raulcn.freeed.domain.model.PublicCompanyProfile
import com.raulcn.freeed.domain.model.PortfolioItem
import com.raulcn.freeed.domain.model.PortfolioItemType
import com.raulcn.freeed.domain.model.PublicStudentProfile
import com.raulcn.freeed.domain.model.Service
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class BrowseRepository {

    private val client = SupabaseClientProvider.client
    private val assetsRepository = AssetsRepository()

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
        val raw = client.from("services").select(
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
        return enrichServicesWithImages(raw)
    }

    suspend fun getPublishedServiceById(serviceId: String): Service? {
        return getPublishedServices().firstOrNull { it.id == serviceId }
    }

    suspend fun searchPublishedServices(
        query: String? = null,
        categoryId: String? = null,
        modality: ServiceModality? = null
    ): List<Service> {
        val raw = client.from("services").select(
            columns = Columns.list(
                "id", "student_id", "category_id", "title", "short_description",
                "description", "service_status", "modality", "price_type",
                "price_amount", "currency_code", "tags"
            )
        ) {
            filter {
                eq("service_status", "published")
                if (!categoryId.isNullOrBlank()) eq("category_id", categoryId)
                if (modality != null) eq("modality", modality.backendValue())
                if (!query.isNullOrBlank()) {
                    or {
                        ilike("title", "%${query.trim()}%")
                        ilike("short_description", "%${query.trim()}%")
                        ilike("description", "%${query.trim()}%")
                    }
                }
            }
            order(column = "created_at", order = Order.DESCENDING)
        }.decodeList<ServiceBrowseDto>().map { dto ->
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
        return enrichServicesWithImages(raw)
    }

    suspend fun searchPublishedServicesByTalent(
        query: String? = null,
        categoryId: String? = null,
        modality: ServiceModality? = null,
        skillQuery: String? = null,
        universityQuery: String? = null,
        degreeQuery: String? = null,
        semester: Int? = null
    ): List<Service> {
        val base = searchPublishedServices(query = query, categoryId = categoryId, modality = modality)
        val hasTalentFilters = !skillQuery.isNullOrBlank() ||
            !universityQuery.isNullOrBlank() ||
            !degreeQuery.isNullOrBlank() ||
            semester != null
        if (!hasTalentFilters || base.isEmpty()) return base

        val studentIds = base.map { it.studentId }.distinct()
        if (studentIds.isEmpty()) return base

        val studentRows = client.from("student_profiles").select(
            columns = Columns.list("profile_id", "university_name", "degree_program", "semester")
        ) {
            filter { isIn("profile_id", studentIds) }
        }.decodeList<PublicStudentRowDto>()

        val skillRows = client.from("profile_skills").select(
            columns = Columns.raw("profile_id,skill_id,skills(name)")
        ) {
            filter { isIn("profile_id", studentIds) }
        }.decodeList<PublicProfileSkillRowDto>()

        val byStudent = studentRows.associateBy { it.profileId }
        val skillsByStudent = skillRows.groupBy { it.profileId }
            .mapValues { (_, rows) ->
                rows.mapNotNull { it.skills?.name?.lowercase()?.trim() }
            }

        val normalizedSkill = skillQuery?.lowercase()?.trim().orEmpty()
        val normalizedUniversity = universityQuery?.lowercase()?.trim().orEmpty()
        val normalizedDegree = degreeQuery?.lowercase()?.trim().orEmpty()

        return base.filter { service ->
            val student = byStudent[service.studentId]
            if (student == null) return@filter false

            val passesUniversity = normalizedUniversity.isBlank() ||
                student.universityName?.lowercase()?.contains(normalizedUniversity) == true
            val passesDegree = normalizedDegree.isBlank() ||
                student.degreeProgram?.lowercase()?.contains(normalizedDegree) == true
            val passesSemester = semester == null || student.semester == semester
            val passesSkill = normalizedSkill.isBlank() ||
                skillsByStudent[service.studentId].orEmpty().any { it.contains(normalizedSkill) }

            passesUniversity && passesDegree && passesSemester && passesSkill
        }
    }

    suspend fun getStudentPublicProfile(profileId: String): PublicStudentProfile? {
        val profile = client.from("profiles").select(
            columns = Columns.list("id", "display_name", "bio", "avatar_asset_id")
        ) {
            filter { eq("id", profileId) }
        }.decodeSingleOrNull<PublicProfileRowDto>() ?: return null

        val student = client.from("student_profiles").select(
            columns = Columns.list("profile_id", "university_name", "degree_program", "semester")
        ) {
            filter { eq("profile_id", profileId) }
        }.decodeSingleOrNull<PublicStudentRowDto>()

        val avatarUrl = profile.avatarAssetId?.let {
            runCatching { assetsRepository.getAssetPublicUrl(it) }.getOrNull()
        } ?: runCatching {
            assetsRepository.findLatestPublicAssetUrl(profile.id, AssetKind.PROFILE_AVATAR)
        }.getOrNull()
        val skills = client.from("profile_skills").select(
            columns = Columns.raw("profile_id,skill_id,skills(name)")
        ) {
            filter { eq("profile_id", profileId) }
        }.decodeList<PublicProfileSkillRowDto>()
            .mapNotNull { it.skills?.name?.takeIf(String::isNotBlank) }
            .distinct()
            .sorted()

        return PublicStudentProfile(
            id = profile.id,
            displayName = profile.displayName,
            bio = profile.bio,
            universityName = student?.universityName,
            degreeProgram = student?.degreeProgram,
            semester = student?.semester,
            avatarUrl = avatarUrl,
            skills = skills
        )
    }

    suspend fun getServicesByStudent(profileId: String): List<Service> {
        val raw = client.from("services").select(
            columns = Columns.list(
                "id", "student_id", "category_id", "title", "short_description",
                "description", "service_status", "modality", "price_type",
                "price_amount", "currency_code", "tags"
            )
        ) {
            filter {
                eq("student_id", profileId)
                eq("service_status", "published")
            }
            order(column = "created_at", order = Order.DESCENDING)
        }.decodeList<ServiceBrowseDto>().map { dto ->
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
        return enrichServicesWithImages(raw)
    }

    suspend fun getCompanyPublicProfile(profileId: String): PublicCompanyProfile? {
        val profile = client.from("profiles").select(
            columns = Columns.list("id", "display_name", "bio", "avatar_asset_id")
        ) {
            filter { eq("id", profileId) }
        }.decodeSingleOrNull<PublicCompanyProfileBaseDto>() ?: return null

        val company = client.from("company_profiles").select(
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
            filter { eq("profile_id", profileId) }
        }.decodeSingleOrNull<PublicCompanyProfileDto>() ?: return null

        val logoUrl = company.logoAssetId?.let {
            runCatching { assetsRepository.getAssetPublicUrl(it) }.getOrNull()
        } ?: profile.avatarAssetId?.let {
            runCatching { assetsRepository.getAssetPublicUrl(it) }.getOrNull()
        } ?: runCatching {
            assetsRepository.findLatestPublicAssetUrl(profile.id, AssetKind.COMPANY_LOGO)
        }.getOrNull() ?: runCatching {
            assetsRepository.findLatestPublicAssetUrl(profile.id, AssetKind.PROFILE_AVATAR)
        }.getOrNull()

        return PublicCompanyProfile(
            id = profile.id,
            displayName = profile.displayName,
            businessName = company.businessName,
            industry = company.industry,
            contactPersonName = company.contactPersonName,
            description = company.description ?: profile.bio,
            websiteUrl = company.websiteUrl,
            logoUrl = logoUrl
        )
    }

    suspend fun getPortfolioByStudent(profileId: String): List<PortfolioItem> {
        val raw = client.from("portfolio_items").select(
            columns = Columns.list(
                "id", "student_id", "item_type", "title", "description", "contribution",
                "project_url", "repository_url", "visibility", "started_on", "completed_on"
            )
        ) {
            filter {
                eq("student_id", profileId)
                eq("visibility", "public")
            }
            order(column = "created_at", order = Order.DESCENDING)
        }.decodeList<PublicPortfolioRowDto>().map { dto ->
            PortfolioItem(
                id = dto.id,
                studentId = dto.studentId,
                itemType = PortfolioItemType.fromBackend(dto.itemType),
                title = dto.title,
                description = dto.description,
                contribution = dto.contribution,
                projectUrl = dto.projectUrl,
                repositoryUrl = dto.repositoryUrl,
                visibility = if (dto.visibility.equals("private", ignoreCase = true)) {
                    VisibilityLevel.PRIVATE
                } else {
                    VisibilityLevel.PUBLIC
                },
                startedOn = dto.startedOn,
                completedOn = dto.completedOn
            )
        }
        return enrichPortfolioWithCovers(raw)
    }

    private suspend fun enrichServicesWithImages(services: List<Service>): List<Service> {
        if (services.isEmpty()) return services
        val servicesRepo = ServicesRepository()
        return services.map { service ->
            val imageUrl = runCatching { servicesRepo.getPrimaryImageUrl(service.id) }.getOrNull()
            service.copy(imageUrl = imageUrl)
        }
    }

    private suspend fun enrichPortfolioWithCovers(items: List<PortfolioItem>): List<PortfolioItem> {
        if (items.isEmpty()) return items
        val portfolioRepo = PortfolioRepository()
        return items.map { item ->
            val cover = runCatching { portfolioRepo.getCoverImageUrl(item.id) }.getOrNull()
            item.copy(coverImageUrl = cover)
        }
    }
}

private fun ServiceModality.backendValue(): String = when (this) {
    ServiceModality.REMOTE -> "remote"
    ServiceModality.HYBRID -> "hybrid"
    ServiceModality.ONSITE -> "onsite"
}

@Serializable
private data class PublicProfileRowDto(
    val id: String,
    @SerialName("display_name")
    val displayName: String,
    val bio: String? = null,
    @SerialName("avatar_asset_id")
    val avatarAssetId: String? = null
)

@Serializable
private data class PublicStudentRowDto(
    @SerialName("profile_id")
    val profileId: String,
    @SerialName("university_name")
    val universityName: String? = null,
    @SerialName("degree_program")
    val degreeProgram: String? = null,
    val semester: Int? = null
)

@Serializable
private data class PublicCompanyProfileBaseDto(
    val id: String,
    @SerialName("display_name")
    val displayName: String,
    val bio: String? = null,
    @SerialName("avatar_asset_id")
    val avatarAssetId: String? = null
)

@Serializable
private data class PublicCompanyProfileDto(
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
private data class PublicProfileSkillRowDto(
    @SerialName("profile_id")
    val profileId: String,
    @SerialName("skill_id")
    val skillId: String,
    val skills: PublicSkillNameDto? = null
)

@Serializable
private data class PublicSkillNameDto(
    val name: String? = null
)

@Serializable
private data class PublicPortfolioRowDto(
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
)

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
