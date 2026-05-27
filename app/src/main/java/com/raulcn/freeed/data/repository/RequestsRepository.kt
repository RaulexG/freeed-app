package com.raulcn.freeed.data.repository

import com.raulcn.freeed.core.model.RequestStatus
import com.raulcn.freeed.data.remote.supabase.SupabaseClientProvider
import com.raulcn.freeed.domain.model.ServiceRequest
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

data class NewRequestDraft(
    val serviceId: String,
    val studentId: String,
    val title: String,
    val message: String,
    val proposedBudget: Double?,
    val desiredDeadline: String?
)

class RequestsRepository {

    private val client = SupabaseClientProvider.client

    suspend fun getReceivedRequests(): List<ServiceRequest> {
        val userId = client.auth.currentUserOrNull()?.id ?: return emptyList()
        return client.from("service_requests").select(columns = requestColumns()) {
            filter { eq("student_id", userId) }
            order(column = "created_at", order = Order.DESCENDING)
        }.decodeList<ServiceRequestDto>().map { it.toDomain() }
    }

    suspend fun getSentRequests(): List<ServiceRequest> {
        val userId = client.auth.currentUserOrNull()?.id ?: return emptyList()
        return client.from("service_requests").select(columns = requestColumns()) {
            filter { eq("company_id", userId) }
            order(column = "created_at", order = Order.DESCENDING)
        }.decodeList<ServiceRequestDto>().map { it.toDomain() }
    }

    suspend fun getRequestById(requestId: String): ServiceRequest? {
        return client.from("service_requests").select(columns = requestColumns()) {
            filter { eq("id", requestId) }
        }.decodeSingleOrNull<ServiceRequestDto>()?.toDomain()
    }

    suspend fun createRequest(draft: NewRequestDraft): String {
        val userId = client.auth.currentUserOrNull()?.id
            ?: error("No authenticated user found.")

        val payload = buildJsonObject {
            put("service_id", draft.serviceId)
            put("company_id", userId)
            put("student_id", draft.studentId)
            put("title", draft.title.trim())
            put("message", draft.message.trim())
            putNullableNumber("proposed_budget", draft.proposedBudget)
            putNullableString("desired_deadline", draft.desiredDeadline)
        }

        val inserted = client.from("service_requests")
            .insert(payload) { select(columns = Columns.list("id")) }
            .decodeSingle<InsertedRequestIdDto>()
        return inserted.id
    }

    suspend fun updateStatus(
        requestId: String,
        newStatus: RequestStatus,
        rejectionReason: String? = null,
        cancelledReason: String? = null
    ) {
        val payload = buildJsonObject {
            put("status", newStatus.backendValue)
            when (newStatus) {
                RequestStatus.REJECTED -> putNullableString("rejection_reason", rejectionReason)
                RequestStatus.CANCELLED -> putNullableString("cancelled_reason", cancelledReason)
                else -> {
                    put("rejection_reason", JsonNull)
                    put("cancelled_reason", JsonNull)
                }
            }
        }

        client.from("service_requests").update(payload) {
            filter { eq("id", requestId) }
        }
    }
}

private fun requestColumns(): Columns = Columns.raw(
    "id,service_id,company_id,student_id,title,message,status," +
        "proposed_budget,currency_code,desired_deadline," +
        "rejection_reason,cancelled_reason,created_at," +
        "services!service_requests_service_id_fkey(title)," +
        "profiles!service_requests_company_id_fkey(display_name)"
)

private val RequestStatus.backendValue: String
    get() = when (this) {
        RequestStatus.PENDING -> "pending"
        RequestStatus.ACCEPTED -> "accepted"
        RequestStatus.REJECTED -> "rejected"
        RequestStatus.IN_PROGRESS -> "in_progress"
        RequestStatus.COMPLETED -> "completed"
        RequestStatus.CANCELLED -> "cancelled"
    }

private fun JsonObjectBuilder.putNullableString(key: String, value: String?) {
    val trimmed = value?.trim()
    if (trimmed.isNullOrEmpty()) {
        put(key, JsonNull)
    } else {
        put(key, JsonPrimitive(trimmed))
    }
}

private fun JsonObjectBuilder.putNullableNumber(key: String, value: Double?) {
    if (value == null) {
        put(key, JsonNull)
    } else {
        put(key, JsonPrimitive(value))
    }
}

@kotlinx.serialization.Serializable
private data class InsertedRequestIdDto(val id: String)

@Serializable
private data class ServiceRequestDto(
    val id: String,
    @SerialName("service_id")
    val serviceId: String,
    @SerialName("company_id")
    val companyId: String,
    @SerialName("student_id")
    val studentId: String,
    val title: String,
    val message: String,
    val status: String,
    @SerialName("proposed_budget")
    val proposedBudget: Double? = null,
    @SerialName("currency_code")
    val currencyCode: String? = null,
    @SerialName("desired_deadline")
    val desiredDeadline: String? = null,
    @SerialName("rejection_reason")
    val rejectionReason: String? = null,
    @SerialName("cancelled_reason")
    val cancelledReason: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    val services: JoinedServiceDto? = null,
    val profiles: JoinedProfileDto? = null
) {
    fun toDomain(): ServiceRequest = ServiceRequest(
        id = id,
        serviceId = serviceId,
        companyId = companyId,
        studentId = studentId,
        title = title,
        message = message,
        status = status.toRequestStatus(),
        proposedBudget = proposedBudget,
        currencyCode = currencyCode,
        desiredDeadline = desiredDeadline,
        rejectionReason = rejectionReason,
        cancelledReason = cancelledReason,
        serviceTitle = services?.title,
        companyDisplayName = profiles?.displayName,
        createdAt = createdAt
    )

    private fun String.toRequestStatus(): RequestStatus = when (lowercase()) {
        "accepted" -> RequestStatus.ACCEPTED
        "rejected" -> RequestStatus.REJECTED
        "in_progress" -> RequestStatus.IN_PROGRESS
        "completed" -> RequestStatus.COMPLETED
        "cancelled" -> RequestStatus.CANCELLED
        else -> RequestStatus.PENDING
    }
}

@Serializable
private data class JoinedServiceDto(val title: String? = null)

@Serializable
private data class JoinedProfileDto(
    @SerialName("display_name")
    val displayName: String? = null
)
