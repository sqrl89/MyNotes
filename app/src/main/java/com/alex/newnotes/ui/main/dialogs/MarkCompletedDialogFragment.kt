package com.alex.newnotes.ui.main.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.alex.newnotes.R
import com.alex.newnotes.ui.main.MainFragment.Companion.CANCEL
import com.alex.newnotes.ui.main.MainFragment.Companion.MAIN_KEY
import com.alex.newnotes.ui.main.MainFragment.Companion.MARK_COMPLETED
import com.alex.newnotes.ui.main.MainFragment.Companion.REQUEST_CODE

class MarkCompletedDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        return builder.setTitle(resources.getString(R.string.mark_completed))
            .setCancelable(true)
            .setPositiveButton(resources.getString(R.string.yes)) { dialog, _ ->
                parentFragmentManager.setFragmentResult(REQUEST_CODE, bundleOf(MAIN_KEY to MARK_COMPLETED))
                dialog.cancel()
            }
            .setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
                dialog.cancel()
            }
            .create()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        parentFragmentManager.setFragmentResult(REQUEST_CODE, bundleOf(MAIN_KEY to CANCEL))
    }
}