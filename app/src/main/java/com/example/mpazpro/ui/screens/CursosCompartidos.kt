package com.example.mpazpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mpazpro.data.repository.PropuestaVinculacionCursos

data class CursoPropuesto(val localId: Long, val remotoId: Long, val nombre: String, val estado: String)

@Composable
internal fun TarjetaCursoPropuesto(propuesta: CursoPropuesto, trabajando: Boolean, onVincular: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("${PropuestaVinculacionCursos.nombreVisible(propuesta.nombre)} (dispositivo) → ${PropuestaVinculacionCursos.nombreVisible("3° ${PropuestaVinculacionCursos.seccion(propuesta.nombre)}")} (compartido)")
            when (propuesta.estado) {
                "vinculado" -> Text("Ya vinculado")
                "otro" -> Text("Ya vinculado a otro catálogo; requiere revisión.")
                "temporal" -> Text("No disponible con un catálogo temporal.")
                "disponible" -> Button(onClick = onVincular, enabled = !trabajando) { Text("Vincular curso") }
                else -> Text("No se puede vincular este curso.")
            }
        }
    }
}
