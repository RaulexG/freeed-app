package com.raulcn.freeed

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.raulcn.freeed.app.FreeEdApp
import com.raulcn.freeed.data.remote.supabase.SupabaseClientProvider
import com.raulcn.freeed.ui.theme.FreeedTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SupabaseClientProvider.handleDeepLinks(intent)
        enableEdgeToEdge()
        setContent {
            FreeedTheme {
                FreeEdApp()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        SupabaseClientProvider.handleDeepLinks(intent)
    }
}
