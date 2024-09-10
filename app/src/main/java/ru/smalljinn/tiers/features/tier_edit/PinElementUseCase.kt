package ru.smalljinn.tiers.features.tier_edit

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.smalljinn.tiers.data.database.repository.TierElementRepository

class PinElementUseCase(
    private val elementRepository: TierElementRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(categoryId: Long, elementId: Long) {
        withContext(dispatcher) {
            val lastPosition = elementRepository.getLastPositionInCategory(categoryId)
            val tierElement = elementRepository.getElementById(elementId)
            elementRepository.insertTierElement(
                tierElement.copy(
                    position = lastPosition + 1,
                    categoryId = categoryId
                )
            )
        }
    }
}