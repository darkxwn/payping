package com.darkxwn.payping.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.darkxwn.payping.R
import com.darkxwn.payping.domain.logic.MoneyFormat
import com.darkxwn.payping.domain.model.AppLanguage
import com.darkxwn.payping.domain.model.BuiltInCategory
import com.darkxwn.payping.domain.model.CategoryCatalog
import com.darkxwn.payping.domain.model.CurrencyOption
import com.darkxwn.payping.domain.model.LiquidGlassLevel
import com.darkxwn.payping.domain.model.PaymentCategory
import com.darkxwn.payping.domain.model.PaymentReminder
import com.darkxwn.payping.domain.model.PaymentType
import com.darkxwn.payping.domain.model.RecurrenceType
import com.darkxwn.payping.domain.model.ReminderConstants
import com.darkxwn.payping.ui.components.CategoryBadgeAvatar
import com.darkxwn.payping.ui.components.CategoryPickerSheet
import com.darkxwn.payping.ui.components.CircularBackButton
import com.darkxwn.payping.ui.components.CurrencyPickerSheet
import com.darkxwn.payping.ui.components.PaymentBadgeAvatar
import com.darkxwn.payping.ui.components.ServiceBadgePickerSheet
import com.darkxwn.payping.ui.components.formatLocalDate
import com.darkxwn.payping.ui.components.paymentTypeLabel
import com.darkxwn.payping.ui.components.recurrenceLabel
import com.darkxwn.payping.ui.components.remindDayFullLabel
import com.darkxwn.payping.ui.components.resolveSystemLanguageTag
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReminderEditScreen(
    reminderId: Long,
    defaultCurrencyCode: String,
    language: AppLanguage,
    loadReminder: suspend (Long) -> PaymentReminder?,
    onSave: (PaymentReminder) -> Unit,
    onDelete: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    liquidGlassLevel: LiquidGlassLevel = LiquidGlassLevel.NONE,
    customCategories: List<PaymentCategory> = emptyList(),
    recentCurrencyCodes: List<String> = emptyList(),
    onSaveCategory: (PaymentCategory) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isEditMode = reminderId > 0L
    val langTag = remember(language) {
        language.resolvedLocaleTag(resolveSystemLanguageTag())
    }

    var loaded by rememberSaveable(reminderId) { mutableStateOf(!isEditMode) }
    var reminderUid by rememberSaveable(reminderId) { mutableStateOf(UUID.randomUUID().toString()) }
    var title by rememberSaveable(reminderId) { mutableStateOf("") }
    var amountInput by rememberSaveable(reminderId) { mutableStateOf("") }
    var currencyCode by rememberSaveable(reminderId) { mutableStateOf(defaultCurrencyCode) }
    var paymentTypeName by rememberSaveable(reminderId) { mutableStateOf(PaymentType.MANUAL.name) }
    var categoryUid by rememberSaveable(reminderId) { mutableStateOf(BuiltInCategory.OTHER.uid) }
    var badgeIconKey by rememberSaveable(reminderId) { mutableStateOf<String?>(null) }
    var badgeText by rememberSaveable(reminderId) { mutableStateOf<String?>(null) }
    var badgeColorArgb by rememberSaveable(reminderId) { mutableLongStateOf(BuiltInCategory.OTHER.colorArgb) }
    var hasCustomBadgeColor by rememberSaveable(reminderId) { mutableStateOf(false) }
    var isArchived by rememberSaveable(reminderId) { mutableStateOf(false) }
    var isVariableAmount by rememberSaveable(reminderId) { mutableStateOf(false) }
    var serviceLink by rememberSaveable(reminderId) { mutableStateOf("") }
    var dueDateEpochDay by rememberSaveable(reminderId) { mutableStateOf(LocalDate.now().toEpochDay()) }
    var notifyHour by rememberSaveable(reminderId) { mutableIntStateOf(ReminderConstants.DEFAULT_NOTIFY_HOUR) }
    var notifyMinute by rememberSaveable(reminderId) { mutableIntStateOf(ReminderConstants.DEFAULT_NOTIFY_MINUTE) }
    var recurrenceName by rememberSaveable(reminderId) { mutableStateOf(RecurrenceType.MONTHLY.name) }
    var selectedDaysCsv by rememberSaveable(reminderId) {
        mutableStateOf(ReminderConstants.DEFAULT_REMIND_DAYS.joinToString(","))
    }
    var note by rememberSaveable(reminderId) { mutableStateOf("") }
    var createdAtMillis by rememberSaveable(reminderId) { mutableStateOf(0L) }

    var showTitleError by remember { mutableStateOf(false) }
    var showAmountError by remember { mutableStateOf(false) }
    var showDaysError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showCurrencyPicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showBadgePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(reminderId) {
        if (isEditMode && !loaded) {
            val existing = loadReminder(reminderId)
            if (existing != null) {
                reminderUid = existing.uid
                title = existing.title
                currencyCode = existing.currencyCode
                amountInput = MoneyFormat.minorToEditableInput(existing.amountMinor, existing.currencyCode)
                paymentTypeName = existing.paymentType.name
                categoryUid = existing.categoryUid
                badgeIconKey = existing.badgeIconKey
                badgeText = existing.badgeText
                badgeColorArgb = existing.badgeColorArgb
                hasCustomBadgeColor = true
                isArchived = existing.isArchived
                isVariableAmount = existing.isVariableAmount
                serviceLink = existing.serviceLink
                dueDateEpochDay = existing.dueDate.toEpochDay()
                notifyHour = existing.notifyHour
                notifyMinute = existing.notifyMinute
                recurrenceName = existing.recurrence.name
                selectedDaysCsv = existing.remindDaysBefore.joinToString(",")
                note = existing.note
                createdAtMillis = existing.createdAtEpochMillis
            }
            loaded = true
        }
    }

    val dueDate = LocalDate.ofEpochDay(dueDateEpochDay)
    val recurrence = RecurrenceType.valueOf(recurrenceName)
    val paymentType = PaymentType.fromRaw(paymentTypeName)
    val resolvedCategory = remember(categoryUid, customCategories, langTag) {
        CategoryCatalog.resolve(categoryUid, customCategories, langTag)
    }
    val selectedDays = remember(selectedDaysCsv) {
        selectedDaysCsv.split(',')
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it >= 0 }
            .distinct()
            .sorted()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            if (isEditMode) R.string.editor_title_edit else R.string.editor_title_new
                        ),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    CircularBackButton(
                        liquidGlassLevel = liquidGlassLevel,
                        onClick = onNavigateBack,
                        contentDescription = stringResource(R.string.action_cancel),
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = stringResource(R.string.action_delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clickable(role = Role.Button) { showBadgePicker = true }
                ) {
                    PaymentBadgeAvatar(
                        title = title,
                        badgeIconKey = badgeIconKey,
                        badgeText = badgeText,
                        badgeColorArgb = badgeColorArgb,
                        size = 52.dp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = stringResource(R.string.badge_picker_title),
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (showTitleError) showTitleError = false
                    },
                    label = { Text(stringResource(R.string.field_title_label)) },
                    placeholder = { Text(stringResource(R.string.field_title_placeholder)) },
                    isError = showTitleError,
                    supportingText = if (showTitleError) {
                        { Text(stringResource(R.string.error_title_required)) }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = amountInput,
                onValueChange = {
                    amountInput = it
                    if (showAmountError) showAmountError = false
                },
                label = { Text(stringResource(R.string.field_amount_label)) },
                placeholder = { Text(stringResource(R.string.field_amount_placeholder)) },
                trailingIcon = {
                    Surface(
                        onClick = { showCurrencyPicker = true },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = CurrencyOption.symbolFor(currencyCode),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = stringResource(R.string.currency_picker_title),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                isError = showAmountError,
                supportingText = if (showAmountError) {
                    { Text(stringResource(R.string.error_amount_invalid)) }
                } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.field_payment_type_label),
                    style = MaterialTheme.typography.labelLarge
                )
                val typeOptions = PaymentType.STAGE_1_SELECTABLE
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    typeOptions.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = paymentType == option,
                            onClick = { paymentTypeName = option.name },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = typeOptions.size
                            ),
                            icon = {}
                        ) {
                            Text(
                                text = paymentTypeLabel(option),
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            OutlinedCard(
                onClick = { showCategoryPicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryBadgeAvatar(
                        iconKey = resolvedCategory.iconKey,
                        colorArgb = resolvedCategory.colorArgb,
                        size = 38.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.field_category_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = resolvedCategory.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedCard(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = stringResource(R.string.field_due_date_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Event,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formatLocalDate(dueDate, language),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                OutlinedCard(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = stringResource(R.string.field_notify_time_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = MoneyFormat.formatTime(notifyHour, notifyMinute),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.field_recurrence_label),
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RecurrenceType.entries.forEach { option ->
                        FilterChip(
                            selected = recurrence == option,
                            onClick = { recurrenceName = option.name },
                            label = { Text(recurrenceLabel(option)) }
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.field_remind_before_label),
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ReminderConstants.SUPPORTED_REMIND_DAYS.forEach { days ->
                        val isSelected = days in selectedDays
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val updatedDays = if (isSelected) {
                                    selectedDays - days
                                } else {
                                    (selectedDays + days).sorted()
                                }
                                selectedDaysCsv = updatedDays.joinToString(",")
                                if (showDaysError && updatedDays.isNotEmpty()) {
                                    showDaysError = false
                                }
                            },
                            label = { Text(remindDayFullLabel(days)) }
                        )
                    }
                }
                if (showDaysError) {
                    Text(
                        text = stringResource(R.string.error_remind_days_required),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(R.string.field_note_label)) },
                placeholder = { Text(stringResource(R.string.field_note_placeholder)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    val trimmedTitle = title.trim()
                    val parsedMinor = MoneyFormat.parseAmountToMinor(amountInput, currencyCode)
                    val validDays = selectedDays.isNotEmpty()

                    showTitleError = trimmedTitle.isEmpty()
                    showAmountError = parsedMinor == null
                    showDaysError = !validDays

                    if (trimmedTitle.isNotEmpty() && parsedMinor != null && validDays) {
                        onSave(
                            PaymentReminder(
                                id = if (isEditMode) reminderId else 0L,
                                uid = reminderUid,
                                title = trimmedTitle,
                                amountMinor = parsedMinor,
                                currencyCode = currencyCode,
                                dueDate = dueDate,
                                notifyHour = notifyHour,
                                notifyMinute = notifyMinute,
                                recurrence = recurrence,
                                remindDaysBefore = selectedDays.sorted(),
                                note = note.trim(),
                                paymentType = paymentType,
                                categoryUid = categoryUid,
                                badgeIconKey = badgeIconKey,
                                badgeText = badgeText,
                                badgeColorArgb = badgeColorArgb,
                                isArchived = isArchived,
                                isVariableAmount = isVariableAmount,
                                serviceLink = serviceLink,
                                anchorDayOfMonth = dueDate.dayOfMonth,
                                createdAtEpochMillis = createdAtMillis
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = stringResource(R.string.action_save),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            dueDateEpochDay = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                                .toEpochDay()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.action_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = notifyHour,
            initialMinute = notifyMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(stringResource(R.string.dialog_select_time_title)) },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        notifyHour = timePickerState.hour
                        notifyMinute = timePickerState.minute
                        showTimePicker = false
                    }
                ) {
                    Text(stringResource(R.string.action_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.dialog_delete_reminder_title)) },
            text = { Text(stringResource(R.string.dialog_delete_reminder_body, title.ifBlank { "" })) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(reminderId)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (showCurrencyPicker) {
        CurrencyPickerSheet(
            selectedCurrencyCode = currencyCode,
            defaultCurrencyCode = defaultCurrencyCode,
            recentCurrencyCodes = recentCurrencyCodes,
            language = language,
            onSelectCurrency = { chosen ->
                currencyCode = chosen
                if (showAmountError && MoneyFormat.parseAmountToMinor(amountInput, chosen) != null) {
                    showAmountError = false
                }
            },
            onDismiss = { showCurrencyPicker = false }
        )
    }

    if (showCategoryPicker) {
        CategoryPickerSheet(
            selectedCategoryUid = categoryUid,
            customCategories = customCategories,
            language = language,
            onSelectCategory = { picked ->
                categoryUid = picked.uid
                if (!hasCustomBadgeColor && badgeIconKey == null) {
                    badgeColorArgb = picked.colorArgb
                }
            },
            onSaveNewCategory = onSaveCategory,
            onDismiss = { showCategoryPicker = false }
        )
    }

    if (showBadgePicker) {
        ServiceBadgePickerSheet(
            titleForPreview = title,
            initialIconKey = badgeIconKey,
            initialText = badgeText,
            initialColorArgb = badgeColorArgb,
            language = language,
            onApply = { newIconKey, newText, newColorArgb ->
                badgeIconKey = newIconKey
                badgeText = newText
                badgeColorArgb = newColorArgb
                hasCustomBadgeColor = true
            },
            onDismiss = { showBadgePicker = false }
        )
    }
}
