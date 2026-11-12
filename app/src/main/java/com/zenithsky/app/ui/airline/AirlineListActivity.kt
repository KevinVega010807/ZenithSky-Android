package com.zenithsky.app.ui.airline

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.zenithsky.app.ui.components.EnConstruccion
import com.zenithsky.app.ui.theme.ZenithSkyTheme

// Responsable: Jefferson
class AirlineListActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZenithSkyTheme {
                EnConstruccion(title = "Aerolíneas")
            }
        }
    }
}
