package com.raulcn.freeed.feature.requests

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raulcn.freeed.core.model.UserRole
import com.raulcn.freeed.domain.model.AppUserProfile
import com.raulcn.freeed.feature.system.FeatureInfoCard
import com.raulcn.freeed.feature.system.FreeEdFeatureScaffold

@Composable
fun RequestsRoute(
    sessionProfile: AppUserProfile?,
    onOpenRequest: () -> Unit,
    onOpenSentRequests: () -> Unit,
    onOpenReceivedRequests: () -> Unit
) {
    FreeEdFeatureScaffold(
        title = "Solicitudes",
        subtitle = "Oportunidades y seguimiento"
    ) {
        FeatureInfoCard(
            eyebrow = if (sessionProfile?.role == UserRole.COMPANY) "EMPRESA" else "ESTUDIANTE",
            title = if (sessionProfile?.role == UserRole.COMPANY) {
                "Administra tus solicitudes enviadas"
            } else {
                "Revisa oportunidades recibidas"
            },
            body = if (sessionProfile?.role == UserRole.COMPANY) {
                "Cuando conectemos la consulta final, aqui veras el historial de solicitudes creadas desde tu negocio."
            } else {
                "Este modulo sera la bandeja de entrada donde aceptaras, rechazaras o daras seguimiento a nuevas oportunidades."
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onOpenRequest) {
            androidx.compose.material3.Text("Abrir detalle base")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onOpenSentRequests) {
            androidx.compose.material3.Text("Solicitudes enviadas")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onOpenReceivedRequests) {
            androidx.compose.material3.Text("Solicitudes recibidas")
        }
    }
}

@Composable
fun RequestDetailRoute(
    requestId: String,
    onBackClick: () -> Unit
) {
    FreeEdFeatureScaffold(
        title = "Detalle de solicitud",
        subtitle = "ID: $requestId",
        onBackClick = onBackClick
    ) {
        FeatureInfoCard(
            eyebrow = "ESTADO",
            title = "Pantalla lista para transiciones reales",
            body = "Aqui mapearemos los estados pendientes, aceptados, rechazados, en progreso y completados directamente desde Supabase."
        )
    }
}
