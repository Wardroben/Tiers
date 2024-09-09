package ru.smalljinn.tiers.domain.usecase

import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import ru.smalljinn.tiers.data.database.model.TierElement
import ru.smalljinn.tiers.data.database.repository.TierElementRepository
import ru.smalljinn.tiers.data.database.repository.TierListRepository
import ru.smalljinn.tiers.data.images.photo_processor.PhotoProcessor
import ru.smalljinn.tiers.data.share.models.ShareCategory
import ru.smalljinn.tiers.data.share.models.ShareElement
import ru.smalljinn.tiers.data.share.models.ShareList

fun TierElement.toShare(image: ByteArray) = ShareElement(position, image)

class CreateShareListUseCase(
    private val elementRepository: TierElementRepository,
    private val listRepository: TierListRepository,
    private val photoProcessor: PhotoProcessor
) {
    suspend operator fun invoke(listId: Long): ShareList {
        return withContext(Dispatchers.IO) {
            //TODO make not flows
            val listWithElements =
                listRepository.getTierListWithCategoriesAndElementsStream(listId).first()
            val unattachedElements =
                elementRepository.getNotAttachedElementsOfListStream(listId).first()

            val notAttachedShareElements = unattachedElements.map { element ->
                element.toShare(photoProcessor.readImageBytes(element.imageUrl.toUri()))
            }

            val shareCategories = listWithElements.categories.map { categoryWithElements ->
                with(categoryWithElements) {
                    ShareCategory(
                        name = category.name,
                        colorArgb = category.colorArgb,
                        position = category.position,
                        elements = elements.map { element ->
                            element.toShare(photoProcessor.readImageBytes(element.imageUrl.toUri()))
                        }
                    )
                }
            }

            val shareList = ShareList(
                name = listWithElements.tierList.name,
                categories = shareCategories,
                unattachedElements = notAttachedShareElements
            )

            shareList
        }
    }
}