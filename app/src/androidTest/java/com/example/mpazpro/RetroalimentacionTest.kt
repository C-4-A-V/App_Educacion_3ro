package com.example.mpazpro

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.local.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RetroalimentacionTest {
    private lateinit var db: MpazDatabase
    private var docente = 0L
    private var curso = 0L
    private var uno = 0L
    private var dos = 0L
    private var actividad = 0L

    @Before fun preparar() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, MpazDatabase::class.java).build()
        docente = db.docenteDao().insertar(DocenteEntity(nombre = "Profesora", correo = "docente@example.com", claveHash = "h", claveSalt = "s", claveIteraciones = 1))
        curso = db.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "Curso"))
        uno = db.estudianteDao().insertar(EstudianteEntity(cursoId = curso, nombre = "Uno"))
        dos = db.estudianteDao().insertar(EstudianteEntity(cursoId = curso, nombre = "Dos"))
        actividad = db.actividadDao().insertar(ActividadEntity(cursoId = curso, asignaturaId = "matematica",
            titulo = "Sumemos", instrucciones = "Responde", pregunta = "¿Cuánto es 2+2?",
            alternativaA = "4", alternativaB = "3", alternativaC = "2", alternativaD = "1",
            respuestaCorrecta = 0, maxIntentos = 3))
        db.intentoActividadDao().insertar(IntentoActividadEntity(estudianteId = uno, actividadId = actividad,
            respuestaElegida = 1, respuestaCorrecta = 0, correcta = false, numero = 1, finalizada = false))
        db.intentoActividadDao().insertar(IntentoActividadEntity(estudianteId = uno, actividadId = actividad,
            respuestaElegida = 0, respuestaCorrecta = 0, correcta = true, numero = 2, finalizada = true))
        db.intentoActividadDao().insertar(IntentoActividadEntity(estudianteId = dos, actividadId = actividad,
            respuestaElegida = 1, respuestaCorrecta = 0, correcta = false, numero = 1, finalizada = false))
        Unit
    }

    @After fun cerrar() = db.close()

    @Test fun cuentaEstudiantesYConservaErroresPrevios() = runBlocking {
        val filas = db.intentoActividadDao().observarDetalleDocente(docente).first()
        assertEquals(2, filas.size)
        val primero = filas.single { it.estudianteId == uno }
        val segundo = filas.single { it.estudianteId == dos }
        assertTrue(primero.correcta)
        assertTrue(primero.finalizada)
        assertTrue(primero.tuvoError)
        assertEquals(2, primero.intentosUsados)
        assertEquals(0, primero.respuestaElegida)
        assertEquals(0, primero.respuestaCorrecta)
        assertEquals(1, segundo.respuestaElegida)
        assertFalse(segundo.finalizada)
        assertTrue(segundo.tuvoError)
        assertTrue(db.intentoActividadDao().observarDetalleDocente(docente + 50).first().isEmpty())
    }

    @Test fun mensajesSonPrivadosYNoPermitenOtroDocente() = runBlocking {
        val dao = db.mensajeDocenteDao()
        val id = dao.enviar(docente, uno, actividad, "¡Buen trabajo!", 1L)
        assertTrue(id > 0)
        assertTrue(dao.enviar(docente + 50, uno, actividad, "No autorizado", 2L) <= 0)
        assertEquals(0, dao.marcarLeido(id, dos))
        val mensaje = dao.observar(uno).first().single()
        assertEquals("Profesora", mensaje.docenteNombre)
        assertEquals("Sumemos", mensaje.actividadTitulo)
        assertFalse(mensaje.leido)
        assertTrue(dao.observar(dos).first().isEmpty())
        assertEquals(1, dao.marcarLeido(id, uno))
        assertTrue(dao.observar(uno).first().single().leido)
    }
}
