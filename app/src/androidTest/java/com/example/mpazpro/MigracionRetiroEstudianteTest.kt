package com.example.mpazpro

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.local.VinculoCursoEntity
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class MigracionRetiroEstudianteTest {
    @Test fun conservaEstudiantesActividadesYResultados() = runBlocking {
        val instrumentacion = InstrumentationRegistry.getInstrumentation()
        val contexto = instrumentacion.targetContext
        val nombre = "prueba-migracion-retiro-estudiantes-13-14.db"
        contexto.deleteDatabase(nombre)
        try {
            val texto = instrumentacion.context.assets.open(
                "com.example.mpazpro.data.local.MpazDatabase/13.json"
            ).bufferedReader().use { it.readText() }
            val esquema = JSONObject(texto).getJSONObject("database")
            val base = contexto.openOrCreateDatabase(nombre, 0, null)
            val entidades = esquema.getJSONArray("entities")
            for (i in 0 until entidades.length()) {
                val entidad = entidades.getJSONObject(i)
                val tabla = entidad.getString("tableName")
                base.execSQL(entidad.getString("createSql").replace("${'$'}{TABLE_NAME}", tabla))
                val indices = entidad.optJSONArray("indices") ?: continue
                for (j in 0 until indices.length()) {
                    base.execSQL(indices.getJSONObject(j).getString("createSql").replace("${'$'}{TABLE_NAME}", tabla))
                }
            }
            base.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)")
            base.execSQL("INSERT INTO room_master_table (id, identity_hash) VALUES (42, ?)", arrayOf(esquema.getString("identityHash")))
            base.execSQL("INSERT INTO docentes (id,nombre,correo,claveHash,claveSalt,claveIteraciones) VALUES (1,'Docente','a@example.com','h','s',1)")
            base.execSQL("INSERT INTO cursos (id,docenteId,nombre) VALUES (7,1,'3 A')")
            base.execSQL("INSERT INTO estudiantes (id,cursoId,nombre,rut,pinHash,pinSalt,pinIteraciones) VALUES (8,7,'Estudiante','123456785','hash','salt',600000)")
            base.execSQL("INSERT INTO actividades (id,cursoId,asignaturaId,titulo,instrucciones,pregunta,alternativaA,alternativaB,alternativaC,alternativaD,respuestaCorrecta,creadaEn) VALUES (9,7,'matematica','Actividad','Lee','Pregunta','A','B','C','D',0,1)")
            base.execSQL("INSERT INTO intentos_actividad (id,estudianteId,actividadId,respuestaElegida,respuestaCorrecta,correcta,creadaEn) VALUES (10,8,9,0,0,1,1)")
            base.execSQL("INSERT INTO vinculos_curso (cursoLocalId,catalogoId,cursoServidorId) VALUES (7,\'catalogo-prueba\',42)")
            base.version = 13
            base.close()

            val migrada = Room.databaseBuilder(contexto, MpazDatabase::class.java, nombre)
                .addMigrations(MpazDatabase.MIGRATION_13_14, MpazDatabase.MIGRATION_14_15, MpazDatabase.MIGRATION_15_16).build()
            try {
                assertEquals(7L, migrada.cursoDao().buscarPorIdYDocente(7, 1)!!.id)
                assertEquals("hash", migrada.estudianteDao().buscarPorRut("123456785")!!.pinHash)
                assertFalse(migrada.estudianteDao().buscarPorRut("123456785")!!.retirado)
                assertTrue(migrada.intentoActividadDao().buscar(8, 9)!!.correcta)
                assertTrue(migrada.publicacionPendienteDao().pendientes().isEmpty())
                assertEquals(42L, migrada.vinculoCursoDao().buscar(7)!!.cursoServidorId)
            } finally { migrada.close() }
        } finally { contexto.deleteDatabase(nombre) }
    }
}
