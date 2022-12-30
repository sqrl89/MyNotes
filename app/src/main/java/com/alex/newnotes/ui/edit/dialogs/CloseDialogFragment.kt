package com.alex.newnotes.ui.edit.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.alex.newnotes.R
import com.alex.newnotes.ui.edit.EditFragment.Companion.CLOSE_KEY
import com.alex.newnotes.ui.edit.EditFragment.Companion.KEY_FOR_SOURCE
import com.alex.newnotes.ui.edit.EditFragment.Companion.REQUEST_KEY
import com.alex.newnotes.ui.edit.EditFragment.Companion.SAVE_KEY

class CloseDialogFragment: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        return builder.setTitle(resources.getString(R.string.save_changes))
            .setCancelable(true)
            .setPositiveButton(resources.getString(R.string.yes)) { dialog, _ ->
                parentFragmentManager.setFragmentResult(
                    REQUEST_KEY,
                    bundleOf(KEY_FOR_SOURCE to SAVE_KEY)
                )
                dialog.cancel()
            }
            .setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
                parentFragmentManager.setFragmentResult(
                    REQUEST_KEY,
                    bundleOf(KEY_FOR_SOURCE to CLOSE_KEY)
                )
                dialog.cancel()
            }
            .create()
    }
}