package ru.smalljinn.tiers.viewmodel

import android.net.Uri
import ru.smalljinn.tiers.data.share.models.ShareList
import ru.smalljinn.tiers.data.share.repository.ShareRepository
import ru.smalljinn.tiers.util.Result

class MockShareRepository : ShareRepository {
    override fun createShareFile(shareList: ShareList): Result<Uri> {
        TODO("Not yet implemented")
    }
}