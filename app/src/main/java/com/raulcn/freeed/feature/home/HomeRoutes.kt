package com.raulcn.freeed.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
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

        when (sessionProfile?.role) {
            null -> {
                GuestHomeHeader(
                    onOpenAuth = onRequireAuth
                )
                Spacer(modifier = Modifier.height(18.dp))
                SectionHeader(
                    title = "Explora por categoria",
                    actionLabel = null,
                    onActionClick = null
                )
                Spacer(modifier = Modifier.height(12.dp))
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
                    title = "Servicios destacados",
                    actionLabel = "Ver todo",
                    onActionClick = onExploreMore
                )
                Spacer(modifier = Modifier.height(14.dp))
                FeaturedServicesRail(
                    services = uiState.featuredServices,
                    favoriteIds = uiState.favoriteIds,
                    isCompany = uiState.isCompany,
                    onOpenService = onOpenService,
                    onToggleFavorite = viewModel::toggleFavorite
                )
            }

            UserRole.STUDENT -> {
                StudentHomeHeader(
                    sessionProfile = sessionProfile,
                    onOpenProfile = onOpenOwnProfile
                )
                Spacer(modifier = Modifier.height(16.dp))
                StudentSearchCard(onExploreMore = onExploreMore)
                Spacer(modifier = Modifier.height(16.dp))
                StudentStatsRow()
                Spacer(modifier = Modifier.height(16.dp))
                StudentOpportunityCard(onOpenRequests = onExploreMore)
                Spacer(modifier = Modifier.height(20.dp))
                SectionHeader("Categorias", "Ver todo", onExploreMore)
                Spacer(modifier = Modifier.height(12.dp))
                CategoryRows(uiState.categories, onExploreMore)
                Spacer(modifier = Modifier.height(20.dp))
                SectionHeader("Servicios destacados", "Ver todo", onExploreMore)
                Spacer(modifier = Modifier.height(14.dp))
                FeaturedServicesRail(
                    services = uiState.featuredServices,
                    favoriteIds = uiState.favoriteIds,
                    isCompany = false,
                    onOpenService = onOpenService,
                    onToggleFavorite = viewModel::toggleFavorite
                )
            }

            UserRole.COMPANY -> {
                CompanyHomeHeader(
                    sessionProfile = sessionProfile,
                    onOpenProfile = onOpenOwnProfile
                )
                Spacer(modifier = Modifier.height(16.dp))
                CompanySearchCard(onExploreMore = onExploreMore)
                Spacer(modifier = Modifier.height(16.dp))
                CompanyDiscoverCard(onExploreMore = onExploreMore)
                Spacer(modifier = Modifier.height(20.dp))
                SectionHeader("Categorias populares", "Ver todo", onExploreMore)
                Spacer(modifier = Modifier.height(12.dp))
                CategoryRows(uiState.categories, onExploreMore)
                Spacer(modifier = Modifier.height(20.dp))
                SectionHeader("Talento y servicios", "Ver todo", onExploreMore)
                Spacer(modifier = Modifier.height(14.dp))
                FeaturedServicesRail(
                    services = uiState.featuredServices,
                    favoriteIds = uiState.favoriteIds,
                    isCompany = true,
                    onOpenService = onOpenService,
                    onToggleFavorite = viewModel::toggleFavorite
                )
            }

            UserRole.ADMIN -> Unit
        }
    }
}

