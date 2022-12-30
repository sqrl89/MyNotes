package com.alex.newnotes.ui.edit.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.alex.newnotes.R
import com.alex.newnotes.ui.edit.EditFragment.Companion.KEY_FOR_SOURCE
import com.alex.newnotes.ui.edit.EditFragment.Companion.REQUEST_KEY
import com.alex.newnotes.ui.edit.EditFragment.Companion.SOURCE_CAMERA
import com.alex.newnotes.ui.edit.EditFragment.Companion.SOURCE_GALLERY

class ChoosePicDialogFragment: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        return builder.setTitle(resources.getString(R.string.choose_image))
            .setCancelable(true)
            .setPositiveButton(resources.getString(R.string.make_photo)) { dialog, _ ->
                parentFragmentManager.setFragmentResult(
                    REQUEST_KEY,
                    bundleOf(KEY_FOR_SOURCE to SOURCE_CAMERA)
                )
                dialog.cancel()
            }
            .setNegativeButton(resources.getString(R.string.from_gallery)) { dialog, _ ->
                parentFragmentManager.setFragmentResult(
                    REQUEST_KEY,
                    bundleOf(KEY_FOR_SOURCE to SOURCE_GALLERY)
                )
                dialog.cancel()
            }
            .create()
    }
}