package com.example.mpazpro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpazpro.model.DocenteSesion
import com.example.mpazpro.ui.viewmodel.CursosViewModel

@Composable
fun PanelDocenteScreen(
    docente: DocenteSesion,
    onCerrarSesion: () -> Unit,
    onAbrirCurso: (Long) -> Unit,
    onAbrirActividades: (Long, String) -> Unit,
    modifier: Modifier = Modifier,
    cursosViewModel: CursosViewModel = viewModel()
) {
    val estado by cursosViewModel.uiState
        .collectAsStateWithLifecycle()

    LaunchedEffect(docente.id) {
        cursosViewModel.cargarCursos(docente.id)
    }

    var accion by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val tituloAccion = when (accion) {
        "crear" -> "Agregar actividad"
        "actividades" -> "Mis actividades"
        "resultados" -> "Ver resultados"
        "estudiantes" -> "Agregar estudiantes"
        else -> "Mis cursos"
    }

    if (accion == "perfil") {
        AlertDialog(
            onDismissRequest = {
                accion = null
            },
            title = {
                Text("Mi perfil")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(docente.nombre)
                    Text(docente.correo)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        accion = null
                    }
                ) {
                    Text("Cerrar")
                }
            }
        )
    } else if (accion != null) {
        AlertDialog(
            onDismissRequest = {
                if (!estado.guardando) {
                    accion = null
                }
            },
            title = {
                Text(tituloAccion)
            },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text("Selecciona un curso")
                    }

                    if (estado.cargando) {
                        item {
                            LinearProgressIndicator(
                                Modifier.fillMaxWidth()
                            )
                        }
                    }

                    items(
                        items = estado.cursos,
                        key = { it.id }
                    ) { curso ->
                        OutlinedButton(
                            onClick = {
                                val elegida =
                                    accion ?: return@OutlinedButton

                                accion = null

                                if (
                                    elegida in listOf(
                                        "crear",
                                        "actividades",
                                        "resultados"
                                    )
                                ) {
                                    onAbrirActividades(
                                        curso.id,
                                        elegida
                                    )
                                } else {
                                    onAbrirCurso(curso.id)
                                }
                            },
                            enabled = !estado.guardando,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(curso.nombre)
                        }
                    }

                    if (
                        !estado.cargando &&
                        estado.cursos.isEmpty()
                    ) {
                        item {
                            Text(
                                "Crea tu primer curso para continuar."
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = estado.nombre,
                            onValueChange = cursosViewModel::actualizarNombre,
                            label = {
                                Text("Nombre del nuevo curso")
                            },
                            singleLine = true,
                            enabled = !estado.guardando,
                            isError = estado.errorNombre != null,
                            supportingText = {
                                estado.errorNombre?.let {
                                    Text(it)
                                }
                            }
                        )
                    }

                    item {
                        Button(
                            onClick = cursosViewModel::crearCurso,
                            enabled = !estado.cargando &&
                                !estado.guardando
                        ) {
                            Text("Crear curso")
                        }
                    }

                    estado.mensaje?.let { mensaje ->
                        item {
                            Text(mensaje)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        accion = null
                    },
                    enabled = !estado.guardando
                ) {
                    Text("Cerrar")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                MaterialTheme.colorScheme.primaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📖",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Column {
                        Text(
                            text = "MPAZ PRO 3.º",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "3.º Básico",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = docente.nombre.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "¡Hola, docente!",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "¿Qué deseas hacer hoy?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MenuBubbleCard(
                        title = "Agregar estudiantes",
                        emoji = "👤+",
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            accion = "estudiantes"
                        }
                    )

                    MenuBubbleCard(
                        title = "Agregar actividad",
                        emoji = "📄+",
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            accion = "crear"
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MenuBubbleCard(
                        title = "Mis cursos",
                        emoji = "📖",
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            accion = "cursos"
                        }
                    )

                    MenuBubbleCard(
                        title = "Mis actividades",
                        emoji = "☑️",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            accion = "actividades"
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MenuBubbleCard(
                        title = "Ver resultados",
                        emoji = "📊",
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            accion = "resultados"
                        }
                    )

                    MenuBubbleCard(
                        title = "Mi perfil",
                        emoji = "⚙️",
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            accion = "perfil"
                        }
                    )
                }
            }
        }

        if (estado.cargando || estado.guardando) {
            item {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        estado.mensaje?.let { mensaje ->
            item {
                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        item {
            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedButton(
                onClick = onCerrarSesion,
                enabled = !estado.guardando,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}

@Composable
private fun MenuBubbleCard(
    title: String,
    emoji: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(28.dp),
        modifier = modifier.height(140.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
