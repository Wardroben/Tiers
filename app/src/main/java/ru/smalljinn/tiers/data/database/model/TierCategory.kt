package ru.smalljinn.tiers.data.database.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey

private const val STANDARD_CATEGORY_NAME = "X"

@Entity(
    "tier_category", foreignKeys = [
        ForeignKey(
            TierList::class,
            childColumns = ["tier_list_id"],
            parentColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        ),
    ]
)
data class TierCategory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(index = true, name = "tier_list_id") val tierListId: Long,
    val name: String = STANDARD_CATEGORY_NAME,
    @ColumnInfo(name = "color_argb") val colorArgb: Int = Color.LightGray.toArgb(),
    val position: Int
    //val elements: List<TierElement>
) {
    @Ignore
    val color: Color = Color(colorArgb)

    companion object {
        fun getCategoryToCreate(tierId: Long, position: Int) =
            TierCategory(tierListId = tierId, position = position)
    }
}

