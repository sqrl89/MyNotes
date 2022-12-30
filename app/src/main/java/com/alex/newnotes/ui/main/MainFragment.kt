package com.alex.newnotes.ui.main

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.SearchView.OnQueryTextListener
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import by.kirich1409.viewbindingdelegate.viewBinding
import com.alex.newnotes.Const.FIRST_START_KEY
import com.alex.newnotes.Const.PREFS_NAME
import com.alex.newnotes.R
import com.alex.newnotes.changeStatusBarColor
import com.alex.newnotes.checkFirstRun
import com.alex.newnotes.data.database.Note
import com.alex.newnotes.databinding.FragmentMainBinding
import com.alex.newnotes.showDialog
import com.alex.newnotes.ui.main.NoteAdapter.ItemClickListener
import com.alex.newnotes.ui.main.dialogs.DeleteNoteDialogFragment
import com.alex.newnotes.ui.main.dialogs.MarkCompletedDialogFragment
import com.alex.newnotes.ui.main.dialogs.MarkUncompletedDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainFragment : Fragment(R.layout.fragment_main), ItemClickListener {
    private val viewModel: MainViewModel by viewModels()
    private val viewBinding: FragmentMainBinding by viewBinding()
    private val adapter: NoteAdapter = NoteAdapter(this)
    private lateinit var sharedPref: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPref = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val firstStart = sharedPref.getBoolean(FIRST_START_KEY, true)
        if (firstStart) checkFirstRun()
        setUi()
        collectNotes()
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
            swipeItem().attachToRecyclerView(rcView)
        }
    }

    private fun collectNotes() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getNotes().collectLatest { list ->
                    adapter.updateList(list)
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

    private fun swipeItem(): ItemTouchHelper {
        return ItemTouchHelper(object :
            SimpleCallback( 0,
//                UP or DOWN,
                RIGHT or LEFT) {

            override fun onMove(
                recyclerView: RecyclerView, viewHolder: ViewHolder, target: ViewHolder
            ): Boolean {
//                adapter.onItemMove(
//                    viewHolder.absoluteAdapterPosition,
//                    target.absoluteAdapterPosition
//                )
//                return true
                return false
            }

            override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                val pos = viewHolder.absoluteAdapterPosition
                val note = adapter.differ.currentList[pos]
                when (direction) {
                    LEFT -> showDialog(DeleteNoteDialogFragment(), TAG_DELETE_NOTE)
                    RIGHT -> {
                        if (!note.completed) showDialog(
                            MarkCompletedDialogFragment(),
                            TAG_MARK_NOTE
                        )
                        else showDialog(MarkUncompletedDialogFragment(), TAG_UNMARK_NOTE)
                    }
                }
                setFragmentListeners(note, viewHolder)
            }
        })
    }

    private fun setFragmentListeners(note: Note, viewHolder: ViewHolder) {
        activity?.supportFragmentManager?.setFragmentResultListener(
            REQUEST_CODE, viewLifecycleOwner
        ) { _, bundle ->
            when (bundle.getString(MAIN_KEY)) {
                DELETE -> {
                    viewModel.onDeleteItem(note.id)
                    view?.let {
                        Snackbar.make(
                            it,
                            getString(R.string.note_deleted),
                            Snackbar.LENGTH_LONG
                        ).apply {
                            setAction(getString(R.string.cancel)) {
                                viewModel.insertNote(note)
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

    companion object {
        const val REQUEST_CODE = "key_delete"
        const val MAIN_KEY = "key"
        const val CANCEL = "0"
        const val DELETE = "1"
        const val MARK_COMPLETED = "2"
        const val MARK_UNCOMPLETED = "3"
        const val TAG_DELETE_NOTE = "delete_note"
        const val TAG_MARK_NOTE = "mark_note_compteted"
        const val TAG_UNMARK_NOTE = "mark_note_incompleted"
    }
}