package ru.smalljinn.tiers.data.database.model

import androidx.compose.ui.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(
    "tier_categories", foreignKeys = [
        ForeignKey(
            TierList::class,
            childColumns = ["tierListId"],
            parentColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        ),
    ]
)
data class TierCategory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(index = true) val tierListId: Long,
    val name: String,
    val colorArgb: Int,
    val position: Int
    //val elements: List<TierElement>
) {
    @Ignore
    val color: Color = Color(colorArgb)
}
