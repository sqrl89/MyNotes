package com.alex.newnotes.ui

import com.alex.newnotes.data.database.Note
import com.alex.newnotes.ui.edit.EditFragment
import com.alex.newnotes.ui.main.MainFragment
import com.github.terrakok.cicerone.androidx.FragmentScreen

object Screens {

    fun Main() = FragmentScreen {
        MainFragment()
    }

    fun Edit(note: Note) = FragmentScreen {
        EditFragment.newInstance(note)
    }
}