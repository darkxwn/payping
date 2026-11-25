package com.darkxwn.payping.domain.backup

import com.darkxwn.payping.domain.model.AppLanguage
import com.darkxwn.payping.domain.model.AppSettings
import com.darkxwn.payping.domain.model.AppThemeMode
import com.darkxwn.payping.domain.model.CurrencyOption
import com.darkxwn.payping.domain.model.PaymentHistory
import com.darkxwn.payping.domain.model.PaymentReminder
import com.darkxwn.payping.domain.model.RecurrenceType
import com.darkxwn.payping.domain.model.ReminderConstants
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDate

enum class ImportMode {
    REPLACE,
    MERGE
}

@Serializable
data class BackupPayload(
    val schemaVersion: Int = BackupSerializer.CURRENT_SCHEMA_VERSION,
    val exportedAtEpochMillis: Long,
    val settings: BackupSettingsDto? = null,
    val reminders: List<BackupReminderDto> = emptyList(),
    val history: List<BackupHistoryDto> = emptyList()
)

@Serializable
data class BackupSettingsDto(
    val language: String? = null,
    val themeMode: String? = null,
    val defaultCurrencyCode: String? = null
)

@Serializable
data class BackupReminderDto(
    val title: String,
    val amountMinor: Long,
    val currencyCode: String,
    val dueDate: String,
    val notifyHour: Int,
    val notifyMinute: Int,
    val recurrence: String,
    val remindDaysBefore: List<Int> = listOf(0),
    val note: String = "",
    val anchorDayOfMonth: Int? = null,
    val createdAtEpochMillis: Long = 0L,
    val updatedAtEpochMillis: Long = 0L
)

@Serializable
data class BackupHistoryDto(
    val reminderId: Long? = null,
    val title: String,
    val amountMinor: Long,
    val currencyCode: String,
    val dueDate: String,
    val paidAtEpochMillis: Long,
    val note: String = "",
    val recurrence: String = RecurrenceType.ONCE.name
)

data class DecodedBackup(
    val settings: AppSettings?,
    val reminders: List<PaymentReminder>,
    val history: List<PaymentHistory>
)

object BackupSerializer {
    const val CURRENT_SCHEMA_VERSION = 1

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(
        reminders: List<PaymentReminder>,
        history: List<PaymentHistory>,
        settings: AppSettings,
        exportedAtEpochMillis: Long = System.currentTimeMillis()
    ): String {
        val payload = BackupPayload(
            schemaVersion = CURRENT_SCHEMA_VERSION,
            exportedAtEpochMillis = exportedAtEpochMillis,
            settings = BackupSettingsDto(
                language = settings.language.name,
                themeMode = settings.themeMode.name,
                defaultCurrencyCode = settings.defaultCurrencyCode
            ),
            reminders = reminders.map { reminder ->
                BackupReminderDto(
                    title = reminder.title,
                    amountMinor = reminder.amountMinor,
                    currencyCode = reminder.currencyCode,
                    dueDate = reminder.dueDate.toString(),
                    notifyHour = reminder.notifyHour,
                    notifyMinute = reminder.notifyMinute,
                    recurrence = reminder.recurrence.name,
                    remindDaysBefore = reminder.remindDaysBefore,
                    note = reminder.note,
                    anchorDayOfMonth = reminder.anchorDayOfMonth,
                    createdAtEpochMillis = reminder.createdAtEpochMillis,
                    updatedAtEpochMillis = reminder.updatedAtEpochMillis
                )
            },
            history = history.map { item ->
                BackupHistoryDto(
                    reminderId = item.reminderId,
                    title = item.title,
                    amountMinor = item.amountMinor,
                    currencyCode = item.currencyCode,
                    dueDate = item.dueDate.toString(),
                    paidAtEpochMillis = item.paidAtEpochMillis,
                    note = item.note,
                    recurrence = item.recurrence.name
                )
            }
        )
        return json.encodeToString(BackupPayload.serializer(), payload)
    }

    fun decode(rawJson: String): Result<DecodedBackup> = runCatching {
        val payload = json.decodeFromString(BackupPayload.serializer(), rawJson)
        require(payload.schemaVersion in 1..CURRENT_SCHEMA_VERSION) {
            "Unsupported backup schema version: ${payload.schemaVersion}"
        }

        val parsedSettings = payload.settings?.let { dto ->
            AppSettings(
                language = enumValueOrNull<AppLanguage>(dto.language) ?: AppLanguage.fromSystem(),
                themeMode = enumValueOrNull<AppThemeMode>(dto.themeMode) ?: AppThemeMode.SYSTEM,
                defaultCurrencyCode = CurrencyOption.fromCode(dto.defaultCurrencyCode).code
            )
        }

        val parsedReminders = payload.reminders.map { dto ->
            val trimmedTitle = dto.title.trim()
            require(trimmedTitle.isNotEmpty()) { "Reminder title cannot be blank" }
            require(dto.amountMinor > 0L) { "Reminder amount must be positive" }
            val dueDate = LocalDate.parse(dto.dueDate)
            val recurrence = enumValueOrNull<RecurrenceType>(dto.recurrence)
                ?: throw IllegalArgumentException("Unknown recurrence: ${dto.recurrence}")
            val remindDays = dto.remindDaysBefore
                .filter { it >= 0 }
                .distinct()
                .sorted()
                .ifEmpty { ReminderConstants.DEFAULT_REMIND_DAYS }

            PaymentReminder(
                id = 0L,
                title = trimmedTitle,
                amountMinor = dto.amountMinor,
                currencyCode = CurrencyOption.fromCode(dto.currencyCode).code,
                dueDate = dueDate,
                notifyHour = dto.notifyHour.coerceIn(0, 23),
                notifyMinute = dto.notifyMinute.coerceIn(0, 59),
                recurrence = recurrence,
                remindDaysBefore = remindDays,
                note = dto.note.trim(),
                anchorDayOfMonth = (dto.anchorDayOfMonth ?: dueDate.dayOfMonth).coerceIn(1, 31),
                createdAtEpochMillis = dto.createdAtEpochMillis,
                updatedAtEpochMillis = dto.updatedAtEpochMillis
            )
        }

        val parsedHistory = payload.history.map { dto ->
            val trimmedTitle = dto.title.trim()
            require(trimmedTitle.isNotEmpty()) { "History title cannot be blank" }
            require(dto.amountMinor > 0L) { "History amount must be positive" }
            val dueDate = LocalDate.parse(dto.dueDate)
            val recurrence = enumValueOrNull<RecurrenceType>(dto.recurrence) ?: RecurrenceType.ONCE

            PaymentHistory(
                id = 0L,
                reminderId = dto.reminderId,
                title = trimmedTitle,
                amountMinor = dto.amountMinor,
                currencyCode = CurrencyOption.fromCode(dto.currencyCode).code,
                dueDate = dueDate,
                paidAtEpochMillis = dto.paidAtEpochMillis,
                note = dto.note.trim(),
                recurrence = recurrence
            )
        }

        DecodedBackup(
            settings = parsedSettings,
            reminders = parsedReminders,
            history = parsedHistory
        )
    }

    private inline fun <reified T : Enum<T>> enumValueOrNull(raw: String?): T? {
        if (raw.isNullOrBlank()) return null
        return enumValues<T>().firstOrNull { it.name.equals(raw.trim(), ignoreCase = true) }
    }
}
