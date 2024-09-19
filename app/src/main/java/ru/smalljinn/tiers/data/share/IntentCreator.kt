package ru.smalljinn.tiers.data.share

import android.content.Intent
import android.net.Uri

private const val INTENT_TYPE = "file/*"
fun createExportIntent(listName: String, fileUri: Uri): Intent {
    return Intent.createChooser(Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, fileUri)
        //TODO make localized strings
        putExtra(Intent.EXTRA_TEXT, "Sharing list $listName")
        putExtra(Intent.EXTRA_TITLE, "Sharing tier list")
        setDataAndType(fileUri, INTENT_TYPE)
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }, null)
}