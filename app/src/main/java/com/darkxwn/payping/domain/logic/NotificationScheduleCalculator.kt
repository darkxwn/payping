package com.darkxwn.payping.domain.logic

import com.darkxwn.payping.domain.model.PaymentReminder
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

data class ScheduledTrigger(
    val reminderId: Long,
    val daysBefore: Int,
    val triggerAtEpochMillis: Long
)

object NotificationScheduleCalculator {
    fun computeFutureTriggers(
        reminder: PaymentReminder,
        now: ZonedDateTime,
        zoneId: ZoneId
    ): List<ScheduledTrigger> {
        if (reminder.id <= 0L) return emptyList()
        val time = LocalTime.of(
            reminder.notifyHour.coerceIn(0, 23),
            reminder.notifyMinute.coerceIn(0, 59)
        )
        return reminder.remindDaysBefore
            .filter { it >= 0 }
            .distinct()
            .sorted()
            .mapNotNull { daysBefore ->
                val triggerDate = reminder.dueDate.minusDays(daysBefore.toLong())
                val triggerZdt = ZonedDateTime.of(triggerDate, time, zoneId)
                if (triggerZdt.isAfter(now)) {
                    ScheduledTrigger(
                        reminderId = reminder.id,
                        daysBefore = daysBefore,
                        triggerAtEpochMillis = triggerZdt.toInstant().toEpochMilli()
                    )
                } else {
                    null
                }
            }
    }
}
