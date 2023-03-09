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
import com.alex.newnotes.R.color.blue
import com.alex.newnotes.R.color.cyan
import com.alex.newnotes.R.color.dark
import com.alex.newnotes.R.color.green
import com.alex.newnotes.R.color.indigo
import com.alex.newnotes.R.color.orange
import com.alex.newnotes.R.color.purple
import com.alex.newnotes.R.color.red
import com.alex.newnotes.R.color.yellow
import com.alex.newnotes.databinding.FragmentEditColorsBinding
import com.alex.newnotes.utils.Const.ACTION
import com.alex.newnotes.utils.Const.BOTTOM_SHEET_ACTION
import com.alex.newnotes.utils.Const.DEFAULT_COLOR
import com.alex.newnotes.utils.Const.SELECTED_COLOR
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_DRAGGING
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_SETTLING
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class NoteBottomSheetFragment : BottomSheetDialogFragment() {

    private var selectedColor = DEFAULT_COLOR
    private val viewBinding: FragmentEditColorsBinding by viewBinding()

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
                        STATE_DRAGGING.apply { state = DRAGGING }
                        STATE_SETTLING.apply { state = SETTLING }
                        STATE_EXPANDED.apply { state = EXPANDED }
                        STATE_COLLAPSED.apply { state = COLLAPSED }
                        STATE_HIDDEN.apply {
                            state = HIDDEN
                            dismiss()
                            behavior.state = STATE_COLLAPSED
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
                    R.id.rbYellow -> setImageResources(yellow, resources.getString(R.string.yellow))
                    R.id.rbIndigo -> setImageResources(indigo, resources.getString(R.string.indigo))
                    R.id.rbOrange -> setImageResources(orange, resources.getString(R.string.orange))
                    R.id.rbPurple -> setImageResources(purple, resources.getString(R.string.purple))
                    R.id.rbGreen -> setImageResources(green, resources.getString(R.string.green))
                    R.id.rbDark -> setImageResources(dark, resources.getString(R.string.dark))
                    R.id.rbBlue -> setImageResources(blue, resources.getString(R.string.blue))
                    R.id.rbCyan -> setImageResources(cyan, resources.getString(R.string.cyan))
                    R.id.rbRed -> setImageResources(red, resources.getString(R.string.red))
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
        val intent = Intent(BOTTOM_SHEET_ACTION)
        intent.putExtra(ACTION, colorString)
        intent.putExtra(SELECTED_COLOR, selectedColor)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }

    companion object {
        var noteId: Int? = null
        const val DRAGGING = "DRAGGING"
        const val SETTLING = "SETTLING"
        const val EXPANDED = "EXPANDED"
        const val COLLAPSED = "COLLAPSED"
        const val HIDDEN = "HIDDEN"

        fun newInstance(id: Int?) = NoteBottomSheetFragment().apply {
            arguments = Bundle().apply {
                noteId = id
            }
        }
    }
}