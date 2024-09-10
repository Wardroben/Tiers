package ru.smalljinn.tiers.features.tier_edit

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.smalljinn.tiers.data.database.model.unpin
import ru.smalljinn.tiers.data.database.repository.TierElementRepository
import ru.smalljinn.tiers.di.IoDispatcher
import javax.inject.Inject

class UnpinElementsUseCase @Inject constructor(
    private val elementRepository: TierElementRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(elementId: Long) {
        withContext(dispatcher) {
            val tierElementToUnpin = elementRepository.getElementById(elementId)
            elementRepository.insertTierElement(tierElementToUnpin.unpin())
        }
    }
}