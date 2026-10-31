# MPAZ PRO 3.º - Resumen Técnico y Lógica del Proyecto

## Arquitectura y Patrones
- **Patrón:** MVVM (Model-View-ViewModel) + Repository Pattern.
- **UI:** Jetpack Compose + Material 3 (Theme.kt personalizado con paleta pastel clara y oscura).
- **Navegación:** Jetpack Navigation Compose con rutas tipadas y argumentos (`Screen.kt`, `AppNavigation.kt`).
- **Base de Datos:** Room Database (Versión 6) con migraciones completas (`MIGRATION_1_2`, `MIGRATION_2_3`, `MIGRATION_3_4`, `MIGRATION_4_5`, `MIGRATION_5_6`).

## Módulos Principales
1. **Módulo Docente:**
   - Registro y autenticación segura con **PBKDF2** (`PasswordHasher`, `DocenteRepository`).
   - Perfil de docente con soporte para fotografía de perfil (`PerfilDocenteScreen`, `PerfilDocenteViewModel`, `DocenteEntity` con `fotoRuta`).
   - Panel Principal (`PanelDocenteScreen`) con menú visual moderno de 6 tarjetas ("burbujas"): Agregar estudiantes, Agregar actividad, Mis cursos, Mis actividades, Ver resultados, Mi perfil.
   - Creación de cursos de 3.º Básico y gestión de actividades evaluativas de opción múltiple (4 alternativas).
   - Panel de resultados y reportes por curso/estudiante con exportación a **JSON**.
2. **Módulo Estudiante:**
   - Selección de perfil de estudiante protegida con claves cifradas y generadas con **Android Keystore** y `PasswordHasher`.
   - Portal de inicio (`InicioEstudianteScreen`) con contador de **estrellas ganadas (gamificación ⭐)** y barra de progreso general.
   - Pantallas por asignatura (*Lenguaje, Matemática, Ciencias, Historia*) con progreso independiente por ramo.
   - Resolución interactiva de actividades con retroalimentación inmediata y persistencia en Room (`intentos_actividad`).

## Base de Datos (Room v6)
- Tablas: `docentes`, `cursos`, `estudiantes`, `actividades`, `intentos_actividad`.
- Migración 5 a 6: Añade la columna `fotoRuta` a la tabla `docentes`.
