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
    val proposedBudget: Double?,
    val currencyCode: String?,
    val desiredDeadline: String?,
    val rejectionReason: String? = null,
    val cancelledReason: String? = null,
    val serviceTitle: String? = null,
    val companyDisplayName: String? = null,
    val createdAt: String? = null
) {
    val budgetLabel: String?
        get() {
            val amount = proposedBudget ?: return null
            val currency = currencyCode ?: "MXN"
            return "$currency ${formatMoney(amount)}"
        }

    private fun formatMoney(amount: Double): String = if (amount % 1.0 == 0.0) {
        amount.toInt().toString()
    } else {
        String.format("%.2f", amount)
    }
}
