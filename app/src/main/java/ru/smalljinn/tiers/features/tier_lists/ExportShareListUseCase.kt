package ru.smalljinn.tiers.features.tier_lists

import android.content.Intent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.smalljinn.tiers.data.share.createExportIntent
import ru.smalljinn.tiers.data.share.repository.ShareRepository
import ru.smalljinn.tiers.di.IoDispatcher
import ru.smalljinn.tiers.util.Result
import javax.inject.Inject

class ExportShareListUseCase @Inject constructor(
    private val createShareListUseCase: CreateShareListUseCase,
    private val shareRepository: ShareRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(listId: Long): Intent {
        return withContext(dispatcher) {
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