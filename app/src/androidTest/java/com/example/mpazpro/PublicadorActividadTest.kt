package com.example.mpazpro

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.local.*
import com.example.mpazpro.data.remote.*
import com.example.mpazpro.data.repository.ActividadRepository
import com.example.mpazpro.data.repository.PublicadorActividad
import com.example.mpazpro.model.ActividadFormulario
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class PublicadorActividadTest {
    @Test fun publicaSoloSeleccionConReintentoSeguro() = runBlocking {
        val base = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, MpazDatabase::class.java).build()
        try {
            val docente = base.docenteDao().insertar(DocenteEntity(
                nombre = "Docente", correo = "seleccion@example.com", claveHash = "h", claveSalt = "s", claveIteraciones = 1))
            val curso = base.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3 A"))
            val otroCurso = base.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3 B"))
            val identidad = "11111111-1111-1111-1111-111111111111"
            base.vinculoCursoDao().insertar(VinculoCursoEntity(curso, identidad, 42))
            val banco = BancoActividades.entidades(curso).filter { it.asignaturaId == "lenguaje" }
            val ids = banco.take(3).map { base.actividadDao().insertar(it) }
            val ajena = base.actividadDao().insertar(banco.first().copy(id = 0, cursoId = otroCurso))
            val claves = mutableMapOf<String, Long>()
            var perderRespuesta = true
            val api = object : PublicacionesApi {
                override suspend fun publicar(clave: String, catalogoId: String, solicitud: SolicitudPublicacion): RespuestaPublicacion {
                    val remoto = claves.getOrPut(clave) { (claves.size + 1).toLong() }
                    if (perderRespuesta) { perderRespuesta = false; throw IOException("Respuesta perdida") }
                    return RespuestaPublicacion(remoto, false)
                }
            }
            val publicador = PublicadorActividad(base, CatalogoFalso(identidad), api)
            try {
                publicador.publicarSeleccion(listOf(ids[0], ajena), curso, "lenguaje", docente)
                fail("Debe rechazar toda la selección si hay una pregunta de otro curso")
            } catch (_: IllegalArgumentException) {}
            assertTrue(claves.isEmpty())
            try {
                publicador.publicarSeleccion(ids.take(2), curso, "lenguaje", docente)
                fail("Se esperaba respuesta perdida")
            } catch (_: IOException) {}
            assertEquals(1, claves.size)
            val primeraClave = base.publicacionPendienteDao().buscar(ids[0])!!.clavePublicacion
            var confirmadas = 0
            publicador.publicarSeleccion(ids.take(2), curso, "lenguaje", docente) { confirmadas++ }
            assertEquals(2, confirmadas)
            assertEquals(2, claves.size)
            assertEquals(primeraClave, base.publicacionPendienteDao().buscar(ids[0])!!.clavePublicacion)
            assertNull(base.publicacionPendienteDao().buscar(ids[2]))
            assertNull(base.publicacionPendienteDao().buscar(ajena))
        } finally { base.close() }
    }

    @Test fun respuestaPerdidaSeReintentaConLaMismaClaveYNoSePierdeLaActividad() = runBlocking {
        val base = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, MpazDatabase::class.java).build()
        try {
            val docente = base.docenteDao().insertar(DocenteEntity(
                nombre = "Docente", correo = "test@example.com", claveHash = "h", claveSalt = "s", claveIteraciones = 1
            ))
            val curso = base.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3 A"))
            val identidad = "11111111-1111-1111-1111-111111111111"
            base.vinculoCursoDao().insertar(VinculoCursoEntity(curso, identidad, 42))
            val id = ActividadRepository(base).crearActividad(curso, "lenguaje", docente,
                ActividadFormulario(titulo = "Lectura", instrucciones = "Lee", pregunta = "¿Qué es un cuento?",
                    alternativas = listOf("Historia", "Número", "Árbol", "Suma"), respuestaCorrecta = 0,
                    maxIntentos = 3, explicacion = "Es una historia"))
            val api = PublicacionesFalsas()
            val publicador = PublicadorActividad(base, CatalogoFalso(identidad), api)
            try {
                publicador.publicar(id, docente)
                fail("Se esperaba una respuesta perdida")
            } catch (_: IOException) {}
            assertNotNull(base.actividadDao().buscarPorId(id))
            assertNull(base.publicacionPendienteDao().buscar(id)!!.actividadServidorId)
            assertEquals(7L, publicador.publicar(id, docente))
            assertEquals(7L, base.publicacionPendienteDao().buscar(id)!!.actividadServidorId)
            assertEquals(1, api.claves.size)
            assertEquals(2, api.intentos)
        } finally { base.close() }
    }

    private class CatalogoFalso(private val id: String) : ActividadesServidorApi {
        override suspend fun identidadCatalogo() = IdentidadCatalogoServidor(id, true)
        override suspend fun listarCursos() = emptyList<CursoCompartidoServidor>()
        override suspend fun crearSiguienteCurso() = error("No se usa")
        override suspend fun listar() = emptyList<ActividadServidor>()
    }

    private class PublicacionesFalsas : PublicacionesApi {
        val claves = mutableSetOf<String>()
        var intentos = 0
        override suspend fun publicar(clave: String, catalogoId: String, solicitud: SolicitudPublicacion): RespuestaPublicacion {
            intentos++
            val creada = claves.add(clave)
            if (intentos == 1) throw IOException("Respuesta perdida")
            return RespuestaPublicacion(7, creada)
        }
    }
}
