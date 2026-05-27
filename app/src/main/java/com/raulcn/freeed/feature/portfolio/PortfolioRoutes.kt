package com.raulcn.freeed.feature.portfolio

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.raulcn.freeed.core.model.VisibilityLevel
import com.raulcn.freeed.domain.model.PortfolioItem
import com.raulcn.freeed.domain.model.PortfolioItemType
import com.raulcn.freeed.feature.system.FeatureInfoCard
import com.raulcn.freeed.feature.system.FreeEdFeatureScaffold
import com.raulcn.freeed.feature.system.ImagePickerField

@Composable
fun MyPortfolioRoute(
    onBackClick: () -> Unit,
    onCreateNew: () -> Unit,
    onOpenItem: (String) -> Unit,
    onEditItem: (String) -> Unit
) {
    val viewModel: MyPortfolioViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    FreeEdFeatureScaffold(
        title = "Mi portafolio",
        subtitle = "Tu evidencia profesional",
        onBackClick = onBackClick
    ) {
        Button(
            onClick = onCreateNew,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text("Agregar elemento")
        }
        Spacer(modifier = Modifier.height(18.dp))

        when {
            uiState.isLoading -> CircularProgressIndicator()
            uiState.errorMessage != null -> FeatureInfoCard(
                eyebrow = "ERROR",
                title = "No pudimos cargar tu portafolio",
                body = uiState.errorMessage.orEmpty()
            )
            uiState.items.isEmpty() -> FeatureInfoCard(
                eyebrow = "EMPIEZA",
                title = "Tu portafolio esta vacio",
                body = "Suma proyectos, practicas, certificaciones o cualquier evidencia de tu trabajo."
            )
            else -> uiState.items.forEach { item ->
                PortfolioItemCard(
                    item = item,
                    isDeleting = uiState.pendingDeleteId == item.id,
                    onOpen = { onOpenItem(item.id) },
                    onEdit = { onEditItem(item.id) },
                    onDelete = { viewModel.delete(item.id) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun PortfolioEditorRoute(
    portfolioItemId: String,
    onBackClick: () -> Unit
) {
    val viewModel: PortfolioEditorViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contentResolver = LocalContext.current.contentResolver

    LaunchedEffect(portfolioItemId) {
        viewModel.initialize(portfolioItemId)
    }

    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) onBackClick()
    }

    FreeEdFeatureScaffold(
        title = if (uiState.isEditing) "Editar elemento" else "Nuevo elemento",
        subtitle = "Suma una evidencia a tu portafolio",
        onBackClick = onBackClick
    ) {
        if (uiState.isLoadingItem) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (uiState.errorMessage != null) {
            FeatureInfoCard(
                eyebrow = "REVISA",
                title = "Antes de guardar",
                body = uiState.errorMessage.orEmpty()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        ImagePickerField(
            label = "Portada",
            helperText = "Opcional. Imagen representativa del proyecto.",
            pickedUri = uiState.pickedCoverUri,
            currentImageUrl = uiState.currentCoverUrl,
            onPicked = viewModel::onCoverPicked
        )
        Spacer(modifier = Modifier.height(14.dp))

        InputLabel("Tipo")
        ItemTypeRow(
            selected = uiState.itemType,
            onSelected = viewModel::onTypeChange
        )
        Spacer(modifier = Modifier.height(14.dp))

        InputLabel("Titulo")
        OutlinedTextField(
            value = uiState.title,
            onValueChange = viewModel::onTitleChange,
            placeholder = { Text("Por ejemplo: App de finanzas personales") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))

        InputLabel("Descripcion")
        OutlinedTextField(
            value = uiState.description,
            onValueChange = viewModel::onDescriptionChange,
            placeholder = { Text("Que es, problema que resuelve, tecnologias usadas.") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))

        InputLabel("Tu rol o contribucion")
        OutlinedTextField(
            value = uiState.contribution,
            onValueChange = viewModel::onContributionChange,
            placeholder = { Text("Lo que tu hiciste en este proyecto") },
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))

        InputLabel("Enlace al proyecto (opcional)")
        OutlinedTextField(
            value = uiState.projectUrl,
            onValueChange = viewModel::onProjectUrlChange,
            placeholder = { Text("https://...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
        )
        Spacer(modifier = Modifier.height(14.dp))

        InputLabel("Repositorio (opcional)")
        OutlinedTextField(
            value = uiState.repositoryUrl,
            onValueChange = viewModel::onRepositoryUrlChange,
            placeholder = { Text("https://github.com/...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
        )
        Spacer(modifier = Modifier.height(14.dp))

        InputLabel("Visibilidad")
        VisibilityRow(
            selected = uiState.visibility,
            onSelected = viewModel::onVisibilityChange
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.save(contentResolver, onSaved = onBackClick) },
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(if (uiState.isEditing) "Guardar cambios" else "Agregar al portafolio")
            }
        }
    }
}

@Composable
fun PortfolioItemDetailRoute(
    portfolioItemId: String,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit
) {
    val viewModel: PortfolioDetailViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(portfolioItemId) {
        viewModel.load(portfolioItemId)
    }

    FreeEdFeatureScaffold(
        title = "Portafolio",
        subtitle = "",
        onBackClick = onBackClick
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator()
            uiState.errorMessage != null -> FeatureInfoCard(
                eyebrow = "ERROR",
                title = "No pudimos cargar el elemento",
                body = uiState.errorMessage.orEmpty()
            )
            uiState.item != null -> {
                val item = uiState.item!!
                uiState.coverUrl?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
                TypeBadge(type = item.itemType)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                if (!item.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (!item.contribution.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    SectionLabel("Mi rol")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.contribution,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                LinkRow(label = "Proyecto", url = item.projectUrl)
                LinkRow(label = "Repositorio", url = item.repositoryUrl)
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedButton(onClick = { onEditClick(item.id) }) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Editar")
                }
            }
        }
    }
}

@Composable
private fun PortfolioItemCard(
    item: PortfolioItem,
    isDeleting: Boolean,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            if (!item.coverImageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.coverImageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    TypeBadge(type = item.itemType)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!item.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.description.take(140),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                VisibilityBadge(visibility = item.visibility)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onOpen) {
                    Text("Ver")
                }
                OutlinedButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Editar")
                }
                OutlinedButton(onClick = onDelete, enabled = !isDeleting) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Eliminar")
                }
                if (isDeleting) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TypeBadge(type: PortfolioItemType) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = type.label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun VisibilityBadge(visibility: VisibilityLevel) {
    val (label, color) = when (visibility) {
        VisibilityLevel.PUBLIC -> "Publico" to MaterialTheme.colorScheme.primary
        VisibilityLevel.PRIVATE -> "Privado" to MaterialTheme.colorScheme.outline
    }
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(999.dp)
    ) {
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
private fun ItemTypeRow(
    selected: PortfolioItemType,
    onSelected: (PortfolioItemType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PortfolioItemType.entries.forEach { type ->
            FilterChip(
                selected = type == selected,
                onClick = { onSelected(type) },
                label = { Text(type.label) }
            )
        }
    }
}

@Composable
private fun VisibilityRow(
    selected: VisibilityLevel,
    onSelected: (VisibilityLevel) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        VisibilityLevel.entries.forEach { value ->
            FilterChip(
                selected = value == selected,
                onClick = { onSelected(value) },
                label = { Text(value.label) }
            )
        }
    }
}

@Composable
private fun LinkRow(label: String, url: String?) {
    if (url.isNullOrBlank()) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Link,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = url,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun InputLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.secondary
    )
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

private val VisibilityLevel.label: String
    get() = when (this) {
        VisibilityLevel.PUBLIC -> "Publico"
        VisibilityLevel.PRIVATE -> "Privado"
    }
