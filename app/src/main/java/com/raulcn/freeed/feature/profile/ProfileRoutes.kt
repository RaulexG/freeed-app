package com.raulcn.freeed.feature.profile

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
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.raulcn.freeed.core.model.UserRole
import com.raulcn.freeed.domain.model.AppUserProfile
import com.raulcn.freeed.feature.system.FeatureInfoCard
import com.raulcn.freeed.feature.system.FreeEdFeatureScaffold

@Composable
fun MyProfileRoute(
    sessionProfile: AppUserProfile?,
    onOpenPortfolioItem: () -> Unit,
    onOpenService: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenMyServices: () -> Unit,
    onSignOut: () -> Unit
) {
    FreeEdFeatureScaffold(
        title = "",
        subtitle = ""
    ) {
        if (sessionProfile == null) {
            FeatureInfoCard(
                eyebrow = "CUENTA",
                title = "Estamos cargando tu perfil",
                body = "Tu sesion ya esta abierta. Espera un momento mientras sincronizamos tu informacion."
            )
            return@FreeEdFeatureScaffold
        }

        ProfileHero(sessionProfile = sessionProfile)
        Spacer(modifier = Modifier.height(18.dp))

        if (sessionProfile.role == UserRole.STUDENT) {
            StudentActionSection(
                onOpenPortfolioItem = onOpenPortfolioItem,
                onOpenService = onOpenService,
                onOpenMyServices = onOpenMyServices
            )
        } else {
            CompanyActionSection(onOpenFavorites = onOpenFavorites)
        }

        Spacer(modifier = Modifier.height(18.dp))
        SectionLabel("Cuenta")
        Spacer(modifier = Modifier.height(12.dp))
        AccountActionRow(
            icon = Icons.Outlined.Settings,
            title = "Configuracion",
            body = "Preferencias, privacidad y datos de tu cuenta.",
            onClick = onOpenSettings
        )
        Spacer(modifier = Modifier.height(10.dp))
        AccountActionRow(
            icon = Icons.Outlined.Logout,
            title = "Cerrar sesion",
            body = "Salir de esta cuenta en el dispositivo actual.",
            onClick = onSignOut,
            isDestructive = true
        )
    }
}

@Composable
fun StudentProfileDetailRoute(
    profileId: String,
    onBackClick: () -> Unit
) {
    FreeEdFeatureScaffold(
        title = "Perfil del estudiante",
        subtitle = "ID: $profileId",
        onBackClick = onBackClick
    ) {
        FeatureInfoCard(
            eyebrow = "DETAIL",
            title = "Perfil publico por ID",
            body = "Aqui conectaremos la vista publica del estudiante usando el ID real del perfil."
        )
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
                    .height(112.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.78f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(78.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = sessionProfile.displayName
                                    .split(" ")
                                    .mapNotNull { it.firstOrNull()?.uppercase() }
                                    .take(2)
                                    .joinToString(""),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = sessionProfile.displayName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
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
                    }
                }
                if (!sessionProfile.bio.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = sessionProfile.bio,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                RoleBadges(sessionProfile = sessionProfile)
            }
        }
    }
}

@Composable
private fun StudentActionSection(
    onOpenPortfolioItem: () -> Unit,
    onOpenService: () -> Unit,
    onOpenMyServices: () -> Unit
) {
    SectionLabel("Tu trabajo")
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionStatCard(
            modifier = Modifier.weight(1f),
            title = "Portafolio",
            body = "Muestra proyectos y evidencias.",
            actionLabel = "Ver",
            onClick = onOpenPortfolioItem
        )
        ActionStatCard(
            modifier = Modifier.weight(1f),
            title = "Servicios",
            body = "Consulta tu oferta publicada.",
            actionLabel = "Abrir",
            onClick = onOpenService
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
    Button(
        onClick = onOpenMyServices,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text("Gestionar mis servicios")
    }
}

@Composable
private fun CompanyActionSection(
    onOpenFavorites: () -> Unit
) {
    SectionLabel("Actividad")
    Spacer(modifier = Modifier.height(12.dp))
    ActionStatCard(
        modifier = Modifier.fillMaxWidth(),
        title = "Favoritos",
        body = "Revisa los perfiles y servicios que guardaste.",
        actionLabel = "Ver favoritos",
        onClick = onOpenFavorites
    )
}

@Composable
private fun ActionStatCard(
    modifier: Modifier = Modifier,
    title: String,
    body: String,
    actionLabel: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
                onClick = onClick,
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(actionLabel)
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
            ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
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
            sessionProfile.universityName?.takeIf { it.isNotBlank() }?.let {
                ProfileChip(text = it)
            }
            sessionProfile.businessName?.takeIf { it.isNotBlank() }?.let {
                ProfileChip(text = it)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            sessionProfile.degreeProgram?.takeIf { it.isNotBlank() }?.let {
                ProfileChip(text = it)
            }
            sessionProfile.industry?.takeIf { it.isNotBlank() }?.let {
                ProfileChip(text = it)
            }
            sessionProfile.semester?.let {
                ProfileChip(text = "${it} semestre")
            }
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
            semester?.let { "${it} semestre" }
        ).takeIf { it.isNotEmpty() }?.joinToString(" · ")

        UserRole.COMPANY -> listOfNotNull(
            industry?.takeIf { it.isNotBlank() },
            contactPersonName?.takeIf { it.isNotBlank() }
        ).takeIf { it.isNotEmpty() }?.joinToString(" · ")

        UserRole.ADMIN -> null
    }
}
