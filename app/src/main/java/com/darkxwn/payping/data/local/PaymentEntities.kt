package com.darkxwn.payping.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.darkxwn.payping.domain.model.BaseAccentColor
import com.darkxwn.payping.domain.model.BuiltInCategory
import com.darkxwn.payping.domain.model.CurrencyOption
import com.darkxwn.payping.domain.model.PaymentCategory
import com.darkxwn.payping.domain.model.PaymentHistory
import com.darkxwn.payping.domain.model.PaymentReminder
import com.darkxwn.payping.domain.model.PaymentType
import com.darkxwn.payping.domain.model.RecurrenceType
import com.darkxwn.payping.domain.model.ReminderConstants
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "payment_categories")
data class PaymentCategoryEntity(
    @PrimaryKey
    val uid: String,
    val name: String,
    val iconKey: String,
    val colorArgb: Long,
    val sortOrder: Int = 0,
    val createdAtEpochMillis: Long = 0L
)

@Entity(tableName = "payment_reminders")
data class PaymentReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val uid: String = UUID.randomUUID().toString(),
    val title: String,
    val amountMinor: Long,
    val currencyCode: String,
    val dueDateEpochDay: Long,
    val notifyHour: Int,
    val notifyMinute: Int,
    val recurrence: String,
    val remindDaysBeforeCsv: String,
    val note: String,
    val paymentType: String = PaymentType.MANUAL.name,
    val categoryUid: String = BuiltInCategory.OTHER.uid,
    val badgeIconKey: String? = null,
    val badgeText: String? = null,
    val badgeColorArgb: Long = BaseAccentColor.DEFAULT_ARGB,
    val isArchived: Boolean = false,
    val isVariableAmount: Boolean = false,
    val serviceLink: String = "",
    val installmentPaid: Int? = null,
    val installmentTotal: Int? = null,
    val anchorDayOfMonth: Int,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)

@Entity(tableName = "payment_history")
data class PaymentHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val uid: String = UUID.randomUUID().toString(),
    val reminderId: Long?,
    val reminderUid: String? = null,
    val title: String,
    val amountMinor: Long,
    val currencyCode: String,
    val dueDateEpochDay: Long,
    val paidAtEpochMillis: Long,
    val note: String,
    val recurrence: String,
    val paymentType: String = PaymentType.MANUAL.name,
    val categoryUid: String = BuiltInCategory.OTHER.uid,
    val badgeIconKey: String? = null,
    val badgeText: String? = null,
    val badgeColorArgb: Long = BaseAccentColor.DEFAULT_ARGB,
    val previousDueDateEpochDay: Long? = null,
    val nextDueDateAfterPaymentEpochDay: Long? = null,
    val rateToUsd: Double? = null
)

fun PaymentCategoryEntity.toDomain(): PaymentCategory = PaymentCategory(
    uid = uid,
    name = name,
    iconKey = iconKey.ifBlank { "other" },
    colorArgb = BaseAccentColor.normalizeOpaqueArgb(colorArgb),
    isBuiltIn = false,
    sortOrder = sortOrder,
    createdAtEpochMillis = createdAtEpochMillis
)

fun PaymentCategory.toEntity(): PaymentCategoryEntity = PaymentCategoryEntity(
    uid = uid,
    name = name.trim(),
    iconKey = iconKey.ifBlank { "other" },
    colorArgb = BaseAccentColor.normalizeOpaqueArgb(colorArgb),
    sortOrder = sortOrder,
    createdAtEpochMillis = createdAtEpochMillis
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
        uid = uid.ifBlank { UUID.randomUUID().toString() },
        title = title,
        amountMinor = amountMinor,
        currencyCode = CurrencyOption.fromCode(currencyCode).code,
        dueDate = LocalDate.ofEpochDay(dueDateEpochDay),
        notifyHour = notifyHour.coerceIn(0, 23),
        notifyMinute = notifyMinute.coerceIn(0, 59),
        recurrence = parsedRecurrence,
        remindDaysBefore = parsedDays,
        note = note,
        paymentType = PaymentType.fromRaw(paymentType),
        categoryUid = categoryUid.ifBlank { BuiltInCategory.OTHER.uid },
        badgeIconKey = badgeIconKey?.takeIf { it.isNotBlank() },
        badgeText = badgeText?.takeIf { it.isNotBlank() },
        badgeColorArgb = BaseAccentColor.normalizeOpaqueArgb(badgeColorArgb),
        isArchived = isArchived,
        isVariableAmount = isVariableAmount,
        serviceLink = serviceLink,
        installmentPaid = installmentPaid,
        installmentTotal = installmentTotal,
        anchorDayOfMonth = anchorDayOfMonth.coerceIn(1, 31),
        createdAtEpochMillis = createdAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis
    )
}

