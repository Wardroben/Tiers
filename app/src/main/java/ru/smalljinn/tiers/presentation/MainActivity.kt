package ru.smalljinn.tiers.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import ru.smalljinn.tiers.data.share.models.ShareList
import ru.smalljinn.tiers.navigation.TierNavHost
import ru.smalljinn.tiers.presentation.ui.theme.TiersTheme

private const val TAG = "Intent"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val importViewModel: ImportViewModel by viewModels()
    @OptIn(ExperimentalSerializationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when {
            //TODO make check type file and show message when imported or on error
            intent?.action == Intent.ACTION_VIEW -> {
                if (intent.type == "application/octet-stream") {
                    val uriShare = intent.data
                    uriShare?.let { uri ->
                        Log.i(TAG, "Received uri: $uri")
                        val type = contentResolver.getType(uri)
                        contentResolver.openInputStream(uri)?.use { stream ->
                            val bytes = stream.readBytes()
                            val shareList = Cbor.decodeFromByteArray<ShareList>(bytes)
                            importViewModel.obtainEvent(ImportEvent.Import(shareList))
                        }
                    }
                }
            }
        }
        enableEdgeToEdge()
        setContent {
            TiersTheme {
                TierNavHost()
            }
        }
    }
}
