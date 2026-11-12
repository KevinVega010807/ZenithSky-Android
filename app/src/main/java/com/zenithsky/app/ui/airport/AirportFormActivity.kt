package com.zenithsky.app.ui.airport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.zenithsky.app.ui.components.EnConstruccion
import com.zenithsky.app.ui.theme.ZenithSkyTheme

// Responsable: Luis
class AirportFormActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZenithSkyTheme {
                EnConstruccion(title = "Aeropuerto")
            }
        }
    }

    companion object {
        // Si viene este extra, el formulario está en modo edición
        const val EXTRA_ID = "extra_id"
    }
}
