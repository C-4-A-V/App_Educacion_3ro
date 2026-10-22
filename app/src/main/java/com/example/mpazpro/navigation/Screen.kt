package com.example.mpazpro.navigation

sealed class Screen(val route: String) {

    data object Bienvenida : Screen("bienvenida")

    data object LoginDocente : Screen("login_docente")

    data object RegistroDocente : Screen("registro_docente")

    data object PanelDocente : Screen("panel_docente")

    data object Estudiantes : Screen("curso/{cursoId}/estudiantes") {

        fun crearRuta(cursoId: Long): String {
            return "curso/$cursoId/estudiantes"
        }
    }

    data object SeleccionEstudiante : Screen("seleccion_estudiante")

    data object InicioEstudiante : Screen("inicio_estudiante")

    data object Asignatura : Screen("asignatura/{asignaturaId}") {

        fun crearRuta(asignaturaId: String): String {
            return "asignatura/$asignaturaId"
        }
    }

    data object AsignaturasDocente :
        Screen("curso/{cursoId}/asignaturas") {

        fun crearRuta(cursoId: Long): String {
            return "curso/$cursoId/asignaturas"
        }
    }

    data object ActividadesDocente :
        Screen("curso/{cursoId}/asignatura/{asignaturaId}/actividades") {

        fun crearRuta(
            cursoId: Long,
            asignaturaId: String
        ): String {
            return "curso/$cursoId/asignatura/$asignaturaId/actividades"
        }
    }

    data object CrearActividad :
        Screen("curso/{cursoId}/asignatura/{asignaturaId}/crear") {

        fun crearRuta(
            cursoId: Long,
            asignaturaId: String
        ): String {
            return "curso/$cursoId/asignatura/$asignaturaId/crear"
        }
    }
}
