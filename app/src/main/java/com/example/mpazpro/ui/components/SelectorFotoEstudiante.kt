package com.example.mpazpro.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.mpazpro.data.media.FotoStorage
import kotlinx.coroutines.launch

@Composable
fun SelectorFotoEstudiante(
    fotoRuta: String?,
    errorFoto: String?,
    habilitado: Boolean,
    onSeleccion: (Uri) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val almacenamiento = remember(context) { FotoStorage(context) }
    val scope = rememberCoroutineScope()

    var capturaUri by rememberSaveable { mutableStateOf<String?>(null) }
    var esperandoResultado by rememberSaveable { mutableStateOf(false) }

    val camara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { aceptada ->
        esperandoResultado = false

        val uri = capturaUri?.let { Uri.parse(it) }
        capturaUri = null

        if (aceptada && uri != null) {
            onSeleccion(uri)
        } else if (uri != null) {
            scope.launch { almacenamiento.descartarTemporal(uri) }
        }
        // Si cancela, conservamos la fotografía anterior.
    }

    fun abrirCamara() {
        try {
            val uri = almacenamiento.crearTemporalCamara()
            capturaUri = uri.toString()
            esperandoResultado = true
            camara.launch(uri)
        } catch (_: Exception) {
            capturaUri = null
            esperandoResultado = false
            onError("No se pudo abrir la cámara.")
        }
    }

    val permisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            abrirCamara()
        } else {
            esperandoResultado = false
            onError(
                "Permiso de cámara denegado. Puedes habilitarlo " +
                    "en Ajustes o seleccionar una imagen de galería."
            )
        }
    }

    val galeria = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        esperandoResultado = false

        if (uri != null) {
            onSeleccion(uri)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ImagenInteligente(
            ruta = fotoRuta,
            descripcion = "Fotografía seleccionada"
        )

        OutlinedButton(
            enabled = habilitado && !esperandoResultado,
            onClick = {
                val concedido = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED

                if (concedido) {
                    abrirCamara()
                } else {
                    esperandoResultado = true
                    permisoCamara.launch(Manifest.permission.CAMERA)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Tomar foto")
        }

        OutlinedButton(
            enabled = habilitado && !esperandoResultado,
            onClick = {
                try {
                    esperandoResultado = true
                    galeria.launch("image/*")
                } catch (_: Exception) {
                    esperandoResultado = false
                    onError("No se pudo abrir el selector de imágenes.")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Elegir de galería")
        }

        errorFoto?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
