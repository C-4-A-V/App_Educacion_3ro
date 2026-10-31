package com.example.mpazpro.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.example.mpazpro.R

@Composable
fun VisibilidadClave(visible: Boolean, error: Boolean, onClick: () -> Unit) {
    Row {
        if (error) Icon(painterResource(android.R.drawable.ic_dialog_alert), "Error en la contraseña")
        IconButton(onClick = onClick) {
            Icon(
                painterResource(R.drawable.ic_visibility),
                contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña"
            )
        }
    }
}
