package com.alex.newnotes.ui.main.core

import com.alex.newnotes.data.database.Note
import kotlinx.coroutines.flow.Flow

interface MainInteractor {

    suspend fun getNotes(query: String): Flow<List<Note>>

    suspend fun deleteNote(noteId: Int)

    suspend fun insertNote(note: Note)

    suspend fun markCompleted(note: Note)

    suspend fun markUncompleted(note: Note)
}