package com.darkxwn.payping.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.darkxwn.payping.domain.model.CurrencyOption
import com.darkxwn.payping.domain.model.PaymentHistory
import com.darkxwn.payping.domain.model.PaymentReminder
import com.darkxwn.payping.domain.model.RecurrenceType
import com.darkxwn.payping.domain.model.ReminderConstants
import java.time.LocalDate

@Entity(tableName = "payment_reminders")
data class PaymentReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val amountMinor: Long,
    val currencyCode: String,
    val dueDateEpochDay: Long,
    val notifyHour: Int,
    val notifyMinute: Int,
    val recurrence: String,
    val remindDaysBeforeCsv: String,
    val note: String,
    val anchorDayOfMonth: Int,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)

@Entity(tableName = "payment_history")
data class PaymentHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val reminderId: Long?,
    val title: String,
    val amountMinor: Long,
    val currencyCode: String,
    val dueDateEpochDay: Long,
    val paidAtEpochMillis: Long,
    val note: String,
    val recurrence: String
)

fun PaymentReminderEntity.toDomain(): PaymentReminder {
    val parsedDays = remindDaysBeforeCsv
        .split(',')
        .mapNotNull { it.trim().toIntOrNull() }
        .filter { it >= 0 }
        .distinct()
        .sorted()
        .ifEmpty { ReminderConstants.DEFAULT_REMIND_DAYS }

    val parsedRecurrence = RecurrenceType.entries.firstOrNull {
        it.name.equals(recurrence, ignoreCase = true)
    } ?: RecurrenceType.ONCE

    return PaymentReminder(
        id = id,
        title = title,
        amountMinor = amountMinor,
        currencyCode = CurrencyOption.fromCode(currencyCode).code,
        dueDate = LocalDate.ofEpochDay(dueDateEpochDay),
        notifyHour = notifyHour.coerceIn(0, 23),
        notifyMinute = notifyMinute.coerceIn(0, 59),
        recurrence = parsedRecurrence,
        remindDaysBefore = parsedDays,
        note = note,
        anchorDayOfMonth = anchorDayOfMonth.coerceIn(1, 31),
        createdAtEpochMillis = createdAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis
    )
}

fun PaymentReminder.toEntity(): PaymentReminderEntity = PaymentReminderEntity(
    id = id,
    title = title.trim(),
    amountMinor = amountMinor,
    currencyCode = currencyCode,
    dueDateEpochDay = dueDate.toEpochDay(),
    notifyHour = notifyHour.coerceIn(0, 23),
    notifyMinute = notifyMinute.coerceIn(0, 59),
    recurrence = recurrence.name,
    remindDaysBeforeCsv = remindDaysBefore
        .filter { it >= 0 }
        .distinct()
        .sorted()
        .ifEmpty { ReminderConstants.DEFAULT_REMIND_DAYS }
        .joinToString(","),
    note = note.trim(),
    anchorDayOfMonth = anchorDayOfMonth.coerceIn(1, 31),
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis
)

fun PaymentHistoryEntity.toDomain(): PaymentHistory {
    val parsedRecurrence = RecurrenceType.entries.firstOrNull {
        it.name.equals(recurrence, ignoreCase = true)
    } ?: RecurrenceType.ONCE

    return PaymentHistory(
        id = id,
        reminderId = reminderId,
        title = title,
        amountMinor = amountMinor,
        currencyCode = CurrencyOption.fromCode(currencyCode).code,
        dueDate = LocalDate.ofEpochDay(dueDateEpochDay),
        paidAtEpochMillis = paidAtEpochMillis,
        note = note,
        recurrence = parsedRecurrence
    )
}

fun PaymentHistory.toEntity(): PaymentHistoryEntity = PaymentHistoryEntity(
    id = id,
    reminderId = reminderId,
    title = title.trim(),
    amountMinor = amountMinor,
    currencyCode = currencyCode,
    dueDateEpochDay = dueDate.toEpochDay(),
    paidAtEpochMillis = paidAtEpochMillis,
    note = note.trim(),
    recurrence = recurrence.name
)
