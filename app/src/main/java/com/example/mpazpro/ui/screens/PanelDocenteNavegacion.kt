package com.example.mpazpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpazpro.model.DocenteSesion
import com.example.mpazpro.ui.utils.claseAnchoVentana
import com.example.mpazpro.ui.viewmodel.CursosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelDocenteScreen(
    docente: DocenteSesion,
    onCerrarSesion: () -> Unit,
    onAbrirCurso: (Long) -> Unit,
    onAbrirActividades: (Long, String) -> Unit,
    onAbrirPerfil: () -> Unit,
    onAbrirResultados: () -> Unit,
    fotoRuta: String? = null,
    modifier: Modifier = Modifier,
    cursosViewModel: CursosViewModel = viewModel()
) {
    val compacto = claseAnchoVentana() == WindowWidthSizeClass.Compact
    NavigationSuiteScaffold(
        modifier = modifier,
        layoutType = if (compacto) NavigationSuiteType.NavigationBar else NavigationSuiteType.NavigationRail,
        navigationSuiteItems = {
            item(
                selected = true, onClick = {},
                icon = { Icon(painterResource(android.R.drawable.ic_menu_myplaces), null) },
                label = { Text("Inicio") }
            )
            item(
                selected = false, onClick = onAbrirResultados,
                icon = { Icon(painterResource(android.R.drawable.ic_menu_view), null) },
                label = { Text("Resultados") }
            )
            item(
                selected = false, onClick = onAbrirPerfil,
                icon = { Icon(painterResource(android.R.drawable.ic_menu_edit), null) },
                label = { Text("Mi perfil") }
            )
        }
    ) {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Espacio docente") }) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->
            PanelDocenteContenido(
                docente, onCerrarSesion, onAbrirCurso, onAbrirActividades,
                onAbrirPerfil, onAbrirResultados, fotoRuta,
                Modifier.padding(padding), cursosViewModel
            )
        }
    }
}
