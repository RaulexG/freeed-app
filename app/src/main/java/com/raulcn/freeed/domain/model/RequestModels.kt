package com.raulcn.freeed.domain.model

import com.raulcn.freeed.core.model.RequestStatus

data class ServiceRequest(
    val id: String,
    val serviceId: String,
    val companyId: String,
    val studentId: String,
    val title: String,
    val message: String,
    val status: RequestStatus,
    val proposedBudget: String?,
    val desiredDeadline: String?
)

