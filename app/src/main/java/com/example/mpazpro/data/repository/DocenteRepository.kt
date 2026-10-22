package com.example.mpazpro.data.repository

import com.example.mpazpro.data.local.DocenteDao
import com.example.mpazpro.data.local.DocenteEntity
import com.example.mpazpro.model.DocenteSesion
import com.example.mpazpro.security.PasswordHasher
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Los dos resultados esperados al intentar registrar una cuenta.
sealed class ResultadoRegistroDocente {

    data class Registrado(
        val docenteId: Long
    ) : ResultadoRegistroDocente()

    data object CorreoExistente : ResultadoRegistroDocente()
}

class DocenteRepository(
    private val docenteDao: DocenteDao
) {

    suspend fun registrar(
        nombre: String,
        correo: String,
        clave: String
    ): ResultadoRegistroDocente = withContext(Dispatchers.Default) {

        // Aplicaremos la misma normalización al iniciar sesión.
        val correoNormalizado = correo
            .trim()
            .lowercase(Locale.ROOT)

        // Evita hacer el cálculo de contraseña si el correo ya existe.
        val existente = docenteDao.buscarPorCorreo(correoNormalizado)

        if (existente != null) {
            return@withContext ResultadoRegistroDocente.CorreoExistente
        }

        // El cálculo se ejecuta fuera del hilo de la interfaz.
        val protegida = PasswordHasher.generar(clave)

        val docente = DocenteEntity(
            nombre = nombre.trim(),
            correo = correoNormalizado,
            claveHash = protegida.hash,
            claveSalt = protegida.salt,
            claveIteraciones = protegida.iteraciones
        )

        val idGenerado = docenteDao.insertar(docente)

        // El índice único también protege frente a registros simultáneos.
        if (idGenerado == -1L) {
            ResultadoRegistroDocente.CorreoExistente
        } else {
            ResultadoRegistroDocente.Registrado(idGenerado)
        }
    }

    suspend fun autenticar(
        correo: String,
        clave: String
    ): DocenteSesion? = withContext(Dispatchers.Default) {

        val correoNormalizado = correo
            .trim()
            .lowercase(Locale.ROOT)

        val docente = docenteDao.buscarPorCorreo(correoNormalizado)
            ?: return@withContext null

        val coincide = PasswordHasher.verificar(
            clave = clave,
            hashGuardado = docente.claveHash,
            saltGuardado = docente.claveSalt,
            iteraciones = docente.claveIteraciones
        )

        if (coincide) {
            DocenteSesion(
                id = docente.id,
                nombre = docente.nombre,
                correo = docente.correo
            )
        } else {
            null
        }
    }
}
