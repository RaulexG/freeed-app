package com.raulcn.freeed.feature.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.raulcn.freeed.domain.model.Service
import com.raulcn.freeed.feature.system.FeatureInfoCard
import com.raulcn.freeed.feature.system.FreeEdFeatureScaffold
import com.raulcn.freeed.ui.theme.FreeEdGold500

private enum class FavoritesTab(val label: String) {
    TALENT("Talento"),
    SERVICES("Servicios")
}

@Composable
fun FavoritesRoute(
    onBackClick: () -> Unit,
    onOpenService: (String) -> Unit
) {
    val viewModel: FavoritesViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(FavoritesTab.SERVICES) }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    FreeEdFeatureScaffold(
        title = "Guardados",
        subtitle = "Lo que marcaste para revisar despues",
        onBackClick = onBackClick
    ) {
        FavoritesTabs(
            selected = selectedTab,
            onSelected = { selectedTab = it }
        )
        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> CircularProgressIndicator()
            selectedTab == FavoritesTab.TALENT -> FeatureInfoCard(
                eyebrow = "TALENTO",
                title = "Muy pronto veras perfiles guardados",
                body = "Por ahora tus guardados se concentran en servicios destacados."
            )
            uiState.errorMessage != null && uiState.items.isEmpty() -> FeatureInfoCard(
                eyebrow = "ERROR",
                title = "No pudimos cargar tus guardados",
                body = uiState.errorMessage.orEmpty()
            )
            uiState.items.isEmpty() -> FeatureInfoCard(
                eyebrow = "GUARDADOS",
                title = "Aun no guardas servicios",
                body = "Explora talento y toca el corazon para ir construyendo tu lista."
            )
            else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                uiState.items.forEach { service ->
                    SavedServiceCard(
                        service = service,
                        isRemoving = uiState.pendingRemoveId == service.id,
                        onOpen = { onOpenService(service.id) },
                        onRemove = { viewModel.remove(service.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoritesTabs(
    selected: FavoritesTab,
    onSelected: (FavoritesTab) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        FavoritesTab.entries.forEach { tab ->
            val isSelected = tab == selected
            Column {
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                            } else {
                                androidx.compose.ui.graphics.Color.Transparent
                            }
                        )
                        .padding(horizontal = 2.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .fillMaxWidth()
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(999.dp)
                        )
                )
            }
        }
    }
}

@Composable
private fun SavedServiceCard(
    service: Service,
    isRemoving: Boolean,
    onOpen: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ServiceThumb(service = service)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = service.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                service.shortDescription?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PriceChip(service.priceLabel ?: "A convenir")
                    ModalityPill(service.modality.name.lowercase().replaceFirstChar { c -> c.uppercase() })
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onRemove, enabled = !isRemoving) {
                    if (isRemoving) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Favorite,
                            contentDescription = "Quitar de guardados",
                            tint = FreeEdGold500
                        )
                    }
                }
                Surface(
                    onClick = onOpen,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                        contentDescription = "Abrir",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(10.dp).size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceThumb(service: Service) {
    val fallbackBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
        )
    )
    Box(
        modifier = Modifier
            .size(width = 88.dp, height = 96.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(fallbackBrush),
        contentAlignment = Alignment.Center
    ) {
        if (!service.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = service.imageUrl,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = service.title.take(1).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun PriceChip(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ModalityPill(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
