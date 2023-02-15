package com.alex.newnotes.di

import android.content.Context
import androidx.room.Room
import com.alex.newnotes.data.database.Database
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataBaseModule {

    @Provides
    @Singleton
    fun providesDataBase(@ApplicationContext context: Context): Database =
        Room.databaseBuilder(context, Database::class.java, "notes.db").build()

    @Provides
    fun getDao(dataBase: Database) = dataBase.getDao()
}