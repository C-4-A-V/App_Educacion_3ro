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
        MensajeDocenteEntity::class,
        VinculoCursoEntity::class,
        PublicacionPendienteEntity::class,
        ActividadRetiradaEntity::class,
        ActividadCopiaEntity::class,
        IdentidadDocenteEntity::class
    ],
    version = 16,
    exportSchema = true
)
abstract class MpazDatabase : RoomDatabase() {

    abstract fun docenteDao(): DocenteDao

    abstract fun identidadDocenteDao(): IdentidadDocenteDao

    abstract fun cursoDao(): CursoDao

    abstract fun estudianteDao(): EstudianteDao

    abstract fun actividadDao(): ActividadDao

    abstract fun intentoActividadDao(): IntentoActividadDao

    abstract fun mensajeDocenteDao(): MensajeDocenteDao

    abstract fun vinculoCursoDao(): VinculoCursoDao

    abstract fun publicacionPendienteDao(): PublicacionPendienteDao

    abstract fun actividadRetiradaDao(): ActividadRetiradaDao

    abstract fun actividadCopiaDao(): ActividadCopiaDao

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

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""CREATE TABLE IF NOT EXISTS vinculos_curso (
                    cursoLocalId INTEGER NOT NULL PRIMARY KEY,
                    catalogoId TEXT NOT NULL,
                    cursoServidorId INTEGER NOT NULL,
                    FOREIGN KEY(cursoLocalId) REFERENCES cursos(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )""")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_vinculos_curso_catalogoId_cursoServidorId ON vinculos_curso(catalogoId, cursoServidorId)")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""CREATE TABLE IF NOT EXISTS publicaciones_pendientes (
                    actividadLocalId INTEGER NOT NULL PRIMARY KEY,
                    clavePublicacion TEXT NOT NULL,
                    catalogoId TEXT NOT NULL,
                    cursoServidorId INTEGER NOT NULL,
                    actividadServidorId INTEGER,
                    FOREIGN KEY(actividadLocalId) REFERENCES actividades(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )""")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_publicaciones_pendientes_clavePublicacion ON publicaciones_pendientes(clavePublicacion)")
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""CREATE TABLE IF NOT EXISTS actividades_retiradas (
                    actividadId INTEGER NOT NULL PRIMARY KEY,
                    FOREIGN KEY(actividadId) REFERENCES actividades(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )""")
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE estudiantes ADD COLUMN retirado INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""CREATE TABLE IF NOT EXISTS actividades_copiadas (
                    origenId INTEGER NOT NULL, cursoDestinoId INTEGER NOT NULL, copiaId INTEGER NOT NULL,
                    PRIMARY KEY(origenId, cursoDestinoId),
                    FOREIGN KEY(origenId) REFERENCES actividades(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(cursoDestinoId) REFERENCES cursos(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(copiaId) REFERENCES actividades(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )""")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_actividades_copiadas_cursoDestinoId ON actividades_copiadas(cursoDestinoId)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_actividades_copiadas_copiaId ON actividades_copiadas(copiaId)")
            }
        }

        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""CREATE TABLE IF NOT EXISTS identidades_docentes (
                    catalogoId TEXT NOT NULL, docenteServidorId INTEGER NOT NULL, docenteLocalId INTEGER NOT NULL,
                    PRIMARY KEY(catalogoId, docenteServidorId),
                    FOREIGN KEY(docenteLocalId) REFERENCES docentes(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )""")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_identidades_docentes_docenteLocalId ON identidades_docentes(docenteLocalId)")
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
                        MIGRATION_10_11,
                        MIGRATION_11_12,
                        MIGRATION_12_13,
                        MIGRATION_13_14,
                        MIGRATION_14_15,
                        MIGRATION_15_16,
                    )
                    .build()
                    .also { instancia = it }
            }
        }
    }
}
