package com.alex.newnotes.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alex.newnotes.data.database.Note
import com.alex.newnotes.ui.Screens
import com.alex.newnotes.ui.main.core.MainInteractor
import com.alex.newnotes.utils.notifications.NotificationUtils
import com.github.terrakok.cicerone.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val router: Router,
    private val interactor: MainInteractor,
    private val notifyUtil: NotificationUtils
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    private val _list = MutableStateFlow<List<Note?>>(emptyList())
    val list = _list.asStateFlow()

    fun getNotes() =
        searchQuery.flatMapLatest { query ->
            interactor.getNotes(query)
        }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000))

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

    fun updateNote(note: Note) {
        viewModelScope.launch {
            interactor.updateNote(note)
        }
    }

    fun onDeleteNote(noteId: Int) {
        viewModelScope.launch {
            interactor.deleteNote(noteId)
        }
    }

    fun onSearchQueryChanged(query: String) {
        viewModelScope.launch {
            searchQuery.emit(query)
        }
    }

    fun emitListForCheck(checkList: List<Note>){
        viewModelScope.launch {
            _list.emit(checkList)
        }
    }

    fun onMarkCompleted(note: Note) {
        viewModelScope.launch {
            interactor.markCompleted(note)
            notifyUtil.cancelReminder(note.id)
        }
    }

    fun onMarkUncompleted(note: Note) {
        viewModelScope.launch {
            interactor.markUncompleted(note)
        }
    }
}