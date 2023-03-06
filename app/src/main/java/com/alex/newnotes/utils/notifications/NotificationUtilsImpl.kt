package com.alex.newnotes.utils.notifications

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.alex.newnotes.AppActivity
import com.alex.newnotes.R
import com.alex.newnotes.utils.Const.CHANNEL_ID
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationUtilsImpl @Inject constructor(@ApplicationContext val context: Context) : NotificationUtils {
    private var notificationManager: NotificationManager? = null

    init {
        createChannel()
    }

    fun setNotification(title: String, color: String): NotificationCompat.Builder {
        val intent = Intent(context, AppActivity::class.java)
        val pendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        val intColor = Color.parseColor(color)

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notify)
            .setContentTitle(context.resources.getString(R.string.unfinished_task))
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(intColor)
            .setColorized(true)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, context.resources.getString(R.string.notification_channel), IMPORTANCE_HIGH
        ).apply {
            description = context.resources.getString(R.string.notification_contains)
        }
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        manager!!.createNotificationChannel(channel)
    }

    val manager: NotificationManager?
        get() {
            if (notificationManager == null) {
                notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            return notificationManager
        }

    override fun setReminder(timeInMillis: Long, title: String, color: String) {
        val intent = Intent(context, ReminderBroadcast::class.java)
        intent.putExtra("title", title)
        intent.putExtra("color", color)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager[AlarmManager.RTC_WAKEUP, timeInMillis] = pendingIntent
    }
}