package ru.smalljinn.tiers.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.smalljinn.tiers.data.database.dao.CategoryDao
import ru.smalljinn.tiers.data.database.dao.ElementDao
import ru.smalljinn.tiers.data.database.dao.TierListDao
import ru.smalljinn.tiers.data.database.model.TierCategory
import ru.smalljinn.tiers.data.database.model.TierElement
import ru.smalljinn.tiers.data.database.model.TierList

const val DATABASE_NAME = "tier_db"

@Database(entities = [TierList::class, TierCategory::class, TierElement::class], version = 1)
//@TypeConverters(ColorConverter::class)
abstract class TierDatabase : RoomDatabase() {
    abstract fun tierListDao(): TierListDao
    abstract fun categoryDao(): CategoryDao
    abstract fun elementDao(): ElementDao

    companion object {
        @Volatile
        private var INSTANCE: TierDatabase? = null

        fun getInstance(context: Context): TierDatabase =
            INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    requireNotNull(context.applicationContext),
                    TierDatabase::class.java,
                    DATABASE_NAME
                )
                    .build()
                INSTANCE = instance
                instance
            }
    }
}