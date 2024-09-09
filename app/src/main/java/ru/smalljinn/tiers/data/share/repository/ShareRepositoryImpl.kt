package ru.smalljinn.tiers.data.share.repository

import android.content.Context
import android.net.Uri
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.encodeToByteArray
import ru.smalljinn.tiers.data.TierFileProvider
import ru.smalljinn.tiers.data.share.models.ShareList
import ru.smalljinn.tiers.util.Result
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

private const val SHARE_FILES_DIRECTORY = "export_files/"
private const val SHARE_FILE_TYPE = ".tier"

class ShareRepositoryImpl(private val appContext: Context) : ShareRepository {
    private val outputDirectory = File(appContext.filesDir, SHARE_FILES_DIRECTORY)

    @OptIn(ExperimentalSerializationApi::class)
    override fun createShareFile(shareList: ShareList): Result<Uri> {
        if (!outputDirectory.exists()) outputDirectory.mkdirs()
        val shareFile = File(outputDirectory, makeFileName(shareList.name))

        try {
            val cbor = Cbor.encodeToByteArray(shareList)
            FileOutputStream(shareFile).use { stream -> stream.write(cbor) }
        } catch (e: SerializationException) {
            e.printStackTrace()
            return Result.Error(message = "Serialization error")
        } catch (e: IOException) {
            e.printStackTrace()
            return Result.Error(message = "Error accessing file")
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.Error(e.message ?: "Unexpected error")
        }
        return Result.Success(
            TierFileProvider.getUriForFile(file = shareFile, context = appContext)
        )
    }

    private fun makeFileName(name: String) = "$name$SHARE_FILE_TYPE"
}