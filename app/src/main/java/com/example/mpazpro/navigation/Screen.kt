package com.example.mpazpro.navigation

sealed class Screen(val route: String) {

    data object Bienvenida : Screen("bienvenida")

    data object LoginDocente : Screen("login_docente")

    data object RegistroDocente : Screen("registro_docente")

    data object PanelDocente : Screen("panel_docente")

    data object PerfilDocente : Screen("perfil_docente")

    data object CursosResultados : Screen("resultados_cursos")

    data object ResumenCurso : Screen("resumen_curso/{cursoId}") {
        fun crearRuta(cursoId: Long): String {
            return "resumen_curso/$cursoId"
        }
    }

    data object ResumenEstudiante :
        Screen("resumen_curso/{cursoId}/estudiante/{estudianteId}") {

        fun crearRuta(
            cursoId: Long,
            estudianteId: Long
        ): String {
            return "resumen_curso/$cursoId/estudiante/$estudianteId"
        }
    }

    data object Estudiantes :
        Screen("curso/{cursoId}/estudiantes") {

        fun crearRuta(cursoId: Long): String {
            return "curso/$cursoId/estudiantes"
        }
    }

    data object SeleccionEstudiante :
        Screen("seleccion_estudiante")

    data object InicioEstudiante :
        Screen("inicio_estudiante")

    data object Asignatura :
        Screen("asignatura/{asignaturaId}") {

        fun crearRuta(asignaturaId: String): String {
            return "asignatura/$asignaturaId"
        }
    }

    data object AsignaturasDocente :
        Screen("curso/{cursoId}/asignaturas?accion={accion}") {

        fun crearRuta(
            cursoId: Long,
            accion: String = "actividades"
        ): String {
            return "curso/$cursoId/asignaturas?accion=$accion"
        }
    }

    data object ActividadesDocente :
        Screen(
            "curso/{cursoId}/asignatura/{asignaturaId}/actividades?accion={accion}"
        ) {

        fun crearRuta(
            cursoId: Long,
            asignaturaId: String,
            accion: String = "actividades"
        ): String {
            return "curso/$cursoId/asignatura/$asignaturaId/actividades?accion=$accion"
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

    data object ResolverActividad :
        Screen("actividad/{actividadId}/resolver") {

        fun crearRuta(actividadId: Long): String {
            return "actividad/$actividadId/resolver"
        }
    }

    data object ResultadosActividad :
        Screen("actividad/{actividadId}/resultados") {

        fun crearRuta(actividadId: Long): String {
            return "actividad/$actividadId/resultados"
        }
    }
}
