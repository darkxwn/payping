package com.darkxwn.payping.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.darkxwn.payping.MainActivity
import com.darkxwn.payping.R
import com.darkxwn.payping.domain.logic.MoneyFormat
import com.darkxwn.payping.domain.model.AppLanguage
import com.darkxwn.payping.domain.model.PaymentReminder
import com.darkxwn.payping.domain.model.PaymentType
import com.darkxwn.payping.ui.components.resolveSystemLanguageTag
import java.util.Locale

object NotificationHelper {
    const val CHANNEL_ID = "payping_reminders"
    const val EXTRA_REMINDER_ID = "extra_reminder_id"
    const val EXTRA_DAYS_BEFORE = "extra_days_before"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    const val ACTION_MARK_DONE = "com.darkxwn.payping.action.MARK_DONE"

    fun ensureChannel(context: Context, language: AppLanguage = AppLanguage.SYSTEM) {
        val localizedCtx = localizedContext(context, language)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            localizedCtx.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = localizedCtx.getString(R.string.notification_channel_desc)
        }
        manager.createNotificationChannel(channel)
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showReminderNotification(
        context: Context,
        reminder: PaymentReminder,
        daysBefore: Int,
        language: AppLanguage
    ) {
        if (!hasNotificationPermission(context)) return

        ensureChannel(context, language)
        val localizedCtx = localizedContext(context, language)
        val notificationId = notificationId(reminder.id, daysBefore)
        val amountText = MoneyFormat.formatMinorAmount(reminder.amountMinor, reminder.currencyCode)

        val title = when (reminder.paymentType) {
            PaymentType.AUTO_DEBIT -> when (daysBefore) {
                0 -> localizedCtx.getString(R.string.notification_title_auto_today, reminder.title)
                1 -> localizedCtx.getString(R.string.notification_title_auto_tomorrow, reminder.title)
                else -> localizedCtx.getString(R.string.notification_title_auto_in_days, daysBefore, reminder.title)
            }
            PaymentType.TRIAL -> when (daysBefore) {
                0 -> localizedCtx.getString(R.string.notification_title_trial_today, reminder.title)
                1 -> localizedCtx.getString(R.string.notification_title_trial_tomorrow, reminder.title)
                else -> localizedCtx.getString(R.string.notification_title_trial_in_days, daysBefore, reminder.title)
            }
            PaymentType.MANUAL, PaymentType.INSTALLMENT -> when (daysBefore) {
                0 -> localizedCtx.getString(R.string.notification_title_today, reminder.title)
                1 -> localizedCtx.getString(R.string.notification_title_tomorrow, reminder.title)
                else -> localizedCtx.getString(R.string.notification_title_in_days, daysBefore, reminder.title)
            }
        }

        val body = when (reminder.paymentType) {
            PaymentType.AUTO_DEBIT -> if (reminder.note.isNotBlank()) {
                localizedCtx.getString(R.string.notification_body_auto_with_note, amountText, reminder.note)
            } else {
                localizedCtx.getString(R.string.notification_body_auto_amount, amountText)
            }
            PaymentType.TRIAL -> if (reminder.note.isNotBlank()) {
                localizedCtx.getString(R.string.notification_body_trial_with_note, amountText, reminder.note)
            } else {
                localizedCtx.getString(R.string.notification_body_trial_amount, amountText)
            }
            PaymentType.MANUAL, PaymentType.INSTALLMENT -> if (reminder.note.isNotBlank()) {
                localizedCtx.getString(R.string.notification_body_with_note, amountText, reminder.note)
            } else {
                localizedCtx.getString(R.string.notification_body_amount, amountText)
            }
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val doneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_MARK_DONE
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 1,
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(
                R.drawable.ic_check_action,
                localizedCtx.getString(R.string.action_mark_done),
                donePendingIntent
            )
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    fun notificationId(reminderId: Long, daysBefore: Int): Int =
        ("payping:$reminderId:$daysBefore").hashCode()

    fun localizedContext(context: Context, language: AppLanguage): Context {
        val locale = Locale(language.resolvedLocaleTag(resolveSystemLanguageTag()))
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
