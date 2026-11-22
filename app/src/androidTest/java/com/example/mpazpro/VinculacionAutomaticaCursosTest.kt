package com.example.mpazpro

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.local.*
import com.example.mpazpro.data.remote.*
import com.example.mpazpro.data.repository.*
import java.io.IOException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test

class VinculacionAutomaticaCursosTest {
    private val catalogoId = "11111111-1111-1111-1111-111111111111"

    private class ServidorFalso(private val id: String) : ActividadesServidorApi, AsegurarCursoApi {
        var persistente = true
        var perderRespuesta = false
        var asegurarLlamadas = 0
        val cursos = mutableListOf(CursoCompartidoServidor(100, "3° A"))
        override suspend fun identidadCatalogo() = IdentidadCatalogoServidor(id, persistente)
        override suspend fun listarCursos() = cursos.toList()
        override suspend fun crearSiguienteCurso(): CursoCompartidoServidor = error("No se usa")
        override suspend fun listar() = emptyList<ActividadServidor>()
        override suspend fun asegurar(seccion: String, catalogoId: String): CursoCompartidoServidor {
            assertEquals(id, catalogoId)
            asegurarLlamadas++
            val curso = cursos.find { PropuestaVinculacionCursos.seccion(it.nombre) == seccion.single() }
                ?: CursoCompartidoServidor((100 + cursos.size).toLong(), "3° $seccion").also { cursos.add(it) }
            if (perderRespuesta) { perderRespuesta = false; throw IOException("Respuesta perdida") }
            return curso
        }
    }

