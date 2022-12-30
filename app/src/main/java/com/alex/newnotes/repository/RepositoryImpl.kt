package com.alex.newnotes.repository

import com.alex.newnotes.data.database.Dao
import com.alex.newnotes.data.database.Note
import com.alex.newnotes.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val dao: Dao
) : Repository {

    override suspend fun getNotes(): Flow<List<Note>> {
        return dao.getNotes()
    }

    override suspend fun getNoteDetails(noteId: Int): Note {
        return withContext(ioDispatcher) {
            dao.getNoteDetails(noteId)
        }
    }

    override suspend fun insert(note: Note) {
        withContext(ioDispatcher) {
            dao.insert(note)
        }
    }

    override suspend fun delete(noteId: Int) {
        withContext(ioDispatcher) {
            dao.delete(noteId)
        }
    }

    override suspend fun update(note: Note) {
        withContext(ioDispatcher) {
            dao.update(note)
        }
    }
}