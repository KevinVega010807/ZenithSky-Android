package com.zenithsky.app.ui.airline

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import com.google.gson.JsonParser
import com.zenithsky.app.data.local.ZenithSkyDatabase
import com.zenithsky.app.data.local.entity.OperacionLog
import com.zenithsky.app.data.remote.RetrofitClient
import com.zenithsky.app.data.remote.dto.AirlineRequest
import com.zenithsky.app.data.remote.dto.AirportDto
import com.zenithsky.app.ui.components.Selector
import com.zenithsky.app.ui.components.ZenithScaffold
import com.zenithsky.app.ui.theme.ZenithSkyTheme
import kotlinx.coroutines.launch
import retrofit2.Response

// Responsable: Jefferson
class AirlineFormActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val idEdicion = intent.getIntExtra(EXTRA_ID, -1).takeIf { it != -1 }

        setContent {
            ZenithSkyTheme {
                var iata by remember { mutableStateOf("") }
                var airlineName by remember { mutableStateOf("") }
                var aeropuertos by remember { mutableStateOf(listOf<AirportDto>()) }
                var aeropuertoSeleccionado by remember { mutableStateOf<AirportDto?>(null) }
                var error by remember { mutableStateOf<String?>(null) }
                val scope = rememberCoroutineScope()
                val context = LocalContext.current

                fun parsearError(response: Response<*>): String {
                    return try {
                        val raw = response.errorBody()?.string()
                        if (raw.isNullOrBlank()) "Error ${response.code()}"
                        else JsonParser.parseString(raw).asJsonObject.get("message").asString
                    } catch (e: Exception) {
                        "Error ${response.code()}"
                    }
                }

                //Cargar catálogo para el selector
                LaunchedEffect(Unit) {
                    try {
                        val response = RetrofitClient.api.getAirports()
                        if (response.isSuccessful) {
                            val lista = response.body() ?: emptyList()
                            aeropuertos = lista

                            if (idEdicion != null) {
                                val airlineResponse = RetrofitClient.api.getAirline(idEdicion)
                                airlineResponse.body()?.let { airline ->
                                    iata = airline.iata
                                    airlineName = airline.airlineName
                                    aeropuertoSeleccionado = lista.find { it.airportId == airline.baseAirport?.airportId }
                                }
                            }
                        } else {
                            Toast.makeText(context, "No se pudieron cargar los aeropuertos", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "No se pudo conectar con la API", Toast.LENGTH_SHORT).show()
                    }
                }

                fun guardar() {
                    error = when {
                        iata.isBlank() || airlineName.isBlank() -> "Todos los campos son obligatorios"
                        iata.trim().length != 2 -> "El código IATA debe tener exactamente 2 letras"
                        aeropuertoSeleccionado == null -> "Debe seleccionar un aeropuerto base"
                        else -> null
                    }
                    if (error != null) return

                    val request = AirlineRequest(
                        iata = iata.trim().uppercase(),
                        airlineName = airlineName.trim(),
                        baseAirport = aeropuertoSeleccionado!!.airportId
                    )

                    scope.launch {
                        try {
                            val response = if (idEdicion == null) {
                                RetrofitClient.api.createAirline(request)
                            } else {
                                RetrofitClient.api.updateAirline(idEdicion, request)
                            }

                            if (response.isSuccessful) {
                                ZenithSkyDatabase.getInstance(context).operacionLogDao().insertar(
                                    OperacionLog(
                                        operacion = if (idEdicion == null) "POST" else "PUT",
                                        detalle = "Aerolínea ${request.iata} ${if (idEdicion == null) "creada" else "actualizada"}",
                                        fecha = System.currentTimeMillis()
                                    )
                                )
                                Toast.makeText(context, "Guardado correctamente", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                error = parsearError(response)
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "No se pudo conectar con la API", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                ZenithScaffold(
                    title = if (idEdicion == null) "Nueva aerolínea" else "Editar aerolínea"
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = iata,
                            onValueChange = { iata = it.take(2).uppercase() },
                            label = { Text("IATA (2 letras)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = airlineName,
                            onValueChange = { airlineName = it },
                            label = { Text("Nombre de la aerolínea") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Selector(
                            etiqueta = "Aeropuerto Base",
                            textoSeleccionado = aeropuertoSeleccionado?.let { "${it.name} (${it.iata})" } ?: "Seleccionar",
                            opciones = aeropuertos.map { "${it.name} (${it.iata})" },
                            onSeleccion = { index ->
                                aeropuertoSeleccionado = aeropuertos[index]
                            }
                        )

                        error?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { guardar() }) {
                                Text("Guardar")
                            }
                            OutlinedButton(onClick = { finish() }) {
                                Text("Cancelar")
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_ID = "extra_id"
    }
}
