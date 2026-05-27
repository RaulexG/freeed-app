package com.raulcn.freeed.domain.model

import com.raulcn.freeed.core.model.VisibilityLevel

enum class PortfolioItemType(val backendValue: String) {
    PROJECT("project"),
    FREELANCE_WORK("freelance_work"),
    INTERNSHIP("internship"),
    VOLUNTEERING("volunteering"),
    COMPETITION("competition"),
    CERTIFICATION("certification"),
    OTHER("other");

    companion object {
        fun fromBackend(value: String?): PortfolioItemType = entries.firstOrNull {
            it.backendValue.equals(value, ignoreCase = true)
        } ?: PROJECT
    }
}

data class PortfolioItem(
    val id: String,
    val studentId: String,
    val itemType: PortfolioItemType,
    val title: String,
    val description: String?,
    val contribution: String?,
    val projectUrl: String?,
    val repositoryUrl: String?,
    val visibility: VisibilityLevel,
    val startedOn: String? = null,
    val completedOn: String? = null,
    val coverImageUrl: String? = null
)

data class MediaAsset(
    val id: String,
    val ownerId: String,
    val fileName: String,
    val publicUrl: String?,
    val mimeType: String?
)
