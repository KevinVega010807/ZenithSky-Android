package com.zenithsky.app.ui.airplane

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zenithsky.app.data.local.ZenithSkyDatabase
import com.zenithsky.app.data.local.entity.AvionDestacado
import com.zenithsky.app.data.local.entity.OperacionLog
import com.zenithsky.app.data.remote.RetrofitClient
import com.zenithsky.app.data.remote.dto.AirlineDto
import com.zenithsky.app.data.remote.dto.AirplaneDto
import com.zenithsky.app.data.remote.dto.AirplaneTypeDto
import com.zenithsky.app.ui.components.Selector
import com.zenithsky.app.ui.theme.ZenithSkyTheme
import kotlinx.coroutines.launch

class AirplaneListActivity : ComponentActivity() {

    private var recargar by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ZenithSkyTheme {
                var aviones by remember { mutableStateOf(listOf<AirplaneDto>()) }
                var tipos by remember { mutableStateOf(listOf<AirplaneTypeDto>()) }
                var aerolineas by remember { mutableStateOf(listOf<AirlineDto>()) }
                var tipoFiltro by remember { mutableStateOf<AirplaneTypeDto?>(null) }
                var aerolineaFiltro by remember { mutableStateOf<AirlineDto?>(null) }
                val scope = rememberCoroutineScope()
                val context = LocalContext.current

                val db = ZenithSkyDatabase.getInstance(context)
                val destacados by db.avionDestacadoDao().obtenerTodos().collectAsState(initial = emptyList())


                fun cargarAviones() {
                    scope.launch {
                        try {
                            val response = RetrofitClient.api.getAirplanes(tipoFiltro?.typeId, aerolineaFiltro?.airlineId)
                            if (response.isSuccessful) {
                                aviones = response.body() ?: emptyList()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "No se pudo conectar con la API", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                LaunchedEffect(recargar) {
                    try {
                        tipos = RetrofitClient.api.getAirplaneTypes().body() ?: emptyList()
                        aerolineas = RetrofitClient.api.getAirlines().body() ?: emptyList()
                    } catch (e: Exception) {
                        Toast.makeText(context, "No se pudo conectar con la API", Toast.LENGTH_SHORT).show()
                    }
                    cargarAviones()
                }

                ListaAvionesScreen(
                    aviones = aviones,
                    tipos = tipos,
                    aerolineas = aerolineas,
                    tipoFiltro = tipoFiltro,
                    aerolineaFiltro = aerolineaFiltro,
                    idsDestacados = destacados.map { it.airplaneId },
                    onTipoSeleccionado = { tipo ->
                        tipoFiltro = tipo
                        cargarAviones()
                    },
                    onAerolineaSeleccionada = { aerolinea ->
                        aerolineaFiltro = aerolinea
                        cargarAviones()
                    },
                    onNuevoClick = {
                        startActivity(Intent(this@AirplaneListActivity, AirplaneFormActivity::class.java))
                    },
                    onEditarClick = { avion ->
                        val intent = Intent(this@AirplaneListActivity, AirplaneFormActivity::class.java)
                        intent.putExtra(AirplaneFormActivity.EXTRA_ID, avion.airplaneId)
                        startActivity(intent)
                    },
                    onEliminarClick = { avion ->
                        scope.launch {
                            try {
                                val response = RetrofitClient.api.deleteAirplane(avion.airplaneId)
                                if (response.isSuccessful) {
                                    db.operacionLogDao().insertar(
                                        OperacionLog(
                                            operacion = "DELETE",
                                            detalle = "Avión #${avion.airplaneId} eliminado (${avion.airplaneType.identifier}, ${avion.airline.airlineName})",
                                            fecha = System.currentTimeMillis()
                                        )
                                    )
                                    db.avionDestacadoDao().eliminarPorAirplaneId(avion.airplaneId)

                                    Toast.makeText(context, "Avión eliminado", Toast.LENGTH_SHORT).show()
                                    cargarAviones()
                                } else {
                                    Toast.makeText(context, "Error ${response.code()} al eliminar", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "No se pudo conectar con la API", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onDestacarClick = { avion ->
                        scope.launch {
                            val yaEsDestacado = destacados.any { it.airplaneId == avion.airplaneId }
                            if (yaEsDestacado) {
                                db.avionDestacadoDao().eliminarPorAirplaneId(avion.airplaneId)
                                Toast.makeText(context, "Quitado de destacados", Toast.LENGTH_SHORT).show()
                            } else {
                                db.avionDestacadoDao().insertar(
                                    AvionDestacado(
                                        airplaneId = avion.airplaneId,
                                        descripcion = "Avión #${avion.airplaneId} - ${avion.airplaneType.identifier} - ${avion.airline.airlineName} (${avion.capacity} pasajeros)"
                                    )
                                )
                                Toast.makeText(context, "Marcado como destacado", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onVolverClick = { finish() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        recargar++
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaAvionesScreen(
    aviones: List<AirplaneDto>,
    tipos: List<AirplaneTypeDto>,
    aerolineas: List<AirlineDto>,
    tipoFiltro: AirplaneTypeDto?,
    aerolineaFiltro: AirlineDto?,
    idsDestacados: List<Int>,
    onTipoSeleccionado: (AirplaneTypeDto?) -> Unit,
    onAerolineaSeleccionada: (AirlineDto?) -> Unit,
    onNuevoClick: () -> Unit,
    onEditarClick: (AirplaneDto) -> Unit,
    onEliminarClick: (AirplaneDto) -> Unit,
    onDestacarClick: (AirplaneDto) -> Unit,
    onVolverClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aviones (${aviones.size})") },
                navigationIcon = {
                    IconButton(onClick = onVolverClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevoClick) {
                Text("+")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            Selector(
                etiqueta = "Tipo",
                textoSeleccionado = tipoFiltro?.identifier ?: "Todos",
                opciones = listOf("Todos") + tipos.map { it.identifier },
                onSeleccion = { index -> onTipoSeleccionado(if (index == 0) null else tipos[index - 1]) }
            )
            Spacer(modifier = Modifier.height(8.dp))


            Selector(
                etiqueta = "Aerolínea",
                textoSeleccionado = aerolineaFiltro?.airlineName ?: "Todas",
                opciones = listOf("Todas") + aerolineas.map { it.airlineName },
                onSeleccion = { index -> onAerolineaSeleccionada(if (index == 0) null else aerolineas[index - 1]) }
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(items = aviones, key = { it.airplaneId }) { avion ->
                    AirplaneItem(
                        avion = avion,
                        esDestacado = idsDestacados.contains(avion.airplaneId),
                        onEditarClick = { onEditarClick(avion) },
                        onEliminarClick = { onEliminarClick(avion) },
                        onDestacarClick = { onDestacarClick(avion) }
                    )
                }
            }
        }
    }
}

@Composable
fun AirplaneItem(
    avion: AirplaneDto,
    esDestacado: Boolean,
    onEditarClick: () -> Unit,
    onEliminarClick: () -> Unit,
    onDestacarClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row {
                Text(text = "ID: ", fontWeight = FontWeight.Bold)
                Text(text = avion.airplaneId.toString())
                if (esDestacado) Text(text = "  ★ Destacado")
            }
            Row {
                Text(text = "Capacidad: ", fontWeight = FontWeight.Bold)
                Text(text = "${avion.capacity} pasajeros")
            }
            Row {
                Text(text = "Tipo: ", fontWeight = FontWeight.Bold)
                Text(text = "${avion.airplaneType.identifier} - ${avion.airplaneType.description}")
            }
            Row {
                Text(text = "Aerolínea: ", fontWeight = FontWeight.Bold)
                Text(text = avion.airline.airlineName)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onEditarClick) { Text("Editar") }
                OutlinedButton(onClick = onEliminarClick) { Text("Eliminar") }
                OutlinedButton(onClick = onDestacarClick) {
                    Text(if (esDestacado) "Quitar ★" else "Destacar")
                }
            }
        }
    }
}