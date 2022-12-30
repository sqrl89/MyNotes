package com.alex.newnotes.repository

import com.alex.newnotes.data.database.Note
import kotlinx.coroutines.flow.Flow

interface Repository {

    suspend fun getNotes(): Flow<List<Note>>

    suspend fun getNoteDetails(noteId: Int): Note

    suspend fun insert(note: Note)

    suspend fun delete(noteId: Int)

    suspend fun update(note: Note)

}