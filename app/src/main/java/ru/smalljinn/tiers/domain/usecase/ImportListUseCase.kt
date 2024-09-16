package ru.smalljinn.tiers.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.smalljinn.tiers.data.database.model.TierCategory
import ru.smalljinn.tiers.data.database.model.TierElement
import ru.smalljinn.tiers.data.database.model.TierList
import ru.smalljinn.tiers.data.database.repository.TierCategoryRepository
import ru.smalljinn.tiers.data.database.repository.TierElementRepository
import ru.smalljinn.tiers.data.database.repository.TierListRepository
import ru.smalljinn.tiers.data.images.photo_processor.PhotoProcessor
import ru.smalljinn.tiers.data.share.models.ShareList
import ru.smalljinn.tiers.di.IoDispatcher
import javax.inject.Inject

fun ShareList.toEntity() = TierList(name = name)

class ImportListUseCase @Inject constructor(
    private val photoProcessor: PhotoProcessor,
    private val listRepository: TierListRepository,
    private val categoryRepository: TierCategoryRepository,
    private val elementRepository: TierElementRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(shareList: ShareList): Boolean {
        return withContext(dispatcher) {
            val insertedListId: Long = listRepository.insertTierList(shareList.toEntity())
            //make unattached elements and insert it to list
            val unattachedElements = shareList.unattachedElements.map { share ->
                TierElement(
                    tierListId = insertedListId,
                    position = share.position,
                    imageUrl = photoProcessor.importImage(share.image).toString()
                )
            }
            elementRepository.insertTierElements(unattachedElements)
            //convert categories and add elements of it
            shareList.categories.forEach { shareCategory ->
                //converts category
                val tierCategory = TierCategory(
                    tierListId = insertedListId,
                    name = shareCategory.name,
                    colorArgb = shareCategory.colorArgb,
                    position = shareCategory.position
                )
                //insert category and get it id
                val categoryId = categoryRepository.insertCategory(tierCategory)
                //convert elements of category and add it to category
                val categoryElements = shareCategory.elements.map {
                    TierElement(
                        categoryId = categoryId,
                        tierListId = insertedListId,
                        imageUrl = photoProcessor.importImage(it.image).toString(),
                        position = it.position
                    )
                }
                elementRepository.insertTierElements(categoryElements)
            }
            //TODO return false if error -> make checks at actions before
            true
        }
    }
}