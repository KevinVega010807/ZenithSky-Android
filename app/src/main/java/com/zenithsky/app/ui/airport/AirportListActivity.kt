package com.zenithsky.app.ui.airport

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.zenithsky.app.data.remote.RetrofitClient
import com.zenithsky.app.data.remote.dto.AirportDto
import com.zenithsky.app.ui.components.ZenithScaffold
import com.zenithsky.app.ui.theme.ZenithSkyTheme
import kotlinx.coroutines.launch


class AirportListActivity : ComponentActivity() {

    private var recargar by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZenithSkyTheme {
                var aeropuertos by remember { mutableStateOf(listOf<AirportDto>()) }
                var busqueda by remember { mutableStateOf("") }
                var errorDialogo by remember { mutableStateOf<String?>(null) }
                val scope = rememberCoroutineScope()
                val context = LocalContext.current

                // Se vuelve a ejecutar al escribir en la búsqueda o al volver del formulario
                LaunchedEffect(busqueda, recargar) {
                    try {
                        val r = RetrofitClient.api.getAirports(busqueda.ifBlank { null })
                        if (r.isSuccessful) {
                            aeropuertos = r.body() ?: emptyList()
                        } else {
                            Toast.makeText(context, "Error ${r.code()} al cargar aeropuertos", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }

                ZenithScaffold(
                    title = "Aeropuertos (${aeropuertos.size})",
                    floatingActionButton = {
                        FloatingActionButton(onClick = {
                            startActivity(Intent(this@AirportListActivity, AirportFormActivity::class.java))
                        }) { Text("+") }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier.padding(innerPadding).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = busqueda,
                            onValueChange = { busqueda = it },
                            label = { Text("Buscar (IATA, ICAO o nombre)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(items = aeropuertos, key = { it.airportId }) { a ->
                                AirportItem(
                                    airport = a,
                                    onEditarClick = {
                                        val i = Intent(this@AirportListActivity, AirportFormActivity::class.java)
                                        i.putExtra(AirportFormActivity.EXTRA_ID, a.airportId)
                                        startActivity(i)
                                    },
                                    onEliminarClick = {
                                        scope.launch {
                                            try {
                                                val r = RetrofitClient.api.deleteAirport(a.airportId)
                                                if (r.isSuccessful) {
                                                    AirportLog.registrar(context, "DELETE", "Aeropuerto ${a.iata} eliminado")
                                                    Toast.makeText(context, "Aeropuerto eliminado", Toast.LENGTH_SHORT).show()
                                                    recargar++
                                                } else if (r.code() == 409) {
                                                    errorDialogo = r.mensajeError()
                                                } else {
                                                    Toast.makeText(context, r.mensajeError(), Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "No se pudo conectar con la API", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                errorDialogo?.let { mensaje ->
                    AlertDialog(
                        onDismissRequest = { errorDialogo = null },
                        title = { Text("No se puede eliminar") },
                        text = { Text(mensaje) },
                        confirmButton = { TextButton(onClick = { errorDialogo = null }) { Text("Entendido") } }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        recargar++
    }
}

@Composable
fun AirportItem(airport: AirportDto, onEditarClick: () -> Unit, onEliminarClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row {
                Text("IATA: ", fontWeight = FontWeight.Bold)
                Text(airport.iata)
            }
            Row {
                Text("ICAO: ", fontWeight = FontWeight.Bold)
                Text(airport.icao)
            }
            Row {
                Text("Nombre: ", fontWeight = FontWeight.Bold)
                Text(airport.name)
            }
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onEditarClick) { Text("Editar") }
                OutlinedButton(onClick = onEliminarClick) { Text("Eliminar") }
            }
        }
    }
}