package com.alex.newnotes.utils.notifications

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.alex.newnotes.AppActivity
import com.alex.newnotes.R
import com.alex.newnotes.ui.edit.EditFragment
import com.alex.newnotes.ui.edit.EditFragment.Companion.CHANNEL_ID

class NotificationUtils(base: Context) : ContextWrapper(base) {
    private var notificationManager: NotificationManager? = null
    private val context: Context

    init {
        context = base
        createChannel()
    }

    fun setNotification(title: String, color: Int): NotificationCompat.Builder {
        val intent = Intent(context, AppActivity::class.java)
        val pendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notify)
            .setContentTitle(resources.getString(R.string.unfinished_task))
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(color)
            .setColorized(true)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            resources.getString(R.string.notification_channel),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = resources.getString(R.string.notification_contains)
        }
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        manager!!.createNotificationChannel(channel)
    }

    val manager: NotificationManager?
        get() {
            if (notificationManager == null) {
                notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            return notificationManager
        }

    fun setReminder(timeInMillis: Long, title: String, color: Int) {
        val intent = Intent(context, ReminderBroadcast::class.java)
        intent.putExtra("title", title)
        intent.putExtra("color", color)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager[AlarmManager.RTC_WAKEUP, timeInMillis] = pendingIntent
    }
}