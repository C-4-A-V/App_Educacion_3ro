package com.example.mpazpro.navigation

import android.content.pm.ApplicationInfo
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mpazpro.ui.components.LocalDensidadOriginal
import com.example.mpazpro.ui.theme.LocalModoOscuroEstudiante
import com.example.mpazpro.ui.screens.PerfilEstudianteScreen
import com.example.mpazpro.ui.theme.LocalModoOscuroDocente
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mpazpro.model.Asignatura
import com.example.mpazpro.ui.screens.ActividadesDocenteScreen
import com.example.mpazpro.ui.screens.AsignaturaScreen
import com.example.mpazpro.ui.screens.AsignaturasDocenteScreen
import com.example.mpazpro.ui.screens.BienvenidaScreen
import com.example.mpazpro.ui.screens.CrearActividadScreen
import com.example.mpazpro.ui.screens.EstudiantesScreen
import com.example.mpazpro.ui.screens.InicioEstudianteScreen
import com.example.mpazpro.ui.screens.LoginDocenteScreen
import com.example.mpazpro.ui.screens.PanelDocenteScreen
import com.example.mpazpro.ui.screens.PerfilDocenteScreen
import com.example.mpazpro.ui.screens.RegistroDocenteScreen
import com.example.mpazpro.ui.screens.ResolverActividadScreen
import com.example.mpazpro.ui.screens.ResolverActividadRemotaScreen
import com.example.mpazpro.ui.screens.ResumenDocenteScreen
import com.example.mpazpro.ui.screens.ResultadosActividadScreen
import com.example.mpazpro.ui.screens.SeleccionEstudianteScreen
import com.example.mpazpro.ui.viewmodel.AccesoEstudianteViewModel
import com.example.mpazpro.ui.viewmodel.LoginDocenteViewModel
import com.example.mpazpro.ui.viewmodel.PerfilDocenteViewModel

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    loginViewModel: LoginDocenteViewModel = viewModel()
) {
    val navController = rememberNavController()
    val esDepuracion = (LocalContext.current.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    val estadoLogin by loginViewModel.uiState
        .collectAsStateWithLifecycle()
    val sesionDocente by loginViewModel.sesionRemota.collectAsStateWithLifecycle()
    LaunchedEffect(estadoLogin.docente, sesionDocente) {
        if (estadoLogin.docente != null && sesionDocente == null) {
            loginViewModel.cerrarSesionRemota()
            navController.navigate(Screen.Bienvenida.route) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    val accesoEstudianteViewModel: AccesoEstudianteViewModel =
        viewModel()

    val estadoEstudiante by accesoEstudianteViewModel.uiState
        .collectAsStateWithLifecycle()

    val perfilDocenteViewModel: PerfilDocenteViewModel = viewModel()

    val estadoPerfil by perfilDocenteViewModel.uiState
        .collectAsStateWithLifecycle()

    LaunchedEffect(estadoLogin.docente?.id) {
        estadoLogin.docente?.let {
            perfilDocenteViewModel.cargar(it.id)
        }
    }

    // Espera la restauración del perfil antes de evaluar rutas protegidas.
    if (estadoEstudiante.cargando) {
        androidx.compose.material3.LinearProgressIndicator()
        return
    }

    // Solo DocenteTheme consume esta preferencia; las pantallas de estudiantes conservan su tema.
    val modoOscuroDocente = estadoPerfil.takeIf {
        it.docente?.id == estadoLogin.docente?.id && estadoLogin.docente != null
    }?.modoOscuro
    val entradaActual by navController.currentBackStackEntryAsState()
    val areaEstudiante = entradaActual?.destination?.route in setOf(
        Screen.InicioEstudiante.route, Screen.PerfilEstudiante.route, Screen.AprendeConPudu.route,
        Screen.Asignatura.route, Screen.ResolverActividad.route, Screen.ResolverActividadRemota.route
    ) && estadoEstudiante.seleccionado != null
    val densidadActual = LocalDensity.current
    val densidadBase = LocalDensidadOriginal.current ?: densidadActual
    val densidadEstudiante = Density(
        densidadBase.density,
        densidadBase.fontScale * if (estadoEstudiante.preferencias.textoGrande) 1.15f else 1f
    )
    CompositionLocalProvider(
        LocalModoOscuroDocente provides modoOscuroDocente,
        com.example.mpazpro.ui.theme.LocalPaletaDocente provides estadoPerfil.takeIf {
            it.docente?.id == estadoLogin.docente?.id && estadoLogin.docente != null
        }?.paleta,
        LocalModoOscuroEstudiante provides if (areaEstudiante) estadoEstudiante.preferencias.modoOscuro else null,
        com.example.mpazpro.ui.theme.LocalPaletaEstudiante provides if (areaEstudiante)
            estadoEstudiante.preferencias.paleta else com.example.mpazpro.data.repository.PaletaEstudiante.AVENTURA,
        LocalDensity provides if (areaEstudiante) densidadEstudiante else densidadActual
    ) {
    NavHost(
        navController = navController,
        startDestination = Screen.Bienvenida.route,
        modifier = modifier
    ) {
        composable(Screen.PerfilDocente.route) {
            val docente = estadoLogin.docente

            if (docente == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Bienvenida.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            } else {
                PerfilDocenteScreen(
                    docenteId = docente.id,
                    perfilViewModel = perfilDocenteViewModel,
                    onVolver = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(Screen.CursosResultados.route) {
            val docente = estadoLogin.docente

            if (docente == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Bienvenida.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            } else {
                ResumenDocenteScreen(
                    docenteId = docente.id,
                    onVolver = {
                        navController.popBackStack()
                    },
                    onAbrirCurso = { cursoId ->
                        navController.navigate(
                            Screen.ResumenCurso.crearRuta(cursoId)
                        ) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        composable(
            route = Screen.ResumenCurso.route,
            arguments = listOf(
                navArgument("cursoId") {
                    type = NavType.LongType
                }
            )
        ) { entrada ->
            val docente = estadoLogin.docente

            val cursoId = entrada.arguments
                ?.getLong("cursoId") ?: -1L

            if (docente == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Bienvenida.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            } else {
                ResumenDocenteScreen(
                    docenteId = docente.id,
                    cursoId = cursoId,
                    onVolver = {
                        navController.popBackStack()
                    },
                    onAbrirEstudiante = { estudianteId ->
                        navController.navigate(
                            Screen.ResumenEstudiante.crearRuta(
                                cursoId = cursoId,
                                estudianteId = estudianteId
                            )
                        ) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        composable(
            route = Screen.ResumenEstudiante.route,
            arguments = listOf(
                navArgument("cursoId") {
                    type = NavType.LongType
                },
                navArgument("estudianteId") {
                    type = NavType.LongType
                }
            )
        ) { entrada ->
            val docente = estadoLogin.docente

            val cursoId = entrada.arguments
                ?.getLong("cursoId") ?: -1L

            val estudianteId = entrada.arguments
                ?.getLong("estudianteId") ?: -1L

            if (docente == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Bienvenida.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            } else {
                ResumenDocenteScreen(
                    docenteId = docente.id,
                    cursoId = cursoId,
                    estudianteId = estudianteId,
                    onVolver = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(Screen.Bienvenida.route) {
            BienvenidaScreen(
                onIngresarDocente = {
                    navController.navigate(Screen.LoginDocente.route) {
                        launchSingleTop = true
                    }
                },
                onIngresarEstudiante = {
                    navController.navigate(
                        Screen.SeleccionEstudiante.route
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.LoginDocente.route) {
            LoginDocenteScreen(
                loginViewModel = loginViewModel,
                onVolver = {
                    loginViewModel.cerrarSesion()
                    navController.popBackStack()
                },
                onRegistrar = {
                    navController.navigate(
                        Screen.RegistroDocente.route
                    ) {
                        launchSingleTop = true
                    }
                },
                onLoginExitoso = {
                    navController.navigate(
                        Screen.PanelDocente.route
                    ) {
                        popUpTo(Screen.Bienvenida.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.RegistroDocente.route) {
            RegistroDocenteScreen(
                onVolver = {
                    navController.popBackStack()
                },
                onRegistroExitoso = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.PanelDocente.route) {
            val docente = estadoLogin.docente
            val remoto by loginViewModel.sesionRemota.collectAsStateWithLifecycle()

            if (docente != null && remoto != null) {
                PanelDocenteScreen(
                    docente = docente,
                    correoCuenta = remoto?.correo,
                    fotoRuta = estadoPerfil.docente
                        ?.takeIf { it.id == docente.id }
                        ?.fotoRuta,
                    onAbrirPerfil = {
                        navController.navigate(Screen.PerfilDocente.route) {
                            launchSingleTop = true
                        }
                    },
                    onAbrirResultados = {
                        navController.navigate(Screen.CursosResultados.route) {
                            launchSingleTop = true
                        }
                    },
                    onCerrarSesion = {
                        loginViewModel.cerrarSesion()

                        navController.navigate(
                            Screen.Bienvenida.route
                        ) {
                            popUpTo(Screen.PanelDocente.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onAbrirActividades = { cursoId, accion ->
                        navController.navigate(
                            Screen.AsignaturasDocente.crearRuta(
                                cursoId = cursoId,
                                accion = accion
                            )
                        ) {
                            launchSingleTop = true
                        }
                    },
                    onAbrirCurso = { cursoId ->
                        navController.navigate(
                            Screen.Estudiantes.crearRuta(cursoId)
                        ) {
                            launchSingleTop = true
                        }
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(
                        Screen.Bienvenida.route
                    ) {
                        popUpTo(Screen.PanelDocente.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }
        }

        composable(
            route = Screen.Estudiantes.route,
            arguments = listOf(
                navArgument("cursoId") {
                    type = NavType.LongType
                }
            )
        ) { entrada ->
            val docente = estadoLogin.docente

            val cursoId = entrada.arguments
                ?.getLong("cursoId") ?: -1L

            if (docente != null) {
                EstudiantesScreen(
                    cursoId = cursoId,
                    docenteId = docente.id,
                    onVolver = {
                        navController.popBackStack()
                    },
                    onGestionarActividades = {
                        navController.navigate(
                            Screen.AsignaturasDocente.crearRuta(
                                cursoId
                            )
                        ) {
                            launchSingleTop = true
                        }
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(
                        Screen.Bienvenida.route
                    ) {
                        popUpTo(Screen.PanelDocente.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }
        }

        composable(
            route = Screen.AsignaturasDocente.route,
            arguments = listOf(
                navArgument("accion") {
                    type = NavType.StringType
                    defaultValue = "actividades"
                },
                navArgument("cursoId") {
                    type = NavType.LongType
                }
            )
        ) { entrada ->
            val accion = entrada.arguments
                ?.getString("accion") ?: "actividades"

            val docente = estadoLogin.docente

            val cursoId = entrada.arguments
                ?.getLong("cursoId") ?: -1L

            if (docente != null) {
                AsignaturasDocenteScreen(
                    cursoId = cursoId,
                    docenteId = docente.id,
                    mostrarBanco = accion != "resultados",
                    onVolver = {
                        navController.popBackStack()
                    },
                    onAbrirAsignatura = { asignaturaId ->
                        val ruta = if (accion == "crear") {
                            Screen.CrearActividad.crearRuta(
                                cursoId = cursoId,
                                asignaturaId = asignaturaId
                            )
                        } else {
                            Screen.ActividadesDocente.crearRuta(
                                cursoId = cursoId,
                                asignaturaId = asignaturaId,
                                accion = accion
                            )
                        }

                        navController.navigate(ruta) {
                            launchSingleTop = true
                        }
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(
                        Screen.Bienvenida.route
                    ) {
                        popUpTo(Screen.PanelDocente.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }
        }

        composable(
            route = Screen.ActividadesDocente.route,
            arguments = listOf(
                navArgument("accion") {
                    type = NavType.StringType
                    defaultValue = "actividades"
                },
                navArgument("cursoId") {
                    type = NavType.LongType
                },
                navArgument("asignaturaId") {
                    type = NavType.StringType
                }
            )
        ) { entrada ->
            val docente = estadoLogin.docente

            val cursoId = entrada.arguments
                ?.getLong("cursoId") ?: -1L

            val asignaturaId = entrada.arguments
                ?.getString("asignaturaId")

            val asignatura = Asignatura.buscarPorId(
                asignaturaId
            )

            if (docente == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(
                        Screen.Bienvenida.route
                    ) {
                        popUpTo(Screen.PanelDocente.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            } else if (asignatura == null) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            } else {
                ActividadesDocenteScreen(
                    cursoId = cursoId,
                    soloResultados = entrada.arguments
                        ?.getString("accion") == "resultados",
                    docenteId = docente.id,
                    asignatura = asignatura,
                    onVolver = {
                        navController.popBackStack()
                    },
                    onCrearActividad = {
                        navController.navigate(
                            Screen.CrearActividad.crearRuta(
                                cursoId = cursoId,
                                asignaturaId = asignatura.id
                            )
                        ) {
                            launchSingleTop = true
                        }
                    },
                    onVerResultados = { actividadId ->
                        navController.navigate(
                            Screen.ResultadosActividad.crearRuta(
                                actividadId
                            )
                        ) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        composable(
            route = Screen.CrearActividad.route,
            arguments = listOf(
                navArgument("cursoId") {
                    type = NavType.LongType
                },
                navArgument("asignaturaId") {
                    type = NavType.StringType
                }
            )
        ) { entrada ->
            val docente = estadoLogin.docente

            val cursoId = entrada.arguments
                ?.getLong("cursoId") ?: -1L

            val asignaturaId = entrada.arguments
                ?.getString("asignaturaId")

            val asignatura = Asignatura.buscarPorId(
                asignaturaId
            )

            if (docente == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(
                        Screen.Bienvenida.route
                    ) {
                        popUpTo(Screen.PanelDocente.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            } else if (asignatura == null) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            } else {
                CrearActividadScreen(
                    cursoId = cursoId,
                    docenteId = docente.id,
                    asignatura = asignatura,
                    onVolver = {
                        navController.popBackStack()
                    },
                    onGuardado = {
                        navController.navigate(
                            Screen.ActividadesDocente.crearRuta(
                                cursoId = cursoId,
                                asignaturaId = asignatura.id
                            )
                        ) {
                            popUpTo(Screen.CrearActividad.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        composable(Screen.SeleccionEstudiante.route) {
            SeleccionEstudianteScreen(
                accesoViewModel = accesoEstudianteViewModel,
                onIngresar = {
                    navController.navigate(
                        Screen.InicioEstudiante.route
                    ) {
                        popUpTo(Screen.SeleccionEstudiante.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.PerfilEstudiante.route) {
            if (estadoEstudiante.seleccionado == null) {
                LaunchedEffect(Unit) { navController.popBackStack() }
            } else {
                PerfilEstudianteScreen(
                    estado = estadoEstudiante,
                    onVolver = { navController.popBackStack() },
                    onContinuar = {
                        val pendiente = estadoEstudiante.paraContinuar
                        navController.popBackStack()
                        if (pendiente != null) {
                            navController.navigate(Screen.Asignatura.crearRuta(pendiente.asignaturaId)) {
                                launchSingleTop = true
                            }
                            navController.navigate(Screen.ResolverActividad.crearRuta(pendiente.id)) {
                                launchSingleTop = true
                            }
                        }
                    },
                    onPaleta = accesoEstudianteViewModel::cambiarPaleta,
                    onOscuro = accesoEstudianteViewModel::cambiarOscuro,
                    onTextoGrande = accesoEstudianteViewModel::cambiarTextoGrande,
                    onObjetivo = accesoEstudianteViewModel::cambiarObjetivo,
                    onFoto = accesoEstudianteViewModel::guardarFotoPerfil,
                    onErrorFoto = accesoEstudianteViewModel::mostrarErrorPerfil
                )
            }
        }

        composable(Screen.InicioEstudiante.route) {
            val docente = estadoLogin.docente // safety
            val estudiante = estadoEstudiante.seleccionado

            if (estudiante != null) {
                InicioEstudianteScreen(
                    estudiante = estudiante,
                    progreso = estadoEstudiante.progreso,
                    mensaje = estadoEstudiante.mensaje,
                    paraContinuar = null,
                    mensajesDocente = estadoEstudiante.mensajesDocente,
                    onLeerMensaje = accesoEstudianteViewModel::marcarMensajeLeido,
                    onAbrirPerfil = {
                        navController.navigate(Screen.PerfilEstudiante.route) { launchSingleTop = true }
                    },
                    onAprendeConPudu = {
                        navController.navigate(Screen.AprendeConPudu.route) { launchSingleTop = true }
                    },
                    onContinuar = { pendiente ->
                        navController.navigate(Screen.Asignatura.crearRuta(pendiente.asignaturaId)) {
                            launchSingleTop = true
                        }
                        navController.navigate(Screen.ResolverActividad.crearRuta(pendiente.id)) {
                            launchSingleTop = true
                        }
                    },
                    onAbrirAsignatura = { asignaturaId ->
                        navController.navigate(
                            Screen.Asignatura.crearRuta(
                                asignaturaId
                            )
                        ) {
                            launchSingleTop = true
                        }
                    },
                    onSalir = {
                        accesoEstudianteViewModel.salir()

                        navController.navigate(
                            Screen.Bienvenida.route
                        ) {
                            popUpTo(Screen.Bienvenida.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(
                        Screen.Bienvenida.route
                    ) {
                        popUpTo(Screen.PanelDocente.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }
        }

        composable(Screen.AprendeConPudu.route) {
            if (estadoEstudiante.seleccionado == null) {
                LaunchedEffect(Unit) { navController.popBackStack() }
            } else {
                com.example.mpazpro.ui.screens.AprendePuduScreen(
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = Screen.Asignatura.route,
            arguments = listOf(
                navArgument("asignaturaId") {
                    type = NavType.StringType
                }
            )
        ) { entrada ->
            val estudiante = estadoEstudiante.seleccionado

            val asignaturaId = entrada.arguments
                ?.getString("asignaturaId")

            val asignatura = Asignatura.buscarPorId(
                asignaturaId
            )

            if (estudiante == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(
                        Screen.Bienvenida.route
                    ) {
                        popUpTo(Screen.PanelDocente.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            } else if (asignatura == null) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            } else {
                AsignaturaScreen(
                    asignatura = asignatura,
                    estudiante = estudiante,
                    onVolver = {
                        navController.popBackStack()
                    },
                    onAbrirActividad = { actividadId ->
                        navController.navigate(
                            Screen.ResolverActividad.crearRuta(
                                actividadId
                            )
                        ) {
                            launchSingleTop = true
                        }
                    },
                    onAbrirActividadRemota = { actividadId ->
                        if (esDepuracion) {
                            navController.navigate(Screen.ResolverActividadRemota.crearRuta(actividadId)) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }

        composable(
            route = Screen.ResolverActividadRemota.route,
            arguments = listOf(navArgument("actividadId") { type = NavType.LongType })
        ) { entrada ->
            val estudiante = estadoEstudiante.seleccionado
            val actividadId = entrada.arguments?.getLong("actividadId") ?: -1L
            if (esDepuracion && estudiante != null && actividadId > 0) {
                ResolverActividadRemotaScreen(
                    actividadId = actividadId,
                    estudiante = estudiante,
                    onVolver = { navController.popBackStack() },
                    onSiguiente = { siguienteId ->
                        navController.navigate(Screen.ResolverActividadRemota.crearRuta(siguienteId)) {
                            popUpTo(entrada.destination.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            } else {
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }

        composable(
            route = Screen.ResolverActividad.route,
            arguments = listOf(
                navArgument("actividadId") {
                    type = NavType.LongType
                }
            )
        ) {
            // Los accesos antiguos a prácticas locales vuelven a la lista de actividades compartidas.
            LaunchedEffect(Unit) { navController.popBackStack() }
        }

        composable(
            route = Screen.ResultadosActividad.route,
            arguments = listOf(
                navArgument("actividadId") {
                    type = NavType.LongType
                }
            )
        ) { entrada ->
            val docente = estadoLogin.docente

            val actividadId = entrada.arguments
                ?.getLong("actividadId") ?: -1L

            if (docente == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(
                        Screen.Bienvenida.route
                    ) {
                        popUpTo(Screen.PanelDocente.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            } else {
                ResultadosActividadScreen(
                    actividadId = actividadId,
                    docenteId = docente.id,
                    onVolver = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
    }
}
