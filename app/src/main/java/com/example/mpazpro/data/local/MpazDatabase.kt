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
        IntentoActividadEntity::class,
        MensajeDocenteEntity::class
    ],
    version = 10,
    exportSchema = true
)
abstract class MpazDatabase : RoomDatabase() {

    abstract fun docenteDao(): DocenteDao

    abstract fun cursoDao(): CursoDao

    abstract fun estudianteDao(): EstudianteDao

    abstract fun actividadDao(): ActividadDao

    abstract fun intentoActividadDao(): IntentoActividadDao

    abstract fun mensajeDocenteDao(): MensajeDocenteDao

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

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE docentes ADD COLUMN fotoRuta TEXT"
                )
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE actividades ADD COLUMN maxIntentos INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE actividades ADD COLUMN explicacion TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE intentos_actividad ADD COLUMN numero INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE intentos_actividad ADD COLUMN finalizada INTEGER NOT NULL DEFAULT 1")
                db.execSQL("DROP INDEX index_intentos_actividad_estudianteId_actividadId")
                db.execSQL("CREATE UNIQUE INDEX index_intentos_actividad_estudianteId_actividadId_numero ON intentos_actividad(estudianteId, actividadId, numero)")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE actividades ADD COLUMN pista TEXT NOT NULL DEFAULT ''")
                // Completa el banco conocido con ayudas específicas, conservando preguntas y respuestas.
                BancoActividades.preguntas.forEach { pregunta ->
                    db.execSQL(
                        "UPDATE actividades SET pista = ? WHERE titulo = ? AND pregunta = ? AND asignaturaId = ? AND pista = ''",
                        arrayOf(PistasIniciales.paraPregunta(pregunta.titulo, pregunta.asignatura.id),
                            pregunta.titulo, pregunta.enunciado, pregunta.asignatura.id)
                    )
                }
                // Las preguntas personalizadas reciben una estrategia general revisable por su docente.
                listOf("lenguaje", "matematica", "ciencias", "historia").forEach { asignatura ->
                    db.execSQL("UPDATE actividades SET pista = ? WHERE asignaturaId = ? AND pista = ''",
                        arrayOf(PistasIniciales.general(asignatura), asignatura))
                }
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE estudiantes ADD COLUMN rut TEXT")
                db.execSQL("ALTER TABLE estudiantes ADD COLUMN pinHash TEXT")
                db.execSQL("ALTER TABLE estudiantes ADD COLUMN pinSalt TEXT")
                db.execSQL("ALTER TABLE estudiantes ADD COLUMN pinIteraciones INTEGER")
                db.execSQL("ALTER TABLE estudiantes ADD COLUMN intentosFallidos INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE estudiantes ADD COLUMN bloqueadoHasta INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_estudiantes_rut ON estudiantes(rut)")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""CREATE TABLE IF NOT EXISTS mensajes_docente (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    estudianteId INTEGER NOT NULL, docenteId INTEGER NOT NULL,
                    actividadId INTEGER, contenido TEXT NOT NULL, creadaEn INTEGER NOT NULL,
                    leido INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY(estudianteId) REFERENCES estudiantes(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(docenteId) REFERENCES docentes(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(actividadId) REFERENCES actividades(id) ON UPDATE NO ACTION ON DELETE SET NULL
                )""")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_mensajes_docente_estudianteId ON mensajes_docente(estudianteId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_mensajes_docente_docenteId ON mensajes_docente(docenteId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_mensajes_docente_actividadId ON mensajes_docente(actividadId)")
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
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9,
                        MIGRATION_9_10,
                    )
                    .build()
                    .also { instancia = it }
            }
        }
    }
}
