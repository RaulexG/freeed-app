package com.raulcn.freeed.core.model

enum class UserRole {
    STUDENT,
    COMPANY,
    ADMIN
}

enum class ProfileStatus {
    ONBOARDING,
    ACTIVE,
    SUSPENDED,
    ARCHIVED
}

enum class VisibilityLevel {
    PUBLIC,
    PRIVATE
}

enum class ServiceStatus {
    DRAFT,
    PUBLISHED,
    PAUSED,
    ARCHIVED
}

enum class ServiceModality {
    REMOTE,
    HYBRID,
    ONSITE
}

enum class RequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

