package com.alex.newnotes

import android.app.Activity
import android.content.Context
import android.view.WindowManager
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.alex.newnotes.utils.Const.FIRST_START_KEY
import com.alex.newnotes.utils.Const.PREFS_NAME
import com.alex.newnotes.ui.main.MainFragment.Companion.TAG_GREETINGS
import com.alex.newnotes.utils.CustomFragmentDialog
import com.google.android.material.snackbar.Snackbar

fun Activity.changeStatusBarColor(color: Int, isLight: Boolean) {
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = color
    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = isLight
}

fun Fragment.showDialog(param: String, tag: String){
    CustomFragmentDialog.newInstance(param).show(requireActivity().supportFragmentManager, tag)
}

fun Fragment.checkFirstRun() {
    showDialog(TAG_GREETINGS, TAG_GREETINGS)
    val sharedPref = requireActivity().getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    sharedPref.edit().also {
        it.putBoolean(FIRST_START_KEY, false)
    }.apply()
}

fun Fragment.showSnackbar(text: String) {
    Snackbar.make(requireView(), text, Snackbar.LENGTH_LONG).show()
}