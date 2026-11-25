package com.example.mpazpro

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.local.MpazDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class MigracionEstudianteTest {
    @Test fun migracionConservaPerfilYDejaAccesoPendiente() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val nombreDb = "prueba-migracion-estudiante.db"
        context.deleteDatabase(nombreDb)
        try {
            val json = instrumentation.context.assets.open(
                "com.example.mpazpro.data.local.MpazDatabase/8.json"
            ).bufferedReader().use { it.readText() }
            val schema = JSONObject(json).getJSONObject("database")
            val base = context.openOrCreateDatabase(nombreDb, 0, null)
            val entidades = schema.getJSONArray("entities")
            for (i in 0 until entidades.length()) {
                val entidad = entidades.getJSONObject(i)
                val tabla = entidad.getString("tableName")
                base.execSQL(entidad.getString("createSql").replace("\${TABLE_NAME}", tabla))
                val indices = entidad.optJSONArray("indices") ?: continue
                for (j in 0 until indices.length()) {
                    base.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}", tabla))
                }
            }
            base.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)")
            base.execSQL("INSERT INTO room_master_table (id, identity_hash) VALUES (42, ?)", arrayOf(schema.getString("identityHash")))
            base.execSQL("INSERT INTO docentes (id,nombre,correo,claveHash,claveSalt,claveIteraciones) VALUES (1,'Docente','a@example.com','h','s',1)")
            base.execSQL("INSERT INTO cursos (id,docenteId,nombre) VALUES (1,1,'Curso')")
            base.execSQL("INSERT INTO estudiantes (id,cursoId,nombre,fotoRuta) VALUES (1,1,'Estudiante anterior','foto')")
            base.execSQL("INSERT INTO actividades (id,cursoId,asignaturaId,titulo,instrucciones,pregunta,alternativaA,alternativaB,alternativaC,alternativaD,respuestaCorrecta,creadaEn) VALUES (1,1,'matematica','Actividad','Lee','Pregunta','A','B','C','D',0,1)")
            base.execSQL("INSERT INTO intentos_actividad (id,estudianteId,actividadId,respuestaElegida,respuestaCorrecta,correcta,creadaEn) VALUES (1,1,1,0,0,1,1)")
            base.version = 8
            base.close()

            val migrada = Room.databaseBuilder(context, MpazDatabase::class.java, nombreDb)
                .addMigrations(MpazDatabase.MIGRATION_8_9, MpazDatabase.MIGRATION_9_10, MpazDatabase.MIGRATION_10_11, MpazDatabase.MIGRATION_11_12, MpazDatabase.MIGRATION_12_13, MpazDatabase.MIGRATION_13_14, MpazDatabase.MIGRATION_14_15, MpazDatabase.MIGRATION_15_16).build()
            try {
                val estudiante = migrada.estudianteDao().observarPorCurso(1, 1).first().single()
                assertEquals(1L, estudiante.id)
                assertEquals("Estudiante anterior", estudiante.nombre)
                assertEquals("foto", estudiante.fotoRuta)
                assertNull(estudiante.rut)
                assertNull(estudiante.pinHash)
                assertTrue(migrada.intentoActividadDao().buscar(1, 1)!!.correcta)
            } finally { migrada.close() }
        } finally { context.deleteDatabase(nombreDb) }
    }
}
