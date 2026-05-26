package com.raulcn.freeed.domain.model

import com.raulcn.freeed.core.model.VisibilityLevel

data class PortfolioItem(
    val id: String,
    val studentId: String,
    val title: String,
    val description: String?,
    val projectUrl: String?,
    val repositoryUrl: String?,
    val visibility: VisibilityLevel
)

data class MediaAsset(
    val id: String,
    val ownerId: String,
    val fileName: String,
    val publicUrl: String?,
    val mimeType: String?
)

