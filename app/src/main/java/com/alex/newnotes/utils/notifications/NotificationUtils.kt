package com.alex.newnotes.utils.notifications

interface NotificationUtils {

    fun setReminder(timeInMillis: Long, id: Int, title: String, color: String)

    fun cancelReminder(id: Int)

}