@Composable
fun ExploreRoute(
    sessionProfile: AppUserProfile?,
    onOpenOwnProfile: () -> Unit,
    onOpenService: (String) -> Unit,
    onOpenStudent: (String) -> Unit,
    onRequireAuth: () -> Unit
) {
    val viewModel: ExploreViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(sessionProfile?.id) {
        viewModel.load(sessionProfile)
    }

    FreeEdFeatureScaffold(
        title = "Explorar",
        subtitle = "Servicios y talento universitario verificado."
    ) {
        ExploreSearchRow(
            value = uiState.query,
            onValueChange = viewModel::onQueryChange,
            isSearching = uiState.isSearching
        )
        Spacer(modifier = Modifier.height(14.dp))

        ExploreToggleHeader()
        Spacer(modifier = Modifier.height(12.dp))
        if (uiState.categories.isNotEmpty()) {
            CategoryFilterRow(
                categories = uiState.categories,
                selectedId = uiState.selectedCategoryId,
                onToggle = viewModel::onCategoryToggle
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        ModalityFilterRow(
            selected = uiState.selectedModality,
            onToggle = viewModel::onModalityToggle
        )

        Spacer(modifier = Modifier.height(10.dp))
        TalentFiltersPanel(
            skillQuery = uiState.skillQuery,
            universityQuery = uiState.universityQuery,
            degreeQuery = uiState.degreeQuery,
            semesterQuery = uiState.semesterQuery,
            onSkillChange = viewModel::onSkillQueryChange,
            onUniversityChange = viewModel::onUniversityQueryChange,
            onDegreeChange = viewModel::onDegreeQueryChange,
            onSemesterChange = viewModel::onSemesterQueryChange
        )

        if (uiState.hasActiveFilters) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = viewModel::clearFilters) {
                Icon(Icons.Outlined.Close, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Limpiar filtros")
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        when {
            uiState.isLoading -> CircularProgressIndicator()
            uiState.errorMessage != null && uiState.services.isEmpty() -> FeatureInfoCard(
                eyebrow = "ERROR",
                title = "No pudimos cargar la exploracion",
                body = uiState.errorMessage.orEmpty()
            )
            uiState.services.isEmpty() -> FeatureInfoCard(
                eyebrow = "BUSQUEDA",
                title = "No hay resultados",
                body = if (uiState.hasActiveFilters) {
                    "Intenta con otra palabra clave o limpia los filtros."
                } else {
                    "Todavia no hay servicios publicados."
                }
            )
            else -> {
                CompactExploreGrid(
                    services = uiState.services,
                    favoriteIds = uiState.favoriteIds,
                    isCompany = uiState.isCompany,
                    onOpenService = onOpenService,
                    onOpenStudent = onOpenStudent,
                    onToggleFavorite = viewModel::toggleFavorite
                )
            }
        }
    }
}

@Composable
private fun TalentFiltersPanel(
    skillQuery: String,
    universityQuery: String,
    degreeQuery: String,
    semesterQuery: String,
    onSkillChange: (String) -> Unit,
    onUniversityChange: (String) -> Unit,
    onDegreeChange: (String) -> Unit,
    onSemesterChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
        Text(if (expanded) "Ocultar filtros de talento" else "Filtros de talento")
    }
    if (!expanded) return

    Spacer(modifier = Modifier.height(10.dp))
    OutlinedTextField(
        value = skillQuery,
        onValueChange = onSkillChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        placeholder = { Text("Skill (ej: Kotlin, Figma)") }
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = universityQuery,
        onValueChange = onUniversityChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        placeholder = { Text("Universidad") }
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = degreeQuery,
        onValueChange = onDegreeChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        placeholder = { Text("Carrera") }
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = semesterQuery,
        onValueChange = onSemesterChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        placeholder = { Text("Semestre (ej: 6)") }
    )
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
                    eyebrow = "CUENTA",
                    title = "Inicia sesion para publicar tus servicios",
                    body = "Crea una oferta clara, agrega una imagen y deja visible tu trabajo para que empresas te encuentren.",
                    actionLabel = "Iniciar sesion",
                    onActionClick = onRequireAuth
                )
                Spacer(modifier = Modifier.height(16.dp))
                CreateInfoCard(
                    title = "Lo que podras mostrar",
                    points = listOf(
                        "Titulo, descripcion y modalidad de trabajo",
                        "Precio fijo o propuesta a convenir",
                        "Imagen principal y evidencia profesional"
                    )
                )
            }

            sessionProfile == null -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Estamos preparando tu espacio de publicacion",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            sessionProfile.role == UserRole.COMPANY -> {
                FeatureInfoCard(
                    eyebrow = "EMPRESA",
                    title = "Tu cuenta esta enfocada en contratar talento",
                    body = "Como empresa puedes explorar perfiles, guardar favoritos y enviar solicitudes a los estudiantes."
                )
            }

            else -> {
                CreateHeroCard(
                    sessionProfile = sessionProfile,
                    onOpenEditor = onOpenEditor
                )
                Spacer(modifier = Modifier.height(16.dp))
                CreateInfoCard(
                    title = "Antes de publicarlo",
                    points = listOf(
                        "Explica entregables y tiempo estimado",
                        "Usa una descripcion directa y concreta",
                        "Agrega una imagen para destacar en explorar"
                    )
                )
            }
        }
    }
}

