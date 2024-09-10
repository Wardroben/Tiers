package ru.smalljinn.tiers.features.tier_lists

import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.smalljinn.tiers.data.share.repository.ShareRepository
import ru.smalljinn.tiers.domain.share.createExportIntent
import ru.smalljinn.tiers.util.Result

class ExportShareListUseCase(
    private val createShareListUseCase: CreateShareListUseCase,
    private val shareRepository: ShareRepository,
) {
    suspend operator fun invoke(listId: Long): Intent {
        return withContext(Dispatchers.IO) {
            val shareList = createShareListUseCase.invoke(listId)
            val result = shareRepository.createShareFile(shareList)
            val uri = when (result) {
                is Result.Success -> result.data!!
                else -> throw Exception("Error")
            }
            createExportIntent(listName = shareList.name, fileUri = uri)
        }
    }
}