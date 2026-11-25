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

class RecuperarDatosDocenteTest {
    @Test fun exigeAmbasCuentasYNoAtribuyeOtraIdentidad() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext,
            MpazDatabase::class.java).build()
        val servidor = MockWebServer()
        servidor.start()
        try {
            val url = servidor.url("/").newBuilder().host("127.0.0.1").build().toString()
            val remoto = DocenteRemotoRepository(ClienteDocenteAutenticado(url))
            val catalogoId = UUID.randomUUID().toString()
            val catalogo = object : ActividadesServidorApi {
                override suspend fun identidadCatalogo() = IdentidadCatalogoServidor(catalogoId, true)
                override suspend fun listarCursos() = emptyList<CursoCompartidoServidor>()
                override suspend fun crearSiguienteCurso(): CursoCompartidoServidor = error("No usado")
                override suspend fun listar() = emptyList<ActividadServidor>()
            }
            val local = DocenteRepository(db.docenteDao())
            val creado = local.registrar("Docente", "local@ejemplo.cl", "Local-segura1") as ResultadoRegistroDocente.Registrado
            fun perfil(id: Int) = MockResponse().setHeader("Content-Type", "application/json")
                .setBody("{\"id\":$id,\"correo\":\"servidor@ejemplo.cl\"}")
            servidor.enqueue(perfil(7).addHeader("Set-Cookie", "JSESSIONID=uno; Path=/"))
            remoto.entrar("servidor@ejemplo.cl", "Clave-segura1")
            val recuperador = RecuperarDatosDocente(db, remoto, catalogo)
            servidor.enqueue(perfil(7))
            try {
                recuperador.recuperar("local@ejemplo.cl", "incorrecta")
                fail("No debe aceptar credenciales locales incorrectas")
            } catch (_: IllegalArgumentException) {}
            assertNull(db.identidadDocenteDao().buscarPorLocal(creado.docenteId))
            servidor.enqueue(perfil(7))
            servidor.enqueue(perfil(7))
            assertEquals(creado.docenteId, recuperador.recuperar("local@ejemplo.cl", "Local-segura1"))
            assertEquals(creado.docenteId, db.identidadDocenteDao().buscar(catalogoId, 7)?.docenteLocalId)
            servidor.enqueue(perfil(8).addHeader("Set-Cookie", "JSESSIONID=dos; Path=/"))
            remoto.entrar("otro@ejemplo.cl", "Clave-segura1")
            servidor.enqueue(perfil(8))
            servidor.enqueue(perfil(8))
            try {
                recuperador.recuperar("local@ejemplo.cl", "Local-segura1")
                fail("No debe transferir datos a otra cuenta")
            } catch (_: IllegalStateException) {}
            assertNull(db.identidadDocenteDao().buscar(catalogoId, 8))
        } finally { db.close(); servidor.shutdown() }
    }
}
