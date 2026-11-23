package com.zenithsky.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zenithsky.app.data.local.ZenithSkyDatabase
import com.zenithsky.app.ui.airline.AirlineListActivity
import com.zenithsky.app.ui.airplane.AirplaneListActivity
import com.zenithsky.app.ui.airport.AirportListActivity
import com.zenithsky.app.ui.components.ZenithScaffold
import com.zenithsky.app.ui.theme.ZenithSkyTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZenithSkyTheme {
                PanelPrincipal()
            }
        }
    }
}

@Composable
fun PanelPrincipal() {
    val context = LocalContext.current

    // Room: cada vez que cambia la tabla, estas listas se actualizan solas
    val db = ZenithSkyDatabase.getInstance(context)
    val historial by db.operacionLogDao().obtenerTodos().collectAsState(initial = emptyList())
    val destacados by db.avionDestacadoDao().obtenerTodos().collectAsState(initial = emptyList())

    ZenithScaffold(title = "ZenithSky — Panel Operativo") { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OpcionMenu("Aeropuertos", "Gestionar aeropuertos") {
                context.startActivity(Intent(context, AirportListActivity::class.java))
            }
            OpcionMenu("Aerolíneas", "Gestionar aerolíneas") {
                context.startActivity(Intent(context, AirlineListActivity::class.java))
            }
            OpcionMenu("Aviones", "Gestionar la flota") {
                context.startActivity(Intent(context, AirplaneListActivity::class.java))
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item { Text("Historial de operaciones", style = MaterialTheme.typography.titleMedium) }
                if (historial.isEmpty()) item { Text("Sin operaciones todavía") }
                items(historial) { log -> Text("${log.operacion}: ${log.detalle}") }

                item { Text("Aviones destacados", style = MaterialTheme.typography.titleMedium) }
                if (destacados.isEmpty()) item { Text("Sin aviones destacados") }
                items(destacados) { avion -> Text(avion.descripcion) }
            }
        }
    }
}

@Composable
fun OpcionMenu(titulo: String, subtitulo: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Text(subtitulo, style = MaterialTheme.typography.bodyMedium)
        }
    }
}