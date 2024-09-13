package ru.smalljinn.tiers.features.tier_lists

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.smalljinn.tiers.data.database.model.TierList
import ru.smalljinn.tiers.data.database.repository.TierElementRepository
import ru.smalljinn.tiers.data.database.repository.TierListRepository
import ru.smalljinn.tiers.di.IoDispatcher
import ru.smalljinn.tiers.domain.usecase.DeleteElementsUseCase
import javax.inject.Inject

private const val TAG = "DeleteList"

class DeleteTierListUseCase @Inject constructor(
    private val elementRepository: TierElementRepository,
    private val deleteElementsUseCase: DeleteElementsUseCase,
    private val listRepository: TierListRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(tierListId: Long) {
        return withContext(dispatcher) {
            val listElements = elementRepository.getListElements(tierListId)
            if (deleteElementsUseCase(listElements)) listRepository.deleteTierListById(tierListId)
            else Log.e(
                TAG,
                "Tierlist elementId:$tierListId not deleted because error deleting photos"
            )
        }
    }

    suspend operator fun invoke(tierList: TierList) {
        return withContext(dispatcher) {
            val listElements = elementRepository.getListElements(tierList.id)
            if (deleteElementsUseCase(listElements)) {
                listRepository.deleteTierList(tierList)
                Log.i(TAG, "$tierList deleted successfully")
            } else Log.e(TAG, "$tierList not deleted because error deleting photos")
        }
    }
}

