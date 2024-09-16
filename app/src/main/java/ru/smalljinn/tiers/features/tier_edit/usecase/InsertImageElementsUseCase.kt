package ru.smalljinn.tiers.features.tier_edit.usecase

import android.net.Uri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.smalljinn.tiers.data.database.model.TierElement
import ru.smalljinn.tiers.data.database.repository.TierElementRepository
import ru.smalljinn.tiers.di.IoDispatcher
import javax.inject.Inject

class InsertImageElementsUseCase @Inject constructor(
    private val elementRepository: TierElementRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(listId: Long, imageUri: Uri): Long {
        return withContext(dispatcher) {
            val tierElement = TierElement(tierListId = listId, imageUrl = imageUri.toString())
            elementRepository.insertTierElement(tierElement)
        }
    }

    suspend operator fun invoke(listId: Long, imageUris: List<Uri>): List<Long> {
        return withContext(dispatcher) {
            val tierElements = imageUris.map { uri ->
                TierElement(
                    tierListId = listId,
                    imageUrl = uri.toString()
                )
            }
            elementRepository.insertTierElements(tierElements)
        }
    }
}