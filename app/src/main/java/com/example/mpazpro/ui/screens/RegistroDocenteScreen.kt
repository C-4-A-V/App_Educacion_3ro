package com.example.mpazpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpazpro.ui.viewmodel.RegistroDocenteViewModel
import kotlinx.coroutines.delay

@Composable
fun RegistroDocenteScreen(
    onVolver: () -> Unit,
    onRegistroExitoso: () -> Unit,
    modifier: Modifier = Modifier,
    registroViewModel: RegistroDocenteViewModel = viewModel()
) {
    val estado by registroViewModel.uiState.collectAsStateWithLifecycle()
    val editable = !estado.guardando && !estado.registrado

    LaunchedEffect(estado.registrado) {
        if (estado.registrado) {
            delay(750)
            onRegistroExitoso()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Crear cuenta docente",
            style = MaterialTheme.typography.headlineMedium
        )

        Text("Utiliza datos ficticios para este proyecto académico.")

        CampoRegistro(
            etiqueta = "Nombre",
            valor = estado.nombre,
            onCambio = registroViewModel::actualizarNombre,
            error = estado.errores.nombre,
            habilitado = editable
        )

        CampoRegistro(
            etiqueta = "Correo electrónico",
            valor = estado.correo,
            onCambio = registroViewModel::actualizarCorreo,
            error = estado.errores.correo,
            habilitado = editable,
            tipoTeclado = KeyboardType.Email
        )

        CampoRegistro(
            etiqueta = "Contraseña",
            valor = estado.clave,
            onCambio = registroViewModel::actualizarClave,
            error = estado.errores.clave,
            habilitado = editable,
            esClave = true
        )

        CampoRegistro(
            etiqueta = "Confirmar contraseña",
            valor = estado.confirmacion,
            onCambio = registroViewModel::actualizarConfirmacion,
            error = estado.errores.confirmacion,
            habilitado = editable,
            esClave = true
        )

        if (estado.guardando) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
            Text("Guardando cuenta…")
        }

        estado.mensaje?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = if (estado.registrado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }

        if (!estado.registrado) {
            Button(
                onClick = registroViewModel::registrar,
                enabled = !estado.guardando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crear cuenta")
            }
        }

        TextButton(
            onClick = onVolver,
            enabled = !estado.guardando && !estado.registrado,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar y volver")
        }
    }
}

// Reutiliza el mismo diseño para los cuatro campos.
@Composable
private fun CampoRegistro(
    etiqueta: String,
    valor: String,
    onCambio: (String) -> Unit,
    error: String?,
    habilitado: Boolean,
    esClave: Boolean = false,
    tipoTeclado: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        enabled = habilitado,
        singleLine = true,
        isError = error != null,
        supportingText = {
            error?.let { Text(it) }
        },
        trailingIcon = {
            if (error != null) {
                Icon(
                    painter = painterResource(
                        android.R.drawable.ic_dialog_alert
                    ),
                    contentDescription = "Error en el campo",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = if (esClave) {
                KeyboardType.Password
            } else {
                tipoTeclado
            }
        ),
        visualTransformation = if (esClave) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        modifier = Modifier.fillMaxWidth()
    )
}
