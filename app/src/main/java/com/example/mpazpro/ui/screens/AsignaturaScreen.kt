package com.example.mpazpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mpazpro.model.Asignatura
import com.example.mpazpro.model.PerfilEstudiante

@Composable
fun AsignaturaScreen(
    asignatura: Asignatura,
    estudiante: PerfilEstudiante,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            TextButton(onClick = onVolver) {
                Text("Volver a mis asignaturas")
            }
        }

        item {
            Text(
                text = asignatura.titulo,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Text(
                text = estudiante.cursoNombre,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = asignatura.descripcion,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Text(
                text = "Mis actividades",
                style = MaterialTheme.typography.titleLarge
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Sin actividades asignadas",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Aquí aparecerán las actividades que tu docente prepare para esta asignatura.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}