@Composable
private fun CreateHeroCard(
    sessionProfile: AppUserProfile,
    onOpenEditor: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Convierte tu experiencia en una propuesta clara",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Publica un servicio alineado con ${sessionProfile.degreeProgram ?: "tu perfil profesional"} y deja visible lo que puedes resolver.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onOpenEditor,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Crear servicio")
            }
        }
    }
}

@Composable
private fun CreateInfoCard(
    title: String,
    points: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            points.forEach { point ->
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "•",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = point,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun GuestHomeHeader(
    onOpenAuth: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp)) {
            Text(
                text = "PLATAFORMA ESTUDIANTIL",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Tu carrera empieza antes de egresar.",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Publica servicios, construye portafolio y conecta con negocios que buscan talento universitario.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.84f)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onOpenAuth,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text("Crear cuenta gratis")
                }
                OutlinedButton(
                    onClick = onOpenAuth,
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.24f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Iniciar sesion")
                }
            }
        }
    }
}

@Composable
private fun StudentHomeHeader(
    sessionProfile: AppUserProfile,
    onOpenProfile: () -> Unit
) {
    HomeAccountHeader(
        title = "Buenos dias,",
        name = sessionProfile.displayName.substringBefore(" "),
        avatarText = sessionProfile.displayName,
        onOpenProfile = onOpenProfile
    )
}

@Composable
private fun CompanyHomeHeader(
    sessionProfile: AppUserProfile,
    onOpenProfile: () -> Unit
) {
    HomeAccountHeader(
        title = "Bienvenidos,",
        name = sessionProfile.businessName ?: sessionProfile.displayName,
        avatarText = sessionProfile.businessName ?: sessionProfile.displayName,
        onOpenProfile = onOpenProfile
    )
}

@Composable
private fun HomeAccountHeader(
    title: String,
    name: String,
    avatarText: String,
    onOpenProfile: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
            ) {
                Text(
                    text = avatarText
                        .split(" ")
                        .mapNotNull { it.firstOrNull()?.uppercase() }
                        .take(2)
                        .joinToString(""),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RoundedActionIcon(Icons.Outlined.NotificationsNone)
            RoundedActionIcon(Icons.Outlined.BookmarkBorder, onClick = onOpenProfile)
        }
    }
}

