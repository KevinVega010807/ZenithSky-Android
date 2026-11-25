package com.zenithsky.app.ui.airline

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
import com.google.gson.JsonParser
import com.zenithsky.app.data.local.ZenithSkyDatabase
import com.zenithsky.app.data.local.entity.OperacionLog
import com.zenithsky.app.data.remote.RetrofitClient
import com.zenithsky.app.data.remote.dto.AirlineDto
import com.zenithsky.app.ui.components.ZenithScaffold
import com.zenithsky.app.ui.theme.ZenithSkyTheme
import kotlinx.coroutines.launch
import retrofit2.Response

// Responsable: Jefferson
class AirlineListActivity : ComponentActivity() {

    private var recargar by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZenithSkyTheme {
                var aerolineas by remember { mutableStateOf(listOf<AirlineDto>()) }
                var errorDialogo by remember { mutableStateOf<String?>(null) }
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

                LaunchedEffect(recargar) {
                    try {
                        val response = RetrofitClient.api.getAirlines()
                        if (response.isSuccessful) {
                            aerolineas = response.body() ?: emptyList()
                        } else {
                            Toast.makeText(context, parsearError(response), Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "No se pudo conectar con la API", Toast.LENGTH_SHORT).show()
                    }
                }

                ZenithScaffold(
                    title = "Aerolíneas (${aerolineas.size})",
                    floatingActionButton = {
                        FloatingActionButton(onClick = {
                            startActivity(Intent(this@AirlineListActivity, AirlineFormActivity::class.java))
                        }) {
                            Text("+")
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(items = aerolineas, key = { it.airlineId }) { aerolinea ->
                                AirlineItem(
                                    airline = aerolinea,
                                    onEditarClick = {
                                        val intent = Intent(this@AirlineListActivity, AirlineFormActivity::class.java)
                                        intent.putExtra(AirlineFormActivity.EXTRA_ID, aerolinea.airlineId)
                                        startActivity(intent)
                                    },
                                    onEliminarClick = {
                                        scope.launch {
                                            try {
                                                val response = RetrofitClient.api.deleteAirline(aerolinea.airlineId)
                                                if (response.isSuccessful) {
                                                    ZenithSkyDatabase.getInstance(context).operacionLogDao().insertar(
                                                        OperacionLog(
                                                            operacion = "DELETE",
                                                            detalle = "Aerolínea ${aerolinea.iata} eliminada",
                                                            fecha = System.currentTimeMillis()
                                                        )
                                                    )
                                                    Toast.makeText(context, "Aerolínea eliminada", Toast.LENGTH_SHORT).show()
                                                    recargar++
                                                } else if (response.code() == 409) {
                                                    errorDialogo = parsearError(response)
                                                } else {
                                                    Toast.makeText(context, parsearError(response), Toast.LENGTH_SHORT).show()
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
                        confirmButton = {
                            TextButton(onClick = { errorDialogo = null }) {
                                Text("Entendido")
                            }
                        }
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
fun AirlineItem(
    airline: AirlineDto,
    onEditarClick: () -> Unit,
    onEliminarClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row {
                Text("IATA: ", fontWeight = FontWeight.Bold)
                Text(airline.iata)
            }
            Row {
                Text("Nombre: ", fontWeight = FontWeight.Bold)
                Text(airline.airlineName)
            }
            Row {
                Text("Aeropuerto Base: ", fontWeight = FontWeight.Bold)
                val baseNombre = airline.baseAirport?.let { "${it.name} (${it.iata})" } ?: "Sin asignar"
                Text(baseNombre)
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
