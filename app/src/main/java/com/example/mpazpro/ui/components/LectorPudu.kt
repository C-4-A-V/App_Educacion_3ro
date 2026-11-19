package com.example.mpazpro.ui.components

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.Locale
import java.util.UUID

class LectorPudu(private val context: Context) {
    var listo by mutableStateOf(false)
        private set
    var leyendo by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    private val principal = Handler(Looper.getMainLooper())
    private var motor: TextToSpeech? = null
    private var activo: String? = null
    private var cerrado = false

    fun iniciar() {
        motor = TextToSpeech(context.applicationContext) { estado ->
            principal.post {
                if (!cerrado) {
                    val tts = motor
                    val idioma = if (estado == TextToSpeech.SUCCESS && tts != null) {
                        val chileno = tts.setLanguage(Locale.forLanguageTag("es-CL"))
                        if (chileno < 0) tts.setLanguage(Locale.forLanguageTag("es")) else chileno
                    } else TextToSpeech.LANG_NOT_SUPPORTED
                    listo = idioma >= 0
                    if (!listo) error = "La lectura en español no está disponible en este dispositivo."
                }
            }
        }.also { tts ->
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) = Unit
                override fun onDone(utteranceId: String?) { terminar(utteranceId, false) }
                @Deprecated("Usado por versiones anteriores de Android")
                override fun onError(utteranceId: String?) { terminar(utteranceId, true) }
                override fun onError(utteranceId: String?, errorCode: Int) { terminar(utteranceId, true) }
            })
        }
    }

    private fun terminar(id: String?, fallo: Boolean) {
        principal.post {
            if (!cerrado && activo == id && id != null) {
                leyendo = false
                activo = null
                if (fallo) error = "No pudimos leer este dato. Inténtalo de nuevo."
            }
        }
    }

    fun leer(texto: String) {
        if (!listo) return
        detener()
        error = null
        val id = UUID.randomUUID().toString()
        activo = id
        leyendo = true
        if (motor?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, id) != TextToSpeech.SUCCESS) {
            activo = null
            leyendo = false
            error = "No pudimos leer este dato. Inténtalo de nuevo."
        }
    }

    fun detener() {
        activo = null
        leyendo = false
        motor?.stop()
    }

    fun cerrar() {
        cerrado = true
        detener()
        motor?.shutdown()
        motor = null
        listo = false
    }
}

@Composable
fun recordarLectorPudu(datoId: String?, tema: Any, cargando: Boolean): LectorPudu {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val lector = remember(context) { LectorPudu(context) }
    DisposableEffect(lector, lifecycle) {
        lector.iniciar()
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_STOP) lector.detener()
        }
        lifecycle.addObserver(observador)
        onDispose {
            lifecycle.removeObserver(observador)
            lector.cerrar()
        }
    }
    LaunchedEffect(datoId, tema, cargando) { lector.detener() }
    return lector
}
