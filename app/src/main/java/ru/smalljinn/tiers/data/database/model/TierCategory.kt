package ru.smalljinn.tiers.data.database.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity("tier_categories")
data class TierCategory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val tierListId: Long,
    val name: String,
    val colorArgb: Int,
    //val elements: List<TierElement>
) {
    @Ignore
    val color: Color = Color(colorArgb)
}
