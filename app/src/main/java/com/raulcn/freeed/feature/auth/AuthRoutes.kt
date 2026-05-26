package com.raulcn.freeed.feature.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raulcn.freeed.R
import com.raulcn.freeed.core.model.UserRole
import com.raulcn.freeed.ui.theme.FreeEdBlue900
import com.raulcn.freeed.ui.theme.FreeEdGold200

@Composable
fun LoginRoute(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onBackClick: (() -> Unit)?
) {
    val viewModel: LoginViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AccessPage(
        onBackClick = onBackClick
    ) {
        AccessHeader(
            title = "Bienvenido de vuelta",
            subtitle = "Entra y sigue construyendo tu perfil profesional."
        )
        Spacer(modifier = Modifier.height(26.dp))
        AccessCard {
            InputLabel("Correo")
            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                placeholder = { Text("nombre@correo.com") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            InputLabel("Contrasena")
            PasswordField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = "Ingresa tu contrasena"
            )
            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = uiState.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(22.dp))
            Button(
                onClick = { viewModel.signIn(onSuccess = onLoginSuccess) },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text("Iniciar sesion")
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "No tienes cuenta?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onRegisterClick) {
                    Text("Registrate")
                }
            }
        }
    }
}

@Composable
fun RegisterRoute(
    onRegistrationFinished: () -> Unit,
    onBackClick: () -> Unit
) {
    val viewModel: RegisterViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentStep by rememberSaveable { mutableStateOf(1) }

    AccessPage(
        onBackClick = {
            if (currentStep == 2) {
                currentStep = 1
            } else {
                onBackClick()
            }
        }
    ) {
        RegisterHeader(
            currentStep = currentStep
        )
        Spacer(modifier = Modifier.height(22.dp))
        AccessCard {
            if (currentStep == 1) {
                Text(
                    text = "Como te describes?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Elige el tipo de cuenta que mejor representa tu objetivo en FreeEd.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(18.dp))
                RoleChoiceRow(
                    selectedRole = uiState.selectedRole,
                    onRoleSelected = viewModel::onRoleSelected
                )
                if (uiState.message != null && !uiState.isSuccessMessage) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = uiState.message.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(22.dp))
                Button(
                    onClick = {
                        if (uiState.selectedRole == null) {
                            viewModel.register {}
                        } else {
                            currentStep = 2
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Continuar")
                }
            } else {
                Text(
                    text = "Tu cuenta",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Crea tu acceso con correo y contrasena. Podras editar tu perfil despues.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(18.dp))
                InputLabel("Nombre completo")
                OutlinedTextField(
                    value = uiState.displayName,
                    onValueChange = viewModel::onDisplayNameChange,
                    placeholder = { Text("Como quieres aparecer") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                InputLabel("Correo electronico")
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChange,
                    placeholder = { Text("nombre@correo.com") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                InputLabel("Contrasena")
                PasswordField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    placeholder = "Minimo 6 caracteres"
                )
                Spacer(modifier = Modifier.height(16.dp))
                InputLabel("Confirmar contrasena")
                PasswordField(
                    value = uiState.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    placeholder = "Repite tu contrasena"
                )
            }
            if (uiState.message != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = uiState.message ?: "",
                    color = if (uiState.isSuccessMessage) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (currentStep == 2) {
                Spacer(modifier = Modifier.height(22.dp))
                Button(
                    onClick = { viewModel.register(onFinished = onRegistrationFinished) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text("Crear cuenta")
                    }
                }
            }
        }
    }
}

@Composable
private fun AccessPage(
    onBackClick: (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AccessBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 18.dp)
        ) {
            if (onBackClick != null) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shape = CircleShape,
                    shadowElevation = 4.dp
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Regresar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            } else {
                Spacer(modifier = Modifier.height(30.dp))
            }
            content()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AccessBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopStart)
                .padding(top = 16.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            FreeEdBlue900.copy(alpha = 0.09f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.BottomEnd)
                .padding(bottom = 28.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            FreeEdGold200.copy(alpha = 0.22f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
private fun AccessHeader(
    title: String,
    subtitle: String
) {
    Column {
        Image(
            painter = painterResource(R.drawable.freeed_logo_banner),
            contentDescription = "FreeEd",
            modifier = Modifier
                .fillMaxWidth(0.56f)
                .height(76.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(26.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AccessCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(30.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    var visible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            TextButton(onClick = { visible = !visible }) {
                Text(if (visible) "Ocultar" else "Ver")
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        )
    )
}

@Composable
private fun RoleChoiceRow(
    selectedRole: UserRole?,
    onRoleSelected: (UserRole) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        RoleChoiceChip(
            title = "Estudiante",
            body = "Construye tu portafolio y consigue tus primeras oportunidades.",
            highlights = listOf(
                "Perfil profesional visible",
                "Servicios y portafolio",
                "Solicitudes de empresas"
            ),
            icon = Icons.Rounded.School,
            isSelected = selectedRole == UserRole.STUDENT,
            onClick = { onRoleSelected(UserRole.STUDENT) }
        )
        RoleChoiceChip(
            title = "Empresa o negocio",
            body = "Encuentra talento universitario para proyectos y colaboraciones.",
            highlights = listOf(
                "Explorar perfiles",
                "Guardar favoritos",
                "Enviar solicitudes"
            ),
            icon = Icons.Rounded.Apartment,
            isSelected = selectedRole == UserRole.COMPANY,
            onClick = { onRoleSelected(UserRole.COMPANY) }
        )
    }
}

@Composable
private fun RoleChoiceChip(
    title: String,
    body: String,
    highlights: List<String>,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.padding(10.dp),
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                highlights.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun RegisterHeader(
    currentStep: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Image(
            painter = painterResource(R.drawable.freeed_logo_banner),
            contentDescription = "FreeEd",
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(68.dp),
            contentScale = ContentScale.Fit
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (currentStep >= 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(999.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 28.dp, vertical = 3.dp)
                )
            }
            Surface(
                color = if (currentStep >= 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(999.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 28.dp, vertical = 3.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "$currentStep/2",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
