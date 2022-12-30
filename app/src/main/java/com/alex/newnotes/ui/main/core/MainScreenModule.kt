package com.alex.newnotes.ui.main.core

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MainScreenModule {
    @Binds
    abstract fun bindInteractor(interactorImpl: MainInteractorImpl): MainInteractor
}