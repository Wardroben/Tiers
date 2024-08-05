package ru.smalljinn.tiers.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import ru.smalljinn.tiers.presentation.navigation.TierNavHost
import ru.smalljinn.tiers.presentation.ui.theme.TiersTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TiersTheme {
                Surface {
                    TierNavHost()
                }
            }
        }
    }
}
