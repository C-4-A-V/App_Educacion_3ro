package com.example.mpazpro

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.repository.ActividadEstudianteRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActividadesAprendizajeTest {
    private lateinit var db: MpazDatabase
    private lateinit var repository: ActividadEstudianteRepository
    private val context: Context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before fun preparar() {
        db = Room.inMemoryDatabaseBuilder(context, MpazDatabase::class.java).build()
        repository = ActividadEstudianteRepository(db)
        sembrar(db.openHelper.writableDatabase::execSQL)
    }

    @After fun cerrar() {
        db.close()
        context.deleteDatabase("prueba-migracion-aprendizaje")
    }

    private fun sembrar(sql: (String) -> Unit) {
        sql("INSERT INTO docentes (id,nombre,correo,claveHash,claveSalt,claveIteraciones) VALUES (1,'Docente','prueba@local.test','hash','salt',1)")
        sql("INSERT INTO cursos (id,docenteId,nombre) VALUES (1,1,'3.º A')")
        sql("INSERT INTO estudiantes (id,cursoId,nombre) VALUES (1,1,'Sofía')")
        sql("INSERT INTO actividades (id,cursoId,asignaturaId,titulo,instrucciones,pregunta,alternativaA,alternativaB,alternativaC,alternativaD,respuestaCorrecta,creadaEn) VALUES (1,1,'matematica','Sumemos','Lee con calma','¿Cuánto es 2 + 2?','4','3','5','6',0,1)")
    }

    private fun practica() {
        db.openHelper.writableDatabase.execSQL("UPDATE actividades SET maxIntentos=3, explicacion='Dos más dos son cuatro.' WHERE id=1")
    }

    @Test fun aciertoInicialOtorgaUnaEstrellaYNoPermiteOtroEnvio() = runBlocking {
        practica()
        val intento = repository.enviarRespuesta(1, 1, 0, 0)
        assertTrue(intento.finalizada)
        assertEquals(intento, repository.enviarRespuesta(1, 1, 1, 1))
        val progreso = db.actividadDao().observarProgresoEstudiante(1).first()
        assertEquals(1, progreso.completadas)
        assertEquals(1, progreso.correctas)
    }

    @Test fun errorIntermedioPersisteSinCompletarNiRevelarExplicacion() = runBlocking {
        practica()
        repository.enviarRespuesta(1, 1, 1, 0)
        val reabierta = ActividadEstudianteRepository(db)
        assertEquals(1, reabierta.obtenerActividad(1, 1)!!.second!!.numero)
        assertEquals("", reabierta.obtenerExplicacion(1, 1))
        assertEquals(0, db.actividadDao().observarProgresoEstudiante(1).first().completadas)
        val listado = db.actividadDao().observarParaEstudiante(1, "matematica").first()
        assertEquals(1, listado.size)
        assertFalse(listado.single().resuelta)
        assertEquals(1, listado.single().intentosUsados)
        val informe = db.intentoActividadDao().observarResultadosActividad(1, 1).first().single()
        assertFalse(informe.respondida)
        assertNull(informe.correcta)
    }

    @Test fun aciertoPosteriorCuentaUnaActividadYConservaHistorial() = runBlocking {
        practica()
        repository.enviarRespuesta(1, 1, 1, 0)
        repository.enviarRespuesta(1, 1, 0, 1)
        assertEquals("Dos más dos son cuatro.", repository.obtenerExplicacion(1, 1))
        val progreso = db.actividadDao().observarProgresoEstudiante(1).first()
        assertEquals(1, progreso.total)
        assertEquals(1, progreso.completadas)
        assertEquals(1, progreso.correctas)
        val informe = db.intentoActividadDao().observarResultadosActividad(1, 1).first().single()
        assertEquals(2, informe.intentosUsados)
        assertEquals(true, informe.correcta)
        val avance = db.estudianteDao().observarAvancesDocente(1).first().single()
        assertEquals(1, avance.total)
        assertEquals(1, avance.completadas)
        db.openHelper.readableDatabase.query("SELECT COUNT(*) FROM intentos_actividad").use {
            assertTrue(it.moveToFirst())
            assertEquals(2, it.getInt(0))
        }
    }

    @Test fun tresErroresCierranPracticaYEvaluacionSeCierraConUno() = runBlocking {
        practica()
        repeat(3) { repository.enviarRespuesta(1, 1, 1, it) }
        val final = repository.enviarRespuesta(1, 1, 0, 3)
        assertEquals(3, final.numero)
        assertFalse(final.correcta)
        assertTrue(final.finalizada)
        assertEquals(1, db.actividadDao().observarProgresoAsignatura(1, "matematica").first().completadas)
        assertEquals(0, db.actividadDao().observarProgresoEstudiante(1).first().correctas)
        assertEquals("Dos más dos son cuatro.", repository.obtenerExplicacion(1, 1))
    }

    @Test fun evaluacionConservaSuUnicoResultado() = runBlocking {
        val primero = repository.enviarRespuesta(1, 1, 1, 0)
        assertTrue(primero.finalizada)
        assertEquals(primero, repository.enviarRespuesta(1, 1, 0, 1))
    }

    @Test fun enviosSimultaneosNoConsumenDosOportunidades() = runBlocking {
        practica()
        coroutineScope {
            val a = async { repository.enviarRespuesta(1, 1, 1, 0) }
            val b = async { repository.enviarRespuesta(1, 1, 1, 0) }
            assertEquals(a.await(), b.await())
        }
        assertEquals(1, db.intentoActividadDao().buscar(1, 1)!!.numero)
    }

    @Test fun migracionDesdeSeisConservaPerfilesYResultado() = runBlocking {
        val nombre = "prueba-migracion-aprendizaje"
        context.deleteDatabase(nombre)
        val ruta = context.getDatabasePath(nombre)
        ruta.parentFile!!.mkdirs()
        val assets = InstrumentationRegistry.getInstrumentation().context.assets
        val schema = assets.open("com.example.mpazpro.data.local.MpazDatabase/6.json")
            .bufferedReader().use { JSONObject(it.readText()).getJSONObject("database") }
        SQLiteDatabase.openOrCreateDatabase(ruta, null).use { sqlite ->
            val entities = schema.getJSONArray("entities")
            for (i in 0 until entities.length()) {
                val entity = entities.getJSONObject(i)
                sqlite.execSQL(entity.getString("createSql").replace("$" + "{TABLE_NAME}", entity.getString("tableName")))
                val indices = entity.getJSONArray("indices")
                for (j in 0 until indices.length()) {
                    sqlite.execSQL(indices.getJSONObject(j).getString("createSql").replace("$" + "{TABLE_NAME}", entity.getString("tableName")))
                }
            }
            sembrar(sqlite::execSQL)
            sqlite.execSQL("INSERT INTO intentos_actividad (estudianteId,actividadId,respuestaElegida,respuestaCorrecta,correcta,creadaEn) VALUES (1,1,0,0,1,1)")
            sqlite.version = 6
        }
        val migrada = Room.databaseBuilder(context, MpazDatabase::class.java, nombre)
            .addMigrations(MpazDatabase.MIGRATION_6_7).build()
        try {
            assertEquals("Sofía", migrada.estudianteDao().observarPerfiles().first().single().nombre)
            val actividad = migrada.actividadDao().buscarPorId(1)!!
            assertEquals(1, actividad.maxIntentos)
            assertEquals("", actividad.explicacion)
            val intento = migrada.intentoActividadDao().buscar(1, 1)!!
            assertTrue(intento.correcta)
            assertTrue(intento.finalizada)
            assertEquals(1, intento.numero)
        } finally {
            migrada.close()
        }
    }
}
