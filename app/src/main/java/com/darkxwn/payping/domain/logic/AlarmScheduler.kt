package com.darkxwn.payping.domain.logic

import com.darkxwn.payping.domain.model.PaymentReminder

interface AlarmScheduler {
    fun scheduleReminder(reminder: PaymentReminder)
    fun cancelReminder(reminderId: Long, extraDaysBefore: List<Int> = emptyList())
    fun canScheduleExactAlarms(): Boolean
}
