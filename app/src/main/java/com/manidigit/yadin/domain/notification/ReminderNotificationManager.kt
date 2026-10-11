package com.manidigit.yadin.domain.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.manidigit.yadin.R

object ReminderNotificationManager {

    private const val CHANNEL_ID = "yadin_daily_reminder_channel"
    private const val CHANNEL_NAME = "یادآور روزانه مرور واژگان"
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "اعلان زمان مرور روزانه فلش‌کارت‌ها در برنامه یادین"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showDailyReminderNotification(context: Context, dueCount: Int) {
        createNotificationChannel(context)

        val title = "زمان مرور واژگان یادین فرا رسید!"
        val message = if (dueCount > 0) {
            "شما امروز $dueCount واژه سررسیدشده برای مرور در برنامه دارید."
        } else {
            "امروز زنجیره مطالعه خود را فعال نگه دارید و واژگان جدید تمرین کنید."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}
