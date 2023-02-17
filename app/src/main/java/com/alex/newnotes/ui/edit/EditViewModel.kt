package com.alex.newnotes.ui.edit

import android.content.ClipDescription
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alex.newnotes.data.database.Note
import com.alex.newnotes.ui.edit.core.EditInteractor
import com.alex.newnotes.utils.Const.DATE_TIME_PATTERN
import com.alex.newnotes.utils.notifications.NotificationUtils
import com.github.terrakok.cicerone.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
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

    private val noteId = MutableStateFlow<Int?>(null)
    val note = noteId.flatMapLatest {
        flowOf(it?.let { it1 -> interactor.getNoteDetails(it1) })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _tmpUri = MutableStateFlow<String?>(null)
    val tmpUri = _tmpUri.asStateFlow()

    private val _selectedColor = MutableStateFlow<String?>(null)
    val selectedColor = _selectedColor.asStateFlow()

    private val _byDateAndTime = MutableStateFlow<String?>(null)
    val byDateAndTime = _byDateAndTime.asStateFlow()

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

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
                    DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)
                )
            if (LocalDateTime.now().isAfter(completeBy)) {
                note.warning = true
                interactor.updateNote(note)
            }
        }
    }

    fun reminderNotification(title: String, color: String, byDateAndTime: String) {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat(DATE_TIME_PATTERN, Locale.ROOT)
        calendar.time = sdf.parse(byDateAndTime)!!
        notifyUtil.setReminder(calendar.timeInMillis, title, color)
    }

    fun setTmpUri(uri: String){
            _tmpUri.value = uri
    }

    fun setSelectedColor(selectedColor: String){
        _selectedColor.value = selectedColor
    }

    fun setByDateAndTime(byDateAndTime: String){
            _byDateAndTime.value = byDateAndTime
    }

    fun setTitle(title: String) {
        _title.value = title
    }

    fun setDesc(description: String) {
        _description.value = description
    }

}