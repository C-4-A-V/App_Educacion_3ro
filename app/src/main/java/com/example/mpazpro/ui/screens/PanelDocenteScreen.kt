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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpazpro.model.DocenteSesion
import com.example.mpazpro.ui.components.ImagenInteligente
import com.example.mpazpro.ui.viewmodel.CursosViewModel

@Composable
internal fun PanelDocenteContenido(
    docente: DocenteSesion,
    onCerrarSesion: () -> Unit,
    onAbrirCurso: (Long) -> Unit,
    onAbrirActividades: (Long, String) -> Unit,
    onAbrirPerfil: () -> Unit,
    onAbrirResultados: () -> Unit,
    fotoRuta: String? = null,
    modifier: Modifier = Modifier,
    cursosViewModel: CursosViewModel = viewModel()
) {
    val estado by cursosViewModel.uiState.collectAsStateWithLifecycle()

    var accion by rememberSaveable(docente.id) {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(docente.id) {
        cursosViewModel.cargarCursos(docente.id)
    }

    // Selección y creación de cursos.
    if (accion != null) {
        AlertDialog(
            onDismissRequest = {
                if (!estado.guardando) {
                    accion = null
                }
            },

            title = {
                Text(
                    text = when (accion) {
                        "actividades" -> "Actividades · elige un curso"
                        "estudiantes" -> "Agregar estudiantes"
                        else -> "Mis cursos"
                    }
                )
            },

            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (estado.cargando) {
                        item {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    items(
                        items = estado.cursos,
                        key = { it.id }
                    ) { curso ->
                        OutlinedButton(
                            onClick = {
                                val elegida = accion
                                    ?: return@OutlinedButton

                                accion = null

                                if (elegida == "actividades") {
                                    onAbrirActividades(
                                        curso.id,
                                        "actividades"
                                    )
                                } else {
                                    onAbrirCurso(curso.id)
                                }
                            },

                            enabled = !estado.guardando,

                            modifier = Modifier.fillMaxWidth(),

                            shape = RoundedCornerShape(16.dp)
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
                                "Crea tu primer curso para comenzar."
                            )
                        }
                    }

                    item {
                        Text(
                            text = "Crear un curso",
                            style = MaterialTheme.typography.titleSmall
                        )

                        OutlinedTextField(
                            value = estado.nombre,

                            onValueChange =
                                cursosViewModel::actualizarNombre,

                            label = {
                                Text("Nombre del curso")
                            },

                            placeholder = {
                                Text("Ejemplo: 3.º A")
                            },

                            singleLine = true,

                            enabled = !estado.guardando,

                            isError = estado.errorNombre != null,
                            trailingIcon = {
                                if (estado.errorNombre != null) Icon(painterResource(android.R.drawable.ic_dialog_alert), "Error en el nombre del curso")
                            },

                            supportingText = {
                                estado.errorNombre?.let {
                                    Text(it)
                                }
                            },

                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Button(
                            onClick = cursosViewModel::crearCurso,

                            enabled =
                                !estado.cargando &&
                                !estado.guardando,

                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (estado.guardando) {
                                    "Guardando…"
                                } else {
                                    "Crear curso"
                                }
                            )
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
            .imePadding(),

        contentPadding = PaddingValues(
            horizontal = 24.dp,
            vertical = 24.dp
        ),

        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Marca y acceso al perfil.
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(12.dp),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),

                    color =
                        MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        painter = painterResource(
                            android.R.drawable.ic_menu_agenda
                        ),

                        contentDescription = null,

                        tint =
                            MaterialTheme.colorScheme.onPrimaryContainer,

                        modifier = Modifier
                            .padding(12.dp)
                            .size(24.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "MPAZ PRO 3.º",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Espacio docente",

                        style = MaterialTheme.typography.bodySmall,

                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    onClick = onAbrirPerfil,

                    shape = CircleShape,

                    color =
                        MaterialTheme.colorScheme.primaryContainer,

                    modifier = Modifier.size(52.dp)
                ) {
                    ImagenInteligente(
                        ruta = fotoRuta,

                        descripcion = "Abrir mi perfil",

                        modifier = Modifier.size(52.dp)
                    )
                }
            }
        }

        // Saludo.
        item {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "¡Hola, ${
                        docente.nombre
                            .trim()
                            .substringBefore(' ')
                    }!",

                    style = MaterialTheme.typography.headlineLarge
                )

                Text(
                    text = "Acompañemos cada nuevo aprendizaje.",

                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Resumen del espacio docente.
        item {
            Surface(
                color =
                    MaterialTheme.colorScheme.surfaceVariant,

                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (estado.cargando) {
                        "Cargando tus cursos…"
                    } else {
                        "${estado.cursos.size} cursos a tu cargo · " +
                            "4 asignaturas"
                    },

                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    ),

                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // Opción unificada: Actividades.
        item {
            Card(
                onClick = {
                    accion = "actividades"
                },

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(28.dp),

                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.primary,

                    contentColor =
                        MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),

                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            android.R.drawable.ic_menu_edit
                        ),

                        contentDescription = null,

                        modifier = Modifier.size(32.dp)
                    )

                    Text(
                        text = "Actividades",

                        style =
                            MaterialTheme.typography.headlineSmall
                    )

                    Text(
                        text = "Consulta las actividades de tus cursos " +
                            "y crea nuevas propuestas."
                    )

                    Text(
                        text = "Elegir curso →",

                        style =
                            MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        item {
            Text(
                text = "Tu aula",
                style = MaterialTheme.typography.titleLarge
            )
        }

        // Estudiantes y cursos.
        item {
            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(16.dp)
            ) {
                TarjetaMenuDocente(
                    titulo = "Agregar estudiantes",

                    detalle = "Incorpora niños a tu curso",

                    icono = android.R.drawable.ic_menu_add,

                    fondo =
                        MaterialTheme.colorScheme.primaryContainer,

                    texto =
                        MaterialTheme.colorScheme.onPrimaryContainer,

                    modifier = Modifier.weight(1f),

                    onClick = {
                        accion = "estudiantes"
                    }
                )

                TarjetaMenuDocente(
                    titulo = "Mis cursos",

                    detalle = "Organiza tus grupos",

                    icono = android.R.drawable.ic_menu_agenda,

                    fondo =
                        MaterialTheme.colorScheme.secondaryContainer,

                    texto =
                        MaterialTheme.colorScheme.onSecondaryContainer,

                    modifier = Modifier.weight(1f),

                    onClick = {
                        accion = "cursos"
                    }
                )
            }
        }

        // Resultados y perfil.
        item {
            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(16.dp)
            ) {
                TarjetaMenuDocente(
                    titulo = "Ver resultados",

                    detalle = "Avances y estrellas",

                    icono =
                        android.R.drawable.ic_menu_sort_by_size,

                    fondo =
                        MaterialTheme.colorScheme.tertiaryContainer,

                    texto =
                        MaterialTheme.colorScheme.onTertiaryContainer,

                    modifier = Modifier.weight(1f),

                    onClick = onAbrirResultados
                )

                TarjetaMenuDocente(
                    titulo = "Mi perfil",

                    detalle = "Tus datos y fotografía",

                    icono =
                        android.R.drawable.ic_menu_myplaces,

                    fondo =
                        MaterialTheme.colorScheme.surfaceVariant,

                    texto =
                        MaterialTheme.colorScheme.onSurfaceVariant,

                    modifier = Modifier.weight(1f),

                    onClick = onAbrirPerfil
                )
            }
        }

        if (estado.cargando) {
            item {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        estado.mensaje?.let { mensaje ->
            item {
                Text(mensaje)
            }
        }

        item {
            OutlinedButton(
                onClick = onCerrarSesion,

                enabled = !estado.guardando,

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(18.dp),

                contentPadding = PaddingValues(16.dp)
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}

@Composable
private fun TarjetaMenuDocente(
    titulo: String,
    detalle: String,
    icono: Int,
    fondo: Color,
    texto: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,

        modifier = modifier.heightIn(min = 180.dp),

        shape = RoundedCornerShape(26.dp),

        colors = CardDefaults.cardColors(
            containerColor = fondo,
            contentColor = texto
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),

            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                painter = painterResource(icono),

                contentDescription = null,

                modifier = Modifier.size(30.dp)
            )

            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = detalle,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
