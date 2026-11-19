package com.example.mpazpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mpazpro.R
import com.example.mpazpro.ui.components.BotonVolver
import com.example.mpazpro.ui.components.FondoAventura
import com.example.mpazpro.ui.components.PuduExplorador
import com.example.mpazpro.ui.theme.AventuraTheme
import com.example.mpazpro.ui.viewmodel.AccesoEstudianteViewModel

@Composable
fun SeleccionEstudianteScreen(
    accesoViewModel: AccesoEstudianteViewModel,
    onIngresar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by accesoViewModel.uiState.collectAsStateWithLifecycle()
    var rut by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(estado.mensaje) { if (estado.mensaje != null) pin = "" }

    AventuraTheme {
        FondoAventura(modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BotonVolver(onClick = onVolver) { Text("Volver al inicio") }
                Text("🎒 ¡Hola, explorador!", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Text("Escribe tu RUT y tu PIN para comenzar tu aventura.", style = MaterialTheme.typography.bodyLarge)
                PuduExplorador(Modifier.size(90.dp).align(Alignment.CenterHorizontally))
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = rut, onValueChange = { rut = it.take(12) },
                            label = { Text("Tu RUT") }, placeholder = { Text("Ej.: 12345678-5") },
                            singleLine = true, enabled = !estado.ingresando,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = pin, onValueChange = { pin = it.filter { c -> c in '0'..'9' }.take(4) },
                            label = { Text("Tu PIN de 4 números") }, singleLine = true,
                            enabled = !estado.ingresando,
                            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            trailingIcon = { IconButton(onClick = { visible = !visible }) {
                                Icon(painterResource(R.drawable.ic_visibility), if (visible) "Ocultar PIN" else "Mostrar PIN")
                            } },
                            modifier = Modifier.fillMaxWidth()
                        )
                        estado.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        Button(
                            onClick = { accesoViewModel.ingresar(rut, pin) { pin = ""; onIngresar() } },
                            enabled = !estado.ingresando && !estado.cargando,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        ) { Text(if (estado.ingresando) "Ingresando..." else "¡Comenzar aventura!") }
                    }
                }
                Text("¿Olvidaste tu PIN? Pide ayuda a tu profesor o profesora.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
