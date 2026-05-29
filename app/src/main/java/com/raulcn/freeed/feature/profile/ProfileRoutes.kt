package com.raulcn.freeed.feature.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raulcn.freeed.core.model.ServiceStatus
import com.raulcn.freeed.core.model.UserRole
import com.raulcn.freeed.domain.model.AppUserProfile
import com.raulcn.freeed.domain.model.PortfolioItem
import com.raulcn.freeed.domain.model.PortfolioItemType
import com.raulcn.freeed.domain.model.PublicCompanyProfile
import com.raulcn.freeed.domain.model.PublicStudentProfile
import com.raulcn.freeed.domain.model.Service
import com.raulcn.freeed.R
import com.raulcn.freeed.feature.system.FeatureInfoCard
import com.raulcn.freeed.feature.system.FreeEdFeatureScaffold

private enum class StudentTab(val label: String) {
    ABOUT("Sobre mi"),
    SERVICES("Servicios"),
    PORTFOLIO("Portafolio")
}

@Composable
fun MyProfileRoute(
    sessionProfile: AppUserProfile?,
    hasActiveSession: Boolean,
    onOpenPortfolioItem: () -> Unit,
    onOpenService: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenMyServices: () -> Unit,
    onOpenServiceDetail: (String) -> Unit,
    onOpenPortfolioItemById: (String) -> Unit,
    onEditProfile: () -> Unit,
    onSignOut: () -> Unit
) {
    val viewModel: MyProfileViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FreeEdFeatureScaffold(
        title = "",
        subtitle = ""
    ) {
        ProfileTopBrandBar()
        Spacer(modifier = Modifier.height(16.dp))
        if (sessionProfile == null) {
            if (hasActiveSession) {
                ProfileLoadingState()
            } else {
                GuestProfileState(
                    onCreateAccount = onEditProfile,
                    onSignIn = onOpenSettings
                )
            }
            return@FreeEdFeatureScaffold
        }

        LaunchedEffect(sessionProfile.id) {
            viewModel.load()
        }

        ProfileHero(sessionProfile = sessionProfile)
        Spacer(modifier = Modifier.height(18.dp))

        if (sessionProfile.role == UserRole.STUDENT) {
            StudentOverviewSection(
                sessionProfile = sessionProfile,
                uiState = uiState,
                onOpenMyServices = onOpenMyServices,
                onOpenMyPortfolio = onOpenPortfolioItem
            )
        } else {
            CompanyOverviewSection(
                sessionProfile = sessionProfile,
                uiState = uiState,
                onOpenFavorites = onOpenFavorites
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
        SectionLabel("Cuenta")
        Spacer(modifier = Modifier.height(12.dp))
        AccountActionRow(
            icon = Icons.Outlined.Edit,
            title = "Editar perfil",
            body = "Actualiza tu informacion profesional y datos visibles.",
            onClick = onEditProfile
        )
        Spacer(modifier = Modifier.height(10.dp))
        AccountActionRow(
            icon = Icons.Outlined.Settings,
            title = "Configuracion",
            body = "Preferencias, privacidad y datos de tu cuenta.",
            onClick = onOpenSettings
        )
        Spacer(modifier = Modifier.height(10.dp))
        AccountActionRow(
            icon = Icons.AutoMirrored.Outlined.Logout,
            title = "Cerrar sesion",
            body = "Salir de esta cuenta en el dispositivo actual.",
            onClick = onSignOut,
            isDestructive = true
        )
    }
}

@Composable
private fun StudentOverviewSection(
    sessionProfile: AppUserProfile,
    uiState: MyProfileTabsUiState,
    onOpenMyServices: () -> Unit,
    onOpenMyPortfolio: () -> Unit
) {
    ProfileStatsRow(
        stats = listOf(
            uiState.services.size.toString() to "Servicios",
            uiState.portfolio.size.toString() to "Portafolio",
            sessionProfile.skills.size.toString() to "Skills"
        )
    )
    Spacer(modifier = Modifier.height(18.dp))
    SectionLabel("Tu actividad")
    Spacer(modifier = Modifier.height(12.dp))
    ActivityListCard(
        items = listOf(
            ActivityEntry(
                icon = Icons.Outlined.WorkOutline,
                title = "Mis servicios",
                subtitle = buildString {
                    append("${uiState.services.count { it.status == ServiceStatus.PUBLISHED }} publicados")
                    val drafts = uiState.services.count { it.status == ServiceStatus.DRAFT }
                    if (drafts > 0) append(" · $drafts borrador")
                },
                onClick = onOpenMyServices
            ),
            ActivityEntry(
                icon = Icons.Outlined.FavoriteBorder,
                title = "Mi portafolio",
                subtitle = "${uiState.portfolio.size} piezas",
                onClick = onOpenMyPortfolio
            ),
            ActivityEntry(
                icon = Icons.Outlined.Edit,
                title = "Sobre mi",
                subtitle = sessionProfile.bio?.takeIf { it.isNotBlank() } ?: "Completa tu presentacion profesional",
                onClick = onOpenMyPortfolio
            )
        )
    )
}

@Composable
private fun CompanyOverviewSection(
    sessionProfile: AppUserProfile,
    uiState: MyProfileTabsUiState,
    onOpenFavorites: () -> Unit
) {
    ProfileStatsRow(
        stats = listOf(
            "1" to "Cuenta",
            sessionProfile.skills.size.toString() to "Intereses",
            uiState.services.size.toString() to "Actividad"
        )
    )
    Spacer(modifier = Modifier.height(18.dp))
    SectionLabel("Tu actividad")
    Spacer(modifier = Modifier.height(12.dp))
    ActivityListCard(
        items = listOf(
            ActivityEntry(
                icon = Icons.Outlined.FavoriteBorder,
                title = "Talento guardado",
                subtitle = "Revisa estudiantes y servicios marcados",
                onClick = onOpenFavorites
            ),
            ActivityEntry(
                icon = Icons.Outlined.WorkOutline,
                title = "Solicitudes enviadas",
                subtitle = "Da seguimiento a oportunidades activas",
                onClick = onOpenFavorites
            ),
            ActivityEntry(
                icon = Icons.Outlined.Settings,
                title = "Perfil del negocio",
                subtitle = sessionProfile.industry ?: "Configura la informacion visible de tu negocio",
                onClick = onOpenFavorites
            )
        )
    )
}

private data class ActivityEntry(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit
)

@Composable
private fun ProfileStatsRow(stats: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            stats.forEachIndexed { index, stat ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stat.first,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stat.second.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (index != stats.lastIndex) {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 14.dp)
                            .size(width = 1.dp, height = 42.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityListCard(items: List<ActivityEntry>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            items.forEachIndexed { index, item ->
                Surface(onClick = item.onClick, color = androidx.compose.ui.graphics.Color.Transparent) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(11.dp).size(18.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                if (index != items.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    )
                }
            }
        }
    }
}

@Composable
fun CompanyProfileDetailRoute(
    profileId: String,
    onBackClick: () -> Unit
) {
    val viewModel: CompanyProfileDetailViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(profileId) {
        viewModel.load(profileId)
    }

    FreeEdFeatureScaffold(
        title = "Perfil de empresa",
        subtitle = "",
        onBackClick = onBackClick
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator()
            uiState.errorMessage != null && uiState.profile == null -> FeatureInfoCard(
                eyebrow = "ERROR",
                title = "No pudimos cargar el perfil",
                body = uiState.errorMessage.orEmpty()
            )
            uiState.profile != null -> CompanyPublicBody(uiState.profile!!)
        }
    }
}

@Composable
private fun CompanyPublicBody(profile: PublicCompanyProfile) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                AvatarBubble(url = profile.logoUrl, fallback = profile.businessName)
                Column(modifier = Modifier.weight(1f)) {
                    Text(profile.businessName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(profile.displayName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            if (!profile.description.isNullOrBlank()) {
                Text(
                    text = profile.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                profile.industry?.takeIf { it.isNotBlank() }?.let { ProfileChip(it) }
                profile.contactPersonName?.takeIf { it.isNotBlank() }?.let { ProfileChip("Contacto: $it") }
            }
            profile.websiteUrl?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(10.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun StudentProfileDetailRoute(
    profileId: String,
    onBackClick: () -> Unit,
    onOpenService: (String) -> Unit,
    onOpenPortfolioItem: (String) -> Unit
) {
    val viewModel: StudentProfileDetailViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(profileId) {
        viewModel.load(profileId)
    }

    FreeEdFeatureScaffold(
        title = "Perfil del estudiante",
        subtitle = "",
        onBackClick = onBackClick
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator()
            uiState.errorMessage != null && uiState.profile == null -> FeatureInfoCard(
                eyebrow = "ERROR",
                title = "No pudimos cargar el perfil",
                body = uiState.errorMessage.orEmpty()
            )
            uiState.profile != null -> PublicProfileBody(
                profile = uiState.profile!!,
                services = uiState.services,
                portfolio = uiState.portfolio,
                onOpenService = onOpenService,
                onOpenPortfolioItem = onOpenPortfolioItem
            )
        }
    }
}

@Composable
private fun PublicProfileBody(
    profile: PublicStudentProfile,
    services: List<Service>,
    portfolio: List<PortfolioItem>,
    onOpenService: (String) -> Unit,
    onOpenPortfolioItem: (String) -> Unit
) {
    PublicProfileHero(profile = profile)
    Spacer(modifier = Modifier.height(18.dp))

    var selectedTab by remember { mutableStateOf(StudentTab.ABOUT) }
    StudentTabRow(selected = selectedTab, onSelected = { selectedTab = it })
    Spacer(modifier = Modifier.height(16.dp))

    when (selectedTab) {
        StudentTab.ABOUT -> AboutPublicPanel(profile = profile)
        StudentTab.SERVICES -> ServicesPanel(
            services = services.filter { it.status == ServiceStatus.PUBLISHED },
            emptyTitle = "Sin servicios publicados",
            emptyBody = "Este estudiante aun no publica servicios.",
            onOpenService = onOpenService
        )
        StudentTab.PORTFOLIO -> PortfolioPanel(
            items = portfolio,
            emptyTitle = "Portafolio vacio",
            emptyBody = "Este estudiante aun no comparte proyectos.",
            onOpenItem = onOpenPortfolioItem
        )
    }
}

@Composable
private fun StudentTabsSection(
    sessionProfile: AppUserProfile,
    onOpenMyServices: () -> Unit,
    onOpenMyPortfolio: () -> Unit,
    onOpenService: (String) -> Unit,
    onOpenPortfolioItem: (String) -> Unit
) {
    val viewModel: MyProfileViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(sessionProfile.id) {
        viewModel.load()
    }

    var selectedTab by remember { mutableStateOf(StudentTab.ABOUT) }
    StudentTabRow(selected = selectedTab, onSelected = { selectedTab = it })
    Spacer(modifier = Modifier.height(16.dp))

    when (selectedTab) {
        StudentTab.ABOUT -> AboutOwnPanel(
            sessionProfile = sessionProfile,
            onOpenMyServices = onOpenMyServices,
            onOpenMyPortfolio = onOpenMyPortfolio
        )
        StudentTab.SERVICES -> {
            if (uiState.isLoadingServices) {
                CircularProgressIndicator()
            } else {
                ServicesPanel(
                    services = uiState.services,
                    emptyTitle = "Aun no tienes servicios",
                    emptyBody = "Publica tu primer servicio para que las empresas puedan encontrarte.",
                    onOpenService = onOpenService,
                    showStatus = true
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(
                    onClick = onOpenMyServices,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Gestionar mis servicios")
                }
            }
        }
        StudentTab.PORTFOLIO -> {
            if (uiState.isLoadingPortfolio) {
                CircularProgressIndicator()
            } else {
                PortfolioPanel(
                    items = uiState.portfolio,
                    emptyTitle = "Tu portafolio esta vacio",
                    emptyBody = "Agrega proyectos, practicas o certificaciones para mostrar tu trabajo.",
                    onOpenItem = onOpenPortfolioItem
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(
                    onClick = onOpenMyPortfolio,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Gestionar mi portafolio")
                }
            }
        }
    }
}

@Composable
private fun StudentTabRow(
    selected: StudentTab,
    onSelected: (StudentTab) -> Unit
) {
    TabRow(selectedTabIndex = selected.ordinal) {
        StudentTab.entries.forEach { tab ->
            Tab(
                selected = selected == tab,
                onClick = { onSelected(tab) },
                text = { Text(tab.label) }
            )
        }
    }
}

@Composable
private fun AboutPublicPanel(profile: PublicStudentProfile) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!profile.bio.isNullOrBlank()) {
            Text(
                text = profile.bio,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            FeatureInfoCard(
                eyebrow = "SOBRE MI",
                title = "Este estudiante aun no comparte una bio",
                body = "Mostrara aqui su perfil profesional cuando lo complete."
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            profile.universityName?.takeIf { it.isNotBlank() }?.let { ProfileChip(it) }
            profile.degreeProgram?.takeIf { it.isNotBlank() }?.let { ProfileChip(it) }
            profile.semester?.let { ProfileChip("$it semestre") }
        }
        if (profile.skills.isNotEmpty()) {
            Text(
                text = "Skills",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            SkillChips(profile.skills)
        }
    }
}

@Composable
private fun AboutOwnPanel(
    sessionProfile: AppUserProfile,
    onOpenMyServices: () -> Unit,
    onOpenMyPortfolio: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (!sessionProfile.bio.isNullOrBlank()) {
            Text(
                text = sessionProfile.bio,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            FeatureInfoCard(
                eyebrow = "SOBRE MI",
                title = "Cuenta quien eres",
                body = "Una bio breve ayuda a empresas y reclutadores a entender que ofreces."
            )
        }
        RoleBadges(sessionProfile = sessionProfile)
        if (sessionProfile.skills.isNotEmpty()) {
            Text(
                text = "Skills",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            SkillChips(sessionProfile.skills)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onOpenMyServices,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp)
            ) { Text("Servicios") }
            OutlinedButton(
                onClick = onOpenMyPortfolio,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp)
            ) { Text("Portafolio") }
        }
    }
}

@Composable
private fun SkillChips(skills: List<String>) {
    val rows = buildSkillRows(skills)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { skill ->
                    ProfileChip(skill)
                }
            }
        }
    }
}

private fun buildSkillRows(skills: List<String>, maxScorePerRow: Int = 30): List<List<String>> {
    if (skills.isEmpty()) return emptyList()
    val rows = mutableListOf<MutableList<String>>()
    var currentRow = mutableListOf<String>()
    var currentScore = 0
    skills.forEach { skill ->
        val score = (skill.length + 4).coerceAtMost(maxScorePerRow)
        if (currentRow.isNotEmpty() && currentScore + score > maxScorePerRow) {
            rows.add(currentRow)
            currentRow = mutableListOf()
            currentScore = 0
        }
        currentRow.add(skill)
        currentScore += score
    }
    if (currentRow.isNotEmpty()) rows.add(currentRow)
    return rows
}

@Composable
private fun ServicesPanel(
    services: List<Service>,
    emptyTitle: String,
    emptyBody: String,
    onOpenService: (String) -> Unit,
    showStatus: Boolean = false
) {
    if (services.isEmpty()) {
        FeatureInfoCard(eyebrow = "SERVICIOS", title = emptyTitle, body = emptyBody)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        services.forEach { service ->
            CompactServiceRow(
                service = service,
                showStatus = showStatus,
                onClick = { onOpenService(service.id) }
            )
        }
    }
}

@Composable
private fun PortfolioPanel(
    items: List<PortfolioItem>,
    emptyTitle: String,
    emptyBody: String,
    onOpenItem: (String) -> Unit
) {
    if (items.isEmpty()) {
        FeatureInfoCard(eyebrow = "PORTAFOLIO", title = emptyTitle, body = emptyBody)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.forEach { item ->
            CompactPortfolioRow(item = item, onClick = { onOpenItem(item.id) })
        }
    }
}

@Composable
private fun CompactServiceRow(
    service: Service,
    showStatus: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (!service.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = service.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = service.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    service.shortDescription?.takeIf { it.isNotBlank() }?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (showStatus) ServiceStatusBadge(status = service.status)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = service.priceLabel ?: "Precio a convenir",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedButton(onClick = onClick) { Text("Ver") }
            }
        }
    }
}

@Composable
private fun ServiceStatusBadge(status: ServiceStatus) {
    val (label, color) = when (status) {
        ServiceStatus.DRAFT -> "Borrador" to MaterialTheme.colorScheme.outline
        ServiceStatus.PUBLISHED -> "Publicado" to MaterialTheme.colorScheme.primary
        ServiceStatus.PAUSED -> "Pausado" to MaterialTheme.colorScheme.secondary
        ServiceStatus.ARCHIVED -> "Archivado" to MaterialTheme.colorScheme.error
    }
    Surface(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(999.dp)) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CompactPortfolioRow(
    item: PortfolioItem,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (!item.coverImageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.coverImageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    text = item.itemType.label,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (!item.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description.take(120),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(onClick = onClick) { Text("Ver detalle") }
        }
    }
}

@Composable
private fun PublicProfileHero(profile: PublicStudentProfile) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(30.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.76f)
                            )
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(999.dp)
                        )
                        .background(
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(999.dp)
                        )
                ) {
                    Text(
                        text = "Perfil profesional",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AvatarBubble(url = profile.avatarUrl, fallback = profile.displayName)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    profile.degreeProgram?.takeIf { it.isNotBlank() }?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    profile.universityName?.takeIf { it.isNotBlank() }?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        profile.semester?.let { ProfileChip("$it semestre") }
                        if (profile.skills.isNotEmpty()) {
                            ProfileChip("${profile.skills.size} skills")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuestProfileState(
    onCreateAccount: () -> Unit,
    onSignIn: () -> Unit
) {
    SectionLabel("Perfil")
    Spacer(modifier = Modifier.height(16.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Unete a FreeEd",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Construye tu portafolio, recibe solicitudes y haz crecer tu experiencia.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onCreateAccount,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Crear cuenta")
            }
            TextButton(onClick = onSignIn) {
                Text("Iniciar sesion")
            }
        }
    }
}

@Composable
private fun ProfileHero(sessionProfile: AppUserProfile) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(30.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.78f)
                            )
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(999.dp)
                        )
                        .background(
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(999.dp)
                        )
                ) {
                    Text(
                        text = if (sessionProfile.role == UserRole.STUDENT) "Mi perfil" else "Perfil de empresa",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val profileImageUrl = if (sessionProfile.role == UserRole.COMPANY) {
                    sessionProfile.companyLogoUrl ?: sessionProfile.avatarUrl
                } else {
                    sessionProfile.avatarUrl
                }
                AvatarBubble(url = profileImageUrl, fallback = sessionProfile.displayName)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sessionProfile.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = sessionProfile.roleTitle(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    sessionProfile.contextLine()?.let { line ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    RoleBadges(sessionProfile)
                }
            }
        }
    }
}