    @Test fun vinculaExistenteYCreaFaltanteSinDuplicarTrasRespuestaPerdida() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext, MpazDatabase::class.java).build()
        try {
            val docente = db.docenteDao().insertar(DocenteEntity(nombre = "Docente", correo = "auto@example.com",
                claveHash = "h", claveSalt = "s", claveIteraciones = 1))
            val a = db.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3.º Básico A"))
            val b = db.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3. BASICO B"))
            val estudiante = db.estudianteDao().insertar(EstudianteEntity(cursoId = a, nombre = "Prueba", fotoRuta = "foto"))
            val actividad = db.actividadDao().insertar(BancoActividades.entidades(a).first())
            db.intentoActividadDao().insertar(IntentoActividadEntity(estudianteId = estudiante, actividadId = actividad,
                respuestaElegida = 1, respuestaCorrecta = 1, correcta = true))
            val servidor = ServidorFalso(catalogoId)
            val auto = VinculacionAutomaticaCursos(db, servidor, servidor)
            assertEquals(ResultadoConexionCurso.CONECTADO, auto.conectar(docente, a))
            assertEquals(100L, db.vinculoCursoDao().buscar(a)!!.cursoServidorId)
            assertEquals(0, servidor.asegurarLlamadas)
            servidor.perderRespuesta = true
            try { auto.conectar(docente, b); fail("Se esperaba error de conexión") } catch (_: IOException) {}
            assertNull(db.vinculoCursoDao().buscar(b))
            assertEquals(ResultadoConexionCurso.CONECTADO, auto.conectar(docente, b))
            assertEquals(101L, db.vinculoCursoDao().buscar(b)!!.cursoServidorId)
            assertEquals(1, servidor.asegurarLlamadas)
            assertEquals(2, servidor.cursos.size)
            assertEquals(ResultadoConexionCurso.CONECTADO, auto.conectar(docente, b))
            assertTrue(db.intentoActividadDao().buscar(estudiante, actividad)!!.correcta)
            assertEquals("foto", db.estudianteDao().observarPorCurso(a, docente).first().single().fotoRuta)
        } finally { db.close() }
    }

    @Test fun incorporaCursosExistentesUnaVezSinModificarEstudiantes() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext, MpazDatabase::class.java).build()
        try {
            val docente = db.docenteDao().insertar(DocenteEntity(nombre = "Docente", correo = "importar@example.com",
                claveHash = "h", claveSalt = "s", claveIteraciones = 1))
            val a = db.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3A"))
            val estudiante = db.estudianteDao().insertar(EstudianteEntity(cursoId = a, nombre = "Alumno", fotoRuta = "foto"))
            val servidor = ServidorFalso(catalogoId)
            servidor.cursos.add(CursoCompartidoServidor(101, "3° B"))
            val auto = VinculacionAutomaticaCursos(db, servidor, servidor)
            assertEquals(ResultadoImportacionCursos.ACTUALIZADO, auto.incorporarCompartidos(docente))
            val locales = db.cursoDao().listarPorDocente(docente)
            assertEquals(2, locales.size)
            val b = locales.single { PropuestaVinculacionCursos.seccion(it.nombre) == 'B' }
            assertEquals(101L, db.vinculoCursoDao().buscar(b.id)!!.cursoServidorId)
            assertEquals(a, db.estudianteDao().buscarCursoId(estudiante))
            assertEquals("foto", db.estudianteDao().observarPorCurso(a, docente).first().single().fotoRuta)
            assertEquals(ResultadoImportacionCursos.ACTUALIZADO, auto.incorporarCompartidos(docente))
            assertEquals(2, db.cursoDao().listarPorDocente(docente).size)
            assertEquals(0, servidor.asegurarLlamadas)
        } finally { db.close() }
    }

    @Test fun importacionAmbiguaNoCreaCursosNiModificaVinculos() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext, MpazDatabase::class.java).build()
        try {
            val docente = db.docenteDao().insertar(DocenteEntity(nombre = "Docente", correo = "importar-ambiguo@example.com",
                claveHash = "h", claveSalt = "s", claveIteraciones = 1))
            val a = db.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3A"))
            db.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3.º Básico A"))
            val servidor = ServidorFalso(catalogoId)
            servidor.cursos.add(CursoCompartidoServidor(101, "3° B"))
            val auto = VinculacionAutomaticaCursos(db, servidor, servidor)
            assertEquals(ResultadoImportacionCursos.REVISION, auto.incorporarCompartidos(docente))
            assertEquals(2, db.cursoDao().listarPorDocente(docente).size)
            assertNull(db.vinculoCursoDao().buscar(a))
            servidor.persistente = false
            assertEquals(ResultadoImportacionCursos.REVISION, auto.incorporarCompartidos(docente))
            assertEquals(2, db.cursoDao().listarPorDocente(docente).size)
        } finally { db.close() }
    }

    @Test fun vinculoExistenteInvalidoImpideImportarOtrosCursos() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext, MpazDatabase::class.java).build()
        try {
            val docente = db.docenteDao().insertar(DocenteEntity(nombre = "Docente", correo = "vinculoinvalido@example.com",
                claveHash = "h", claveSalt = "s", claveIteraciones = 1))
            val a = db.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3 A"))
            db.vinculoCursoDao().insertar(VinculoCursoEntity(a, catalogoId, 999))
            val servidor = ServidorFalso(catalogoId)
            servidor.cursos.add(CursoCompartidoServidor(101, "3° B"))
            val auto = VinculacionAutomaticaCursos(db, servidor, servidor)
            assertEquals(ResultadoImportacionCursos.REVISION, auto.incorporarCompartidos(docente))
            assertEquals(1, db.cursoDao().listarPorDocente(docente).size)
            assertEquals(999L, db.vinculoCursoDao().buscar(a)!!.cursoServidorId)
        } finally { db.close() }
    }

    @Test fun noVinculaAmbiguosNiSobrescribeOtroCatalogo() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext, MpazDatabase::class.java).build()
        try {
            val docente = db.docenteDao().insertar(DocenteEntity(nombre = "Docente", correo = "ambiguo@example.com",
                claveHash = "h", claveSalt = "s", claveIteraciones = 1))
            val a = db.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3A"))
            db.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3.º Básico A"))
            val b = db.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3 B"))
            val servidor = ServidorFalso(catalogoId)
            val auto = VinculacionAutomaticaCursos(db, servidor, servidor)
            assertEquals(ResultadoConexionCurso.REVISION, auto.conectar(docente, a))
            assertNull(db.vinculoCursoDao().buscar(a))
            assertEquals(0, servidor.asegurarLlamadas)
            db.vinculoCursoDao().insertar(VinculoCursoEntity(b, "22222222-2222-2222-2222-222222222222", 99))
            assertEquals(ResultadoConexionCurso.REVISION, auto.conectar(docente, b))
            assertEquals("22222222-2222-2222-2222-222222222222", db.vinculoCursoDao().buscar(b)!!.catalogoId)
            assertEquals(0, servidor.asegurarLlamadas)
        } finally { db.close() }
    }
}
