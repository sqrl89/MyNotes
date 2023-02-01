package com.alex.newnotes.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alex.newnotes.data.database.Note
import com.alex.newnotes.ui.edit.core.EditInteractor
import com.alex.newnotes.utils.notifications.NotificationUtils
import com.github.terrakok.cicerone.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    private val router: Router,
    private val interactor: EditInteractor,
    private val notifyUtil: NotificationUtils
) : ViewModel() {

    val noteId = MutableStateFlow<Int?>(null)
    val note = noteId.flatMapLatest {
        flowOf(it?.let { it1 -> interactor.getNoteDetails(it1) })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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

    fun onBackPressed() {
        router.exit()
    }

    fun checkExpiration(note: Note) {
        viewModelScope.launch {
            val completeBy =
                LocalDateTime.parse(
                    note.completeBy,
                    DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
                )
            if (LocalDateTime.now().isAfter(completeBy)) {
                note.warning = true
                interactor.updateNote(note)
            }
        }
    }

    fun reminderNotification(title: String, color: String, byDateAndTime: String){
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ROOT)
        calendar.time = sdf.parse(byDateAndTime)!!
        notifyUtil.setReminder(calendar.timeInMillis, title, color)
    }
}