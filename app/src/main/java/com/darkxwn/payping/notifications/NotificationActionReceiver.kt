package com.darkxwn.payping.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.darkxwn.payping.PayPingApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != NotificationHelper.ACTION_MARK_DONE) return
        val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, -1L)
        val notificationId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, -1)
        if (reminderId <= 0L) return

        val pendingResult = goAsync()
        val app = context.applicationContext as PayPingApp
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                app.container.paymentRepository.markReminderDone(reminderId)
                if (notificationId != -1) {
                    NotificationManagerCompat.from(context.applicationContext).cancel(notificationId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
