package com.alex.newnotes.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alex.newnotes.data.database.Dao
import com.alex.newnotes.data.database.Note
import com.alex.newnotes.ui.Screens
import com.alex.newnotes.ui.main.core.MainInteractor
import com.github.terrakok.cicerone.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val router: Router,
    private val interactor: MainInteractor
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    fun getNotes() =
        searchQuery.flatMapLatest { query ->
            interactor.getNotes(query)
        }.shareIn(viewModelScope, SharingStarted.Eagerly)

    fun onNoteItemClick(note: Note) {
        router.navigateTo(Screens.Edit(note))
    }

    fun onNewClick() {
        router.navigateTo(Screens.Edit(Note()))
    }

    fun insertNote(note: Note) {
        viewModelScope.launch {
            interactor.insertNote(note)
        }
    }

    fun onDeleteItem(noteId: Int) {
        viewModelScope.launch {
            interactor.deleteNote(noteId)
        }
    }

    fun onSearchQueryChanged(query: String) {
        viewModelScope.launch {
            searchQuery.emit(query)
        }
    }

    fun onMarkCompleted(note: Note){
        viewModelScope.launch {
            interactor.markCompleted(note)
        }
    }

    fun onMarkUncompleted(note: Note){
        viewModelScope.launch {
            interactor.markUncompleted(note)
        }
    }
}