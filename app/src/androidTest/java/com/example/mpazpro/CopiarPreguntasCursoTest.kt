package com.example.mpazpro

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.local.*
import com.example.mpazpro.data.remote.*
import com.example.mpazpro.data.repository.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class CopiarPreguntasCursoTest {
    @Test fun copiaCursosSeparadosYReintentaSinDuplicar() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext, MpazDatabase::class.java
        ).build()
        try {
            val docente = db.docenteDao().insertar(DocenteEntity(
                nombre = "Docente", correo = "copias@example.com", claveHash = "h", claveSalt = "s", claveIteraciones = 1))
            val cursoA = db.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3 A"))
            val cursoB = db.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3 B"))
            val cursoC = db.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3 C"))
            val catalogo = "11111111-1111-1111-1111-111111111111"
            db.vinculoCursoDao().insertar(VinculoCursoEntity(cursoB, catalogo, 42))
            val origen = db.actividadDao().insertar(BancoActividades.entidades(cursoA).first())
            val repo = ActividadRepository(db)
            try {
                repo.copiarParaCurso(origen, cursoA, cursoC, "lenguaje", docente)
                fail("No debe copiar a un curso sin vincular")
            } catch (_: IllegalArgumentException) {}
            assertTrue(repo.observarActividades(cursoC, "lenguaje", docente).first().isEmpty())
            val copiaB = repo.copiarParaCurso(origen, cursoA, cursoB, "lenguaje", docente)
            assertNotEquals(origen, copiaB)
            assertEquals(cursoB, db.actividadDao().buscarPorId(copiaB)!!.cursoId)
            val clave = db.publicacionPendienteDao().buscar(copiaB)!!.clavePublicacion
            var intentos = 0
            val api = object : PublicacionesApi {
                override suspend fun publicar(clave: String, catalogoId: String, solicitud: SolicitudPublicacion): RespuestaPublicacion {
                    intentos++
                    assertEquals(42L, solicitud.cursoId)
                    if (intentos == 1) throw IOException("Respuesta perdida")
                    return RespuestaPublicacion(70, false)
                }
            }
            val catalogoApi = object : ActividadesServidorApi {
                override suspend fun identidadCatalogo() = IdentidadCatalogoServidor(catalogo, true)
                override suspend fun listarCursos() = emptyList<CursoCompartidoServidor>()
                override suspend fun crearSiguienteCurso(): CursoCompartidoServidor = error("No se usa")
                override suspend fun listar() = emptyList<ActividadServidor>()
            }
            val publicador = PublicadorActividad(db, catalogoApi, api)
            try { publicador.publicar(copiaB, docente); fail("Se esperaba un fallo") } catch (_: IOException) {}
            assertEquals(copiaB, repo.copiarParaCurso(origen, cursoA, cursoB, "lenguaje", docente))
            assertEquals(clave, db.publicacionPendienteDao().buscar(copiaB)!!.clavePublicacion)
            assertEquals(70L, publicador.publicar(copiaB, docente))
            assertEquals(1, repo.observarActividades(cursoB, "lenguaje", docente).first().size)
            db.vinculoCursoDao().insertar(VinculoCursoEntity(cursoC, catalogo, 43))
            val copiaC = repo.copiarParaCurso(origen, cursoA, cursoC, "lenguaje", docente)
            assertNotEquals(copiaB, copiaC)
            assertEquals(43L, db.publicacionPendienteDao().buscar(copiaC)!!.cursoServidorId)
            try { repo.copiarParaCurso(origen, cursoA, cursoB, "ciencias", docente); fail("Asignatura incorrecta") }
            catch (_: IllegalArgumentException) {}
            db.actividadRetiradaDao().insertar(listOf(ActividadRetiradaEntity(copiaB)))
            try { repo.copiarParaCurso(origen, cursoA, cursoB, "lenguaje", docente); fail("No debe reactivar una copia retirada") }
            catch (_: IllegalStateException) {}
        } finally { db.close() }
    }
}
