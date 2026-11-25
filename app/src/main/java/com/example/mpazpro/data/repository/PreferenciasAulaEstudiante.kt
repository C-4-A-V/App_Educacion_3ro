package com.example.mpazpro.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.util.UUID
import kotlinx.coroutines.flow.first

private val Context.preferenciasAula by preferencesDataStore(name = "aula_estudiante")

data class IntentoAulaPendiente(val numero: Int, val respuestaElegida: Int)
data class RevisionAulaAnterior(val tokenAnterior: String?, val actividadesPendientes: List<Long>)

/** Identificadores aleatorios y reintentos locales; nunca almacena RUT ni PIN. */
class PreferenciasAulaEstudiante internal constructor(private val store: DataStore<Preferences>) {
    constructor(context: Context) : this(context.applicationContext.preferenciasAula)

    private fun prefijo(estudianteId: Long, catalogoId: String): String {
        require(estudianteId > 0)
        require(UUID.fromString(catalogoId).toString() == catalogoId)
        return "e_${estudianteId}_c_${catalogoId.replace("-", "")}_"
    }

    suspend fun tokenExistente(estudianteId: Long, catalogoId: String): String? =
        store.data.first()[stringPreferencesKey(prefijo(estudianteId, catalogoId) + "token")]

    /** Revisión local sin crear identificadores ni modificar respuestas pendientes. */
    suspend fun revisarMigracion(estudianteId: Long, catalogoId: String): RevisionAulaAnterior {
        val prefijo = prefijo(estudianteId, catalogoId)
        val datos = store.data.first()
        val token = datos[stringPreferencesKey(prefijo + "token")]
        val pendientes = datos.asMap().keys
            .filter { it.name.startsWith(prefijo + "a_") && it.name.endsWith("_pendiente") }
            .map { clave ->
                val textoId = clave.name.removePrefix(prefijo + "a_").removeSuffix("_pendiente")
                val id = textoId.toLongOrNull()
                check(id != null && id > 0) { "Identificador de respuesta pendiente inválido" }
                id
            }.sorted()
        return RevisionAulaAnterior(token, pendientes)
    }

    suspend fun token(estudianteId: Long, catalogoId: String): String {
        val clave = stringPreferencesKey(prefijo(estudianteId, catalogoId) + "token")
        val preferencias = store.edit { datos ->
            if (datos[clave] == null) datos[clave] = UUID.randomUUID().toString()
        }
        return requireNotNull(preferencias[clave])
    }

    private fun claveIntento(estudianteId: Long, catalogoId: String, actividadId: Long): Preferences.Key<String> {
        require(actividadId > 0)
        return stringPreferencesKey(prefijo(estudianteId, catalogoId) + "a_${actividadId}_pendiente")
    }

    suspend fun pendiente(estudianteId: Long, catalogoId: String, actividadId: Long): IntentoAulaPendiente? {
        val valor = store.data.first()[claveIntento(estudianteId, catalogoId, actividadId)] ?: return null
        val partes = valor.split(":")
        check(partes.size == 2)
        val numero = partes[0].toInt()
        val respuesta = partes[1].toInt()
        check(numero in 1..3 && respuesta in 0..3)
        return IntentoAulaPendiente(numero, respuesta)
    }

    suspend fun preparar(estudianteId: Long, catalogoId: String, actividadId: Long, intento: IntentoAulaPendiente) {
        require(intento.numero in 1..3 && intento.respuestaElegida in 0..3)
        val clave = claveIntento(estudianteId, catalogoId, actividadId)
        val valor = "${intento.numero}:${intento.respuestaElegida}"
        store.edit { datos ->
            check(datos[clave] == null || datos[clave] == valor) { "Hay una respuesta pendiente distinta" }
            datos[clave] = valor
        }
    }

    suspend fun confirmar(estudianteId: Long, catalogoId: String, actividadId: Long, intento: IntentoAulaPendiente) {
        val clave = claveIntento(estudianteId, catalogoId, actividadId)
        val valor = "${intento.numero}:${intento.respuestaElegida}"
        store.edit { datos -> if (datos[clave] == valor) datos.remove(clave) }
    }
}
