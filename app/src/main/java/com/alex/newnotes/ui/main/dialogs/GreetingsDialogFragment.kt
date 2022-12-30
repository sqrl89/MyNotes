package com.alex.newnotes.ui.main.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.alex.newnotes.R

class GreetingsDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        return builder.setTitle(resources.getString(R.string.main_greetings))
            .setMessage(resources.getString(R.string.main_greetings_text))
            .setCancelable(true)
            .setNegativeButton(resources.getString(R.string.ok)) { dialog, _ ->
                dialog.cancel()
            }
            .create()
    }
}