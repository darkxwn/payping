package com.darkxwn.payping.domain.backup

import com.darkxwn.payping.domain.model.AppFont
import com.darkxwn.payping.domain.model.AppLanguage
import com.darkxwn.payping.domain.model.AppSettings
import com.darkxwn.payping.domain.model.AppThemeMode
import com.darkxwn.payping.domain.model.BaseAccentColor
import com.darkxwn.payping.domain.model.BottomBarStyle
import com.darkxwn.payping.domain.model.BuiltInCategory
import com.darkxwn.payping.domain.model.CurrencyCatalog
import com.darkxwn.payping.domain.model.CurrencyOption
import com.darkxwn.payping.domain.model.LiquidGlassLevel
import com.darkxwn.payping.domain.model.PaymentCategory
import com.darkxwn.payping.domain.model.PaymentHistory
import com.darkxwn.payping.domain.model.PaymentReminder
import com.darkxwn.payping.domain.model.PaymentType
import com.darkxwn.payping.domain.model.RecurrenceType
import com.darkxwn.payping.domain.model.ReminderConstants
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.util.UUID

enum class ImportMode {
    REPLACE,
    MERGE
}

@Serializable
data class BackupPayload(
    val schemaVersion: Int = BackupSerializer.CURRENT_SCHEMA_VERSION,
    val exportedAtEpochMillis: Long,
    val settings: BackupSettingsDto? = null,
    val categories: List<BackupCategoryDto> = emptyList(),
    val reminders: List<BackupReminderDto> = emptyList(),
    val history: List<BackupHistoryDto> = emptyList()
)

@Serializable
data class BackupSettingsDto(
    val language: String? = null,
    val themeMode: String? = null,
    val pureBlack: Boolean = false,
    val useDynamicColor: Boolean = false,
    val accentColorArgb: Long = BaseAccentColor.DEFAULT_ARGB,
    val customAccentColors: List<Long> = emptyList(),
    val appFont: String? = null,
    val useSystemFontScale: Boolean = true,
    val fontScalePercent: Int = 100,
    val bottomBarStyle: String? = null,
    val liquidGlassLevel: String? = null,
    val defaultCurrencyCode: String? = null,
    val recentCurrencies: List<String> = emptyList()
)

@Serializable
data class BackupCategoryDto(
    val uid: String,
    val name: String,
    val iconKey: String = "other",
    val colorArgb: Long = BaseAccentColor.DEFAULT_ARGB,
    val sortOrder: Int = 0,
    val createdAtEpochMillis: Long = 0L
)

@Serializable
data class BackupReminderDto(
    val uid: String? = null,
    val title: String,
    val amountMinor: Long,
    val currencyCode: String,
    val dueDate: String,
    val notifyHour: Int,
    val notifyMinute: Int,
    val recurrence: String,
    val remindDaysBefore: List<Int> = listOf(0),
    val note: String = "",
    val paymentType: String? = null,
    val categoryUid: String? = null,
    val badgeIconKey: String? = null,
    val badgeText: String? = null,
    val badgeColorArgb: Long = BaseAccentColor.DEFAULT_ARGB,
    val isArchived: Boolean = false,
    val isVariableAmount: Boolean = false,
    val serviceLink: String = "",
    val installmentPaid: Int? = null,
    val installmentTotal: Int? = null,
    val anchorDayOfMonth: Int? = null,
    val createdAtEpochMillis: Long = 0L,
    val updatedAtEpochMillis: Long = 0L
)

@Serializable
data class BackupHistoryDto(
    val uid: String? = null,
    val reminderId: Long? = null,
    val reminderUid: String? = null,
    val title: String,
    val amountMinor: Long,
    val currencyCode: String,
    val dueDate: String,
    val paidAtEpochMillis: Long,
    val note: String = "",
    val recurrence: String = RecurrenceType.ONCE.name,
    val paymentType: String? = null,
    val categoryUid: String? = null,
    val badgeIconKey: String? = null,
    val badgeText: String? = null,
    val badgeColorArgb: Long = BaseAccentColor.DEFAULT_ARGB,
    val previousDueDate: String? = null,
    val nextDueDateAfterPayment: String? = null,
    val rateToUsd: Double? = null
)

data class DecodedBackup(
    val settings: AppSettings?,
    val categories: List<PaymentCategory> = emptyList(),
    val reminders: List<PaymentReminder>,
    val history: List<PaymentHistory>
)

