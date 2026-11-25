package com.darkxwn.payping.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.darkxwn.payping.domain.logic.AlarmScheduler
import com.darkxwn.payping.domain.logic.NotificationScheduleCalculator
import com.darkxwn.payping.domain.model.PaymentReminder
import com.darkxwn.payping.domain.model.ReminderConstants
import java.time.ZoneId
import java.time.ZonedDateTime

class AndroidAlarmScheduler(
    private val context: Context
) : AlarmScheduler {

    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun scheduleReminder(reminder: PaymentReminder) {
        if (reminder.id <= 0L) return
        val triggers = NotificationScheduleCalculator.computeFutureTriggers(
            reminder = reminder,
            now = ZonedDateTime.now(),
            zoneId = ZoneId.systemDefault()
        )
        for (trigger in triggers) {
            val pendingIntent = buildPendingIntent(
                reminderId = trigger.reminderId,
                daysBefore = trigger.daysBefore,
                flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            ) ?: continue

            if (canScheduleExactAlarms()) {
                try {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        trigger.triggerAtEpochMillis,
                        pendingIntent
                    )
                } catch (_: SecurityException) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        trigger.triggerAtEpochMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    trigger.triggerAtEpochMillis,
                    pendingIntent
                )
            }
        }
    }

    override fun cancelReminder(reminderId: Long, extraDaysBefore: List<Int>) {
        if (reminderId <= 0L) return
        val allOffsets = (ReminderConstants.SUPPORTED_REMIND_DAYS + extraDaysBefore)
            .filter { it >= 0 }
            .distinct()

        for (daysBefore in allOffsets) {
            val pendingIntent = buildPendingIntent(
                reminderId = reminderId,
                daysBefore = daysBefore,
                flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    override fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    private fun buildPendingIntent(
        reminderId: Long,
        daysBefore: Int,
        flags: Int
    ): PendingIntent? {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_REMINDER
            data = Uri.parse("payping://alarm/$reminderId/$daysBefore")
            putExtra(NotificationHelper.EXTRA_REMINDER_ID, reminderId)
            putExtra(NotificationHelper.EXTRA_DAYS_BEFORE, daysBefore)
        }
        val requestCode = ("alarm:$reminderId:$daysBefore").hashCode()
        return PendingIntent.getBroadcast(context, requestCode, intent, flags)
    }

    companion object {
        const val ACTION_TRIGGER_REMINDER = "com.darkxwn.payping.action.TRIGGER_REMINDER"
    }
}
