package com.example.mpazpro.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.preferencias by preferencesDataStore(name = "preferencias_lectura")

/** Preferencias pequeñas separadas de las entidades académicas de Room. */
class PreferenciasLectura internal constructor(private val store: DataStore<Preferences>) {
    constructor(context: Context) : this(context.applicationContext.preferencias)
    private val clave = booleanPreferencesKey("lectura_comoda")
    val modoLectura = store.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }.map { it[clave] ?: false }

    suspend fun guardar(activado: Boolean) {
        store.edit { it[clave] = activado }
    }
}
