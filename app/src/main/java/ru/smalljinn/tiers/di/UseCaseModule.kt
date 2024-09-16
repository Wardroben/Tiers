package ru.smalljinn.tiers.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import ru.smalljinn.tiers.features.tier_edit.usecase.GetInternetImagesUseCase

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {
    @Provides
    @ViewModelScoped
    fun provideGetInternetImagesUseCase(): GetInternetImagesUseCase = GetInternetImagesUseCase()
}