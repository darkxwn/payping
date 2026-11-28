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
import com.darkxwn.payping.domain.model.BuiltInCategory
import com.darkxwn.payping.domain.model.CategoryCatalog
import com.darkxwn.payping.domain.model.PaymentCategory
import com.darkxwn.payping.domain.model.PaymentHistory
import com.darkxwn.payping.domain.model.PaymentReminder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID

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
    private val categoryDao = database.categoryDao()

    val reminders: Flow<List<PaymentReminder>> = reminderDao.observeAll().map { entities ->
        entities.map { it.toDomain() }
    }

    val history: Flow<List<PaymentHistory>> = historyDao.observeAll().map { entities ->
        entities.map { it.toDomain() }
    }

    val categories: Flow<List<PaymentCategory>> = categoryDao.observeAll().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun getReminder(id: Long): PaymentReminder? =
        reminderDao.getById(id)?.toDomain()

    suspend fun upsertCategory(category: PaymentCategory) {
        if (BuiltInCategory.entries.any { it.uid == category.uid }) return
        val trimmedName = category.name.trim()
        if (trimmedName.isEmpty()) return
        val now = System.currentTimeMillis()
        val existing = if (category.uid.isNotBlank()) categoryDao.getByUid(category.uid) else null
        val sanitized = category.copy(
            uid = category.uid.ifBlank { CategoryCatalog.newCustomUid() },
            name = trimmedName,
            isBuiltIn = false,
            sortOrder = existing?.sortOrder ?: if (category.sortOrder > 0) category.sortOrder else 0,
            createdAtEpochMillis = existing?.createdAtEpochMillis
                ?: category.createdAtEpochMillis.takeIf { it > 0L }
                ?: now
        )
        categoryDao.insert(sanitized.toEntity())
    }

    suspend fun deleteCategory(uid: String) {
        if (BuiltInCategory.entries.any { it.uid == uid }) return
        database.withTransaction {
            reminderDao.reassignCategory(
                oldCategoryUid = uid,
                targetCategoryUid = BuiltInCategory.OTHER.uid
            )
            historyDao.reassignCategory(
                oldCategoryUid = uid,
                targetCategoryUid = BuiltInCategory.OTHER.uid
            )
            categoryDao.deleteByUid(uid)
        }
    }

    suspend fun upsertReminder(reminder: PaymentReminder): Long {
        val now = System.currentTimeMillis()
        return if (reminder.id <= 0L) {
            val entity = reminder.copy(
                uid = reminder.uid.ifBlank { UUID.randomUUID().toString() },
                createdAtEpochMillis = if (reminder.createdAtEpochMillis > 0L) reminder.createdAtEpochMillis else now,
                updatedAtEpochMillis = now
            ).toEntity()
            val newId = reminderDao.insert(entity)
            val saved = entity.copy(id = newId).toDomain()
            if (!saved.isArchived) {
                alarmScheduler.scheduleReminder(saved)
            }
            newId
        } else {
            val previous = reminderDao.getById(reminder.id)?.toDomain()
            if (previous != null) {
                alarmScheduler.cancelReminder(previous.id, previous.remindDaysBefore)
            }
            val updated = reminder.copy(
                uid = previous?.uid ?: reminder.uid.ifBlank { UUID.randomUUID().toString() },
                createdAtEpochMillis = previous?.createdAtEpochMillis ?: reminder.createdAtEpochMillis.takeIf { it > 0L } ?: now,
                updatedAtEpochMillis = now
            )
            reminderDao.update(updated.toEntity())
            if (!updated.isArchived) {
                alarmScheduler.scheduleReminder(updated)
            }
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

            nextDueDate = RecurrenceCalculator.nextDueDate(
                currentDueDate = domain.dueDate,
                recurrence = domain.recurrence,
                anchorDayOfMonth = domain.anchorDayOfMonth
            )

            historyDao.insert(
                PaymentHistoryEntity(
                    uid = UUID.randomUUID().toString(),
                    reminderId = domain.id,
                    reminderUid = domain.uid,
                    title = domain.title,
                    amountMinor = domain.amountMinor,
                    currencyCode = domain.currencyCode,
                    dueDateEpochDay = domain.dueDate.toEpochDay(),
                    paidAtEpochMillis = now,
                    note = domain.note,
                    recurrence = domain.recurrence.name,
                    paymentType = domain.paymentType.name,
                    categoryUid = domain.categoryUid,
                    badgeIconKey = domain.badgeIconKey,
                    badgeText = domain.badgeText,
                    badgeColorArgb = domain.badgeColorArgb,
                    previousDueDateEpochDay = domain.dueDate.toEpochDay(),
                    nextDueDateAfterPaymentEpochDay = nextDueDate?.toEpochDay()
                )
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
            if (!reminder.isArchived) {
                alarmScheduler.scheduleReminder(reminder)
            }
        }
    }

    suspend fun exportBackupJson(): String {
        val currentReminders = reminderDao.getAllIncludingArchived().map { it.toDomain() }
        val currentHistory = historyDao.getAll().map { it.toDomain() }
        val currentCategories = categoryDao.getAll().map { it.toDomain() }
        val currentSettings = settingsRepository.getCurrentSettings()
        return BackupSerializer.encode(
            reminders = currentReminders,
            history = currentHistory,
            settings = currentSettings,
            categories = currentCategories
        )
    }

    suspend fun importBackupJson(rawJson: String, mode: ImportMode): Result<ImportSummary> {
        val decoded = BackupSerializer.decode(rawJson).getOrElse { error ->
            return Result.failure(error)
        }

        val now = System.currentTimeMillis()
        var insertedReminders = emptyList<PaymentReminder>()
        var insertedHistoryCount = 0
        val builtInUids = BuiltInCategory.entries.mapTo(HashSet()) { it.uid }

        when (mode) {
            ImportMode.REPLACE -> {
                val oldReminders = reminderDao.getAllIncludingArchived().map { it.toDomain() }
                oldReminders.forEach { old ->
                    alarmScheduler.cancelReminder(old.id, old.remindDaysBefore)
                }

                database.withTransaction {
                    categoryDao.deleteAll()
                    reminderDao.deleteAll()
                    historyDao.deleteAll()

                    val customCategories = decoded.categories
                        .filterNot { it.uid in builtInUids }
                        .map { it.toEntity() }
                    categoryDao.insertAll(customCategories)

                    val validCategoryUids = builtInUids + customCategories.map { it.uid }

                    val reminderEntities = decoded.reminders.map { reminder ->
                        reminder.copy(
                            id = 0L,
                            uid = reminder.uid.ifBlank { UUID.randomUUID().toString() },
                            categoryUid = reminder.categoryUid.takeIf { it in validCategoryUids } ?: BuiltInCategory.OTHER.uid,
                            createdAtEpochMillis = if (reminder.createdAtEpochMillis > 0L) reminder.createdAtEpochMillis else now,
                            updatedAtEpochMillis = now
                        ).toEntity()
                    }
                    val newIds = reminderDao.insertAll(reminderEntities)
                    insertedReminders = reminderEntities.zip(newIds).map { (entity, id) ->
                        entity.copy(id = id).toDomain()
                    }

                    val uidToNewId = insertedReminders.associate { it.uid to it.id }

                    val historyEntities = decoded.history.map { item ->
                        item.copy(
                            id = 0L,
                            uid = item.uid.ifBlank { UUID.randomUUID().toString() },
                            reminderId = item.reminderUid?.let(uidToNewId::get) ?: item.reminderId,
                            categoryUid = item.categoryUid.takeIf { it in validCategoryUids } ?: BuiltInCategory.OTHER.uid
                        ).toEntity()
                    }
                    historyDao.insertAll(historyEntities)
                    insertedHistoryCount = historyEntities.size
                }

                decoded.settings?.let { importedSettings ->
                    settingsRepository.applyBackupSettings(importedSettings)
                }
            }

            ImportMode.MERGE -> {
                database.withTransaction {
                    val existingCategories = categoryDao.getAll().mapTo(HashSet()) { it.uid }
                    val newCategories = decoded.categories
                        .filter { it.uid !in builtInUids && it.uid !in existingCategories }
                        .map { it.toEntity() }
                    categoryDao.insertAll(newCategories)

                    val validCategoryUids = builtInUids + existingCategories + newCategories.map { it.uid }

                    val existingUidToId = reminderDao.getAllIncludingArchived()
                        .associate { it.uid to it.id }
                        .toMutableMap()

                    val reminderEntities = decoded.reminders.map { reminder ->
                        val resolvedUid = reminder.uid.takeIf { it.isNotBlank() && !existingUidToId.containsKey(it) }
                            ?: UUID.randomUUID().toString()
                        reminder.copy(
                            id = 0L,
                            uid = resolvedUid,
                            categoryUid = reminder.categoryUid.takeIf { it in validCategoryUids } ?: BuiltInCategory.OTHER.uid,
                            createdAtEpochMillis = if (reminder.createdAtEpochMillis > 0L) reminder.createdAtEpochMillis else now,
                            updatedAtEpochMillis = now
                        ).toEntity()
                    }
                    val newIds = reminderDao.insertAll(reminderEntities)
                    insertedReminders = reminderEntities.zip(newIds).map { (entity, id) ->
                        entity.copy(id = id).toDomain()
                    }
                    insertedReminders.forEach { existingUidToId[it.uid] = it.id }

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
                        .map { item ->
                            item.copy(
                                id = 0L,
                                uid = item.uid.ifBlank { UUID.randomUUID().toString() },
                                reminderId = item.reminderUid?.let(existingUidToId::get) ?: item.reminderId,
                                categoryUid = item.categoryUid.takeIf { it in validCategoryUids } ?: BuiltInCategory.OTHER.uid
                            ).toEntity()
                        }
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

        insertedReminders.filterNot { it.isArchived }.forEach { alarmScheduler.scheduleReminder(it) }

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
