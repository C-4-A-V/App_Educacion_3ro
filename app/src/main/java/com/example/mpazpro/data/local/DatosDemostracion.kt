package com.example.mpazpro.data.local

import androidx.room.withTransaction
import com.example.mpazpro.security.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Carga aditiva: nunca reemplaza cuentas, cursos, actividades o respuestas existentes. */
object DatosDemostracion {
    const val CORREO = "milo@gmail.com"
    const val CLAVE_INICIAL = "Alegria2002"

    suspend fun preparar(database: MpazDatabase): Long = withContext(Dispatchers.Default) {
        // El hash se calcula fuera de la transacción y fuera del hilo principal.
        val protegida = if (database.docenteDao().buscarPorCorreo(CORREO) == null) {
            PasswordHasher.generar(CLAVE_INICIAL)
        } else null
        database.withTransaction {
            val docentes = database.docenteDao()
            var docente = docentes.buscarPorCorreo(CORREO)
            if (docente == null) {
                val hash = checkNotNull(protegida)
                docentes.insertar(DocenteEntity(
                    nombre = "Milo", correo = CORREO,
                    claveHash = hash.hash, claveSalt = hash.salt, claveIteraciones = hash.iteraciones
                ))
                docente = checkNotNull(docentes.buscarPorCorreo(CORREO))
            }
            val cursos = database.cursoDao()
            val cursoId = cursos.buscarPrimero(docente.id)?.id ?: cursos.insertar(
                CursoEntity(docenteId = docente.id, nombre = "3.º Básico A")
            )
            check(cursoId > 0)
            val dao = database.actividadDao()
            BancoActividades.entidades(cursoId).forEach { actividad ->
                if (dao.buscarPredeterminada(cursoId, actividad.asignaturaId, actividad.titulo) == null) {
                    dao.insertar(actividad)
                }
            }
            cursoId
        }
    }
}
