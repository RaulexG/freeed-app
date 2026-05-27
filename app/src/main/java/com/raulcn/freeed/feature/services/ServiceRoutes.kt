package com.raulcn.freeed.feature.services

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.raulcn.freeed.core.model.ServiceModality
import com.raulcn.freeed.core.model.ServiceStatus
import com.raulcn.freeed.data.repository.ServicePriceType
import com.raulcn.freeed.domain.model.AppUserProfile
import com.raulcn.freeed.domain.model.Category
import com.raulcn.freeed.domain.model.Service
import com.raulcn.freeed.feature.system.FeatureInfoCard
import com.raulcn.freeed.feature.system.FreeEdFeatureScaffold
import com.raulcn.freeed.feature.system.ImagePickerField

@Composable
fun ServiceDetailRoute(
    serviceId: String,
    sessionProfile: AppUserProfile?,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onRequestClick: (String) -> Unit,
    onRequireAuth: () -> Unit
) {
    val viewModel: ServiceDetailViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(serviceId, sessionProfile?.id) {
        viewModel.load(serviceId, sessionProfile)
    }

    FreeEdFeatureScaffold(
        title = "Servicio",
        subtitle = "",
        onBackClick = onBackClick,
        actions = if (uiState.isCompany) {
            {
                IconButton(onClick = viewModel::toggleFavorite) {
                    Icon(
                        imageVector = if (uiState.isFavorite) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (uiState.isFavorite) "Quitar de favoritos" else "Guardar en favoritos",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else null
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator()
            uiState.errorMessage != null -> FeatureInfoCard(
                eyebrow = "SERVICIO",
                title = "No pudimos cargar este servicio",
                body = uiState.errorMessage ?: ""
            )
            uiState.service != null -> {
                val service = uiState.service!!
                uiState.imageUrl?.let { url ->
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
                FeatureInfoCard(
                    eyebrow = service.modality.label.uppercase(),
                    title = service.title,
                    body = service.description
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = service.priceLabel ?: "Precio a convenir",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                val isOwner = sessionProfile?.id == service.studentId
                val isCompany = sessionProfile?.role == com.raulcn.freeed.core.model.UserRole.COMPANY
                when {
                    sessionProfile == null -> Button(onClick = onRequireAuth) {
                        Text("Inicia sesion para contactar")
                    }
                    isOwner -> OutlinedButton(onClick = { onEditClick(service.id) }) {
                        Text("Editar servicio")
                    }
                    isCompany -> Button(onClick = { onRequestClick(service.id) }) {
                        Text("Solicitar este servicio")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceEditorRoute(
    serviceId: String,
    onBackClick: () -> Unit
) {
    val viewModel: ServiceEditorViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contentResolver = LocalContext.current.contentResolver

    LaunchedEffect(serviceId) {
        viewModel.initialize(serviceId)
    }

    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) onBackClick()
    }

    FreeEdFeatureScaffold(
        title = if (uiState.isEditing) "Editar servicio" else "Nuevo servicio",
        subtitle = "Publica tu propuesta profesional",
        onBackClick = onBackClick
    ) {
        if (uiState.isLoadingService || uiState.isLoadingCategories) {
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
            label = "Imagen del servicio",
            helperText = "Opcional. Una imagen ayuda a que tu servicio destaque.",
            pickedUri = uiState.pickedImageUri,
            currentImageUrl = uiState.currentImageUrl,
            onPicked = viewModel::onImagePicked
        )
        Spacer(modifier = Modifier.height(14.dp))

        InputLabel("Titulo")
        OutlinedTextField(
            value = uiState.title,
            onValueChange = viewModel::onTitleChange,
            placeholder = { Text("Por ejemplo: Diseno de logotipo profesional") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))

        InputLabel("Categoria")
        CategoryDropdown(
            categories = uiState.categories,
            selected = uiState.selectedCategory,
            onSelected = viewModel::onCategorySelected
        )
        Spacer(modifier = Modifier.height(14.dp))

        InputLabel("Resumen (opcional, max 180)")
        OutlinedTextField(
            value = uiState.shortDescription,
            onValueChange = viewModel::onShortDescriptionChange,
            placeholder = { Text("Una linea que enganche") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))

        InputLabel("Descripcion")
        OutlinedTextField(
            value = uiState.description,
            onValueChange = viewModel::onDescriptionChange,
            placeholder = { Text("Explica que ofreces, entregables, tiempos.") },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))

        InputLabel("Modalidad")
        ModalityRow(
            selected = uiState.modality,
            onSelected = viewModel::onModalityChange
        )
        Spacer(modifier = Modifier.height(14.dp))

        InputLabel("Precio")
        PriceTypeRow(
            selected = uiState.priceType,
            onSelected = viewModel::onPriceTypeChange
        )
        if (uiState.requiresPriceAmount) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = uiState.priceAmount,
                onValueChange = viewModel::onPriceAmountChange,
                placeholder = { Text("MXN") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { viewModel.save(contentResolver, publish = false, onSaved = onBackClick) },
                enabled = !uiState.isSaving,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (uiState.isEditing) "Guardar cambios" else "Guardar borrador")
            }
            Button(
                onClick = { viewModel.save(contentResolver, publish = true, onSaved = onBackClick) },
                enabled = !uiState.isSaving,
                modifier = Modifier.weight(1f)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text("Publicar")
                }
            }
        }
    }
}

@Composable
fun MyServicesRoute(
    onBackClick: () -> Unit,
    onCreateNew: () -> Unit,
    onEditService: (String) -> Unit
) {
    val viewModel: MyServicesViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    FreeEdFeatureScaffold(
        title = "Mis servicios",
        subtitle = "Lo que ofreces a las empresas",
        onBackClick = onBackClick
    ) {
        Button(
            onClick = onCreateNew,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text("Crear servicio")
        }
        Spacer(modifier = Modifier.height(18.dp))

        when {
            uiState.isLoading -> CircularProgressIndicator()
            uiState.errorMessage != null -> FeatureInfoCard(
                eyebrow = "ERROR",
                title = "No pudimos cargar tus servicios",
                body = uiState.errorMessage.orEmpty()
            )
            uiState.services.isEmpty() -> FeatureInfoCard(
                eyebrow = "EMPIEZA",
                title = "Aun no tienes servicios",
                body = "Publica tu primer servicio para que las empresas puedan encontrarte."
            )
            else -> uiState.services.forEach { service ->
                ServiceManagementCard(
                    service = service,
                    pendingStatus = uiState.pendingStatusServiceId == service.id,
                    onEdit = { onEditService(service.id) },
                    onPublish = { viewModel.publish(service.id) },
                    onPause = { viewModel.pause(service.id) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ServiceManagementCard(
    service: Service,
    pendingStatus: Boolean,
    onEdit: () -> Unit,
    onPublish: () -> Unit,
    onPause: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
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
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = service.shortDescription
                            ?: service.description.take(120),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(status = service.status)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = service.priceLabel ?: "Precio a convenir",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Editar")
                }
                when (service.status) {
                    ServiceStatus.PUBLISHED -> OutlinedButton(
                        onClick = onPause,
                        enabled = !pendingStatus
                    ) {
                        Icon(Icons.Outlined.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Pausar")
                    }
                    ServiceStatus.DRAFT, ServiceStatus.PAUSED -> Button(
                        onClick = onPublish,
                        enabled = !pendingStatus
                    ) {
                        Icon(Icons.Outlined.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Publicar")
                    }
                    ServiceStatus.ARCHIVED -> Unit
                }
                if (pendingStatus) {
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
private fun StatusBadge(status: ServiceStatus) {
    val (label, color) = when (status) {
        ServiceStatus.DRAFT -> "Borrador" to MaterialTheme.colorScheme.outline
        ServiceStatus.PUBLISHED -> "Publicado" to MaterialTheme.colorScheme.primary
        ServiceStatus.PAUSED -> "Pausado" to MaterialTheme.colorScheme.secondary
        ServiceStatus.ARCHIVED -> "Archivado" to MaterialTheme.colorScheme.error
    }
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    categories: List<Category>,
    selected: Category?,
    onSelected: (Category) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected?.name ?: "Selecciona una categoria",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(16.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onSelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ModalityRow(
    selected: ServiceModality,
    onSelected: (ServiceModality) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ServiceModality.entries.forEach { modality ->
            FilterChip(
                selected = modality == selected,
                onClick = { onSelected(modality) },
                label = { Text(modality.label) }
            )
        }
    }
}

@Composable
private fun PriceTypeRow(
    selected: ServicePriceType,
    onSelected: (ServicePriceType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ServicePriceType.entries.forEach { type ->
            FilterChip(
                selected = type == selected,
                onClick = { onSelected(type) },
                label = { Text(type.label) }
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

private val ServiceModality.label: String
    get() = when (this) {
        ServiceModality.REMOTE -> "Remoto"
        ServiceModality.HYBRID -> "Hibrido"
        ServiceModality.ONSITE -> "Presencial"
    }

private val ServicePriceType.label: String
    get() = when (this) {
        ServicePriceType.FIXED -> "Fijo"
        ServicePriceType.HOURLY -> "Por hora"
        ServicePriceType.CUSTOM -> "A convenir"
        ServicePriceType.FREE -> "Gratis"
    }
