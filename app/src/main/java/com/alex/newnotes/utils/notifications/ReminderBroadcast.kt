package com.alex.newnotes.utils.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class ReminderBroadcast : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationUtils = NotificationUtilsImpl(context!!)
        val title = intent?.getStringExtra("title")
        val color = intent?.getStringExtra("color")
        val builder = notificationUtils.setNotification(title!!, color!!)
        with(NotificationManagerCompat.from(context)) {
            notificationUtils.manager!!.notify(
                (System.currentTimeMillis() % 1000).toInt(),
                builder.build()
            )
        }
    }
}

