package com.darkxwn.payping.data.repository

import androidx.room.withTransaction
import com.darkxwn.payping.data.local.PayPingDatabase
import com.darkxwn.payping.data.local.PaymentHistoryEntity
import com.darkxwn.payping.data.local.toDomain
import com.darkxwn.payping.data.local.toEntity
import com.darkxwn.payping.data.settings.SettingsRepository
import com.darkxwn.payping.domain.backup.BackupSerializer
import com.darkxwn.payping.domain.backup.ImportMode
import com.darkxwn.payping.domain.logic.AlarmScheduler
import com.darkxwn.payping.domain.logic.RecurrenceCalculator
import com.darkxwn.payping.domain.model.AppSettings
import com.darkxwn.payping.domain.model.PaymentHistory
import com.darkxwn.payping.domain.model.PaymentReminder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

data class MarkDoneResult(
    val title: String,
    val advancedToDate: LocalDate?
)

data class ImportSummary(
    val importedRemindersCount: Int,
    val importedHistoryCount: Int,
    val importedSettings: AppSettings?
)

class PaymentRepository(
    private val database: PayPingDatabase,
    private val settingsRepository: SettingsRepository,
    private val alarmScheduler: AlarmScheduler
) {
    private val reminderDao = database.reminderDao()
    private val historyDao = database.historyDao()

    val reminders: Flow<List<PaymentReminder>> = reminderDao.observeAll().map { entities ->
        entities.map { it.toDomain() }
    }

    val history: Flow<List<PaymentHistory>> = historyDao.observeAll().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun getReminder(id: Long): PaymentReminder? =
        reminderDao.getById(id)?.toDomain()

    suspend fun upsertReminder(reminder: PaymentReminder): Long {
        val now = System.currentTimeMillis()
        return if (reminder.id <= 0L) {
            val entity = reminder.copy(
                createdAtEpochMillis = if (reminder.createdAtEpochMillis > 0L) reminder.createdAtEpochMillis else now,
                updatedAtEpochMillis = now
            ).toEntity()
            val newId = reminderDao.insert(entity)
            val saved = entity.copy(id = newId).toDomain()
            alarmScheduler.scheduleReminder(saved)
            newId
        } else {
            val previous = reminderDao.getById(reminder.id)?.toDomain()
            if (previous != null) {
                alarmScheduler.cancelReminder(previous.id, previous.remindDaysBefore)
            }
            val updated = reminder.copy(
                createdAtEpochMillis = previous?.createdAtEpochMillis ?: reminder.createdAtEpochMillis.takeIf { it > 0L } ?: now,
                updatedAtEpochMillis = now
            )
            reminderDao.update(updated.toEntity())
            alarmScheduler.scheduleReminder(updated)
            updated.id
        }
    }

    suspend fun deleteReminder(id: Long) {
        val existing = reminderDao.getById(id)?.toDomain()
        alarmScheduler.cancelReminder(id, existing?.remindDaysBefore.orEmpty())
        reminderDao.deleteById(id)
    }

    suspend fun markReminderDone(id: Long): MarkDoneResult? {
        val now = System.currentTimeMillis()
        var previousReminder: PaymentReminder? = null
        var updatedReminder: PaymentReminder? = null
        var nextDueDate: LocalDate? = null

        database.withTransaction {
            val entity = reminderDao.getById(id) ?: return@withTransaction
            val domain = entity.toDomain()
            previousReminder = domain

            historyDao.insert(
                PaymentHistoryEntity(
                    reminderId = domain.id,
                    title = domain.title,
                    amountMinor = domain.amountMinor,
                    currencyCode = domain.currencyCode,
                    dueDateEpochDay = domain.dueDate.toEpochDay(),
                    paidAtEpochMillis = now,
                    note = domain.note,
                    recurrence = domain.recurrence.name
                )
            )

            nextDueDate = RecurrenceCalculator.nextDueDate(
                currentDueDate = domain.dueDate,
                recurrence = domain.recurrence,
                anchorDayOfMonth = domain.anchorDayOfMonth
            )

            val targetDate = nextDueDate
            if (targetDate != null) {
                val advanced = domain.copy(
                    dueDate = targetDate,
                    updatedAtEpochMillis = now
                )
                reminderDao.update(advanced.toEntity())
                updatedReminder = advanced
            } else {
                reminderDao.deleteById(domain.id)
            }
        }

        val completed = previousReminder ?: return null
        alarmScheduler.cancelReminder(completed.id, completed.remindDaysBefore)
        updatedReminder?.let { alarmScheduler.scheduleReminder(it) }

        return MarkDoneResult(
            title = completed.title,
            advancedToDate = nextDueDate
        )
    }

    suspend fun deleteHistoryItem(id: Long) {
        historyDao.deleteById(id)
    }

    suspend fun clearHistory() {
        historyDao.deleteAll()
    }

    suspend fun rescheduleAllReminders() {
        val all = reminderDao.getAll().map { it.toDomain() }
        all.forEach { reminder ->
            alarmScheduler.cancelReminder(reminder.id, reminder.remindDaysBefore)
            alarmScheduler.scheduleReminder(reminder)
        }
    }

    suspend fun exportBackupJson(): String {
        val currentReminders = reminderDao.getAll().map { it.toDomain() }
        val currentHistory = historyDao.getAll().map { it.toDomain() }
        val currentSettings = settingsRepository.getCurrentSettings()
        return BackupSerializer.encode(
            reminders = currentReminders,
            history = currentHistory,
            settings = currentSettings
        )
    }

    suspend fun importBackupJson(rawJson: String, mode: ImportMode): Result<ImportSummary> {
        val decoded = BackupSerializer.decode(rawJson).getOrElse { error ->
            return Result.failure(error)
        }

        val now = System.currentTimeMillis()
        var insertedReminders = emptyList<PaymentReminder>()
        var insertedHistoryCount = 0

        when (mode) {
            ImportMode.REPLACE -> {
                val oldReminders = reminderDao.getAll().map { it.toDomain() }
                oldReminders.forEach { old ->
                    alarmScheduler.cancelReminder(old.id, old.remindDaysBefore)
                }

                database.withTransaction {
                    reminderDao.deleteAll()
                    historyDao.deleteAll()

                    val reminderEntities = decoded.reminders.map { reminder ->
                        reminder.copy(
                            id = 0L,
                            createdAtEpochMillis = if (reminder.createdAtEpochMillis > 0L) reminder.createdAtEpochMillis else now,
                            updatedAtEpochMillis = now
                        ).toEntity()
                    }
                    val newIds = reminderDao.insertAll(reminderEntities)
                    insertedReminders = reminderEntities.zip(newIds).map { (entity, id) ->
                        entity.copy(id = id).toDomain()
                    }

                    val historyEntities = decoded.history.map { it.copy(id = 0L).toEntity() }
                    historyDao.insertAll(historyEntities)
                    insertedHistoryCount = historyEntities.size
                }

                decoded.settings?.let { importedSettings ->
                    settingsRepository.applyBackupSettings(importedSettings)
                }
            }

            ImportMode.MERGE -> {
                database.withTransaction {
                    val reminderEntities = decoded.reminders.map { reminder ->
                        reminder.copy(
                            id = 0L,
                            createdAtEpochMillis = if (reminder.createdAtEpochMillis > 0L) reminder.createdAtEpochMillis else now,
                            updatedAtEpochMillis = now
                        ).toEntity()
                    }
                    val newIds = reminderDao.insertAll(reminderEntities)
                    insertedReminders = reminderEntities.zip(newIds).map { (entity, id) ->
                        entity.copy(id = id).toDomain()
                    }

                    val existingKeys = historyDao.getAll().mapTo(HashSet()) { existing ->
                        HistoryDedupKey(
                            title = existing.title,
                            amountMinor = existing.amountMinor,
                            currencyCode = existing.currencyCode,
                            dueDateEpochDay = existing.dueDateEpochDay,
                            paidAtEpochMillis = existing.paidAtEpochMillis
                        )
                    }
                    val newHistoryEntities = decoded.history
                        .map { it.copy(id = 0L).toEntity() }
                        .filter { candidate ->
                            existingKeys.add(
                                HistoryDedupKey(
                                    title = candidate.title,
                                    amountMinor = candidate.amountMinor,
                                    currencyCode = candidate.currencyCode,
                                    dueDateEpochDay = candidate.dueDateEpochDay,
                                    paidAtEpochMillis = candidate.paidAtEpochMillis
                                )
                            )
                        }
                    historyDao.insertAll(newHistoryEntities)
                    insertedHistoryCount = newHistoryEntities.size
                }
            }
        }

        insertedReminders.forEach { alarmScheduler.scheduleReminder(it) }

        return Result.success(
            ImportSummary(
                importedRemindersCount = insertedReminders.size,
                importedHistoryCount = insertedHistoryCount,
                importedSettings = if (mode == ImportMode.REPLACE) decoded.settings else null
            )
        )
    }

    private data class HistoryDedupKey(
        val title: String,
        val amountMinor: Long,
        val currencyCode: String,
        val dueDateEpochDay: Long,
        val paidAtEpochMillis: Long
    )
}
