package ru.smalljinn.tiers.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import ru.smalljinn.tiers.R
import java.io.File

const val authority = "com.mydomain.fileprovider"

class TierFileProvider : FileProvider(R.xml.file_paths) {
    companion object {
        fun getUriForFile(file: File, context: Context): Uri =
            getUriForFile(context, authority, file)
    }
}