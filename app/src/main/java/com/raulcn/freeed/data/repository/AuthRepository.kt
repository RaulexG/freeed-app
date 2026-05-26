package com.raulcn.freeed.data.repository

import com.raulcn.freeed.core.model.UserRole
import com.raulcn.freeed.data.remote.supabase.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository {

    private val client = SupabaseClientProvider.client

    val sessionStatus: StateFlow<SessionStatus> = client.auth.sessionStatus

    suspend fun restoreSession() {
        client.auth.loadFromStorage()
        client.auth.awaitInitialization()
    }

    suspend fun signIn(
        email: String,
        password: String
    ) {
        client.auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }
    }

    suspend fun signUp(
        displayName: String,
        email: String,
        password: String,
        role: UserRole
    ): Boolean {
        client.auth.signUpWith(Email) {
            this.email = email.trim()
            this.password = password
            data = buildJsonObject {
                put("role", role.backendValue)
                put("display_name", displayName.trim())
            }
        }
        return client.auth.currentSessionOrNull() != null
    }

    suspend fun signOut() {
        client.auth.signOut()
    }
}

private val UserRole.backendValue: String
    get() = when (this) {
        UserRole.STUDENT -> "student"
        UserRole.COMPANY -> "company"
        UserRole.ADMIN -> "admin"
    }
