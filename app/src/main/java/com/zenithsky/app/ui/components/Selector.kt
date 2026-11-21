package com.zenithsky.app.ui.components


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun Selector(
    etiqueta: String,
    textoSeleccionado: String,
    opciones: List<String>,
    onSeleccion: (Int) -> Unit
) {
    var menuExpandido by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { menuExpandido = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "$etiqueta: $textoSeleccionado")
        }
        DropdownMenu(
            expanded = menuExpandido,
            onDismissRequest = { menuExpandido = false }
        ) {
            opciones.forEachIndexed { index, opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = { menuExpandido = false; onSeleccion(index) }
                )
            }
        }
    }
}