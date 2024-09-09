package ru.smalljinn.tiers.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import ru.smalljinn.tiers.data.share.models.ShareList
import ru.smalljinn.tiers.presentation.navigation.TierNavHost
import ru.smalljinn.tiers.presentation.ui.theme.TiersTheme

private const val TAG = "Intent"


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalSerializationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TiersTheme {
                TierNavHost()
                val viewModel: ImportViewModel = viewModel(factory = ImportViewModel.Factory)
                when {
                    intent?.action == Intent.ACTION_VIEW -> {
                        if (intent.type == "application/octet-stream") {
                            val uriShare = intent.data
                            uriShare?.let { uri ->
                                Log.i(TAG, "Received uri: $uri")
                                val type = contentResolver.getType(uri)
                                contentResolver.openInputStream(uri)?.use { stream ->
                                    val bytes = stream.readBytes()
                                    val shareList = Cbor.decodeFromByteArray<ShareList>(bytes)
                                    viewModel.obtainEvent(ImportEvent.Import(shareList))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
