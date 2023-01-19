package com.alex.newnotes.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class Database : RoomDatabase() {
    abstract fun getDao(): Dao
}