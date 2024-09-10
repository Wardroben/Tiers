package ru.smalljinn.tiers.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.smalljinn.tiers.data.database.TierDatabase
import ru.smalljinn.tiers.data.database.dao.CategoryDao
import ru.smalljinn.tiers.data.database.dao.ElementDao
import ru.smalljinn.tiers.data.database.dao.TierListDao

@Module
@InstallIn(SingletonComponent::class)
object DaosModule {
    @Provides
    fun provideTierListDao(db: TierDatabase): TierListDao = db.tierListDao()

    @Provides
    fun provideTierCategoryDao(db: TierDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideTierElementDao(db: TierDatabase): ElementDao = db.elementDao()
}