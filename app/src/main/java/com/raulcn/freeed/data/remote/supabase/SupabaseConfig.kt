package com.raulcn.freeed.data.remote.supabase

import com.raulcn.freeed.BuildConfig

object SupabaseConfig {
    val url: String = BuildConfig.SUPABASE_URL
    val publishableKey: String = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    val authScheme: String = BuildConfig.AUTH_DEEP_LINK_SCHEME
    val authHost: String = BuildConfig.AUTH_DEEP_LINK_HOST

    val isConfigured: Boolean
        get() = url.isNotBlank() && publishableKey.isNotBlank()
}

