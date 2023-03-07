package com.alex.newnotes.utils

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.view.WindowManager
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.alex.newnotes.ui.main.MainFragment.Companion.TAG_GREETINGS
import com.alex.newnotes.utils.Const.PREFS_FIRST_START_KEY
import com.alex.newnotes.utils.Const.PREFS_FIRST_START_NAME
import com.alex.newnotes.utils.Const.PREFS_NEW_ID_KEY
import com.alex.newnotes.utils.Const.PREFS_NEW_ID_NAME
import com.google.android.material.snackbar.Snackbar

fun Activity.changeStatusBarColor(color: Int, isLight: Boolean) {
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = color
    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = isLight
}

fun Fragment.showDialog(param: String, tag: String) {
    CustomFragmentDialog.newInstance(param).show(requireActivity().supportFragmentManager, tag)
}

fun Fragment.checkFirstRun() {
    showDialog(TAG_GREETINGS, TAG_GREETINGS)
    requireActivity().getSharedPreferences(PREFS_FIRST_START_NAME, MODE_PRIVATE).edit().also {
        it.putBoolean(PREFS_FIRST_START_KEY, false)
    }.apply()

    requireActivity().getSharedPreferences(PREFS_NEW_ID_NAME, MODE_PRIVATE).edit().also {
        it.putInt(PREFS_NEW_ID_KEY, 0)
    }.apply()
}

fun Fragment.showSnackbar(text: String) {
    Snackbar.make(requireView(), text, Snackbar.LENGTH_LONG).show()
}