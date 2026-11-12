package com.zenithsky.app.ui.airline

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.zenithsky.app.ui.components.EnConstruccion
import com.zenithsky.app.ui.theme.ZenithSkyTheme

// Responsable: Jefferson
class AirlineFormActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZenithSkyTheme {
                EnConstruccion(title = "Aerolínea")
            }
        }
    }

    companion object {
        // Si viene este extra, el formulario está en modo edición
        const val EXTRA_ID = "extra_id"
    }
}