object BackupSerializer {
    const val CURRENT_SCHEMA_VERSION = 2

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(
        reminders: List<PaymentReminder>,
        history: List<PaymentHistory>,
        settings: AppSettings,
        categories: List<PaymentCategory> = emptyList(),
        exportedAtEpochMillis: Long = System.currentTimeMillis()
    ): String {
        val payload = BackupPayload(
            schemaVersion = CURRENT_SCHEMA_VERSION,
            exportedAtEpochMillis = exportedAtEpochMillis,
            settings = BackupSettingsDto(
                language = settings.language.name,
                themeMode = settings.themeMode.name,
                pureBlack = settings.pureBlack,
                useDynamicColor = settings.useDynamicColor,
                accentColorArgb = BaseAccentColor.normalizeOpaqueArgb(settings.accentColorArgb),
                customAccentColors = settings.customAccentColors.map(BaseAccentColor::normalizeOpaqueArgb),
                appFont = settings.appFont.name,
                useSystemFontScale = settings.useSystemFontScale,
                fontScalePercent = settings.fontScalePercent.coerceIn(80, 140),
                bottomBarStyle = settings.bottomBarStyle.name,
                liquidGlassLevel = settings.liquidGlassLevel.name,
                defaultCurrencyCode = settings.defaultCurrencyCode,
                recentCurrencies = settings.recentCurrencies
                    .filter(CurrencyCatalog::isSupported)
                    .distinct()
                    .take(3)
            ),
            categories = categories.filterNot { it.isBuiltIn }.map { category ->
                BackupCategoryDto(
                    uid = category.uid,
                    name = category.name.trim(),
                    iconKey = category.iconKey,
                    colorArgb = BaseAccentColor.normalizeOpaqueArgb(category.colorArgb),
                    sortOrder = category.sortOrder,
                    createdAtEpochMillis = category.createdAtEpochMillis
                )
            },
            reminders = reminders.map { reminder ->
                BackupReminderDto(
                    uid = reminder.uid,
                    title = reminder.title,
                    amountMinor = reminder.amountMinor,
                    currencyCode = reminder.currencyCode,
                    dueDate = reminder.dueDate.toString(),
                    notifyHour = reminder.notifyHour,
                    notifyMinute = reminder.notifyMinute,
                    recurrence = reminder.recurrence.name,
                    remindDaysBefore = reminder.remindDaysBefore,
                    note = reminder.note,
                    paymentType = reminder.paymentType.name,
                    categoryUid = reminder.categoryUid,
                    badgeIconKey = reminder.badgeIconKey,
                    badgeText = reminder.badgeText,
                    badgeColorArgb = BaseAccentColor.normalizeOpaqueArgb(reminder.badgeColorArgb),
                    isArchived = reminder.isArchived,
                    isVariableAmount = reminder.isVariableAmount,
                    serviceLink = reminder.serviceLink,
                    installmentPaid = reminder.installmentPaid,
                    installmentTotal = reminder.installmentTotal,
                    anchorDayOfMonth = reminder.anchorDayOfMonth,
                    createdAtEpochMillis = reminder.createdAtEpochMillis,
                    updatedAtEpochMillis = reminder.updatedAtEpochMillis
                )
            },
            history = history.map { item ->
                BackupHistoryDto(
                    uid = item.uid,
                    reminderId = item.reminderId,
                    reminderUid = item.reminderUid,
                    title = item.title,
                    amountMinor = item.amountMinor,
                    currencyCode = item.currencyCode,
                    dueDate = item.dueDate.toString(),
                    paidAtEpochMillis = item.paidAtEpochMillis,
                    note = item.note,
                    recurrence = item.recurrence.name,
                    paymentType = item.paymentType.name,
                    categoryUid = item.categoryUid,
                    badgeIconKey = item.badgeIconKey,
                    badgeText = item.badgeText,
                    badgeColorArgb = BaseAccentColor.normalizeOpaqueArgb(item.badgeColorArgb),
                    previousDueDate = item.previousDueDate?.toString(),
                    nextDueDateAfterPayment = item.nextDueDateAfterPayment?.toString(),
                    rateToUsd = item.rateToUsd
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
                language = enumValueOrNull<AppLanguage>(dto.language) ?: AppLanguage.SYSTEM,
                themeMode = enumValueOrNull<AppThemeMode>(dto.themeMode) ?: AppThemeMode.SYSTEM,
                pureBlack = dto.pureBlack,
                useDynamicColor = dto.useDynamicColor,
                accentColorArgb = BaseAccentColor.normalizeOpaqueArgb(dto.accentColorArgb),
                customAccentColors = dto.customAccentColors
                    .map(BaseAccentColor::normalizeOpaqueArgb)
                    .distinct(),
                appFont = enumValueOrNull<AppFont>(dto.appFont) ?: AppFont.SYSTEM,
                useSystemFontScale = dto.useSystemFontScale,
                fontScalePercent = dto.fontScalePercent.coerceIn(80, 140),
                bottomBarStyle = enumValueOrNull<BottomBarStyle>(dto.bottomBarStyle) ?: BottomBarStyle.PINNED,
                liquidGlassLevel = enumValueOrNull<LiquidGlassLevel>(dto.liquidGlassLevel) ?: LiquidGlassLevel.NONE,
                defaultCurrencyCode = CurrencyOption.fromCode(dto.defaultCurrencyCode).code,
                recentCurrencies = dto.recentCurrencies
                    .map { CurrencyOption.fromCode(it).code }
                    .distinct()
                    .take(3)
            )
        }

        val parsedCategories = payload.categories.mapNotNull { dto ->
            val trimmedName = dto.name.trim()
            val trimmedUid = dto.uid.trim()
            if (trimmedUid.isEmpty() || trimmedName.isEmpty()) {
                null
            } else {
                PaymentCategory(
                    uid = trimmedUid,
                    name = trimmedName,
                    iconKey = dto.iconKey.ifBlank { "other" },
                    colorArgb = BaseAccentColor.normalizeOpaqueArgb(dto.colorArgb),
                    isBuiltIn = false,
                    sortOrder = dto.sortOrder,
                    createdAtEpochMillis = dto.createdAtEpochMillis
                )
            }
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
                uid = dto.uid?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
                title = trimmedTitle,
                amountMinor = dto.amountMinor,
                currencyCode = CurrencyOption.fromCode(dto.currencyCode).code,
                dueDate = dueDate,
                notifyHour = dto.notifyHour.coerceIn(0, 23),
                notifyMinute = dto.notifyMinute.coerceIn(0, 59),
                recurrence = recurrence,
                remindDaysBefore = remindDays,
                note = dto.note.trim(),
                paymentType = PaymentType.fromRaw(dto.paymentType),
                categoryUid = dto.categoryUid?.takeIf { it.isNotBlank() } ?: BuiltInCategory.OTHER.uid,
                badgeIconKey = dto.badgeIconKey?.takeIf { it.isNotBlank() },
                badgeText = dto.badgeText?.trim()?.takeIf { it.isNotBlank() },
                badgeColorArgb = BaseAccentColor.normalizeOpaqueArgb(dto.badgeColorArgb),
                isArchived = dto.isArchived,
                isVariableAmount = dto.isVariableAmount,
                serviceLink = dto.serviceLink.trim(),
                installmentPaid = dto.installmentPaid,
                installmentTotal = dto.installmentTotal,
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
                uid = dto.uid?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
                reminderId = dto.reminderId,
                reminderUid = dto.reminderUid?.takeIf { it.isNotBlank() },
                title = trimmedTitle,
                amountMinor = dto.amountMinor,
                currencyCode = CurrencyOption.fromCode(dto.currencyCode).code,
                dueDate = dueDate,
                paidAtEpochMillis = dto.paidAtEpochMillis,
                note = dto.note.trim(),
                recurrence = recurrence,
                paymentType = PaymentType.fromRaw(dto.paymentType),
                categoryUid = dto.categoryUid?.takeIf { it.isNotBlank() } ?: BuiltInCategory.OTHER.uid,
                badgeIconKey = dto.badgeIconKey?.takeIf { it.isNotBlank() },
                badgeText = dto.badgeText?.trim()?.takeIf { it.isNotBlank() },
                badgeColorArgb = BaseAccentColor.normalizeOpaqueArgb(dto.badgeColorArgb),
                previousDueDate = dto.previousDueDate?.takeIf { it.isNotBlank() }?.let(LocalDate::parse),
                nextDueDateAfterPayment = dto.nextDueDateAfterPayment?.takeIf { it.isNotBlank() }?.let(LocalDate::parse),
                rateToUsd = dto.rateToUsd
            )
        }

        DecodedBackup(
            settings = parsedSettings,
            categories = parsedCategories,
            reminders = parsedReminders,
            history = parsedHistory
        )
    }

    private inline fun <reified T : Enum<T>> enumValueOrNull(raw: String?): T? {
        if (raw.isNullOrBlank()) return null
        return enumValues<T>().firstOrNull { it.name.equals(raw.trim(), ignoreCase = true) }
    }
}
