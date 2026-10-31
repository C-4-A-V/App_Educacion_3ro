package com.example.mpazpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.mpazpro.ui.components.anchoFormulario
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

    var claveVisible by rememberSaveable { mutableStateOf(false) }
    var confirmacionVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(estado.registrado) {
        if (estado.registrado) {
            delay(750)
            onRegistroExitoso()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize().anchoFormulario()
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

        OutlinedTextField(
            value = estado.nombre,
            onValueChange = registroViewModel::actualizarNombre,
            label = { Text("Nombre") },
            enabled = editable,
            singleLine = true,
            isError = estado.errores.nombre != null,
            supportingText = {
                estado.errores.nombre?.let { Text(it) }
            },
            trailingIcon = {
                if (estado.errores.nombre != null) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_dialog_alert),
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = estado.correo,
            onValueChange = registroViewModel::actualizarCorreo,
            label = { Text("Correo electrónico") },
            enabled = editable,
            singleLine = true,
            isError = estado.errores.correo != null,
            supportingText = {
                estado.errores.correo?.let { Text(it) }
            },
            trailingIcon = {
                if (estado.errores.correo != null) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_dialog_alert),
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = estado.clave,
            onValueChange = registroViewModel::actualizarClave,
            label = { Text("Contraseña") },
            enabled = editable,
            singleLine = true,
            isError = estado.errores.clave != null,
            supportingText = {
                estado.errores.clave?.let { Text(it) }
            },
            trailingIcon = {
                com.example.mpazpro.ui.components.VisibilidadClave(claveVisible, estado.errores.clave != null) {
                    claveVisible = !claveVisible
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (claveVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = estado.confirmacion,
            onValueChange = registroViewModel::actualizarConfirmacion,
            label = { Text("Confirmar contraseña") },
            enabled = editable,
            singleLine = true,
            isError = estado.errores.confirmacion != null,
            supportingText = {
                estado.errores.confirmacion?.let { Text(it) }
            },
            trailingIcon = {
                com.example.mpazpro.ui.components.VisibilidadClave(confirmacionVisible, estado.errores.confirmacion != null) {
                    confirmacionVisible = !confirmacionVisible
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (confirmacionVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
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
