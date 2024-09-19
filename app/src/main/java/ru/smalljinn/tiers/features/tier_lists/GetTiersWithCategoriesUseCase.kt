package ru.smalljinn.tiers.features.tier_lists

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import ru.smalljinn.tiers.data.database.model.TierListWithCategories
import ru.smalljinn.tiers.data.database.repository.TierListRepository
import ru.smalljinn.tiers.di.IoDispatcher
import ru.smalljinn.tiers.util.Result
import javax.inject.Inject

class GetTiersWithCategoriesUseCase @Inject constructor(
    private val tierListRepository: TierListRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {
    operator fun invoke(): Flow<Result<List<TierListWithCategories>>> {
        return flow {
            emit(Result.Loading(true))
            tierListRepository.getAllListsWithCategoriesStream()
                .flowOn(dispatcher)
                .catch { emit(Result.Error(it.message)) }
                .onCompletion { emit(Result.Loading(false)) }
                .collect { lists -> emit(Result.Success(lists)) }
        }
    }
}