fun PaymentReminder.toEntity(): PaymentReminderEntity = PaymentReminderEntity(
    id = id,
    uid = uid.ifBlank { UUID.randomUUID().toString() },
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
    paymentType = paymentType.name,
    categoryUid = categoryUid.ifBlank { BuiltInCategory.OTHER.uid },
    badgeIconKey = badgeIconKey?.takeIf { it.isNotBlank() },
    badgeText = badgeText?.trim()?.takeIf { it.isNotBlank() },
    badgeColorArgb = BaseAccentColor.normalizeOpaqueArgb(badgeColorArgb),
    isArchived = isArchived,
    isVariableAmount = isVariableAmount,
    serviceLink = serviceLink.trim(),
    installmentPaid = installmentPaid,
    installmentTotal = installmentTotal,
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
        uid = uid.ifBlank { UUID.randomUUID().toString() },
        reminderId = reminderId,
        reminderUid = reminderUid?.takeIf { it.isNotBlank() },
        title = title,
        amountMinor = amountMinor,
        currencyCode = CurrencyOption.fromCode(currencyCode).code,
        dueDate = LocalDate.ofEpochDay(dueDateEpochDay),
        paidAtEpochMillis = paidAtEpochMillis,
        note = note,
        recurrence = parsedRecurrence,
        paymentType = PaymentType.fromRaw(paymentType),
        categoryUid = categoryUid.ifBlank { BuiltInCategory.OTHER.uid },
        badgeIconKey = badgeIconKey?.takeIf { it.isNotBlank() },
        badgeText = badgeText?.takeIf { it.isNotBlank() },
        badgeColorArgb = BaseAccentColor.normalizeOpaqueArgb(badgeColorArgb),
        previousDueDate = previousDueDateEpochDay?.let(LocalDate::ofEpochDay),
        nextDueDateAfterPayment = nextDueDateAfterPaymentEpochDay?.let(LocalDate::ofEpochDay),
        rateToUsd = rateToUsd
    )
}

fun PaymentHistory.toEntity(): PaymentHistoryEntity = PaymentHistoryEntity(
    id = id,
    uid = uid.ifBlank { UUID.randomUUID().toString() },
    reminderId = reminderId,
    reminderUid = reminderUid?.takeIf { it.isNotBlank() },
    title = title.trim(),
    amountMinor = amountMinor,
    currencyCode = currencyCode,
    dueDateEpochDay = dueDate.toEpochDay(),
    paidAtEpochMillis = paidAtEpochMillis,
    note = note.trim(),
    recurrence = recurrence.name,
    paymentType = paymentType.name,
    categoryUid = categoryUid.ifBlank { BuiltInCategory.OTHER.uid },
    badgeIconKey = badgeIconKey?.takeIf { it.isNotBlank() },
    badgeText = badgeText?.trim()?.takeIf { it.isNotBlank() },
    badgeColorArgb = BaseAccentColor.normalizeOpaqueArgb(badgeColorArgb),
    previousDueDateEpochDay = previousDueDate?.toEpochDay(),
    nextDueDateAfterPaymentEpochDay = nextDueDateAfterPayment?.toEpochDay(),
    rateToUsd = rateToUsd
)
