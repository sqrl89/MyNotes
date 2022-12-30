package com.alex.newnotes.ui.edit.core

import com.alex.newnotes.data.database.Note

interface EditInteractor {

    suspend fun getNoteDetails(noteId: Int): Note

    suspend fun insertNote(note: Note)

    suspend fun updateNote(note: Note)

}