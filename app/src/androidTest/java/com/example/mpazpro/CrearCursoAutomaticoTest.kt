package com.example.mpazpro

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.local.CursoEntity
import com.example.mpazpro.data.local.DocenteEntity
import com.example.mpazpro.data.local.MpazDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class CrearCursoAutomaticoTest {
    @Test fun respetaLaSeccionConfirmadaYConservaCursosExistentes() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext, MpazDatabase::class.java
        ).build()
        try {
            val docente = db.docenteDao().insertar(DocenteEntity(
                nombre = "Docente", correo = "cursos@example.com", claveHash = "h", claveSalt = "s", claveIteraciones = 1))
            val anterior = db.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3A"))
            val creado = db.cursoDao().crearSiguienteConfirmado(docente, 'B')
            assertEquals("3.º Básico B", db.cursoDao().buscarPorIdYDocente(creado, docente)!!.nombre)
            assertEquals("3A", db.cursoDao().buscarPorIdYDocente(anterior, docente)!!.nombre)
            try {
                db.cursoDao().crearSiguienteConfirmado(docente, 'B')
                fail("No debe crear otro curso si la sección confirmada ya cambió")
            } catch (_: IllegalStateException) {}
            assertEquals(2, db.cursoDao().listarPorDocente(docente).size)
            db.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "Otro formato"))
            try {
                db.cursoDao().crearSiguienteConfirmado(docente, 'C')
                fail("No debe adivinar con cursos de nombre desconocido")
            } catch (_: IllegalStateException) {}
            assertEquals(3, db.cursoDao().listarPorDocente(docente).size)
        } finally { db.close() }
    }
}
