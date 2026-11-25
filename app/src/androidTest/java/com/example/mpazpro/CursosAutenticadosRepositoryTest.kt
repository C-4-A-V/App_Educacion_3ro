package com.example.mpazpro

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.local.*
import com.example.mpazpro.data.remote.*
import com.example.mpazpro.data.repository.*
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Assert.*
import org.junit.Test

class CursosAutenticadosRepositoryTest {
    @Test fun resultadoInciertoReutilizaClaveYNoDuplicaCurso() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext,
            MpazDatabase::class.java).build()
        val server = MockWebServer()
        server.start()
        try {
            val catalogoId = UUID.randomUUID().toString()
            val local = db.docenteDao().insertar(DocenteEntity.soporteRemoto("uno@ejemplo.cl"))
            db.identidadDocenteDao().insertar(IdentidadDocenteEntity(catalogoId, 1, local))
            val remoto = DocenteRemotoRepository(ClienteDocenteAutenticado(server.url("/").newBuilder()
                .host("127.0.0.1").build().toString()))
            val catalogo = catalogo(catalogoId)
            server.enqueue(perfil(1, "uno@ejemplo.cl").addHeader("Set-Cookie", "JSESSIONID=uno; Path=/"))
            remoto.entrar("uno@ejemplo.cl", "Clave-demo-2026")
            val repo = CursosAutenticadosRepository(db, remoto, catalogo)
            sesiones(server, 1, "uno@ejemplo.cl")
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))
            try { repo.crear(local, 'A'); fail("Debe informar conexión incierta") } catch (_: IOException) {}
            val pendiente = requireNotNull(db.cursoAutenticadoDao().buscarPendiente(catalogoId, 1, "A"))
            assertTrue(db.cursoDao().listarPorDocente(local).isEmpty())

            sesiones(server, 1, "uno@ejemplo.cl")
            server.enqueue(json("""[{"id":11,"nombre":"3° A"}]"""))
            val listado = repo.actualizar(local)
            assertEquals(1, listado.pendientes)
            assertEquals(1, listado.noVinculados.size)
            assertTrue(db.cursoDao().listarPorDocente(local).isEmpty())

            sesiones(server, 1, "uno@ejemplo.cl")
            server.enqueue(json("""{"id":11,"nombre":"3° A"}"""))
            server.enqueue(json("""[{"id":11,"nombre":"3° A"}]"""))
            // Una instancia nueva representa la recreación del repositorio sin perder Room.
            val repositorioRestaurado = CursosAutenticadosRepository(db, remoto, catalogo)
            repositorioRestaurado.reintentarPendientes(local)
            val cursos = db.cursoDao().listarPorDocente(local)
            assertEquals(1, cursos.size)
            assertEquals(cursos.single().id, db.cursoAutenticadoDao().buscarVinculo(catalogoId, 1, 11)?.cursoLocalId)
            assertNull(db.cursoAutenticadoDao().buscarPendiente(catalogoId, 1, "A"))
            val solicitudes = (1..11).map { server.takeRequest() }
                .filter { it.path == "/api/docentes/cursos" && it.method == "POST" }
            assertEquals(2, solicitudes.size)
            assertTrue(solicitudes.all { it.getHeader("Idempotency-Key") == pendiente.clave })
        } finally { db.close(); server.shutdown() }
    }

    @Test fun colisionNoAtribuyeCursoAnteriorNiMezclaCuentas() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext,
            MpazDatabase::class.java).build()
        val server = MockWebServer()
        server.start()
        try {
            val catalogoId = UUID.randomUUID().toString()
            val uno = db.docenteDao().insertar(DocenteEntity.soporteRemoto("uno@ejemplo.cl"))
            val dos = db.docenteDao().insertar(DocenteEntity.soporteRemoto("dos@ejemplo.cl"))
            db.identidadDocenteDao().insertar(IdentidadDocenteEntity(catalogoId, 1, uno))
            db.identidadDocenteDao().insertar(IdentidadDocenteEntity(catalogoId, 2, dos))
            val anterior = db.cursoDao().insertar(CursoEntity(docenteId = uno, nombre = "3.º Básico A"))
            val remoto = DocenteRemotoRepository(ClienteDocenteAutenticado(server.url("/").newBuilder()
                .host("127.0.0.1").build().toString()))
            server.enqueue(perfil(1, "uno@ejemplo.cl"))
            remoto.entrar("uno@ejemplo.cl", "Clave-demo-2026")
            val repo = CursosAutenticadosRepository(db, remoto, catalogo(catalogoId))
            sesiones(server, 1, "uno@ejemplo.cl")
            server.enqueue(json("""[{"id":11,"nombre":"3° A"}]"""))
            val resultado = repo.actualizar(uno)
            assertEquals(1, resultado.noVinculados.size)
            assertEquals(1, db.cursoDao().listarPorDocente(uno).size)
            assertNull(db.cursoAutenticadoDao().buscarPorLocal(anterior))
            sesiones(server, 1, "uno@ejemplo.cl")
            server.enqueue(json("""[{"id":11,"nombre":"3° A"}]"""))
            repo.asociarExistente(uno, anterior, 11)
            assertEquals(anterior, db.cursoAutenticadoDao().buscarVinculo(catalogoId, 1, 11)?.cursoLocalId)

            server.enqueue(perfil(2, "dos@ejemplo.cl"))
            remoto.entrar("dos@ejemplo.cl", "Clave-demo-2026")
            sesiones(server, 2, "dos@ejemplo.cl")
            try { repo.actualizar(uno); fail("No se debe usar el perfil local de otro docente") }
            catch (_: IllegalStateException) {}
            assertTrue(db.cursoDao().listarPorDocente(dos).isEmpty())
        } finally { db.close(); server.shutdown() }
    }

    private fun perfil(id: Long, correo: String) = json("""{"id":$id,"correo":"$correo"}""")
    private fun json(body: String) = MockResponse().setHeader("Content-Type", "application/json").setBody(body)
    private fun sesiones(server: MockWebServer, id: Long, correo: String) {
        server.enqueue(perfil(id, correo))
        server.enqueue(perfil(id, correo))
    }
    private fun catalogo(id: String) = object : ActividadesServidorApi {
        override suspend fun identidadCatalogo() = IdentidadCatalogoServidor(id, true)
        override suspend fun listarCursos() = emptyList<CursoCompartidoServidor>()
        override suspend fun crearSiguienteCurso(): CursoCompartidoServidor = error("No usado")
        override suspend fun listar() = emptyList<ActividadServidor>()
    }
}
