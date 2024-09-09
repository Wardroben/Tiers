package ru.smalljinn.tiers.data.share.repository

import android.net.Uri
import ru.smalljinn.tiers.data.share.models.ShareList
import ru.smalljinn.tiers.util.Result

interface ShareRepository {
    fun createShareFile(shareList: ShareList): Result<Uri>
}