package com.raulcn.freeed.feature.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.raulcn.freeed.R
import com.raulcn.freeed.app.session.AppSessionUiState

@Composable
fun SplashRoute(
    sessionUiState: AppSessionUiState
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.95f)
                    )
                )
            )
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        radius = 950f
                    )
                )
        )

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.freeed_icon),
                contentDescription = "FreeEd",
                modifier = Modifier
                    .fillMaxWidth(0.34f)
                    .height(120.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(20.dp))
            Image(
                painter = painterResource(R.drawable.freeed_logo_banner),
                contentDescription = "FreeEd",
                modifier = Modifier
                    .fillMaxWidth(0.58f)
                    .height(64.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Tu talento. Tu portafolio. Tu primer paso.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.86f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(34.dp))
            if (sessionUiState.isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.height(24.dp)
                    )
                    Text(
                        text = "Preparando tu experiencia",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f)
                    )
                }
            } else if (sessionUiState.errorMessage != null) {
                Surface(
                    color = Color.White.copy(alpha = 0.12f),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text(
                        text = sessionUiState.errorMessage,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp),
            color = Color.White.copy(alpha = 0.08f),
            shape = CircleShape
        ) {
            Text(
                text = "FreeEd v1",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
