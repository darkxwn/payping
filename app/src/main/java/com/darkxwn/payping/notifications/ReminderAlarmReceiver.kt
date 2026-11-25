package com.darkxwn.payping.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.darkxwn.payping.PayPingApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, -1L)
        val daysBefore = intent.getIntExtra(NotificationHelper.EXTRA_DAYS_BEFORE, 0)
        if (reminderId <= 0L) return

        val pendingResult = goAsync()
        val app = context.applicationContext as PayPingApp
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val reminder = app.container.paymentRepository.getReminder(reminderId)
                if (reminder != null) {
                    val settings = app.container.settingsRepository.getCurrentSettings()
                    NotificationHelper.showReminderNotification(
                        context = context.applicationContext,
                        reminder = reminder,
                        daysBefore = daysBefore,
                        language = settings.language
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
