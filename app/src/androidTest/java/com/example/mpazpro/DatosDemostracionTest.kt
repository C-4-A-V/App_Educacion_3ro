package com.example.mpazpro

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.local.*
import com.example.mpazpro.security.PasswordHasher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DatosDemostracionTest {
    private lateinit var db: MpazDatabase

    @Before fun abrir() {
        db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext, MpazDatabase::class.java
        ).build()
    }
    @After fun cerrar() = db.close()

    @Test fun creaCuentaYDiezPorAsignaturaSinDuplicados() = runBlocking {
        val a = async { DatosDemostracion.preparar(db) }
        val b = async { DatosDemostracion.preparar(db) }
        val curso = a.await()
        assertEquals(curso, b.await())
        DatosDemostracion.preparar(db)
        val docente = db.docenteDao().buscarPorCorreo("MILO@GMAIL.COM")!!
        assertTrue(PasswordHasher.verificar(
            DatosDemostracion.CLAVE_INICIAL, docente.claveHash, docente.claveSalt, docente.claveIteraciones
        ))
        listOf("lenguaje", "matematica", "ciencias", "historia").forEach { asignatura ->
            assertEquals(10, db.actividadDao().observarPorCursoYAsignatura(curso, asignatura, docente.id).first().size)
        }
    }

    @Test fun conservaClaveCursoYResultadosExistentes() = runBlocking {
        val protegida = PasswordHasher.generar("OtraClave123")
        val docente = db.docenteDao().insertar(DocenteEntity(
            nombre = "Docente existente", correo = DatosDemostracion.CORREO,
            claveHash = protegida.hash, claveSalt = protegida.salt, claveIteraciones = protegida.iteraciones
        ))
        val curso = db.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "Mi curso"))
        val estudiante = db.estudianteDao().insertar(EstudianteEntity(cursoId = curso, nombre = "Perfil de prueba"))
        val actividad = db.actividadDao().insertar(BancoActividades.entidades(curso).first())
        db.intentoActividadDao().insertar(IntentoActividadEntity(
            estudianteId = estudiante, actividadId = actividad,
            respuestaElegida = 1, respuestaCorrecta = 1, correcta = true
        ))
        assertEquals(curso, DatosDemostracion.preparar(db))
        assertEquals(protegida.hash, db.docenteDao().buscarPorCorreo(DatosDemostracion.CORREO)!!.claveHash)
        assertTrue(db.intentoActividadDao().buscar(estudiante, actividad)!!.correcta)
        assertEquals(10, db.actividadDao().observarPorCursoYAsignatura(curso, "lenguaje", docente).first().size)
    }
}
