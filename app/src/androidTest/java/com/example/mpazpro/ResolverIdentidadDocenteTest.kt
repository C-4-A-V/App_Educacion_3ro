package com.example.mpazpro

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.remote.*
import com.example.mpazpro.data.repository.*
import java.util.UUID
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test

class ResolverIdentidadDocenteTest {
    @Test fun creaUnaSolaAsociacionYNoReclamaPerfilesExistentes() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext,
            MpazDatabase::class.java).build()
        val server = MockWebServer()
        server.start()
        try {
            val url = server.url("/").newBuilder().host("127.0.0.1").build().toString()
            val remoto = DocenteRemotoRepository(ClienteDocenteAutenticado(url))
            val catalogoId = UUID.randomUUID().toString()
            val catalogo = object : ActividadesServidorApi {
                override suspend fun identidadCatalogo() = IdentidadCatalogoServidor(catalogoId, true)
                override suspend fun listarCursos() = emptyList<CursoCompartidoServidor>()
                override suspend fun crearSiguienteCurso(): CursoCompartidoServidor = error("No usado")
                override suspend fun listar() = emptyList<ActividadServidor>()
            }
            fun perfil(id: Long, correo: String) = MockResponse().setHeader("Content-Type", "application/json")
                .setBody("""{"id":$id,"correo":"$correo"}""")
            fun respuestas(id: Long, correo: String) {
                server.enqueue(perfil(id, correo)); server.enqueue(perfil(id, correo))
            }
            val resolver = ResolverIdentidadDocente(db, remoto, catalogo)
            val correo = "nueva@ejemplo.cl"
            server.enqueue(perfil(1, correo).addHeader("Set-Cookie", "JSESSIONID=uno; Path=/"))
            remoto.entrar(correo, "Clave-segura1")
            respuestas(1, correo)
            val nuevo = resolver.resolver()
            respuestas(1, correo)
            assertEquals(nuevo.id, resolver.resolver().id)
            assertEquals(nuevo.id, db.identidadDocenteDao().buscar(catalogoId, 1)?.docenteLocalId)
            assertNull(DocenteRepository(db.docenteDao()).autenticar(correo, "Clave-segura1"))

            val antigua = DocenteRepository(db.docenteDao())
                .registrar("Anterior", "antigua@ejemplo.cl", "Clave-anterior1") as ResultadoRegistroDocente.Registrado
            server.enqueue(perfil(2, "antigua@ejemplo.cl"))
            remoto.entrar("antigua@ejemplo.cl", "Clave-remota1")
            respuestas(2, "antigua@ejemplo.cl")
            try { resolver.resolver(); fail("Debe exigir recuperación explícita") }
            catch (_: RecuperacionDocenteNecesaria) {}
            assertNull(db.identidadDocenteDao().buscar(catalogoId, 2))
            assertNull(db.identidadDocenteDao().buscarPorLocal(antigua.docenteId))
            assertEquals(nuevo.id, db.identidadDocenteDao().buscar(catalogoId, 1)?.docenteLocalId)

            respuestas(2, "antigua@ejemplo.cl")
            val recuperado = RecuperarDatosDocente(db, remoto, catalogo)
                .recuperar("antigua@ejemplo.cl", "Clave-anterior1")
            assertEquals(antigua.docenteId, recuperado)
            respuestas(2, "antigua@ejemplo.cl")
            assertEquals(antigua.docenteId, resolver.resolver().id)

            server.enqueue(perfil(1, correo))
            remoto.entrar(correo, "Clave-segura1")
            respuestas(1, correo)
            assertEquals(nuevo.id, resolver.resolver().id)
        } finally { db.close(); server.shutdown() }
    }
}
