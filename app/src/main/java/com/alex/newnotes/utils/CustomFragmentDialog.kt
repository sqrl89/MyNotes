package com.alex.newnotes.utils

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.alex.newnotes.R
import com.alex.newnotes.ui.edit.EditFragment
import com.alex.newnotes.ui.edit.EditFragment.Companion.TAG_CLOSE_EDIT_FRAGMENT
import com.alex.newnotes.ui.edit.EditFragment.Companion.TAG_GET_PICTURE
import com.alex.newnotes.ui.main.MainFragment
import com.alex.newnotes.ui.main.MainFragment.Companion.TAG_GREETINGS
import com.alex.newnotes.ui.main.MainFragment.Companion.TAG_DELETE_NOTE
import com.alex.newnotes.ui.main.MainFragment.Companion.TAG_MARK_NOTE
import com.alex.newnotes.ui.main.MainFragment.Companion.TAG_UNMARK_NOTE

class CustomFragmentDialog : DialogFragment() {
    private var data: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        data = arguments?.getString(PARAM)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let {
            when (data) {
                TAG_DELETE_NOTE -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle(resources.getString(R.string.delete_note))
                        .setCancelable(true)
                        .setPositiveButton(resources.getString(R.string.yes)) { dialog, _ ->
                            parentFragmentManager.setFragmentResult(
                                MainFragment.REQUEST_CODE, bundleOf(
                                    MainFragment.MAIN_KEY to MainFragment.DELETE
                                )
                            )
                            dialog.cancel()
                        }
                        .setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
                            dialog.cancel()
                        }
                        .create()
                }

                TAG_MARK_NOTE -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle(resources.getString(R.string.mark_completed))
                        .setCancelable(true)
                        .setPositiveButton(resources.getString(R.string.yes)) { dialog, _ ->
                            parentFragmentManager.setFragmentResult(
                                MainFragment.REQUEST_CODE,
                                bundleOf(MainFragment.MAIN_KEY to MainFragment.MARK_COMPLETED)
                            )
                            dialog.cancel()
                        }
                        .setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
                            dialog.cancel()
                        }
                        .create()
                }

                TAG_UNMARK_NOTE -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle(resources.getString(R.string.mark_uncompleted))
                        .setCancelable(true)
                        .setPositiveButton(resources.getString(R.string.yes)) { dialog, _ ->
                            parentFragmentManager.setFragmentResult(
                                MainFragment.REQUEST_CODE,
                                bundleOf(MainFragment.MAIN_KEY to MainFragment.MARK_UNCOMPLETED)
                            )
                            dialog.cancel()
                        }
                        .setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
                            dialog.cancel()
                        }
                        .create()

                }

                TAG_GREETINGS -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle(resources.getString(R.string.main_greetings))
                        .setMessage(resources.getString(R.string.main_greetings_text))
                        .setCancelable(true)
                        .setNegativeButton(resources.getString(R.string.ok)) { dialog, _ ->
                            dialog.cancel()
                        }
                        .create()
                }

                TAG_GET_PICTURE -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle(resources.getString(R.string.choose_image))
                        .setCancelable(true)
                        .setPositiveButton(resources.getString(R.string.make_photo)) { dialog, _ ->
                            parentFragmentManager.setFragmentResult(
                                EditFragment.REQUEST_KEY,
                                bundleOf(EditFragment.KEY_FOR_SOURCE to EditFragment.SOURCE_CAMERA)
                            )
                            dialog.cancel()
                        }
                        .setNegativeButton(resources.getString(R.string.from_gallery)) { dialog, _ ->
                            parentFragmentManager.setFragmentResult(
                                EditFragment.REQUEST_KEY,
                                bundleOf(EditFragment.KEY_FOR_SOURCE to EditFragment.SOURCE_GALLERY)
                            )
                            dialog.cancel()
                        }
                        .create()
                }

                TAG_CLOSE_EDIT_FRAGMENT -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle(resources.getString(R.string.save_changes))
                        .setCancelable(true)
                        .setPositiveButton(resources.getString(R.string.yes)) { dialog, _ ->
                            parentFragmentManager.setFragmentResult(
                                EditFragment.REQUEST_KEY,
                                bundleOf(EditFragment.KEY_FOR_SOURCE to EditFragment.SAVE_KEY)
                            )
                            dialog.cancel()
                        }
                        .setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
                            parentFragmentManager.setFragmentResult(
                                EditFragment.REQUEST_KEY,
                                bundleOf(EditFragment.KEY_FOR_SOURCE to EditFragment.CLOSE_KEY)
                            )
                            dialog.cancel()
                        }
                        .create()
                }

                else -> throw Exception("Dialog error, data = $data")
            }
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        parentFragmentManager.setFragmentResult(
            MainFragment.REQUEST_CODE,
            bundleOf(MainFragment.MAIN_KEY to MainFragment.CANCEL)
        )
    }

    companion object {
        private const val PARAM = "param"

        fun newInstance(param: String?) = CustomFragmentDialog().apply {
            arguments = Bundle().apply {
                putString(PARAM, param)
            }
        }
    }

}