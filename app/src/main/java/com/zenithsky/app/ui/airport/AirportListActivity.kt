package com.zenithsky.app.ui.airport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.zenithsky.app.ui.components.EnConstruccion
import com.zenithsky.app.ui.theme.ZenithSkyTheme

// Responsable: Luis
class AirportListActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZenithSkyTheme {
                EnConstruccion(title = "Aeropuertos")
            }
        }
    }
}
