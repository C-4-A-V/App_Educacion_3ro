package com.example.mpazpro.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mpazpro.ui.theme.MPAZPROTheme

@Preview(name = "Pudú: expresiones y tamaños", widthDp = 420, heightDp = 280, showBackground = true)
@Composable
fun PuduDetallesPreview() {
    MPAZPROTheme(darkTheme = false) {
        Surface {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AnimoPudu.entries.forEach { animo ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            PuduExplorador(Modifier.size(112.dp), animo)
                            Text(animo.name, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    PuduExplorador(Modifier.size(48.dp))
                    PuduExplorador(Modifier.size(64.dp), AnimoPudu.Pensando)
                    PuduExplorador(Modifier.size(80.dp), AnimoPudu.Alegre)
                    Text("48 · 64 · 80 dp", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
