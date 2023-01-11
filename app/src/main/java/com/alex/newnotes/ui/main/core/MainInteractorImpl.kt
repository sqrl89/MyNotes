package com.alex.newnotes.ui.main.core

import com.alex.newnotes.data.database.Note
import com.alex.newnotes.repository.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class MainInteractorImpl @Inject constructor(
    private val repository: Repository
) : MainInteractor {

    override suspend fun getNotes(query: String): Flow<List<Note>> {
        return repository.getNotes().map { it ->
            it.filter {
                it.title?.contains(query, ignoreCase = true) == true ||
                        it.content?.contains(query, ignoreCase = true) == true
            }
        }
    }

    override suspend fun deleteNote(noteId: Int) {
        repository.delete(noteId)
    }

    override suspend fun insertNote(note: Note) {
        repository.insert(note)
    }

    override suspend fun markCompleted(note: Note) {
        note.completed = true
        note.completionDate = SimpleDateFormat("dd-MM-yyyy", Locale.ROOT).format(Date())
        note.warning = false
        repository.update(note)
    }

    override suspend fun markUncompleted(note:Note){
        note.completed = false
        note.completionDate = null
        repository.update(note)
    }

}