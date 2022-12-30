package com.alex.newnotes.ui.edit

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.alex.newnotes.R
import com.alex.newnotes.databinding.FragmentEditColorsBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class NoteBottomSheetFragment : BottomSheetDialogFragment() {

    private var selectedColor = "#373737"
    private val viewBinding: FragmentEditColorsBinding by viewBinding()

    companion object {
        var noteId: Int? = null
        const val DRAGGING = "DRAGGING"
        const val SETTLING = "SETTLING"
        const val EXPANDED = "EXPANDED"
        const val COLLAPSED = "COLLAPSED"
        const val HIDDEN = "HIDDEN"

        fun newInstance(id: Int?): NoteBottomSheetFragment {
            val args = Bundle()
            val fragment = NoteBottomSheetFragment()
            fragment.arguments = args
            noteId = id
            return fragment
        }
    }

    @SuppressLint("RestrictedApi", "InflateParams")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_edit_colors, null)
        dialog.setContentView(view)
        val param = (view.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        val behavior = param.behavior

        if (behavior is BottomSheetBehavior<*>) {
            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    var state = ""
                    while (newState == newState) {
                        BottomSheetBehavior.STATE_DRAGGING.apply {
                            state = DRAGGING
                        }
                        BottomSheetBehavior.STATE_SETTLING.apply {
                            state = SETTLING
                        }
                        BottomSheetBehavior.STATE_EXPANDED.apply {
                            state = EXPANDED
                        }
                        BottomSheetBehavior.STATE_COLLAPSED.apply {
                            state = COLLAPSED
                        }
                        BottomSheetBehavior.STATE_HIDDEN.apply {
                            state = HIDDEN
                            dismiss()
                            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_colors, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListener()
    }

    private fun setListener() {
        viewBinding.apply {
            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.rbDark -> {
                        setImageResources(R.color.dark, getString(R.string.dark))
                    }

                    R.id.rbBlue -> {
                        setImageResources(R.color.blue, getString(R.string.blue))
                    }

                    R.id.rbCyan -> {
                        setImageResources(R.color.cyan, getString(R.string.cyan))
                    }

                    R.id.rbYellow -> {
                        setImageResources(R.color.yellow, getString(R.string.yellow))
                    }

                    R.id.rbRed -> {
                        setImageResources(R.color.red, getString(R.string.red))
                    }

                    R.id.rbGreen -> {
                        setImageResources(R.color.green, getString(R.string.green))
                    }

                    R.id.rbOrange -> {
                        setImageResources(R.color.orange, getString(R.string.orange))
                    }

                    R.id.rbIndigo -> {
                        setImageResources(R.color.indigo, getString(R.string.indigo))
                    }

                    R.id.rbPurple -> {
                        setImageResources(R.color.purple, getString(R.string.purple))
                    }
                }
            }
        }
    }


    private fun setImageResources(color: Int, colorString: String) {
        selectedColor = String.format(
            "#%06x",
            ContextCompat.getColor(requireContext(), color) and 0xffffff
        )
        sendBroadcast(colorString)
    }

    private fun sendBroadcast(colorString: String) {
        val intent = Intent(getString(R.string.bottom_sheet_action))
        intent.putExtra(getString(R.string.action), colorString)
        intent.putExtra(getString(R.string.selected_color), selectedColor)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }
}
