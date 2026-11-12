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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zenithsky.app.ui.airline.AirlineListActivity
import com.zenithsky.app.ui.airplane.AirplaneListActivity
import com.zenithsky.app.ui.airport.AirportListActivity
import com.zenithsky.app.ui.components.ZenithScaffold
import com.zenithsky.app.ui.theme.ZenithSkyTheme

// Responsable: Luis (agregar historial de OperacionLog y aviones destacados con collectAsState)
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

            // TODO Luis: LazyColumn con historial (OperacionLog) y aviones destacados (AvionDestacado)
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
