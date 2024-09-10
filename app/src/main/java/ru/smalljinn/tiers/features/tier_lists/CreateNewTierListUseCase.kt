package ru.smalljinn.tiers.features.tier_lists

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.smalljinn.tiers.data.database.model.TierCategory
import ru.smalljinn.tiers.data.database.model.TierList
import ru.smalljinn.tiers.data.database.repository.TierCategoryRepository
import ru.smalljinn.tiers.data.database.repository.TierListRepository
import javax.inject.Inject

class CreateNewTierListUseCase @Inject constructor(
    private val tierListRepository: TierListRepository,
    private val tierCategoryRepository: TierCategoryRepository,
) {
    suspend operator fun invoke(name: String): Long {
        return withContext(Dispatchers.IO) {
            val tierListId = tierListRepository.insertTierList(TierList(name = name))
            val categoriesToAdd = mutableListOf<TierCategory>()
            categoriesToAdd.add(
                TierCategory(
                    tierListId = tierListId,
                    name = "S",
                    position = 0,
                    colorArgb = getColorForTier(name = 'S')
                )
            )
            ('A'..'D').forEachIndexed { index, name ->
                val category =
                    TierCategory(
                        tierListId = tierListId,
                        name = name.toString(),
                        position = index + 1, //because S category
                        colorArgb = getColorForTier(name)
                    )
                categoriesToAdd.add(category)
            }
            tierCategoryRepository.insertCategories(categoriesToAdd.toList())
            tierListId
        }
    }
}

fun getColorForTier(name: Char) = when (name) {
    'S' -> Color(0xFFFF8080).toArgb()
    'A' -> Color(0xFFFFAA80).toArgb()
    'B' -> Color(0xFFFFEA80).toArgb()
    'C' -> Color(0xFFBFFF80).toArgb()
    'D' -> Color(0xFF99FFEE).toArgb()
    'E' -> Color(0xFF9999FF).toArgb()
    else -> Color.Black.toArgb()
}

