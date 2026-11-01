package com.example.mpazpro.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mpazpro.ui.components.FondoAventura
import com.example.mpazpro.ui.components.anchoFormulario
import com.example.mpazpro.ui.theme.AventuraTheme

@Composable
fun BienvenidaScreen(
    onIngresarDocente: () -> Unit,
    onIngresarEstudiante: () -> Unit,
    modifier: Modifier = Modifier
) {
    AventuraTheme {
        FondoAventura(modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().anchoFormulario().verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text("✨ ¡Una nueva aventura te espera!", Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
                Box(
                    Modifier.size(132.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(38.dp)),
                    contentAlignment = Alignment.Center
                ) { Text("📚", style = MaterialTheme.typography.displayLarge) }
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("MPAZ PRO", style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                    Text("¡Aprender es una aventura!", style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center)
                    Text("Explora, descubre y aprende en 3.º básico.",
                        style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("📖     🔢     🌱     🌎", style = MaterialTheme.typography.headlineSmall)
                TarjetaAcceso(
                    titulo = "Ingresar como estudiante",
                    detalle = "Elige tu perfil y comienza tu aventura",
                    icono = "🎒",
                    fondo = MaterialTheme.colorScheme.primary,
                    texto = MaterialTheme.colorScheme.onPrimary,
                    onClick = onIngresarEstudiante
                )
                TarjetaAcceso(
                    titulo = "Ingresar como docente",
                    detalle = "Prepara nuevas aventuras para tu curso",
                    icono = "👨‍🏫",
                    fondo = MaterialTheme.colorScheme.secondaryContainer,
                    texto = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = onIngresarDocente
                )
                Text("Cada pequeño paso es un gran descubrimiento.",
                    style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))
            }
        }
    }
}

@Composable
private fun TarjetaAcceso(
    titulo: String, detalle: String, icono: String, fondo: Color, texto: Color, onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(2.dp, texto.copy(alpha = .12f)),
        colors = CardDefaults.cardColors(containerColor = fondo, contentColor = texto),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(Modifier.fillMaxWidth().heightIn(min = 102.dp).padding(20.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = .18f)) {
                Text(icono, Modifier.padding(12.dp), style = MaterialTheme.typography.headlineMedium)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(detalle, style = MaterialTheme.typography.bodyMedium)
            }
            Text("›", style = MaterialTheme.typography.headlineMedium)
        }
    }
}
