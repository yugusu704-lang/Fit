package com.fit.tracker.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.time.LocalDateTime
import java.time.ZoneId

class FitWidgetRefreshReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        FitWidgetProvider.notifyWidgetsUpdate(context)
        scheduleNextMidnightAlarm(context)
    }

    companion object {
        const val ACTION_MIDNIGHT_REFRESH = "com.fit.tracker.widget.ACTION_MIDNIGHT_REFRESH"

        fun scheduleNextMidnightAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val now = LocalDateTime.now()
            val tomorrowMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
            val triggerMillis = tomorrowMidnight.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val intent = Intent(context, FitWidgetRefreshReceiver::class.java).apply {
                action = ACTION_MIDNIGHT_REFRESH
            }
            val pi = PendingIntent.getBroadcast(
                context,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            try {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pi)
            } catch (_: SecurityException) {
                // In case setAndAllowWhileIdle is restricted, fallback to standard set
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pi)
            }
        }
    }
}
