package com.zenithsky.app.ui.airport

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zenithsky.app.data.remote.RetrofitClient
import com.zenithsky.app.data.remote.dto.AirportRequest
import com.zenithsky.app.ui.components.ZenithScaffold
import com.zenithsky.app.ui.theme.ZenithSkyTheme
import kotlinx.coroutines.launch

// Responsable: Luis
class AirportFormActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val idEdicion = intent.getIntExtra(EXTRA_ID, -1).takeIf { it != -1 }

        setContent {
            ZenithSkyTheme {
                var iata by remember { mutableStateOf("") }
                var icao by remember { mutableStateOf("") }
                var nombre by remember { mutableStateOf("") }
                var error by remember { mutableStateOf<String?>(null) }
                val scope = rememberCoroutineScope()
                val context = LocalContext.current

                // Modo edición: carga los datos actuales
                LaunchedEffect(idEdicion) {
                    if (idEdicion != null) {
                        try {
                            val r = RetrofitClient.api.getAirport(idEdicion)
                            r.body()?.let { iata = it.iata; icao = it.icao; nombre = it.name }
                        } catch (e: Exception) {
                            Toast.makeText(context, "No se pudo conectar con la API", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                fun guardar() {
                    error = when {
                        iata.isBlank() || icao.isBlank() || nombre.isBlank() -> "Todos los campos son obligatorios"
                        iata.length != 3 -> "El IATA debe tener 3 letras"
                        icao.length != 4 -> "El ICAO debe tener 4 letras"
                        else -> null
                    }
                    if (error != null) return

                    val request = AirportRequest(iata.trim().uppercase(), icao.trim().uppercase(), nombre.trim())
                    scope.launch {
                        try {
                            val r = if (idEdicion == null) RetrofitClient.api.createAirport(request)
                            else RetrofitClient.api.updateAirport(idEdicion, request)
                            if (r.isSuccessful) {
                                AirportLog.registrar(
                                    context,
                                    if (idEdicion == null) "POST" else "PUT",
                                    "Aeropuerto ${request.iata} ${if (idEdicion == null) "creado" else "actualizado"}"
                                )
                                Toast.makeText(context, "Guardado", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                error = r.mensajeError()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "No se pudo conectar con la API", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                ZenithScaffold(title = if (idEdicion == null) "Nuevo aeropuerto" else "Editar aeropuerto") { innerPadding ->
                    Column(
                        modifier = Modifier.padding(innerPadding).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = iata, onValueChange = { iata = it.take(3) },
                            label = { Text("IATA (3 letras)") }, singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = icao, onValueChange = { icao = it.take(4) },
                            label = { Text("ICAO (4 letras)") }, singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = nombre, onValueChange = { nombre = it },
                            label = { Text("Nombre") }, singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        error?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { guardar() }) { Text("Guardar") }
                            OutlinedButton(onClick = { finish() }) { Text("Cancelar") }
                        }
                    }
                }
            }
        }
    }

    companion object {
        // Si viene este extra, el formulario está en modo edición
        const val EXTRA_ID = "extra_id"
    }
}