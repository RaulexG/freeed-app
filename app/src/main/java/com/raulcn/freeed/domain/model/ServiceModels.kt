package com.raulcn.freeed.domain.model

import com.raulcn.freeed.core.model.ServiceModality
import com.raulcn.freeed.core.model.ServiceStatus

data class Category(
    val id: String,
    val name: String,
    val slug: String,
    val description: String?
)

data class Service(
    val id: String,
    val studentId: String,
    val categoryId: String,
    val title: String,
    val shortDescription: String?,
    val description: String,
    val status: ServiceStatus,
    val modality: ServiceModality,
    val priceLabel: String?,
    val tags: List<String> = emptyList(),
    val imageUrl: String? = null
)
