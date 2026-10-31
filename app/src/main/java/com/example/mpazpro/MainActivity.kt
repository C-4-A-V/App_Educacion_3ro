package com.example.mpazpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.mpazpro.navigation.AppNavigation
import com.example.mpazpro.ui.components.ContenidoAdaptativo
import com.example.mpazpro.ui.components.LecturaComoda
import com.example.mpazpro.ui.theme.MPAZPROTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LecturaComoda {
                MPAZPROTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        ContenidoAdaptativo(Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)) {
                            AppNavigation()
                        }
                    }
                }
            }
        }
    }
}
