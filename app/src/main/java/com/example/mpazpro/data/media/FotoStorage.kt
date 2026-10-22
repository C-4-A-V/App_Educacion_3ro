package com.example.mpazpro.data.media

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FotoStorage(context: Context) {

    private val appContext = context.applicationContext

    fun crearTemporalCamara(): Uri {
        val carpeta = File(appContext.cacheDir, "camara")
        check(carpeta.isDirectory || carpeta.mkdirs())

        val archivo = File.createTempFile("captura_", ".jpg", carpeta)

        return FileProvider.getUriForFile(
            appContext,
            "${appContext.packageName}.fileprovider",
            archivo
        )
    }

    // Copia el contenido mientras tenemos permiso para leer el URI.
    // La foto permanente no dependerá de permisos futuros de galería.
    suspend fun importar(uri: Uri): String = withContext(Dispatchers.IO) {
        val carpeta = File(appContext.filesDir, "fotos_estudiantes")
        check(carpeta.isDirectory || carpeta.mkdirs())

        val destino = File(carpeta, "${UUID.randomUUID()}.img")

        try {
            val entrada = appContext.contentResolver.openInputStream(uri)
                ?: error("No se pudo abrir la imagen")

            entrada.use { origen ->
                destino.outputStream().use { salida ->
                    val buffer = ByteArray(8192)
                    var total = 0L

                    while (true) {
                        val leidos = origen.read(buffer)
                        if (leidos == -1) break

                        total += leidos
                        require(total <= 10L * 1024 * 1024) {
                            "La imagen supera los 10 MB"
                        }

                        salida.write(buffer, 0, leidos)
                    }
                }
            }

            // Comprueba las dimensiones sin cargar el bitmap completo.
            val opciones = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            BitmapFactory.decodeFile(destino.absolutePath, opciones)

            require(opciones.outWidth > 0 && opciones.outHeight > 0) {
                "Formato de imagen no compatible"
            }

            destino.absolutePath
        } catch (error: Exception) {
            destino.delete()
            throw error
        }
    }
}
