package com.raulcn.freeed.feature.services

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raulcn.freeed.domain.model.AppUserProfile
import com.raulcn.freeed.feature.system.FeatureInfoCard
import com.raulcn.freeed.feature.system.FreeEdFeatureScaffold

@Composable
fun ServiceDetailRoute(
    serviceId: String,
    sessionProfile: AppUserProfile?,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onRequireAuth: () -> Unit
) {
    val viewModel: ServiceDetailViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(serviceId) {
        viewModel.load(serviceId)
    }

    FreeEdFeatureScaffold(
        title = "Servicio",
        subtitle = "",
        onBackClick = onBackClick
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator()
            uiState.errorMessage != null -> FeatureInfoCard(
                eyebrow = "SERVICIO",
                title = "No pudimos cargar este servicio",
                body = uiState.errorMessage ?: ""
            )
            uiState.service != null -> {
                FeatureInfoCard(
                    eyebrow = uiState.service?.modality?.name ?: "SERVICIO",
                    title = uiState.service?.title ?: "",
                    body = uiState.service?.description ?: ""
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uiState.service?.priceLabel ?: "Precio a convenir",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                when {
                    sessionProfile == null -> Button(onClick = onRequireAuth) {
                        Text("Inicia sesion para contratar")
                    }
                    else -> OutlinedButton(onClick = { onEditClick(serviceId) }) {
                        Text("Abrir siguiente accion")
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceEditorRoute(
    serviceId: String,
    onBackClick: () -> Unit
) {
    FreeEdFeatureScaffold(
        title = "Editor de servicio",
        subtitle = "",
        onBackClick = onBackClick
    ) {
        FeatureInfoCard(
            eyebrow = "EDITOR",
            title = "Editor preparado para datos reales",
            body = "Aqui seguiremos con el formulario profesional para crear y editar servicios."
        )
    }
}
