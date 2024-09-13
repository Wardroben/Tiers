package ru.smalljinn.tiers.data.images.photo_processor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.net.toFile
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import kotlin.math.roundToInt


const val COMPRESSED_PHOTOS_OUTPUT_DIR = "images/"
private const val TAG = "PhotoProcessorImpl"
private const val TARGET_IMAGE_SIZE = 256F

class PhotoProcessorImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
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

    override suspend fun compressAndSaveImageFromInternet(bitmap: Bitmap): Uri {
        val compressedImageUri = saveCompressedImage(bitmap)
        Log.i(TAG, "Image from internet successfully saved path: $compressedImageUri")
        return compressedImageUri
    }

    override fun readImageBytes(uri: Uri): ByteArray {
        val bitmap = readImage(uri)
        val stream = ByteArrayOutputStream()
        bitmap.compress(CompressFormat.PNG, 0, stream)
        val bytes = stream.toByteArray()
        stream.close()
        return bytes
    }

    override fun importImage(bytes: ByteArray): Uri {
        val fileName = getRandomName()
        checkAndCreateOutputDir()

        val imageFile = File(outputDirectory, fileName)
        try {
            FileOutputStream(imageFile).use { stream ->
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                bitmap.compress(CompressFormat.PNG, 0, stream)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
        return imageFile.toUri()
    }

    private fun readImage(uri: Uri): Bitmap {
        try {
            appContext.contentResolver.openInputStream(uri).use { inputStream ->
                return BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: IOException) {
            Log.e(TAG, e.toString())
            throw IOException(e.message)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.toString())
            throw e
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
                true
            )
            return scaledBitmap
        }
    }

    private fun saveCompressedImage(bitmap: Bitmap): Uri {
        val fileName = getRandomName()
        checkAndCreateOutputDir()

        val imageFile = File(outputDirectory, fileName)
        try {
            FileOutputStream(imageFile).use { outputStream ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    bitmap.compress(CompressFormat.WEBP_LOSSY, 30, outputStream)
                } else {
                    bitmap.compress(CompressFormat.JPEG, 50, outputStream)
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, e.message.toString())
            throw IOException(e.message)
        }
        Log.v(TAG, "Image saved to file: ${imageFile.absolutePath}")
        return imageFile.toUri()
    }

    private fun getRandomName() = "image_${UUID.randomUUID()}"

    private fun checkAndCreateOutputDir() {
        if (!outputDirectory.exists()) outputDirectory.mkdirs()
    }
}