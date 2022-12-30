package com.alex.newnotes.ui.edit.core

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class EditScreenModule {
    @Binds
    abstract fun bindInteractor(interactorImpl: EditInteractorImpl): EditInteractor
}