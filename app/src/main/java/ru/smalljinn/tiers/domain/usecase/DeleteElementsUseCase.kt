package ru.smalljinn.tiers.domain.usecase

import androidx.core.net.toUri
import kotlinx.coroutines.coroutineScope
import ru.smalljinn.tiers.data.database.model.TierElement
import ru.smalljinn.tiers.data.database.repository.TierElementRepository
import ru.smalljinn.tiers.data.images.repository.device.DeviceImageRepository

class DeleteElementsUseCase(
    private val elementRepository: TierElementRepository,
    private val photoRepository: DeviceImageRepository
) {
    suspend operator fun invoke(element: TierElement): Boolean {
        return coroutineScope {
            elementRepository.deleteTierElement(element)
            photoRepository.deletePhotos(listOf(element.imageUrl.toUri()))
        }
    }

    suspend operator fun invoke(elements: List<TierElement>): Boolean {
        return coroutineScope {
            elementRepository.deleteTierElements(elements)
            photoRepository.deletePhotos(elements.map { element -> element.imageUrl.toUri() })
        }
    }
}