@Composable
private fun SearchShell(
    placeholder: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Outlined.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StudentSearchCard(onExploreMore: () -> Unit) {
    SearchShell(
        placeholder = "Buscar servicios, estudiantes, skills...",
        onClick = onExploreMore
    )
}

@Composable
private fun CompanySearchCard(onExploreMore: () -> Unit) {
    SearchShell(
        placeholder = "Encuentra talento por skill o universidad...",
        onClick = onExploreMore
    )
}

@Composable
private fun ExploreSearchRow(
    value: String,
    onValueChange: (String) -> Unit,
    isSearching: Boolean
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            placeholder = { Text("Servicios, skills, herramientas...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null
                )
            },
            trailingIcon = {
                when {
                    isSearching -> CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.padding(8.dp)
                    )
                    value.isNotBlank() -> IconButton(onClick = { onValueChange("") }) {
                        Icon(Icons.Outlined.Close, contentDescription = "Limpiar")
                    }
                }
            }
        )
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 1.dp
        ) {
            Icon(
                imageVector = Icons.Outlined.Tune,
                contentDescription = null,
                modifier = Modifier.padding(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExploreToggleHeader() {
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Servicios",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Talento",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CategoryFilterRow(
    categories: List<Category>,
    selectedId: String?,
    onToggle: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            FilterChip(
                selected = selectedId == category.id,
                onClick = { onToggle(category.id) },
                label = { Text(category.name) }
            )
        }
    }
}

@Composable
private fun ModalityFilterRow(
    selected: ServiceModality?,
    onToggle: (ServiceModality) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ServiceModality.entries.forEach { modality ->
            FilterChip(
                selected = selected == modality,
                onClick = { onToggle(modality) },
                label = { Text(modality.label) }
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
    onOpen: () -> Unit,
    onOpenAuthor: (() -> Unit)? = null,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null
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
                if (!service.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = service.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                }
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Text(
                            text = service.modality.label,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (onToggleFavorite != null) {
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = if (isFavorite) "Quitar de favoritos" else "Guardar en favoritos",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
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
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onOpen) {
                        Text("Ver detalle")
                    }
                    if (onOpenAuthor != null) {
                        TextButton(onClick = onOpenAuthor) {
                            Text("Ver perfil")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactExploreGrid(
    services: List<Service>,
    favoriteIds: Set<String>,
    isCompany: Boolean,
    onOpenService: (String) -> Unit,
    onOpenStudent: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    val rows = services.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEach { service ->
                    CompactServiceCard(
                        modifier = Modifier.weight(1f),
                        service = service,
                        isFavorite = favoriteIds.contains(service.id),
                        showFavorite = isCompany,
                        onOpen = { onOpenService(service.id) },
                        onOpenAuthor = { onOpenStudent(service.studentId) },
                        onToggleFavorite = { onToggleFavorite(service.id) }
                    )
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun FeaturedServicesRail(
    services: List<Service>,
    favoriteIds: Set<String>,
    isCompany: Boolean,
    onOpenService: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    if (services.isEmpty()) {
        EmptyInfoCard(
            title = "Todavia no hay servicios visibles",
            body = "Cuando se publiquen servicios, se mostraran aqui."
        )
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        services.forEach { service ->
            CompactServiceCard(
                modifier = Modifier.width(210.dp),
                service = service,
                isFavorite = favoriteIds.contains(service.id),
                showFavorite = isCompany,
                onOpen = { onOpenService(service.id) },
                onOpenAuthor = null,
                onToggleFavorite = { onToggleFavorite(service.id) }
            )
        }
    }
}

@Composable
private fun CompactServiceCard(
    modifier: Modifier = Modifier,
    service: Service,
    isFavorite: Boolean,
    showFavorite: Boolean,
    onOpen: () -> Unit,
    onOpenAuthor: (() -> Unit)?,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onOpen),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ) {
                if (!service.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = service.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                }
                Box(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.align(Alignment.BottomStart)
                    ) {
                        Text(
                            text = service.modality.label,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (showFavorite) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .clickable(onClick = onToggleFavorite),
                            color = MaterialTheme.colorScheme.surface,
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                tint = if (isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = service.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                if (!service.shortDescription.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = service.shortDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = service.priceLabel ?: "Precio a convenir",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                if (onOpenAuthor != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onOpenAuthor) {
                        Text("Ver perfil")
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentStatsRow() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            StatCell(number = "4", label = "Servicios", modifier = Modifier.weight(1f))
            StatCell(number = "3", label = "Portafolio", modifier = Modifier.weight(1f))
            StatCell(number = "3", label = "Solicitudes", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun RowScope.StatCell(number: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(number, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StudentOpportunityCard(onOpenRequests: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "NUEVA OPORTUNIDAD",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Cafeteria La Manana solicito tu servicio",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Identidad visual · Hace 2 horas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onOpenRequests,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) { Text("Ver solicitud") }
                OutlinedButton(
                    onClick = onOpenRequests,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.24f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) { Text("Todas") }
            }
        }
    }
}

@Composable
private fun CompanyDiscoverCard(onExploreMore: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "DESCUBRE",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Estudiantes verificados de tu ciudad",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Filtra por skills, semestre y universidad.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onExploreMore,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) { Text("Explorar talento") }
        }
    }
}

@Composable
private fun RoundedActionIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.clickable(enabled = onClick != null) { onClick?.invoke() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.padding(12.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
