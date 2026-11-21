package com.zenithsky.app.ui.airplane

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zenithsky.app.data.remote.RetrofitClient
import com.zenithsky.app.data.remote.dto.AirlineDto
import com.zenithsky.app.data.remote.dto.AirplaneRequest
import com.zenithsky.app.data.remote.dto.AirplaneTypeDto
import com.zenithsky.app.ui.components.Selector
import com.zenithsky.app.ui.theme.ZenithSkyTheme
import kotlinx.coroutines.launch

class AirplaneFormActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val airplaneId = intent.getIntExtra(EXTRA_ID, 0)
        val esEdicion = airplaneId != 0

        setContent {
            ZenithSkyTheme {
                var capacidad by remember { mutableStateOf("") }
                var tipos by remember { mutableStateOf(listOf<AirplaneTypeDto>()) }
                var aerolineas by remember { mutableStateOf(listOf<AirlineDto>()) }
                var tipoSeleccionado by remember { mutableStateOf<AirplaneTypeDto?>(null) }
                var aerolineaSeleccionada by remember { mutableStateOf<AirlineDto?>(null) }
                val scope = rememberCoroutineScope()
                val context = LocalContext.current


                LaunchedEffect(Unit) {
                    try {
                        tipos = RetrofitClient.api.getAirplaneTypes().body() ?: emptyList()
                        aerolineas = RetrofitClient.api.getAirlines().body() ?: emptyList()

                        if (esEdicion) {
                            val avion = RetrofitClient.api.getAirplane(airplaneId).body()
                            if (avion != null) {
                                capacidad = avion.capacity.toString()
                                tipoSeleccionado = tipos.find { it.typeId == avion.airplaneType.typeId }
                                aerolineaSeleccionada = aerolineas.find { it.airlineId == avion.airline.airlineId }
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "No se pudo conectar con la API", Toast.LENGTH_SHORT).show()
                    }
                }

                FormularioAvionScreen(
                    titulo = if (esEdicion) "Editar avión" else "Nuevo avión",
                    capacidad = capacidad,
                    onCapacidadChange = { nuevoTexto -> capacidad = nuevoTexto },
                    tipos = tipos,
                    tipoSeleccionado = tipoSeleccionado,
                    onTipoSeleccionado = { tipo -> tipoSeleccionado = tipo },
                    aerolineas = aerolineas,
                    aerolineaSeleccionada = aerolineaSeleccionada,
                    onAerolineaSeleccionada = { aerolinea -> aerolineaSeleccionada = aerolinea },
                    onGuardarClick = {
                        val cap = capacidad.toIntOrNull()
                        val tipo = tipoSeleccionado
                        val aerolinea = aerolineaSeleccionada

                        if (capacidad.isBlank() || cap == null || cap <= 0) {
                            Toast.makeText(context, "La capacidad debe ser un número mayor que cero", Toast.LENGTH_SHORT).show()
                        } else if (tipo == null) {
                            Toast.makeText(context, "Seleccione el tipo de avión", Toast.LENGTH_SHORT).show()
                        } else if (aerolinea == null) {
                            Toast.makeText(context, "Seleccione la aerolínea", Toast.LENGTH_SHORT).show()
                        } else {
                            val request = AirplaneRequest(cap, tipo.typeId, aerolinea.airlineId)
                            scope.launch {
                                try {

                                    val response = if (esEdicion) {
                                        RetrofitClient.api.updateAirplane(airplaneId, request)
                                    } else {
                                        RetrofitClient.api.createAirplane(request)
                                    }

                                    if (response.isSuccessful) {
                                        Toast.makeText(context, "Avión guardado correctamente", Toast.LENGTH_SHORT).show()

                                        finish()
                                    } else {
                                        Toast.makeText(context, "Error ${response.code()} al guardar", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "No se pudo conectar con la API", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    onVolverClick = { finish() }
                )
            }
        }
    }

    companion object {
        const val EXTRA_ID = "extra_id"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioAvionScreen(
    titulo: String,
    capacidad: String,
    onCapacidadChange: (String) -> Unit,
    tipos: List<AirplaneTypeDto>,
    tipoSeleccionado: AirplaneTypeDto?,
    onTipoSeleccionado: (AirplaneTypeDto) -> Unit,
    aerolineas: List<AirlineDto>,
    aerolineaSeleccionada: AirlineDto?,
    onAerolineaSeleccionada: (AirlineDto) -> Unit,
    onGuardarClick: () -> Unit,
    onVolverClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    IconButton(onClick = onVolverClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = capacidad,
                onValueChange = onCapacidadChange,
                label = { Text("Capacidad de pasajeros") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            Selector(
                etiqueta = "Tipo de avión",
                textoSeleccionado = tipoSeleccionado?.identifier ?: "Seleccione",
                opciones = tipos.map { "${it.identifier} - ${it.description}" },
                onSeleccion = { index -> onTipoSeleccionado(tipos[index]) }
            )
            Spacer(modifier = Modifier.height(12.dp))

            Selector(
                etiqueta = "Aerolínea",
                textoSeleccionado = aerolineaSeleccionada?.airlineName ?: "Seleccione",
                opciones = aerolineas.map { it.airlineName },
                onSeleccion = { index -> onAerolineaSeleccionada(aerolineas[index]) }
            )
            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = onGuardarClick, modifier = Modifier.fillMaxWidth()) {
                Text("GUARDAR")
            }
        }
    }
}