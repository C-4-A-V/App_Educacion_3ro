package com.example.mpazpro.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.preferenciasEstudiante by preferencesDataStore(name = "preferencias_estudiante")

data class PreferenciasPerfilEstudiante(
    val modoOscuro: Boolean? = null,
    val textoGrande: Boolean = false,
    val objetivo: String = ""
) {
    companion object {
        const val MAX_OBJETIVO = 160
    }
}

/** Preferencias locales independientes para cada perfil del estudiante. */
class PreferenciasEstudiante internal constructor(private val store: DataStore<Preferences>) {
    constructor(context: Context) : this(context.applicationContext.preferenciasEstudiante)
    private fun prefijo(id: Long): String { require(id > 0); return "estudiante_${id}_" }

    fun observar(id: Long) = store.data.map { datos ->
        val p = prefijo(id)
        PreferenciasPerfilEstudiante(
            modoOscuro = datos[booleanPreferencesKey(p + "oscuro")],
            textoGrande = datos[booleanPreferencesKey(p + "texto_grande")] ?: false,
            objetivo = datos[stringPreferencesKey(p + "objetivo")].orEmpty()
        )
    }.distinctUntilChanged()

    suspend fun guardarOscuro(id: Long, valor: Boolean) {
        store.edit { it[booleanPreferencesKey(prefijo(id) + "oscuro")] = valor }
    }
    suspend fun guardarTextoGrande(id: Long, valor: Boolean) {
        store.edit { it[booleanPreferencesKey(prefijo(id) + "texto_grande")] = valor }
    }
    suspend fun guardarObjetivo(id: Long, valor: String) {
        val objetivo = valor.trim()
        require(objetivo.length in 1..PreferenciasPerfilEstudiante.MAX_OBJETIVO)
        store.edit { it[stringPreferencesKey(prefijo(id) + "objetivo")] = objetivo }
    }
}
