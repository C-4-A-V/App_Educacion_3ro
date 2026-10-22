package com.example.mpazpro.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File

@Composable
fun ImagenInteligente(
    ruta: String?,
    descripcion: String,
    modifier: Modifier = Modifier
) {
    val imagenPorDefecto = painterResource(
        android.R.drawable.ic_menu_myplaces
    )

    AsyncImage(
        model = ruta?.let { File(it) },
        contentDescription = descripcion,
        placeholder = imagenPorDefecto,
        fallback = imagenPorDefecto,
        error = imagenPorDefecto,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(88.dp)
            .clip(CircleShape)
    )
}
