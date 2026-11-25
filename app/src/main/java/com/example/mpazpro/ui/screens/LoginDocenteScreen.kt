package com.example.mpazpro.ui.screens

import com.example.mpazpro.ui.theme.DocenteTheme
import com.example.mpazpro.ui.theme.EncabezadoDocente

import com.example.mpazpro.ui.components.BotonVolver

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
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
import com.example.mpazpro.ui.components.BotonSecundario
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.mpazpro.ui.components.anchoFormulario
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
    DocenteTheme {
    // Observa el estado y actualiza la interfaz cuando cambia.
    val estado by loginViewModel.uiState.collectAsStateWithLifecycle()
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var correoAnterior by rememberSaveable { mutableStateOf("") }
    var claveAnterior by androidx.compose.runtime.remember { mutableStateOf("") }

    if (estado.recuperacionNecesaria) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { if (!estado.cargando) loginViewModel.cerrarSesionRemota() },
            title = { Text("Recuperar mis datos guardados") },
            text = {
                Column {
                    Text("Ya existe un perfil en este dispositivo. Confirma sus credenciales para recuperar sus datos.")
                    OutlinedTextField(
                        value = correoAnterior, onValueChange = { correoAnterior = it },
                        label = { Text("Correo anterior") }, singleLine = true, enabled = !estado.cargando
                    )
                    OutlinedTextField(
                        value = claveAnterior, onValueChange = { claveAnterior = it },
                        label = { Text("Contraseña anterior") }, singleLine = true,
                        enabled = !estado.cargando, visualTransformation = PasswordVisualTransformation()
                    )
                    estado.mensaje?.let { Text(it) }
                    if (estado.cargando) LinearProgressIndicator()
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    enabled = !estado.cargando,
                    onClick = {
                        loginViewModel.recuperarDatos(correoAnterior, claveAnterior)
                        claveAnterior = ""
                    }
                ) { Text("Recuperar") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    enabled = !estado.cargando,
                    onClick = { claveAnterior = ""; loginViewModel.cerrarSesionRemota() }
                ) { Text("Cancelar") }
            }
        )
    }

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
            .fillMaxSize().anchoFormulario()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EncabezadoDocente(
            titulo = "Un espacio para enseñar",
            detalle = "Organiza tus cursos y acompaña cada aprendizaje."
        )
        androidx.compose.material3.Card(
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Acceso docente", style = MaterialTheme.typography.titleLarge)

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
                com.example.mpazpro.ui.components.VisibilidadClave(passwordVisible, estado.errores.clave != null) {
                    passwordVisible = !passwordVisible
                }
            },
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
        )

        androidx.compose.foundation.layout.Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            androidx.compose.material3.Checkbox(
                checked = estado.recuperarAnterior,
                onCheckedChange = loginViewModel::solicitarRecuperacion,
                enabled = !estado.cargando
            )
            Text("Recuperar datos guardados en este dispositivo")
        }

        if (estado.cargando) {
            LinearProgressIndicator(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
            )
        }

        Button(
            onClick = loginViewModel::iniciarSesionRemota,
            enabled = !estado.cargando,
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()
                .then(Modifier.defaultMinSize(minHeight = 52.dp))
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

        BotonSecundario(
            onClick = onRegistrar,
            enabled = !estado.cargando,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("No tengo cuenta: registrarme")
        }

        BotonVolver(
            onClick = onVolver,
            enabled = !estado.cargando,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Volver al inicio")
        }
        }
        }
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
