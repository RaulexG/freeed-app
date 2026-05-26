package com.raulcn.freeed.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raulcn.freeed.domain.model.AppUserProfile
import com.raulcn.freeed.feature.system.FreeEdFeatureScaffold

@Composable
fun RoleSelectionRoute(
    onStudentSelected: () -> Unit,
    onCompanySelected: () -> Unit,
    onCancel: () -> Unit
) {
    FreeEdFeatureScaffold(
        title = "",
        subtitle = "",
        onBackClick = onCancel
    ) {
        StepIndicator(currentStep = "01", totalSteps = "02")
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Como te describes?",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Selecciona el tipo de cuenta con el que vas a usar FreeEd.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(26.dp))
        RoleOptionCard(
            title = "Estudiante",
            body = "Construye tu perfil, publica servicios y crea un portafolio visible.",
            points = listOf("Perfil profesional", "Servicios publicados", "Portafolio y solicitudes"),
            icon = Icons.Rounded.School,
            onClick = onStudentSelected
        )
        Spacer(modifier = Modifier.height(14.dp))
        RoleOptionCard(
            title = "Empresa o negocio",
            body = "Explora talento universitario y envia solicitudes para proyectos reales.",
            points = listOf("Explorar perfiles", "Guardar favoritos", "Enviar solicitudes"),
            icon = Icons.Rounded.Apartment,
            onClick = onCompanySelected
        )
    }
}

@Composable
fun StudentProfileSetupRoute(
    sessionProfile: AppUserProfile?,
    onComplete: () -> Unit,
    onBackClick: () -> Unit
) {
    val viewModel: StudentProfileSetupViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FreeEdFeatureScaffold(
        title = "Completa tu perfil",
        subtitle = "Paso 2 de 2",
        onBackClick = onBackClick
    ) {
        if (!uiState.isInitialized) {
            viewModel.prefill(sessionProfile)
        }
        Text(
            text = "Lo esencial para empezar a mostrar tu perfil.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(18.dp))
        ProfileFormCard {
            OutlinedTextField(
                value = uiState.displayName,
                onValueChange = viewModel::onDisplayNameChange,
                label = { Text("Nombre profesional") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.universityName,
                onValueChange = viewModel::onUniversityNameChange,
                label = { Text("Universidad") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.degreeProgram,
                onValueChange = viewModel::onDegreeProgramChange,
                label = { Text("Carrera") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.semester,
                onValueChange = viewModel::onSemesterChange,
                label = { Text("Semestre") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.bio,
                onValueChange = viewModel::onBioChange,
                label = { Text("Descripcion profesional") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )
            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = { viewModel.save(onComplete) },
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Guardar y continuar")
                }
            }
        }
    }
}

@Composable
fun CompanyProfileSetupRoute(
    sessionProfile: AppUserProfile?,
    onComplete: () -> Unit,
    onBackClick: () -> Unit
) {
    val viewModel: CompanyProfileSetupViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FreeEdFeatureScaffold(
        title = "Completa tu negocio",
        subtitle = "Paso 2 de 2",
        onBackClick = onBackClick
    ) {
        if (!uiState.isInitialized) {
            viewModel.prefill(sessionProfile)
        }
        Text(
            text = "Comparte la informacion clave para empezar a explorar talento.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(18.dp))
        ProfileFormCard {
            OutlinedTextField(
                value = uiState.displayName,
                onValueChange = viewModel::onDisplayNameChange,
                label = { Text("Nombre visible") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.businessName,
                onValueChange = viewModel::onBusinessNameChange,
                label = { Text("Nombre del negocio") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.industry,
                onValueChange = viewModel::onIndustryChange,
                label = { Text("Giro o industria") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.contactPersonName,
                onValueChange = viewModel::onContactPersonNameChange,
                label = { Text("Responsable") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Descripcion del negocio") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )
            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = { viewModel.save(onComplete) },
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Guardar y continuar")
                }
            }
        }
    }
}

@Composable
private fun RoleOptionCard(
    title: String,
    body: String,
    points: List<String>,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.padding(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            points.forEach { point ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                    Text(
                        text = point,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Continuar")
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileFormCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            content()
        }
    }
}

@Composable
private fun StepIndicator(
    currentStep: String,
    totalSteps: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(999.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 22.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        Text(
            text = "$currentStep / $totalSteps",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