@Composable
private fun ProfileLoadingState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircularProgressIndicator(strokeWidth = 2.5.dp)
            Text(
                text = "Estamos preparando tu perfil",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Tu sesion ya esta abierta. En un momento cargaremos tu informacion profesional.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            GuestSettingsRow(title = "Idioma", subtitle = "Espanol")
            DividerLine()
            GuestSettingsRow(title = "Privacidad", subtitle = "Tu informacion y seguridad")
            DividerLine()
            GuestSettingsRow(title = "Contacto", subtitle = "Ayuda y soporte")
        }
    }
}

@Composable
private fun ProfileTopBrandBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = painterResource(R.drawable.freeed_logo_banner),
            contentDescription = "FreeEd",
            modifier = Modifier
                .size(width = 96.dp, height = 34.dp),
            contentScale = ContentScale.Fit
        )
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(10.dp).size(18.dp)
            )
        }
    }
}

@Composable
private fun GuestSettingsRow(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(10.dp).size(12.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    )
}

@Composable
private fun AvatarBubble(url: String?, fallback: String) {
    Surface(
        modifier = Modifier.size(72.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    ) {
        if (!url.isNullOrBlank()) {
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = fallback
                        .split(" ")
                        .mapNotNull { it.firstOrNull()?.uppercase() }
                        .take(2)
                        .joinToString(""),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CompanyActionSection(onOpenFavorites: () -> Unit) {
    SectionLabel("Actividad")
    Spacer(modifier = Modifier.height(12.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Favoritos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Revisa los perfiles y servicios que guardaste.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
                onClick = onOpenFavorites,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.FavoriteBorder, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Ver favoritos")
            }
        }
    }
}

@Composable
private fun AccountActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = if (isDestructive) {
            ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        } else {
            ButtonDefaults.outlinedButtonColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isDestructive) {
                    MaterialTheme.colorScheme.error.copy(alpha = 0.10f)
                } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RoleBadges(sessionProfile: AppUserProfile) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProfileChip(text = if (sessionProfile.role == UserRole.STUDENT) "Estudiante" else "Empresa")
            sessionProfile.universityName?.takeIf { it.isNotBlank() }?.let { ProfileChip(it) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            sessionProfile.degreeProgram?.takeIf { it.isNotBlank() }?.let { ProfileChip(it) }
            sessionProfile.semester?.let { ProfileChip("$it semestre") }
            sessionProfile.industry?.takeIf { it.isNotBlank() }?.let { ProfileChip(it) }
        }
    }
}

@Composable
private fun ProfileChip(text: String) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold
    )
}

