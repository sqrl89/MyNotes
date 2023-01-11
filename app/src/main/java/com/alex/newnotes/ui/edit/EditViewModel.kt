package com.alex.newnotes.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alex.newnotes.data.database.Note
import com.alex.newnotes.ui.edit.core.EditInteractor
import com.github.terrakok.cicerone.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    private val router: Router,
    private val interactor: EditInteractor
) : ViewModel() {

    val noteId = MutableStateFlow<Int?>(null)
    val note = noteId.flatMapLatest {
        flowOf(it?.let { it1 -> interactor.getNoteDetails(it1) })
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setNoteId(id: Int) {
        viewModelScope.launch {
            noteId.emit(id)
        }
    }

    fun onSaveClick(note: Note) {
        viewModelScope.launch {
            interactor.insertNote(note)
        }
        router.exit()
    }

    fun updateNoteAndClose(note: Note) {
        viewModelScope.launch {
            interactor.updateNote(note)
        }
        router.exit()
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            interactor.updateNote(note)
        }
    }

    fun onBackPressed() {
        router.exit()
    }

}