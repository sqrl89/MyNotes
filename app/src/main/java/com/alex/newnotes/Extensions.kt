package com.alex.newnotes

import android.app.Activity
import android.content.Context
import android.view.WindowManager
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.alex.newnotes.Const.FIRST_START_KEY
import com.alex.newnotes.Const.PREFS_NAME
import com.alex.newnotes.ui.main.dialogs.GreetingsDialogFragment

fun Activity.changeStatusBarColor(color: Int, isLight: Boolean) {
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = color
    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = isLight
}

fun Fragment.showDialog(dialogFragment: DialogFragment, tag: String) {
    activity?.supportFragmentManager?.let {
        dialogFragment.show(it, tag)
    }
}

fun Fragment.checkFirstRun() {
    showDialog(GreetingsDialogFragment(), "greetings")
    val sharedPref = requireActivity().getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    sharedPref.edit().also {
        it.putBoolean(FIRST_START_KEY, false)
    }.apply()
}



