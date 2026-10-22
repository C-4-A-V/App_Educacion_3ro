package com.example.mpazpro.navigation

import androidx.compose.runtime.Composable
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
import com.example.mpazpro.ui.screens.RegistroDocenteScreen
import com.example.mpazpro.ui.screens.ResolverActividadScreen
import com.example.mpazpro.ui.screens.SeleccionEstudianteScreen
import com.example.mpazpro.ui.viewmodel.AccesoEstudianteViewModel
import com.example.mpazpro.ui.viewmodel.LoginDocenteViewModel

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    loginViewModel: LoginDocenteViewModel = viewModel()
) {
    val navController = rememberNavController()

    val estadoLogin by loginViewModel.uiState
        .collectAsStateWithLifecycle()

    val accesoEstudianteViewModel: AccesoEstudianteViewModel =
        viewModel()

    val estadoEstudiante by accesoEstudianteViewModel.uiState
        .collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Screen.Bienvenida.route,
        modifier = modifier
    ) {
        composable(Screen.Bienvenida.route) {
            BienvenidaScreen(
                onIngresarDocente = {
                    navController.navigate(Screen.LoginDocente.route) {
                        launchSingleTop = true
                    }
                },
                onIngresarEstudiante = {
                    navController.navigate(Screen.SeleccionEstudiante.route) {
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
                    navController.navigate(Screen.RegistroDocente.route) {
                        launchSingleTop = true
                    }
                },
                onLoginExitoso = {
                    navController.navigate(Screen.PanelDocente.route) {
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
                }
            )
        }

        composable(Screen.PanelDocente.route) {
            val docente = estadoLogin.docente

            if (docente != null) {
                PanelDocenteScreen(
                    docente = docente,
                    onCerrarSesion = {
                        loginViewModel.cerrarSesion()

                        navController.navigate(Screen.Bienvenida.route) {
                            popUpTo(Screen.PanelDocente.route) {
                                inclusive = true
                            }
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
                    navController.navigate(Screen.Bienvenida.route) {
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
            val cursoId = entrada.arguments?.getLong("cursoId") ?: -1L

            if (docente != null) {
                EstudiantesScreen(
                    cursoId = cursoId,
                    docenteId = docente.id,
                    onVolver = {
                        navController.popBackStack()
                    },
                    onGestionarActividades = {
                        navController.navigate(
                            Screen.AsignaturasDocente.crearRuta(cursoId)
                        ) {
                            launchSingleTop = true
                        }
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Bienvenida.route) {
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
                navArgument("cursoId") {
                    type = NavType.LongType
                }
            )
        ) { entrada ->
            val docente = estadoLogin.docente
            val cursoId = entrada.arguments?.getLong("cursoId") ?: -1L

            if (docente != null) {
                AsignaturasDocenteScreen(
                    cursoId = cursoId,
                    docenteId = docente.id,
                    onVolver = {
                        navController.popBackStack()
                    },
                    onAbrirAsignatura = { asignaturaId ->
                        navController.navigate(
                            Screen.ActividadesDocente.crearRuta(
                                cursoId = cursoId,
                                asignaturaId = asignaturaId
                            )
                        ) {
                            launchSingleTop = true
                        }
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Bienvenida.route) {
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

            val asignatura = Asignatura.buscarPorId(asignaturaId)

            if (docente == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Bienvenida.route) {
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

            val asignatura = Asignatura.buscarPorId(asignaturaId)

            if (docente == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Bienvenida.route) {
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
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(Screen.SeleccionEstudiante.route) {
            SeleccionEstudianteScreen(
                accesoViewModel = accesoEstudianteViewModel,
                onSeleccionar = { estudianteId ->
                    accesoEstudianteViewModel.seleccionar(estudianteId)

                    navController.navigate(Screen.InicioEstudiante.route) {
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

        composable(Screen.InicioEstudiante.route) {
            val estudiante = estadoEstudiante.seleccionado

            if (estudiante != null) {
                InicioEstudianteScreen(
                    estudiante = estudiante,
                    onAbrirAsignatura = { asignaturaId ->
                        navController.navigate(
                            Screen.Asignatura.crearRuta(asignaturaId)
                        ) {
                            launchSingleTop = true
                        }
                    },
                    onSalir = {
                        accesoEstudianteViewModel.salir()

                        navController.navigate(Screen.Bienvenida.route) {
                            popUpTo(Screen.Bienvenida.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Bienvenida.route) {
                        popUpTo(Screen.Bienvenida.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
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

            val asignatura = Asignatura.buscarPorId(asignaturaId)

            if (estudiante == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Bienvenida.route) {
                        popUpTo(Screen.Bienvenida.route) {
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
                    }
                )
            }
        }

        composable(
            route = Screen.ResolverActividad.route,
            arguments = listOf(
                navArgument("actividadId") {
                    type = NavType.LongType
                }
            )
        ) { entrada ->
            val estudiante = estadoEstudiante.seleccionado

            val actividadId = entrada.arguments
                ?.getLong("actividadId") ?: -1L

            if (estudiante == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Bienvenida.route) {
                        popUpTo(Screen.PanelDocente.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            } else {
                ResolverActividadScreen(
                    actividadId = actividadId,
                    estudiante = estudiante,
                    onVolver = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
