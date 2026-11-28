package com.darkxwn.payping.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.darkxwn.payping.PayPingApp
import com.darkxwn.payping.data.repository.ImportSummary
import com.darkxwn.payping.data.repository.MarkDoneResult
import com.darkxwn.payping.domain.backup.ImportMode
import com.darkxwn.payping.domain.logic.GroupedReminders
import com.darkxwn.payping.domain.logic.ReminderPlanner
import com.darkxwn.payping.domain.logic.ReminderSummary
import com.darkxwn.payping.domain.model.AppFont
import com.darkxwn.payping.domain.model.AppLanguage
import com.darkxwn.payping.domain.model.AppSettings
import com.darkxwn.payping.domain.model.AppThemeMode
import com.darkxwn.payping.domain.model.BottomBarStyle
import com.darkxwn.payping.domain.model.LiquidGlassLevel
import com.darkxwn.payping.domain.model.PaymentCategory
import com.darkxwn.payping.domain.model.PaymentHistory
import com.darkxwn.payping.domain.model.PaymentReminder
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class MainUiState(
    val today: LocalDate = LocalDate.now(),
    val settings: AppSettings = AppSettings(),
    val reminders: List<PaymentReminder> = emptyList(),
    val groupedReminders: GroupedReminders = GroupedReminders(
        overdue = emptyList(),
        today = emptyList(),
        upcoming = emptyList(),
        later = emptyList()
    ),
    val summary: ReminderSummary = ReminderSummary(
        next7DaysTotals = emptyList(),
        endOfMonthTotals = emptyList(),
        overdueTotals = emptyList(),
        overdueCount = 0
    ),
    val history: List<PaymentHistory> = emptyList(),
    val customCategories: List<PaymentCategory> = emptyList()
)

sealed interface UiFeedbackEvent {
    data class MarkedDone(val result: MarkDoneResult) : UiFeedbackEvent
    data object ExportSuccess : UiFeedbackEvent
    data object ExportError : UiFeedbackEvent
    data class ImportSuccess(val summary: ImportSummary) : UiFeedbackEvent
    data object ImportError : UiFeedbackEvent
}

class PayPingViewModel(
    private val app: PayPingApp
) : ViewModel() {

    private val paymentRepository = app.container.paymentRepository
    private val settingsRepository = app.container.settingsRepository
    private val alarmScheduler = app.container.alarmScheduler

    private val todayFlow = MutableStateFlow(LocalDate.now())

    private val _events = MutableSharedFlow<UiFeedbackEvent>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()

    val uiState: StateFlow<MainUiState> = combine(
        todayFlow,
        settingsRepository.settings,
        paymentRepository.reminders,
        paymentRepository.history,
        paymentRepository.categories
    ) { today, settings, reminders, history, categories ->
        MainUiState(
            today = today,
            settings = settings,
            reminders = reminders,
            groupedReminders = ReminderPlanner.groupReminders(reminders, today),
            summary = ReminderPlanner.calculateSummary(
                reminders = reminders,
                today = today,
                defaultCurrencyCode = settings.defaultCurrencyCode
            ),
            history = history,
            customCategories = categories
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState()
    )

    fun refreshToday() {
        val current = LocalDate.now()
        if (todayFlow.value != current) {
            todayFlow.value = current
        }
    }

    fun canScheduleExactAlarms(): Boolean = alarmScheduler.canScheduleExactAlarms()

    fun markDone(reminderId: Long) {
        viewModelScope.launch {
            val result = paymentRepository.markReminderDone(reminderId)
            if (result != null) {
                _events.emit(UiFeedbackEvent.MarkedDone(result))
            }
        }
    }

    suspend fun loadReminder(id: Long): PaymentReminder? =
        paymentRepository.getReminder(id)

    fun saveReminder(reminder: PaymentReminder, onSaved: () -> Unit) {
        viewModelScope.launch {
            paymentRepository.upsertReminder(reminder)
            settingsRepository.recordRecentCurrency(reminder.currencyCode)
            onSaved()
        }
    }

    fun saveCategory(category: PaymentCategory) {
        viewModelScope.launch {
            paymentRepository.upsertCategory(category)
        }
    }

    fun deleteCategory(uid: String) {
        viewModelScope.launch {
            paymentRepository.deleteCategory(uid)
        }
    }

    fun deleteReminder(id: Long, onDeleted: () -> Unit) {
        viewModelScope.launch {
            paymentRepository.deleteReminder(id)
            onDeleted()
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            paymentRepository.deleteHistoryItem(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            paymentRepository.clearHistory()
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            settingsRepository.setLanguage(language)
            AppCompatDelegate.setApplicationLocales(language.toLocaleListCompat())
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun setPureBlack(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setPureBlack(enabled)
        }
    }

    fun selectAccentColor(argb: Long) {
        viewModelScope.launch {
            settingsRepository.selectAccentColor(argb)
        }
    }

    fun selectDynamicColor() {
        viewModelScope.launch {
            settingsRepository.selectDynamicColor()
        }
    }

    fun addCustomAccentColor(argb: Long) {
        viewModelScope.launch {
            settingsRepository.addCustomAccentColor(argb)
        }
    }

    fun removeCustomAccentColor(argb: Long) {
        viewModelScope.launch {
            settingsRepository.removeCustomAccentColor(argb)
        }
    }

    fun setAppFont(font: AppFont) {
        viewModelScope.launch {
            settingsRepository.setAppFont(font)
        }
    }

    fun setBottomBarStyle(style: BottomBarStyle) {
        viewModelScope.launch {
            settingsRepository.setBottomBarStyle(style)
        }
    }

    fun setLiquidGlassLevel(level: LiquidGlassLevel) {
        viewModelScope.launch {
            settingsRepository.setLiquidGlassLevel(level)
        }
    }

    fun setDefaultCurrency(currencyCode: String) {
        viewModelScope.launch {
            settingsRepository.setDefaultCurrency(currencyCode)
        }
    }

    suspend fun buildExportBackupJson(): String =
        paymentRepository.exportBackupJson()

    fun notifyExportSuccess() {
        viewModelScope.launch {
            _events.emit(UiFeedbackEvent.ExportSuccess)
        }
    }

    fun notifyExportError() {
        viewModelScope.launch {
            _events.emit(UiFeedbackEvent.ExportError)
        }
    }

    fun importBackupJson(rawJson: String, mode: ImportMode) {
        viewModelScope.launch {
            paymentRepository.importBackupJson(rawJson, mode)
                .onSuccess { summary ->
                    summary.importedSettings?.let { imported ->
                        AppCompatDelegate.setApplicationLocales(
                            imported.language.toLocaleListCompat()
                        )
                    }
                    _events.emit(UiFeedbackEvent.ImportSuccess(summary))
                }
                .onFailure {
                    _events.emit(UiFeedbackEvent.ImportError)
                }
        }
    }

    class Factory(private val app: PayPingApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PayPingViewModel(app) as T
        }
    }
}

private fun AppLanguage.toLocaleListCompat(): LocaleListCompat =
    tag?.let(LocaleListCompat::forLanguageTags) ?: LocaleListCompat.getEmptyLocaleList()
