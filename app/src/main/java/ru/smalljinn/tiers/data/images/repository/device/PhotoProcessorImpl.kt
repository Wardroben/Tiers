package ru.smalljinn.tiers.data.images.repository.device

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.net.toFile
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import kotlin.math.roundToInt

const val COMPRESSED_PHOTOS_OUTPUT_DIR = "images/"
private const val TAG = "PhotoProcessorImpl"
private const val TARGET_IMAGE_SIZE = 256F

class PhotoProcessorImpl(
    private val appContext: Context,
) : PhotoProcessor {
    private val outputDirectory = File(appContext.filesDir, COMPRESSED_PHOTOS_OUTPUT_DIR)
    override suspend fun compressAndSaveImages(imageUris: List<Uri>): List<Uri> =
        withContext(Dispatchers.IO) {
            //compress images
            val bitmaps: List<Bitmap> = imageUris.map { uri: Uri -> readImage(uri) }
            val scaledBitmaps: List<Bitmap> = bitmaps.map { scaleImage(it) }
            //save images
            val savedBitmapUris: List<Uri> =
                scaledBitmaps.map { bitmap -> saveCompressedImage(bitmap) }
            return@withContext savedBitmapUris
        }

    override suspend fun deleteImagesFromDevice(imageUris: List<Uri>): Boolean {
        imageUris.forEach { uri ->
            val imageFile = uri.toFile()
            if (imageFile.exists() && imageFile.isFile) {
                try {
                    imageFile.delete()
                } catch (e: IOException) {
                    Log.e(TAG, "Can't delete ${imageFile.name}")
                    return false
                } catch (e: Exception) {
                    Log.e(TAG, "Unexpected error ${e.message}")
                    return false
                }
                Log.v(TAG, "Image deleted from device: ${imageFile.absolutePath}")
            } else {
                Log.v(TAG, "${imageFile.absolutePath} is not file")
            }
        }
        return true
    }

    private fun readImage(uri: Uri): Bitmap {
        try {
            appContext.contentResolver.openInputStream(uri).use { inputStream ->
                return BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: IOException) {
            Log.e(TAG, e.toString())
            throw IOException(e.message)
        }
    }

    /**
     * Scales images to 256px size
     */
    private fun scaleImage(bitmap: Bitmap): Bitmap {
        with(bitmap) {
            val ratio: Float =
                if (width >= height) TARGET_IMAGE_SIZE / width else TARGET_IMAGE_SIZE / height
            val scaledWidth = (width * ratio).roundToInt()
            val scaledHeight = (height * ratio).roundToInt()

            val scaledBitmap = Bitmap.createScaledBitmap(
                this@with,
                scaledWidth,
                scaledHeight,
                false
            )
            return scaledBitmap
        }
    }

    private fun saveCompressedImage(bitmap: Bitmap): Uri {
        val fileName = "image_${UUID.randomUUID()}"
        if (!outputDirectory.exists()) outputDirectory.mkdirs()

        val photoFile = File(outputDirectory, fileName)
        try {
            FileOutputStream(photoFile).use { outputStream ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 50, outputStream)
                } else {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 65, outputStream)
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, e.message.toString())
            throw IOException(e.message)
        }
        Log.v(TAG, "Image saved to file: ${photoFile.absolutePath}")
        return photoFile.toUri()
    }
}