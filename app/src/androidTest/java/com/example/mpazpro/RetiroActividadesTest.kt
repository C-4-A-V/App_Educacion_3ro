package com.example.mpazpro

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.local.*
import com.example.mpazpro.data.remote.*
import com.example.mpazpro.data.repository.*
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class RetiroActividadesTest {
    @Test fun retiraSeleccionSinBorrarRespuestasYReintentaError() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext, MpazDatabase::class.java
        ).build()
        try {
            val docente = db.docenteDao().insertar(DocenteEntity(
                nombre = "Docente", correo = "retiro@example.com", claveHash = "h", claveSalt = "s", claveIteraciones = 1))
            val curso = db.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3 A"))
            val estudiante = db.estudianteDao().insertar(EstudianteEntity(cursoId = curso, nombre = "Prueba"))
            val identidad = "11111111-1111-1111-1111-111111111111"
            db.vinculoCursoDao().insertar(VinculoCursoEntity(curso, identidad, 42))
            val banco = BancoActividades.entidades(curso).filter { it.asignaturaId == "lenguaje" }
            val ids = banco.take(3).map { db.actividadDao().insertar(it) }
            val actividades = ActividadRepository(db)
            val publicacion = actividades.prepararPublicacion(ids[0], docente)
            db.publicacionPendienteDao().marcarPublicada(ids[0], 7)
            val pendiente = actividades.prepararPublicacion(ids[1], docente)
            db.intentoActividadDao().insertar(IntentoActividadEntity(estudianteId = estudiante, actividadId = ids[0],
                respuestaElegida = 0, respuestaCorrecta = 0, correcta = true))
            var falla = true
            var solicitud: SolicitudRetiro? = null
            val servidor = object : RetiroPublicacionesApi {
                override suspend fun retirar(catalogoId: String, datos: SolicitudRetiro) {
                    if (falla) throw IOException("sin conexión")
                    assertEquals(identidad, catalogoId)
                    solicitud = datos
                }
            }
            val catalogo = object : ActividadesServidorApi {
                override suspend fun identidadCatalogo() = IdentidadCatalogoServidor(identidad, true)
                override suspend fun listarCursos() = emptyList<CursoCompartidoServidor>()
                override suspend fun crearSiguienteCurso(): CursoCompartidoServidor = error("No se usa")
                override suspend fun listar() = emptyList<ActividadServidor>()
            }
            val retiro = RetiroActividadesRepository(db, catalogo, servidor)
            try {
                retiro.retirar(curso, "lenguaje", docente, ids.take(2))
                fail("No debe ocultar las preguntas si el servidor falla")
            } catch (_: IOException) {}
            assertFalse(db.actividadRetiradaDao().existe(ids[0]))
            assertFalse(db.actividadRetiradaDao().existe(ids[1]))
            falla = false
            retiro.retirar(curso, "lenguaje", docente, ids.take(2))
            assertEquals(setOf(publicacion.clavePublicacion, pendiente.clavePublicacion), solicitud!!.claves.toSet())
            assertEquals(listOf(ids[2]), actividades.observarActividades(curso, "lenguaje", docente).first().map { it.id })
            assertNotNull(db.intentoActividadDao().buscar(estudiante, ids[0]))
            assertNotNull(db.actividadDao().buscarPorId(ids[0]))
            assertTrue(db.publicacionPendienteDao().pendientes().isEmpty())
            assertEquals(1, actividades.prepararBancoParaPublicacion(curso, docente, "lenguaje"))
            assertTrue(db.actividadRetiradaDao().existe(ids[0]))
        } finally { db.close() }
    }
}
