package com.example.mpazpro

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.local.*
import com.example.mpazpro.data.repository.ActividadEstudianteRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PistasAprendizajeTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var db: MpazDatabase
    private lateinit var repo: ActividadEstudianteRepository

    private fun sembrar(sql: (String) -> Unit) {
        sql("INSERT INTO docentes (id,nombre,correo,claveHash,claveSalt,claveIteraciones) VALUES (1,'Docente','prueba@local.test','hash','salt',1)")
        sql("INSERT INTO cursos (id,docenteId,nombre) VALUES (1,1,'3.º A')")
        sql("INSERT INTO estudiantes (id,cursoId,nombre) VALUES (1,1,'Sofía')")
    }
    @Before fun abrir() {
        db = Room.inMemoryDatabaseBuilder(context, MpazDatabase::class.java).build()
        repo = ActividadEstudianteRepository(db)
        sembrar(db.openHelper.writableDatabase::execSQL)
    }
    @After fun cerrar() { db.close(); context.deleteDatabase("prueba-migracion-pistas") }

    @Test fun pistaSeGuardaPeroEvaluacionNoLaExpone() = runBlocking {
        val pregunta = BancoActividades.entidades(1).first()
        val practica = db.actividadDao().insertar(pregunta)
        val evaluacion = db.actividadDao().insertar(pregunta.copy(maxIntentos = 1))
        assertEquals(pregunta.pista, repo.obtenerActividad(1, practica)!!.first.pista)
        assertEquals("", repo.obtenerActividad(1, evaluacion)!!.first.pista)
        assertEquals(pregunta.pista, db.actividadDao().buscarPorId(evaluacion)!!.pista)
        assertEquals(0, db.actividadDao().actualizarPista(practica, 999, "No autorizado"))
        assertEquals(1, db.actividadDao().actualizarPista(practica, 1, "Dibuja la situación."))
        assertEquals("Dibuja la situación.", repo.obtenerActividad(1, practica)!!.first.pista)
    }

    @Test fun continuarRetomaPracticaIncompletaYOmiteFinalizadas() = runBlocking {
        val pregunta = BancoActividades.entidades(1).first()
        val primera = db.actividadDao().insertar(pregunta.copy(creadaEn = 10))
        val segunda = db.actividadDao().insertar(pregunta.copy(creadaEn = 20))
        repo.enviarRespuesta(1, primera, 0, 0) // incorrecta, queda en práctica
        assertEquals(primera, db.actividadDao().observarParaContinuar(1).first()!!.id)
        repo.enviarRespuesta(1, primera, pregunta.respuestaCorrecta, 1)
        assertEquals(segunda, db.actividadDao().observarParaContinuar(1).first()!!.id)
        repo.enviarRespuesta(1, segunda, pregunta.respuestaCorrecta, 0)
        assertNull(db.actividadDao().observarParaContinuar(1).first())
        assertNull(db.actividadDao().observarParaContinuar(999).first())
    }

    @Test fun migracionSieteAgregaPistasSinPerderResultados() = runBlocking {
        val nombre = "prueba-migracion-pistas"
        context.deleteDatabase(nombre)
        val ruta = context.getDatabasePath(nombre)
        ruta.parentFile!!.mkdirs()
        val schema = InstrumentationRegistry.getInstrumentation().context.assets
            .open("com.example.mpazpro.data.local.MpazDatabase/7.json")
            .bufferedReader().use { JSONObject(it.readText()).getJSONObject("database") }
        val pregunta = BancoActividades.preguntas.first()
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
            sqlite.execSQL(
                "INSERT INTO actividades (id,cursoId,asignaturaId,titulo,instrucciones,pregunta,alternativaA,alternativaB,alternativaC,alternativaD,respuestaCorrecta,creadaEn,maxIntentos,explicacion) VALUES (1,1,?,?,?,?,'A','B','C','D',1,1,3,'Explicación conservada')",
                arrayOf(pregunta.asignatura.id, pregunta.titulo, "Lee", pregunta.enunciado)
            )
            sqlite.execSQL("INSERT INTO intentos_actividad (estudianteId,actividadId,respuestaElegida,respuestaCorrecta,correcta,creadaEn,numero,finalizada) VALUES (1,1,1,1,1,1,1,1)")
            sqlite.version = 7
        }
        val migrada = Room.databaseBuilder(context, MpazDatabase::class.java, nombre)
            .addMigrations(MpazDatabase.MIGRATION_7_8, MpazDatabase.MIGRATION_8_9, MpazDatabase.MIGRATION_9_10, MpazDatabase.MIGRATION_10_11, MpazDatabase.MIGRATION_11_12, MpazDatabase.MIGRATION_12_13, MpazDatabase.MIGRATION_13_14, MpazDatabase.MIGRATION_14_15).build()
        try {
            val actividad = migrada.actividadDao().buscarPorId(1)!!
            assertEquals(PistasIniciales.paraPregunta(pregunta.titulo, pregunta.asignatura.id), actividad.pista)
            assertEquals("Explicación conservada", actividad.explicacion)
            assertEquals(pregunta.enunciado, actividad.pregunta)
            assertTrue(migrada.intentoActividadDao().buscar(1, 1)!!.correcta)
            assertEquals("Sofía", migrada.estudianteDao().observarPerfiles().first().single().nombre)
        } finally { migrada.close() }
    }
}
