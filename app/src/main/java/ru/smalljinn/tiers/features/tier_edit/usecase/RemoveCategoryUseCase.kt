package ru.smalljinn.tiers.features.tier_edit.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.smalljinn.tiers.data.database.model.TierCategory
import ru.smalljinn.tiers.data.database.repository.TierCategoryRepository
import ru.smalljinn.tiers.di.IoDispatcher
import javax.inject.Inject

class RemoveCategoryUseCase @Inject constructor(
    private val categoryRepository: TierCategoryRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(category: TierCategory) {
        withContext(dispatcher) {
            categoryRepository.unpinElementsFromCategory(categoryId = category.id)
            categoryRepository.deleteCategory(category)
        }
    }
}