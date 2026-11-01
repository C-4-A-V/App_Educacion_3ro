package com.example.mpazpro.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.preferenciasDocente by preferencesDataStore(name = "preferencias_docente")

/** Cada cuenta local conserva su elección; null sigue el tema del sistema hasta elegir. */
class PreferenciasDocente internal constructor(private val store: DataStore<Preferences>) {
    constructor(context: Context) : this(context.applicationContext.preferenciasDocente)

    private fun clave(docenteId: Long): Preferences.Key<Boolean> {
        require(docenteId > 0)
        return booleanPreferencesKey("modo_oscuro_docente_$docenteId")
    }

    fun modoOscuro(docenteId: Long): Flow<Boolean?> {
        val clave = clave(docenteId)
        return store.data.map { it[clave] }.distinctUntilChanged()
    }

    suspend fun guardarModoOscuro(docenteId: Long, activado: Boolean) {
        val clave = clave(docenteId)
        store.edit { it[clave] = activado }
    }
}
