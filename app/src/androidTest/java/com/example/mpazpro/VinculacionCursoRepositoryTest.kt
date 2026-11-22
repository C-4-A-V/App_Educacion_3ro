package com.example.mpazpro

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.local.CursoEntity
import com.example.mpazpro.data.local.DocenteEntity
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.remote.ActividadServidor
import com.example.mpazpro.data.remote.ActividadesServidorApi
import com.example.mpazpro.data.remote.CursoCompartidoServidor
import com.example.mpazpro.data.remote.IdentidadCatalogoServidor
import com.example.mpazpro.data.repository.VinculacionCursoRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class VinculacionCursoRepositoryTest {
    @Test fun vinculaSoloCursoCorrespondienteYNoSobrescribeOtroCatalogo() = runBlocking {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val base = Room.inMemoryDatabaseBuilder(contexto, MpazDatabase::class.java).build()
        try {
            val docente = base.docenteDao().insertar(DocenteEntity(
                nombre = "Prueba", correo = "prueba@example.com", claveHash = "h", claveSalt = "s", claveIteraciones = 1
            ))
            val local = base.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3.º Básico A"))
            val api = ApiFalsa()
            val repo = VinculacionCursoRepository(base, api)
            repo.vincular(docente, local, 1)
            repo.vincular(docente, local, 1)
            assertEquals(1L, base.vinculoCursoDao().buscar(local)!!.cursoServidorId)
            api.identidad = "22222222-2222-2222-2222-222222222222"
            try {
                repo.vincular(docente, local, 1)
                fail("No debe reemplazar el vínculo existente")
            } catch (_: IllegalStateException) {}
            assertEquals("11111111-1111-1111-1111-111111111111", base.vinculoCursoDao().buscar(local)!!.catalogoId)
        } finally { base.close() }
    }

    @Test fun rechazaSeccionDiferenteSinGuardar() = runBlocking {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val base = Room.inMemoryDatabaseBuilder(contexto, MpazDatabase::class.java).build()
        try {
            val docente = base.docenteDao().insertar(DocenteEntity(
                nombre = "Prueba", correo = "otro@example.com", claveHash = "h", claveSalt = "s", claveIteraciones = 1
            ))
            val local = base.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3 B"))
            try {
                VinculacionCursoRepository(base, ApiFalsa()).vincular(docente, local, 1)
                fail("No debe vincular secciones distintas")
            } catch (_: IllegalArgumentException) {}
            assertNull(base.vinculoCursoDao().buscar(local))
        } finally { base.close() }
    }

    @Test fun rechazaCatalogoTemporal() = runBlocking {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val base = Room.inMemoryDatabaseBuilder(contexto, MpazDatabase::class.java).build()
        try {
            val docente = base.docenteDao().insertar(DocenteEntity(
                nombre = "Prueba", correo = "temporal@example.com", claveHash = "h", claveSalt = "s", claveIteraciones = 1
            ))
            val local = base.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3 A"))
            val api = ApiFalsa().apply { persistente = false }
            try {
                VinculacionCursoRepository(base, api).vincular(docente, local, 1)
                fail("No debe vincularse un catálogo temporal")
            } catch (_: IllegalStateException) {}
            assertNull(base.vinculoCursoDao().buscar(local))
        } finally { base.close() }
    }

    private class ApiFalsa : ActividadesServidorApi {
        var identidad = "11111111-1111-1111-1111-111111111111"
        var persistente = true
        override suspend fun identidadCatalogo() = IdentidadCatalogoServidor(identidad, persistente)
        override suspend fun listarCursos() = listOf(CursoCompartidoServidor(1, "3° A"))
        override suspend fun crearSiguienteCurso() = error("No se crea ningún curso")
        override suspend fun listar(): List<ActividadServidor> = emptyList()
    }
}
