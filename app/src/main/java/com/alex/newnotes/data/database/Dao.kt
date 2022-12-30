package com.alex.newnotes.data.database

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList

@Dao
interface Dao {

    @Query("SELECT * FROM notes")
    fun getNotes(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(note: Note)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun delete(noteId: Int)

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteDetails(noteId: Int): Note

//    @Update(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun updateList(notes: List<Note>) {
//        Log.e("Error", "updateList: $notes")
//    }

}