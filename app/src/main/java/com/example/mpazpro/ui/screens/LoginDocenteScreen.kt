package com.example.mpazpro.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpazpro.ui.viewmodel.LoginDocenteViewModel

@Composable
fun LoginDocenteScreen(
    onVolver: () -> Unit,
    onRegistrar: () -> Unit,
    onLoginExitoso: () -> Unit,
    modifier: Modifier = Modifier,
    loginViewModel: LoginDocenteViewModel = viewModel()
) {
    // Observa el estado y actualiza la interfaz cuando cambia.
    val estado by loginViewModel.uiState.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }

    // La navegación ocurre como efecto del resultado del acceso.
    LaunchedEffect(estado.docente) {
        if (estado.docente != null) {
            onLoginExitoso()
        }
    }

    // El botón Atrás también pasa por nuestra acción de salida.
    BackHandler(onBack = onVolver)

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Acceso docente",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = estado.correo,
            onValueChange = loginViewModel::actualizarCorreo,
            label = { Text("Correo electrónico") },
            enabled = !estado.cargando,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            isError = estado.errores.correo != null,
            supportingText = {
                estado.errores.correo?.let { error ->
                    Text(error)
                }
            },
            trailingIcon = {
                if (estado.errores.correo != null) {
                    IconoErrorCampo()
                }
            },
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth()
        )

        OutlinedTextField(
            value = estado.clave,
            onValueChange = loginViewModel::actualizarClave,
            label = { Text("Contraseña") },
            enabled = !estado.cargando,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            isError = estado.errores.clave != null,
            supportingText = {
                estado.errores.clave?.let { error ->
                    Text(error)
                }
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(if (passwordVisible) "🙈" else "👁️")
                }
            },
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
        )

        if (estado.cargando) {
            LinearProgressIndicator(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
            )
        }

        Button(
            onClick = loginViewModel::iniciarSesion,
            enabled = !estado.cargando,
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()
        ) {
            Text(if (estado.cargando) "Comprobando…" else "Ingresar")
        }

        estado.mensaje?.let { mensaje ->
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        TextButton(
            onClick = onRegistrar,
            enabled = !estado.cargando,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("No tengo cuenta: registrarme")
        }

        TextButton(
            onClick = onVolver,
            enabled = !estado.cargando,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Volver al inicio")
        }
    }
}

@Composable
private fun IconoErrorCampo() {
    Icon(
        painter = painterResource(
            id = android.R.drawable.ic_dialog_alert
        ),
        contentDescription = "Error en el campo",
        tint = MaterialTheme.colorScheme.error
    )
}
