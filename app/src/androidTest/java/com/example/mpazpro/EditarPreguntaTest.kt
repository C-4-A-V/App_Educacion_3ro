package com.example.mpazpro

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.local.*
import com.example.mpazpro.data.remote.*
import com.example.mpazpro.data.repository.ActividadRepository
import com.example.mpazpro.model.ActividadFormulario
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class EditarPreguntaTest {
    @Test fun edicionRespetaPublicacionYNoModificaOtroCurso() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, MpazDatabase::class.java).build()
        try {
            val docente = db.docenteDao().insertar(DocenteEntity(nombre = "Docente", correo = "edicion@example.test",
                claveHash = "h", claveSalt = "s", claveIteraciones = 1))
            val cursoA = db.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3 A"))
            val cursoB = db.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3 B"))
            val catalogo = "11111111-1111-1111-1111-111111111111"
            db.vinculoCursoDao().insertar(VinculoCursoEntity(cursoA, catalogo, 42))
            val datos = ActividadFormulario(titulo = "Lectura", instrucciones = "Lee", pregunta = "¿Qué es un cuento?",
                alternativas = listOf("Historia", "Número", "Planta", "Suma"), respuestaCorrecta = 0,
                maxIntentos = 3, explicacion = "Una historia")
            val repo = ActividadRepository(db)
            val idA = repo.crearActividad(cursoA, "lenguaje", docente, datos)
            val idB = repo.crearActividad(cursoB, "lenguaje", docente, datos)
            val apiCatalogo = object : ActividadesServidorApi {
                override suspend fun identidadCatalogo() = IdentidadCatalogoServidor(catalogo, true)
                override suspend fun listarCursos() = emptyList<CursoCompartidoServidor>()
                override suspend fun crearSiguienteCurso(): CursoCompartidoServidor = error("No se usa")
                override suspend fun listar() = emptyList<ActividadServidor>()
            }
            var publicada = true
            val estado = object : EstadoPublicacionApi {
                override suspend fun estadoClave(catalogoId: String, clave: String) =
                    EstadoClavePublicacion(publicada = publicada, retirada = false)
            }
            try {
                repo.editarNoCompartida(idA, cursoA, "lenguaje", docente, datos.copy(titulo = "Nuevo"), apiCatalogo, estado)
                fail("No debe editar una publicación que llegó al servidor")
            } catch (_: IllegalStateException) {}
            assertEquals("Lectura", db.actividadDao().buscarPorId(idA)!!.titulo)
            publicada = false
            repo.editarNoCompartida(idA, cursoA, "lenguaje", docente, datos.copy(titulo = "Nuevo"), apiCatalogo, estado)
            assertEquals("Nuevo", db.actividadDao().buscarPorId(idA)!!.titulo)
            assertEquals("Lectura", db.actividadDao().buscarPorId(idB)!!.titulo)
            val estudiante = db.estudianteDao().insertar(EstudianteEntity(cursoId = cursoB, nombre = "Perfil"))
            db.intentoActividadDao().insertar(IntentoActividadEntity(
                estudianteId = estudiante, actividadId = idB, respuestaElegida = 0,
                respuestaCorrecta = 0, correcta = true
            ))
            try {
                repo.editarNoCompartida(idB, cursoB, "lenguaje", docente, datos.copy(titulo = "Otro"), apiCatalogo, estado)
                fail("No debe alterar una pregunta con respuestas anteriores")
            } catch (_: IllegalStateException) {}
            assertEquals("Lectura", db.actividadDao().buscarPorId(idB)!!.titulo)
            db.publicacionPendienteDao().marcarPublicada(idA, 7)
            try {
                repo.editarNoCompartida(idA, cursoA, "lenguaje", docente, datos, apiCatalogo, estado)
                fail("No debe editar una pregunta compartida")
            } catch (_: IllegalStateException) {}
            assertEquals("Nuevo", db.actividadDao().buscarPorId(idA)!!.titulo)
        } finally { db.close() }
    }
}
