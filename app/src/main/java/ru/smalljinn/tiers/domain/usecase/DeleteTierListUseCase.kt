package ru.smalljinn.tiers.domain.usecase

import android.util.Log
import kotlinx.coroutines.coroutineScope
import ru.smalljinn.tiers.data.database.model.TierList
import ru.smalljinn.tiers.data.database.repository.TierElementRepository
import ru.smalljinn.tiers.data.database.repository.TierListRepository

class DeleteTierListUseCase(
    private val elementRepository: TierElementRepository,
    private val deleteElementsUseCase: DeleteElementsUseCase,
    private val listRepository: TierListRepository
) {
    suspend operator fun invoke(tierListId: Long) {
        return coroutineScope {
            val listElements = elementRepository.getListElements(tierListId)
            if (deleteElementsUseCase(listElements)) listRepository.deleteTierListById(tierListId)
            else Log.e(TAG, "Tierlist id:$tierListId not deleted because error deleting photos")
        }
    }

    suspend operator fun invoke(tierList: TierList) {
        return coroutineScope {
            val listElements = elementRepository.getListElements(tierList.id)
            if (deleteElementsUseCase(listElements)) {
                listRepository.deleteTierList(tierList)
                Log.i(TAG, "$tierList deleted successfully")
            } else Log.e(TAG, "$tierList not deleted because error deleting photos")
        }
    }
}

private const val TAG = "DeleteList"