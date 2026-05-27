package com.raulcn.freeed.feature.requests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raulcn.freeed.core.model.RequestStatus
import com.raulcn.freeed.core.model.UserRole
import com.raulcn.freeed.domain.model.AppUserProfile
import com.raulcn.freeed.domain.model.ServiceRequest
import com.raulcn.freeed.feature.system.FeatureInfoCard
import com.raulcn.freeed.feature.system.FreeEdFeatureScaffold

@Composable
fun RequestsRoute(
    sessionProfile: AppUserProfile?,
    onOpenRequest: (String) -> Unit,
    onOpenCompanyProfile: (String) -> Unit
) {
    val isCompany = sessionProfile?.role == UserRole.COMPANY
    val receivedViewModel: ReceivedRequestsViewModel = viewModel()
    val sentViewModel: SentRequestsViewModel = viewModel()
    val receivedUiState by receivedViewModel.uiState.collectAsStateWithLifecycle()
    val sentUiState by sentViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(isCompany) {
        if (isCompany) sentViewModel.load() else receivedViewModel.load()
    }

    FreeEdFeatureScaffold(
        title = "Solicitudes",
        subtitle = "Oportunidades y seguimiento"
    ) {
        if (isCompany) {
            when {
                sentUiState.isLoading -> CircularProgressIndicator()
                sentUiState.errorMessage != null -> FeatureInfoCard(
                    eyebrow = "ERROR",
                    title = "No pudimos cargar la lista",
                    body = sentUiState.errorMessage.orEmpty()
                )
                sentUiState.items.isEmpty() -> FeatureInfoCard(
                    eyebrow = "VACIO",
                    title = "Aun no envias solicitudes",
                    body = "Explora servicios y crea tu primera solicitud."
                )
                else -> sentUiState.items.forEach { request ->
                    SentRequestCard(
                        request = request,
                        onOpen = { onOpenRequest(request.id) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        } else {
            when {
                receivedUiState.isLoading -> CircularProgressIndicator()
                receivedUiState.errorMessage != null -> FeatureInfoCard(
                    eyebrow = "ERROR",
                    title = "No pudimos cargar la bandeja",
                    body = receivedUiState.errorMessage.orEmpty()
                )
                receivedUiState.items.isEmpty() -> FeatureInfoCard(
                    eyebrow = "VACIO",
                    title = "Aun no tienes solicitudes",
                    body = "Cuando una empresa te contacte, aparecera aqui."
                )
                else -> receivedUiState.items.forEach { request ->
                    ReceivedRequestCard(
                        request = request,
                        onOpen = { onOpenRequest(request.id) },
                        onOpenCompany = { onOpenCompanyProfile(request.companyId) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun ReceivedRequestsRoute(
    onBackClick: () -> Unit,
    onOpenRequest: (String) -> Unit,
    onOpenCompanyProfile: (String) -> Unit
) {
    val viewModel: ReceivedRequestsViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    FreeEdFeatureScaffold(
        title = "Solicitudes recibidas",
        subtitle = "Bandeja de oportunidades",
        onBackClick = onBackClick
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator()
            uiState.errorMessage != null -> FeatureInfoCard(
                eyebrow = "ERROR",
                title = "No pudimos cargar la bandeja",
                body = uiState.errorMessage.orEmpty()
            )
            uiState.items.isEmpty() -> FeatureInfoCard(
                eyebrow = "VACIO",
                title = "Aun no tienes solicitudes",
                body = "Cuando una empresa solicite alguno de tus servicios, aparecera aqui."
            )
            else -> uiState.items.forEach { request ->
                ReceivedRequestCard(
                    request = request,
                    onOpen = { onOpenRequest(request.id) },
                    onOpenCompany = { onOpenCompanyProfile(request.companyId) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun RequestDetailRoute(
    requestId: String,
    sessionProfile: AppUserProfile?,
    onBackClick: () -> Unit
) {
    val viewModel: RequestDetailViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(requestId) {
        viewModel.load(requestId)
    }

    FreeEdFeatureScaffold(
        title = "Solicitud",
        subtitle = "",
        onBackClick = onBackClick
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator()
            uiState.errorMessage != null && uiState.request == null -> FeatureInfoCard(
                eyebrow = "ERROR",
                title = "No pudimos cargar la solicitud",
                body = uiState.errorMessage.orEmpty()
            )
            uiState.request != null -> RequestDetailContent(
                request = uiState.request!!,
                actorRole = sessionProfile?.role,
                isUpdating = uiState.isUpdatingStatus,
                inlineError = uiState.errorMessage,
                onAccept = viewModel::accept,
                onReject = viewModel::reject,
                onStart = viewModel::markInProgress,
                onComplete = viewModel::markCompleted,
                onCancel = viewModel::cancel
            )
        }
    }
}

@Composable
private fun RequestDetailContent(
    request: ServiceRequest,
    actorRole: UserRole?,
    isUpdating: Boolean,
    inlineError: String?,
    onAccept: () -> Unit,
    onReject: (String?) -> Unit,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onCancel: (String?) -> Unit
) {
    var showRejectDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    StatusBanner(status = request.status)
    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = request.title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
    if (!request.companyDisplayName.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "De: ${request.companyDisplayName}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    if (!request.serviceTitle.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Servicio: ${request.serviceTitle}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Spacer(modifier = Modifier.height(16.dp))
    SectionLabel("Mensaje de la empresa")
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = request.message,
        style = MaterialTheme.typography.bodyMedium
    )

    request.budgetLabel?.let {
        Spacer(modifier = Modifier.height(16.dp))
        SectionLabel("Presupuesto propuesto")
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = it, style = MaterialTheme.typography.titleMedium)
    }

    if (!request.desiredDeadline.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(12.dp))
        SectionLabel("Fecha deseada")
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = request.desiredDeadline, style = MaterialTheme.typography.bodyMedium)
    }

    if (!request.rejectionReason.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(16.dp))
        SectionLabel("Motivo de rechazo")
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = request.rejectionReason,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }

    if (!request.cancelledReason.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(16.dp))
        SectionLabel("Motivo de cancelacion")
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = request.cancelledReason,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }

    if (!inlineError.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = inlineError,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
    }

    Spacer(modifier = Modifier.height(24.dp))
    ActionRow(
        status = request.status,
        actorRole = actorRole,
        isUpdating = isUpdating,
        onAccept = onAccept,
        onReject = { showRejectDialog = true },
        onStart = onStart,
        onComplete = onComplete,
        onCancel = { showCancelDialog = true }
    )

    if (showRejectDialog) {
        ReasonDialog(
            title = "Rechazar solicitud",
            label = "Motivo (opcional)",
            confirmLabel = "Rechazar",
            onConfirm = { reason ->
                showRejectDialog = false
                onReject(reason)
            },
            onDismiss = { showRejectDialog = false }
        )
    }

    if (showCancelDialog) {
        ReasonDialog(
            title = "Cancelar solicitud",
            label = "Motivo (opcional)",
            confirmLabel = "Cancelar solicitud",
            onConfirm = { reason ->
                showCancelDialog = false
                onCancel(reason)
            },
            onDismiss = { showCancelDialog = false }
        )
    }
}

@Composable
private fun ActionRow(
    status: RequestStatus,
    actorRole: UserRole?,
    isUpdating: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    val isStudent = actorRole == UserRole.STUDENT
    val isParticipant = actorRole == UserRole.STUDENT || actorRole == UserRole.COMPANY

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (status) {
            RequestStatus.PENDING -> when {
                isStudent -> {
                    Button(onClick = onAccept, enabled = !isUpdating, modifier = Modifier.weight(1f)) {
                        Text("Aceptar")
                    }
                    OutlinedButton(onClick = onReject, enabled = !isUpdating, modifier = Modifier.weight(1f)) {
                        Text("Rechazar")
                    }
                }
                isParticipant -> OutlinedButton(
                    onClick = onCancel,
                    enabled = !isUpdating,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar solicitud")
                }
                else -> NoActionsHint()
            }
            RequestStatus.ACCEPTED -> if (isParticipant) {
                Button(onClick = onStart, enabled = !isUpdating, modifier = Modifier.weight(1f)) {
                    Text("Iniciar trabajo")
                }
                OutlinedButton(onClick = onCancel, enabled = !isUpdating, modifier = Modifier.weight(1f)) {
                    Text("Cancelar")
                }
            } else NoActionsHint()
            RequestStatus.IN_PROGRESS -> if (isParticipant) {
                Button(onClick = onComplete, enabled = !isUpdating, modifier = Modifier.weight(1f)) {
                    Text("Marcar completado")
                }
                OutlinedButton(onClick = onCancel, enabled = !isUpdating, modifier = Modifier.weight(1f)) {
                    Text("Cancelar")
                }
            } else NoActionsHint()
            RequestStatus.COMPLETED,
            RequestStatus.REJECTED,
            RequestStatus.CANCELLED -> NoActionsHint()
        }
        if (isUpdating) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun NoActionsHint() {
    Text(
        text = "No hay acciones disponibles para este estado.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ReceivedRequestCard(
    request: ServiceRequest,
    onOpen: () -> Unit,
    onOpenCompany: () -> Unit
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
                        text = request.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!request.companyDisplayName.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "De: ${request.companyDisplayName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!request.serviceTitle.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Servicio: ${request.serviceTitle}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusChip(status = request.status)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = request.message.take(160),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                request.budgetLabel?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                OutlinedButton(onClick = onOpenCompany) {
                    Text("Ver perfil")
                }
                OutlinedButton(onClick = onOpen) {
                    Text("Ver detalle")
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: RequestStatus) {
    val (label, color) = status.display()
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

@Composable
private fun StatusBanner(status: RequestStatus) {
    val (label, color) = status.display()
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Estado: $label",
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ReasonDialog(
    title: String,
    label: String,
    confirmLabel: String,
    onConfirm: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                placeholder = { Text(label) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(14.dp)
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(reason.takeIf { it.isNotBlank() }) }) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Volver") }
        }
    )
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

@Composable
private fun RequestStatus.display(): Pair<String, androidx.compose.ui.graphics.Color> = when (this) {
    RequestStatus.PENDING -> "Pendiente" to MaterialTheme.colorScheme.secondary
    RequestStatus.ACCEPTED -> "Aceptada" to MaterialTheme.colorScheme.primary
    RequestStatus.IN_PROGRESS -> "En progreso" to MaterialTheme.colorScheme.tertiary
    RequestStatus.COMPLETED -> "Completada" to MaterialTheme.colorScheme.primary
    RequestStatus.REJECTED -> "Rechazada" to MaterialTheme.colorScheme.error
    RequestStatus.CANCELLED -> "Cancelada" to MaterialTheme.colorScheme.outline
}

@Composable
fun SentRequestsRoute(
    onBackClick: () -> Unit,
    onOpenRequest: (String) -> Unit
) {
    val viewModel: SentRequestsViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    FreeEdFeatureScaffold(
        title = "Solicitudes enviadas",
        subtitle = "Seguimiento de tus oportunidades",
        onBackClick = onBackClick
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator()
            uiState.errorMessage != null -> FeatureInfoCard(
                eyebrow = "ERROR",
                title = "No pudimos cargar la lista",
                body = uiState.errorMessage.orEmpty()
            )
            uiState.items.isEmpty() -> FeatureInfoCard(
                eyebrow = "VACIO",
                title = "Aun no envias solicitudes",
                body = "Explora servicios publicados y envia una solicitud desde el detalle del servicio."
            )
            else -> uiState.items.forEach { request ->
                SentRequestCard(
                    request = request,
                    onOpen = { onOpenRequest(request.id) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun SentRequestCard(
    request: ServiceRequest,
    onOpen: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
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
                        text = request.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!request.serviceTitle.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Servicio: ${request.serviceTitle}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusChip(status = request.status)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = request.message.take(160),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                request.budgetLabel?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                OutlinedButton(onClick = onOpen) {
                    Text("Ver detalle")
                }
            }
        }
    }
}

@Composable
fun SendRequestRoute(
    serviceId: String,
    onBackClick: () -> Unit
) {
    val viewModel: SendRequestViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(serviceId) {
        viewModel.load(serviceId)
    }

    LaunchedEffect(uiState.sentSuccessfully) {
        if (uiState.sentSuccessfully) onBackClick()
    }

    FreeEdFeatureScaffold(
        title = "Solicitar servicio",
        subtitle = "Envia tu propuesta al estudiante",
        onBackClick = onBackClick
    ) {
        when {
            uiState.isLoadingService -> CircularProgressIndicator()
            uiState.errorMessage != null && uiState.service == null -> FeatureInfoCard(
                eyebrow = "ERROR",
                title = "No pudimos cargar el servicio",
                body = uiState.errorMessage.orEmpty()
            )
            uiState.service != null -> {
                val service = uiState.service!!
                FeatureInfoCard(
                    eyebrow = "SERVICIO",
                    title = service.title,
                    body = service.shortDescription ?: service.description.take(140)
                )
                Spacer(modifier = Modifier.height(18.dp))

                InputLabel("Titulo")
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = viewModel::onTitleChange,
                    placeholder = { Text("Resumen breve de lo que necesitas") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))

                InputLabel("Mensaje al estudiante")
                OutlinedTextField(
                    value = uiState.message,
                    onValueChange = viewModel::onMessageChange,
                    placeholder = { Text("Cuenta que necesitas, plazos y cualquier detalle clave.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))

                InputLabel("Presupuesto propuesto (opcional)")
                OutlinedTextField(
                    value = uiState.budget,
                    onValueChange = viewModel::onBudgetChange,
                    placeholder = { Text("MXN") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Spacer(modifier = Modifier.height(14.dp))

                InputLabel("Fecha deseada (opcional)")
                OutlinedTextField(
                    value = uiState.deadline,
                    onValueChange = viewModel::onDeadlineChange,
                    placeholder = { Text("AAAA-MM-DD") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                )

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))
                Button(
                    onClick = viewModel::submit,
                    enabled = !uiState.isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("Enviar solicitud")
                    }
                }
            }
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
