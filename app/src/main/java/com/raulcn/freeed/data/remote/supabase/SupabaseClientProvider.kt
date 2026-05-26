package com.raulcn.freeed.data.remote.supabase

import android.content.Intent
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.ExternalAuthAction
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import kotlin.time.Duration.Companion.seconds

object SupabaseClientProvider {

    val client by lazy {
        require(SupabaseConfig.isConfigured) {
            "Supabase URL or publishable key is missing. Add FREEED_SUPABASE_URL and FREEED_SUPABASE_PUBLISHABLE_KEY to local.properties or environment variables."
        }

        createSupabaseClient(
            supabaseUrl = SupabaseConfig.url,
            supabaseKey = SupabaseConfig.publishableKey
        ) {
            install(Auth) {
                host = SupabaseConfig.authHost
                scheme = SupabaseConfig.authScheme
                defaultExternalAuthAction = ExternalAuthAction.CustomTabs()
            }
            install(Postgrest) {
                defaultSchema = "public"
            }
            install(Storage) {
                transferTimeout = 90.seconds
            }
            install(Realtime) {
                reconnectDelay = 5.seconds
            }
        }
    }

    fun handleDeepLinks(intent: Intent?) {
        if (!SupabaseConfig.isConfigured || intent == null) return
        client.handleDeeplinks(intent)
    }
}
