package com.example.mpazpro

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.local.*
import com.example.mpazpro.data.repository.EstudianteRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AccesoEstudianteTest {
    private lateinit var db: MpazDatabase
    private lateinit var repository: EstudianteRepository
    private var docente = 0L
    private var curso = 0L

    @Before fun preparar() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext, MpazDatabase::class.java
        ).build()
        repository = EstudianteRepository(db.estudianteDao(), db.cursoDao(), db.actividadDao())
        docente = db.docenteDao().insertar(DocenteEntity(nombre = "Docente", correo = "prueba@example.com",
            claveHash = "hash", claveSalt = "salt", claveIteraciones = 1))
        curso = db.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3 A"))
    }

    @After fun cerrar() { db.close() }

    @Test fun credencialesCorrectasYError() = runBlocking {
        val id = repository.registrar(curso, docente, "Estudiante", "foto", "12.345.678-5", "1234")
        val guardado = db.estudianteDao().buscarPorRut("123456785")!!
        assertNotEquals("1234", guardado.pinHash)
        assertNull(repository.autenticar("12.345.678-5", "9999"))
        assertEquals(id, repository.autenticar("12345678-5", "1234"))
    }

    @Test fun perfilAntiguoPuedeConfigurarAccesoYMantenerIdentidad() = runBlocking {
        val id = db.estudianteDao().insertar(EstudianteEntity(cursoId = curso, nombre = "Perfil antiguo"))
        repository.configurarAcceso(id, curso, docente, "12.345.678-5", "4321")
        assertEquals(id, repository.autenticar("12.345.678-5", "4321"))
        assertEquals("Perfil antiguo", db.estudianteDao().buscarPorRut("123456785")!!.nombre)
    }

    @Test fun bloqueaDespuesDeCincoErroresYDocentePuedeRestablecer() = runBlocking {
        val id = repository.registrar(curso, docente, "Estudiante", "foto", "12.345.678-5", "1234")
        repeat(5) { assertNull(repository.autenticar("12345678-5", "9999")) }
        assertNull(repository.autenticar("12345678-5", "1234"))
        repository.configurarAcceso(id, curso, docente, "12345678-5", "4321")
        assertEquals(id, repository.autenticar("12345678-5", "4321"))
    }

    @Test fun otroDocenteNoPuedeModificarElAcceso() = runBlocking {
        val id = db.estudianteDao().insertar(EstudianteEntity(cursoId = curso, nombre = "Perfil"))
        try {
            repository.configurarAcceso(id, curso, docente + 100, "12.345.678-5", "1234")
            fail("Otro docente no puede modificar el acceso")
        } catch (_: IllegalStateException) { }
        assertNull(db.estudianteDao().buscarPorRut("123456785"))
    }

    @Test fun noPermiteAsignarUnRutYaRegistrado() = runBlocking {
        repository.registrar(curso, docente, "Uno", "foto", "12.345.678-5", "1234")
        try {
            repository.registrar(curso, docente, "Dos", "foto", "12345678-5", "2222")
            fail("Debe rechazar el RUT duplicado")
        } catch (_: IllegalArgumentException) { }
    }
}
