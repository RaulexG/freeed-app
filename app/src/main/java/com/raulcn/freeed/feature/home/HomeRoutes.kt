package com.raulcn.freeed.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raulcn.freeed.core.model.ServiceModality
import com.raulcn.freeed.core.model.UserRole
import com.raulcn.freeed.domain.model.AppUserProfile
import com.raulcn.freeed.domain.model.Category
import com.raulcn.freeed.domain.model.Service
import com.raulcn.freeed.feature.system.FeatureInfoCard
import com.raulcn.freeed.feature.system.FreeEdFeatureScaffold

@Composable
fun HomeRoute(
    sessionProfile: AppUserProfile?,
    onExploreMore: () -> Unit,
    onOpenOwnProfile: () -> Unit,
    onOpenService: (String) -> Unit,
    onRequireAuth: () -> Unit
) {
    val viewModel: HomeViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(sessionProfile?.id) {
        viewModel.load(sessionProfile)
    }

    FreeEdFeatureScaffold(
        title = "",
        subtitle = ""
    ) {
        HomeTopBar(
            sessionProfile = sessionProfile,
            onPrimaryAction = if (sessionProfile == null) onRequireAuth else onOpenOwnProfile
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(18.dp))
        }

        if (uiState.errorMessage != null) {
            FeatureInfoCard(
                eyebrow = "SINCRONIZACION",
                title = "No pudimos cargar el inicio",
                body = uiState.errorMessage.orEmpty()
            )
            Spacer(modifier = Modifier.height(18.dp))
        }

        SectionHeader(
            title = "Categorias",
            actionLabel = "Ver todo",
            onActionClick = onExploreMore
        )
        Spacer(modifier = Modifier.height(14.dp))
        if (uiState.categories.isEmpty() && !uiState.isLoading) {
            EmptyInfoCard(
                title = "No hay categorias disponibles",
                body = "Apareceran aqui en cuanto se publiquen en la plataforma."
            )
        } else {
            CategoryRows(
                categories = uiState.categories,
                onCategoryClick = onExploreMore
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        SectionHeader(
            title = if (sessionProfile == null) "Servicios destacados" else "Oportunidades",
            actionLabel = "Explorar",
            onActionClick = onExploreMore
        )
        Spacer(modifier = Modifier.height(14.dp))
        if (uiState.featuredServices.isEmpty() && !uiState.isLoading) {
            EmptyInfoCard(
                title = "Todavia no hay servicios visibles",
                body = "Cuando se publiquen servicios, se mostraran aqui."
            )
        } else {
            uiState.featuredServices.forEach { service ->
                ServicePreviewCard(
                    service = service,
                    onOpen = { onOpenService(service.id) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ExploreRoute(
    sessionProfile: AppUserProfile?,
    onOpenOwnProfile: () -> Unit,
    onOpenService: (String) -> Unit,
    onRequireAuth: () -> Unit
) {
    val viewModel: ExploreViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    FreeEdFeatureScaffold(
        title = "Explorar",
        subtitle = "Servicios publicados"
    ) {
        SearchSurface()
        Spacer(modifier = Modifier.height(20.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (uiState.errorMessage != null) {
            FeatureInfoCard(
                eyebrow = "ERROR",
                title = "No pudimos cargar la exploracion",
                body = uiState.errorMessage.orEmpty()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        SectionHeader(title = "Servicios", actionLabel = null, onActionClick = null)
        Spacer(modifier = Modifier.height(14.dp))

        uiState.services.forEach { service ->
            ServicePreviewCard(
                service = service,
                onOpen = { onOpenService(service.id) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun CreateServiceRoute(
    sessionProfile: AppUserProfile?,
    hasActiveSession: Boolean,
    onOpenEditor: () -> Unit,
    onRequireAuth: () -> Unit
) {
    FreeEdFeatureScaffold(
        title = "Publicar",
        subtitle = "Servicios y oferta profesional"
    ) {
        when {
            !hasActiveSession -> {
                FeatureInfoCard(
                    eyebrow = "ACCESO",
                    title = "Inicia sesion para publicar",
                    body = "Necesitas una cuenta de estudiante para crear servicios.",
                    actionLabel = "Iniciar sesion",
                    onActionClick = onRequireAuth
                )
            }

            sessionProfile == null -> {
                EmptyInfoCard(
                    title = "Estamos preparando tu cuenta",
                    body = "Tu sesion ya esta activa. Espera un momento y vuelve a intentarlo."
                )
            }

            sessionProfile.role == UserRole.COMPANY -> {
                EmptyInfoCard(
                    title = "Las empresas no publican servicios",
                    body = "Tu cuenta esta pensada para explorar talento y enviar solicitudes."
                )
            }

            else -> {
                FeatureInfoCard(
                    eyebrow = "SERVICIOS",
                    title = "Tu propuesta profesional empieza aqui",
                    body = "Desde aqui crearemos tus servicios con categoria, precio, modalidad e imagenes."
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onOpenEditor) {
                    Text("Ir al editor")
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    sessionProfile: AppUserProfile?,
    onPrimaryAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.clickable(onClick = onPrimaryAction),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    if (sessionProfile == null) {
                        Icon(
                            imageVector = Icons.Rounded.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(10.dp)
                        )
                    } else {
                        Text(
                            text = sessionProfile.displayName
                                .split(" ")
                                .mapNotNull { it.firstOrNull()?.uppercase() }
                                .take(2)
                                .joinToString(""),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Column {
                    Text(
                        text = if (sessionProfile == null) {
                            "Bienvenido"
                        } else {
                            "Bienvenido, ${sessionProfile.displayName.substringBefore(" ")}"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (sessionProfile != null) {
            Spacer(modifier = Modifier.width(12.dp))
            OutlinedButton(onClick = onPrimaryAction) {
                Icon(
                    imageVector = Icons.Outlined.BookmarkBorder,
                    contentDescription = "Mi perfil"
                )
            }
        }
    }
}

@Composable
private fun SearchSurface() {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Buscar habilidades, servicios o talento",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String?,
    onActionClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        if (actionLabel != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun ServicePreviewCard(
    service: Service,
    onOpen: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            text = service.modality.label,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    text = service.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                service.shortDescription?.takeIf { it.isNotBlank() }?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Chip(text = service.priceLabel ?: "Precio a convenir")
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(onClick = onOpen) {
                    Text("Ver detalle")
                }
            }
        }
    }
}

@Composable
private fun Chip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(18.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun CategoryRows(
    categories: List<Category>,
    onCategoryClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        categories.forEach { category ->
            OutlinedButton(
                onClick = onCategoryClick,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = category.name,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun EmptyInfoCard(
    title: String,
    body: String
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private val ServiceModality.label: String
    get() = when (this) {
        ServiceModality.REMOTE -> "Remoto"
        ServiceModality.HYBRID -> "Hibrido"
        ServiceModality.ONSITE -> "Presencial"
    }
