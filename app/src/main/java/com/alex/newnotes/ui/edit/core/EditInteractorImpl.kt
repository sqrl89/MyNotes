package com.alex.newnotes.ui.edit.core

import com.alex.newnotes.data.database.Note
import com.alex.newnotes.repository.Repository
import javax.inject.Inject

class EditInteractorImpl @Inject constructor(
    private val repository: Repository
) : EditInteractor {

    override suspend fun getNoteDetails(noteId: Int) = repository.getNoteDetails(noteId)

    override suspend fun insertNote(note: Note) = repository.insert(note)

    override suspend fun updateNote(note: Note) = repository.update(note)

}