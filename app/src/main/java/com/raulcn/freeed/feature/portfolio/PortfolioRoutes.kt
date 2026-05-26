package com.raulcn.freeed.feature.portfolio

import androidx.compose.runtime.Composable
import com.raulcn.freeed.feature.system.FeatureInfoCard
import com.raulcn.freeed.feature.system.FreeEdFeatureScaffold

@Composable
fun PortfolioItemDetailRoute(
    portfolioItemId: String,
    onBackClick: () -> Unit
) {
    FreeEdFeatureScaffold(
        title = "Portafolio",
        subtitle = "ID: $portfolioItemId",
        onBackClick = onBackClick
    ) {
        FeatureInfoCard(
            eyebrow = "PHASE 8",
            title = "Portfolio detail route already works by ID",
            body = "This screen is ready to become the public evidence layer of the student's professional growth."
        )
    }
}

