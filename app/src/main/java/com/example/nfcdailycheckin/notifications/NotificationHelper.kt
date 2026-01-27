package com.example.nfcdailycheckin.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    const val CHANNEL_ID = "daily_checkin_reminders"

    fun ensureChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = nm.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Daily Check-in Erinnerungen",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Erinnert dich an noch offene Aufgaben."
            }
        )
    }

    fun showReminder(context: Context, taskTitle: String, notificationId: Int) {
        ensureChannel(context)
        val text = "Aufgabe \"$taskTitle\" wurde noch nicht beendet. Bitte Aufgabe erledigen."
        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Daily Check-in")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, n)
    }
}