private fun AppUserProfile.roleTitle(): String = when (role) {
    UserRole.STUDENT -> degreeProgram ?: "Perfil estudiantil"
    UserRole.COMPANY -> businessName ?: "Perfil empresarial"
    UserRole.ADMIN -> "Administrador"
}

private fun AppUserProfile.contextLine(): String? {
    return when (role) {
        UserRole.STUDENT -> listOfNotNull(
            universityName?.takeIf { it.isNotBlank() },
            semester?.let { "$it semestre" }
        ).takeIf { it.isNotEmpty() }?.joinToString(" - ")

        UserRole.COMPANY -> listOfNotNull(
            industry?.takeIf { it.isNotBlank() },
            contactPersonName?.takeIf { it.isNotBlank() }
        ).takeIf { it.isNotEmpty() }?.joinToString(" - ")

        UserRole.ADMIN -> null
    }
}

private val PortfolioItemType.label: String
    get() = when (this) {
        PortfolioItemType.PROJECT -> "Proyecto"
        PortfolioItemType.FREELANCE_WORK -> "Freelance"
        PortfolioItemType.INTERNSHIP -> "Practica"
        PortfolioItemType.VOLUNTEERING -> "Voluntariado"
        PortfolioItemType.COMPETITION -> "Competencia"
        PortfolioItemType.CERTIFICATION -> "Certificacion"
        PortfolioItemType.OTHER -> "Otro"
    }
