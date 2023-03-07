package com.alex.newnotes.ui.main

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.SearchView.OnQueryTextListener
import androidx.appcompat.widget.SearchView
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import by.kirich1409.viewbindingdelegate.viewBinding
import com.alex.newnotes.R
import com.alex.newnotes.data.database.Note
import com.alex.newnotes.databinding.FragmentMainBinding
import com.alex.newnotes.ui.main.NoteAdapter.ItemClickListener
import com.alex.newnotes.utils.Const.DATE_TIME_PATTERN
import com.alex.newnotes.utils.Const.PREFS_FIRST_START_KEY
import com.alex.newnotes.utils.Const.PREFS_FIRST_START_NAME
import com.alex.newnotes.utils.SwipeCallbacks
import com.alex.newnotes.utils.changeStatusBarColor
import com.alex.newnotes.utils.checkFirstRun
import com.alex.newnotes.utils.showDialog
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class MainFragment : Fragment(R.layout.fragment_main), ItemClickListener {
    private val viewModel: MainViewModel by viewModels()
    private val viewBinding: FragmentMainBinding by viewBinding()
    private val adapter: NoteAdapter = NoteAdapter(this)
    private lateinit var sharedPref: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPref = requireActivity().getSharedPreferences(PREFS_FIRST_START_NAME, MODE_PRIVATE)
        if (sharedPref.getBoolean(PREFS_FIRST_START_KEY, true)) checkFirstRun()
        setUi()
        collectNotes()
        NotificationManagerCompat.from(requireContext()).cancelAll()
        val swipeHandler = object : SwipeCallbacks(requireContext()) {
            override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                val note = adapter.differ.currentList[viewHolder.absoluteAdapterPosition]
                when (direction) {
                    LEFT -> showDialog(TAG_DELETE_NOTE, TAG_DELETE_NOTE)
                    RIGHT -> {
                        if (!note.completed) showDialog(TAG_MARK_NOTE, TAG_MARK_NOTE)
                        else showDialog(TAG_UNMARK_NOTE, TAG_UNMARK_NOTE)
                    }
                }
                setFragmentListeners(note, viewHolder)
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(viewBinding.rcView)
    }

    private fun setUi() {
        activity?.changeStatusBarColor(
            ContextCompat.getColor(requireContext(), R.color.main_theme), false
        )
        viewBinding.apply {
            rcView.adapter = adapter
            fbNew.setOnClickListener {
                viewModel.onNewClick()
            }
            rcView.setOnScrollChangeListener { _, _, _, _, oldScrollY ->
                if (oldScrollY < 0) {
                    searchView.clearFocus()
                    fbNew.hide()
                } else fbNew.show()
            }
            onQueryChange()
        }
    }

    private fun collectNotes() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getNotes().collect { list ->
                    adapter.updateList(list)
                    viewModel.emitListForCheck(list)
                    if (list.isEmpty()) viewBinding.tvNoElements.visibility = View.VISIBLE
                    else viewBinding.tvNoElements.visibility = View.GONE
                }
            }
        }
    }

    private fun onQueryChange() {
        viewBinding.searchView.setOnQueryTextListener(object :
            OnQueryTextListener,
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                viewModel.onSearchQueryChanged(p0.toString())
                return true
            }
        })
    }

    override fun onItemClick(note: Note) {
        viewModel.onNoteItemClick(note)
    }

    private fun setFragmentListeners(note: Note, viewHolder: ViewHolder) {
        activity?.supportFragmentManager?.setFragmentResultListener(
            REQUEST_CODE, viewLifecycleOwner
        ) { _, bundle ->
            when (bundle.getString(MAIN_KEY)) {
                DELETE -> {
                    viewModel.onDeleteNote(note.id)
                    view?.let {
                        Snackbar.make(
                            it,
                            resources.getString(R.string.note_deleted),
                            Snackbar.LENGTH_LONG
                        )
                            .apply {
                                setAction(resources.getString(R.string.cancel)) {
                                    viewModel.insertNote(
                                        note
                                    )
                                }
                                show()
                            }
                    }
                }

                CANCEL -> adapter.notifyItemChanged(viewHolder.absoluteAdapterPosition)
                MARK_COMPLETED -> viewModel.onMarkCompleted(note)
                MARK_UNCOMPLETED -> viewModel.onMarkUncompleted(note)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkExpiration()
    }

    private fun checkExpiration() {
        viewLifecycleOwner.lifecycleScope.launch {
            delay(1000)
            viewModel.list.value.forEach { note ->
                if (note?.completeBy?.isNotEmpty() == true) {
                    val completeBy =
                        LocalDateTime.parse(
                            note.completeBy,
                            DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)
                        )
                    if (LocalDateTime.now().isAfter(completeBy)) {
                        note.warning = true
                        viewModel.updateNote(note)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding.rcView.adapter = null
    }

    companion object {
        const val REQUEST_CODE = "request_code"
        const val MAIN_KEY = "main_key"
        const val CANCEL = "cancel"
        const val DELETE = "delete"
        const val MARK_COMPLETED = "completed"
        const val MARK_UNCOMPLETED = "uncompleted"
        const val TAG_DELETE_NOTE = "delete_note"
        const val TAG_MARK_NOTE = "mark_note_compteted"
        const val TAG_UNMARK_NOTE = "mark_note_incompleted"
        const val TAG_GREETINGS = "greetings"
    }
}