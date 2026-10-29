package com.example.mpazpro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mpazpro.security.PasswordHasher

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

        private fun sembrarDatosPrueba(db: SupportSQLiteDatabase) {
            try {
                // 1. Depurar cualquier otro docente y dejar solo el de prueba (Milo@gmail.com / id = 1)
                db.execSQL("DELETE FROM docentes WHERE id != 1 OR correo != 'Milo@gmail.com'")
                db.execSQL("DELETE FROM cursos WHERE docenteId != 1")

                // 2. Insertar o actualizar el docente de prueba (Milo@gmail.com / Alegria2002) usando REPLACE
                val protegido = PasswordHasher.generar("Alegria2002")
                db.execSQL(
                    """
                    INSERT OR REPLACE INTO docentes (id, nombre, correo, claveHash, claveSalt, claveIteraciones)
                    VALUES (1, 'Profesor Milo', 'Milo@gmail.com', '${protegido.hash}', '${protegido.salt}', ${protegido.iteraciones})
                    """.trimIndent()
                )

                // 3. Curso de prueba (3.º Básico A)
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO cursos (id, docenteId, nombre)
                    VALUES (1, 1, '3.º Básico A')
                    """.trimIndent()
                )

                // 4. Estudiantes de prueba
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO estudiantes (id, cursoId, nombre, fotoRuta)
                    VALUES 
                    (1, 1, 'Sofía Pérez', NULL),
                    (2, 1, 'Lucas González', NULL),
                    (3, 1, 'Martina Silva', NULL)
                    """.trimIndent()
                )

                data class PreguntaPrueba(
                    val pregunta: String,
                    val instruccion: String,
                    val alternativas: List<String>,
                    val correcta: Int
                )

                val actividadesLenguaje = listOf(
                    PreguntaPrueba("¿Cuál es un sustantivo propio?", "Identifica nombres de personas o lugares.", listOf("Perro", "Santiago", "Mesa", "Lápiz"), 1),
                    PreguntaPrueba("¿Qué palabra es un antónimo de 'feliz'?", "Encuentra lo opuesto.", listOf("Contento", "Triste", "Alegre", "Rápido"), 1),
                    PreguntaPrueba("¿Cuál de las siguientes palabras es un artículo definido?", "Selecciona el artículo correcto.", listOf("Un", "Una", "La", "Unos"), 2),
                    PreguntaPrueba("¿Qué es un adjetivo calificativo?", "Palabra que indica una cualidad.", listOf("Una acción", "Una característica del sustantivo", "Un lugar", "Un número"), 1),
                    PreguntaPrueba("¿Cuál palabra tiene dígrafo 'ch'?", "Identifica el dígrafo.", listOf("Gato", "Perro", "Churro", "Casa"), 2),
                    PreguntaPrueba("¿Cuál es el sinónimo de 'enorme'?", "Significado similar.", listOf("Pequeño", "Gigante", "Corto", "Lento"), 1),
                    PreguntaPrueba("¿Qué oración tiene puntuación correcta?", "Revisa el uso de mayúscula y punto.", listOf("el perro ladra.", "El gato duerme.", "la casa es roja.", "mi mama cocina."), 1),
                    PreguntaPrueba("¿Qué es una fábula?", "Texto literario con moraleja.", listOf("Un poema", "Una historia con animales y enseñanza", "Una noticia", "Una receta"), 1),
                    PreguntaPrueba("¿Cuál es un verbo (acción)?", "Palabra que indica acción.", listOf("Pelota", "Correr", "Azul", "Mesa"), 1),
                    PreguntaPrueba("¿Qué palabra está escrita correctamente?", "Ortografía básica.", listOf("Baca", "Vaca", "Vaka", "Baka"), 1)
                )

                val actividadesMatematica = listOf(
                    PreguntaPrueba("¿Cuánto es 7 x 8?", "Resuelve la multiplicación.", listOf("54", "56", "64", "49"), 1),
                    PreguntaPrueba("¿Cuál es el resultado de 450 + 230?", "Suma con centenas.", listOf("670", "680", "780", "690"), 1),
                    PreguntaPrueba("¿Cuántos lados tiene un triángulo?", "Geometría básica.", listOf("3", "4", "5", "6"), 0),
                    PreguntaPrueba("¿Cuánto es 9 x 9?", "Tabla del 9.", listOf("81", "72", "90", "79"), 0),
                    PreguntaPrueba("Resuelve: 789 - 145", "Resta sin reserva.", listOf("634", "644", "654", "643"), 1),
                    PreguntaPrueba("¿Qué figura es un cuerpo geométrico 3D?", "Identifica el cuerpo 3D.", listOf("Cuadrado", "Cubo", "Círculo", "Triángulo"), 1),
                    PreguntaPrueba("Si el reloj marca las 3:00 y pasan 30 minutos, ¿qué hora es?", "Lectura del tiempo.", listOf("3:15", "3:30", "4:00", "3:45"), 1),
                    PreguntaPrueba("¿Cuánto es 5 x 6?", "Tabla del 5 y 6.", listOf("25", "30", "35", "20"), 1),
                    PreguntaPrueba("¿Cuál número es mayor?", "Comparación numérica.", listOf("405", "450", "399", "449"), 1),
                    PreguntaPrueba("¿Cuántos centímetros tiene 1 metro?", "Unidades de medida.", listOf("10 cm", "100 cm", "1,000 cm", "50 cm"), 1)
                )

                val actividadesCiencias = listOf(
                    PreguntaPrueba("¿En qué estado de la materia se encuentra el agua al hervir (vapor)?", "Estados del agua.", listOf("Sólido", "Líquido", "Gaseoso", "Helado"), 2),
                    PreguntaPrueba("¿Cuál es la función principal del esqueleto humano?", "Sistemas del cuerpo.", listOf("Digerir alimentos", "Sostener el cuerpo y proteger órganos", "Bombear sangre", "Respirar"), 1),
                    PreguntaPrueba("¿Qué parte de la planta absorbe el agua del suelo?", "Partes de la planta.", listOf("Las hojas", "Las flores", "La raíz", "El fruto"), 2),
                    PreguntaPrueba("¿Cuál es un animal vertebrado?", "Clasificación de animales.", listOf("Mariposa", "Perro", "Lombriz", "Araña"), 1),
                    PreguntaPrueba("¿Qué gas necesitamos los seres humanos para respirar?", "Atmósfera y vida.", listOf("Dióxido de carbono", "Oxígeno", "Helio", "Nitrógeno"), 1),
                    PreguntaPrueba("¿Cuál de los siguientes alimentos es más saludable?", "Hábitos de vida saludable.", listOf("Papas fritas", "Manzana", "Bebida", "Caramelos"), 1),
                    PreguntaPrueba("¿En qué estación del año hace más frío en Chile?", "Estaciones del año.", listOf("Verano", "Primavera", "Invierno", "Otoño"), 2),
                    PreguntaPrueba("¿Qué órgano bombea la sangre por todo nuestro cuerpo?", "Sistema circulatorio.", listOf("Los pulmones", "El corazón", "El estómago", "El cerebro"), 1),
                    PreguntaPrueba("¿Cuál es un animal herbívoro?", "Alimentación animal.", listOf("León", "Vaca", "Tiburón", "Gato"), 1),
                    PreguntaPrueba("¿Qué movimiento de la Tierra genera el día y la noche?", "Movimientos terrestres.", listOf("La traslación", "La rotación", "La órbita", "El eclipse"), 1)
                )

                val actividadesHistoria = listOf(
                    PreguntaPrueba("¿Cuál es el pueblo originario nómade del norte de Chile que vivía de la pesca?", "Pueblos originarios.", listOf("Mapuche", "Rapa Nui", "Chango", "Selk'nam"), 2),
                    PreguntaPrueba("¿Qué indican los puntos cardinales?", "Orientación espacial.", listOf("La hora", "La temperatura", "La dirección y orientación (Norte, Sur, Este, Oeste)", "La altitud"), 2),
                    PreguntaPrueba("¿En qué zona climática de la Tierra se ubica Chile?", "Zonas climáticas.", listOf("Solo zona fría", "Zona cálida", "Zona templada principalmente", "Zona desértica solamente"), 2),
                    PreguntaPrueba("¿Cuál es la capital de Chile?", "Geografía nacional.", listOf("Concepción", "Valparaíso", "Santiago", "La Serena"), 2),
                    PreguntaPrueba("¿Qué símbolo patrio de Chile tiene una estrella, un rectángulo azul y franjas blanca y roja?", "Símbolos patrios.", listOf("El escudo", "La bandera", "La cordillera", "El copihue"), 1),
                    PreguntaPrueba("¿Qué hacían los diaguitas en el norte de Chile?", "Culturas precolombinas.", listOf("Eran navegantes y pescadores", "Eran alfareros destacados (jarros pato)", "Eran cazadores nómades", "Vivían en la Antártida"), 1),
                    PreguntaPrueba("¿Cuál es un derecho fundamental de los niños?", "Derechos y deberes.", listOf("Trabajar todo el día", "Recibir educación y salud", "No ir a la escuela", "Comer dulces siempre"), 1),
                    PreguntaPrueba("¿Qué institución se encarga de proteger la seguridad de los ciudadanos?", "Organización social.", listOf("El hospital", "Los bomberos y Carabineros", "La escuela", "El museo"), 1),
                    PreguntaPrueba("¿Cuál es el océano que baña las costas de Chile?", "Océanos del mundo.", listOf("Océano Atlántico", "Océano Pacífico", "Océano Índico", "Océano Ártico"), 1),
                    PreguntaPrueba("¿Qué nos enseña la historia de los pueblos originarios?", "Legado cultural.", listOf("Nuestras raíces y tradiciones", "A cocinar pizzas", "Matemáticas avanzadas", "Idiomas extranjeros"), 0)
                )

                val asignaturasMap = mapOf(
                    "asig_lenguaje" to actividadesLenguaje,
                    "asig_matematica" to actividadesMatematica,
                    "asig_ciencias" to actividadesCiencias,
                    "asig_historia" to actividadesHistoria
                )

                val timestampBase = System.currentTimeMillis() - 86400000L

                var idContador = 1L
                for ((asigId, lista) in asignaturasMap) {
                    for (p in lista) {
                        db.execSQL(
                            """
                            INSERT OR IGNORE INTO actividades (id, cursoId, asignaturaId, titulo, instrucciones, pregunta, alternativaA, alternativaB, alternativaC, alternativaD, respuestaCorrecta, creadaEn)
                            VALUES ($idContador, 1, '$asigId', 'Evaluación de ${asigId.substringAfter("_").replaceFirstChar { it.uppercase() }} #$idContador', '${p.instruccion}', '${p.pregunta}', '${p.alternativas[0]}', '${p.alternativas[1]}', '${p.alternativas[2]}', '${p.alternativas[3]}', ${p.correcta}, $timestampBase)
                            """.trimIndent()
                        )
                        idContador++
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MpazDatabase", "Error al sembrar datos de prueba", e)
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
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            sembrarDatosPrueba(db)
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            sembrarDatosPrueba(db)
                        }
                    })
                    .fallbackToDestructiveMigrationOnDowngrade(true)
                    .build()
                    .also { instancia = it }
            }
        }
    }
}
