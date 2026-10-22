package com.example.mpazpro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        DocenteEntity::class,
        CursoEntity::class,
        EstudianteEntity::class,
        ActividadEntity::class,
        IntentoActividadEntity::class
    ],
    version = 5,
    exportSchema = true
)
abstract class MpazDatabase : RoomDatabase() {

    abstract fun docenteDao(): DocenteDao

    abstract fun cursoDao(): CursoDao

    abstract fun estudianteDao(): EstudianteDao

    abstract fun actividadDao(): ActividadDao

    abstract fun intentoActividadDao(): IntentoActividadDao

    companion object {

        @Volatile
        private var instancia: MpazDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS cursos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        docenteId INTEGER NOT NULL,
                        nombre TEXT NOT NULL,
                        FOREIGN KEY(docenteId)
                            REFERENCES docentes(id)
                            ON UPDATE NO ACTION
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    index_cursos_docenteId_nombre
                    ON cursos(docenteId, nombre)
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS estudiantes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        cursoId INTEGER NOT NULL,
                        nombre TEXT NOT NULL,
                        fotoRuta TEXT,
                        FOREIGN KEY(cursoId)
                            REFERENCES cursos(id)
                            ON UPDATE NO ACTION
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_estudiantes_cursoId
                    ON estudiantes(cursoId)
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS actividades (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        cursoId INTEGER NOT NULL,
                        asignaturaId TEXT NOT NULL,
                        titulo TEXT NOT NULL,
                        instrucciones TEXT NOT NULL,
                        pregunta TEXT NOT NULL,
                        alternativaA TEXT NOT NULL,
                        alternativaB TEXT NOT NULL,
                        alternativaC TEXT NOT NULL,
                        alternativaD TEXT NOT NULL,
                        respuestaCorrecta INTEGER NOT NULL,
                        creadaEn INTEGER NOT NULL,
                        FOREIGN KEY(cursoId)
                            REFERENCES cursos(id)
                            ON UPDATE NO ACTION
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_actividades_cursoId_asignaturaId
                    ON actividades(cursoId, asignaturaId)
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS intentos_actividad (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        estudianteId INTEGER NOT NULL,
                        actividadId INTEGER NOT NULL,
                        respuestaElegida INTEGER NOT NULL,
                        respuestaCorrecta INTEGER NOT NULL,
                        correcta INTEGER NOT NULL,
                        creadaEn INTEGER NOT NULL,
                        FOREIGN KEY(estudianteId)
                            REFERENCES estudiantes(id)
                            ON UPDATE NO ACTION
                            ON DELETE CASCADE,
                        FOREIGN KEY(actividadId)
                            REFERENCES actividades(id)
                            ON UPDATE NO ACTION
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    index_intentos_actividad_estudianteId_actividadId
                    ON intentos_actividad(estudianteId, actividadId)
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_intentos_actividad_actividadId
                    ON intentos_actividad(actividadId)
                    """.trimIndent()
                )
            }
        }

        fun obtenerInstancia(context: Context): MpazDatabase {
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    MpazDatabase::class.java,
                    "mpaz.db"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5
                    )
                    .fallbackToDestructiveMigrationOnDowngrade(true)
                    .build()
                    .also { instancia = it }
            }
        }
    }
}
