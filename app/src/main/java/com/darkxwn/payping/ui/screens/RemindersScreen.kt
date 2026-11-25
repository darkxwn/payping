package com.darkxwn.payping.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.EventRepeat
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.darkxwn.payping.R
import com.darkxwn.payping.domain.logic.CurrencyTotal
import com.darkxwn.payping.domain.logic.MoneyFormat
import com.darkxwn.payping.domain.logic.ReminderSummary
import com.darkxwn.payping.domain.model.AppLanguage
import com.darkxwn.payping.domain.model.PaymentReminder
import com.darkxwn.payping.domain.model.RecurrenceType
import com.darkxwn.payping.ui.MainUiState
import com.darkxwn.payping.ui.components.formatLocalDate
import com.darkxwn.payping.ui.components.recurrenceLabel
import com.darkxwn.payping.ui.components.remindDayChipLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    uiState: MainUiState,
    hasNotificationPermission: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    onMarkDoneClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.action_add_reminder)
                )
            }
        }
    ) { innerPadding ->
        val grouped = uiState.groupedReminders
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!hasNotificationPermission) {
                item(key = "permission_banner") {
                    NotificationPermissionBanner(
                        onAllowClick = onRequestNotificationPermission
                    )
                }
            }

            item(key = "summary_card") {
                SummaryOverviewCard(
                    summary = uiState.summary,
                    defaultCurrencyCode = uiState.settings.defaultCurrencyCode
                )
            }

            if (grouped.isEmpty) {
                item(key = "empty_state") {
                    EmptyRemindersState(onAddClick = onAddClick)
                }
            } else {
                if (grouped.overdue.isNotEmpty()) {
                    item(key = "header_overdue") {
                        BucketSectionHeader(
                            title = stringResource(R.string.bucket_overdue),
                            count = grouped.overdue.size,
                            accentColor = MaterialTheme.colorScheme.error
                        )
                    }
                    items(grouped.overdue, key = { "overdue_${it.id}" }) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            language = uiState.settings.language,
                            isOverdue = true,
                            isToday = false,
                            onCardClick = { onEditClick(reminder.id) },
                            onDoneClick = { onMarkDoneClick(reminder.id) }
                        )
                    }
                }

                if (grouped.today.isNotEmpty()) {
                    item(key = "header_today") {
                        BucketSectionHeader(
                            title = stringResource(R.string.bucket_today),
                            count = grouped.today.size,
                            accentColor = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(grouped.today, key = { "today_${it.id}" }) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            language = uiState.settings.language,
                            isOverdue = false,
                            isToday = true,
                            onCardClick = { onEditClick(reminder.id) },
                            onDoneClick = { onMarkDoneClick(reminder.id) }
                        )
                    }
                }

                if (grouped.upcoming.isNotEmpty()) {
                    item(key = "header_upcoming") {
                        BucketSectionHeader(
                            title = stringResource(R.string.bucket_upcoming),
                            count = grouped.upcoming.size,
                            accentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(grouped.upcoming, key = { "upcoming_${it.id}" }) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            language = uiState.settings.language,
                            isOverdue = false,
                            isToday = false,
                            onCardClick = { onEditClick(reminder.id) },
                            onDoneClick = { onMarkDoneClick(reminder.id) }
                        )
                    }
                }

                if (grouped.later.isNotEmpty()) {
                    item(key = "header_later") {
                        BucketSectionHeader(
                            title = stringResource(R.string.bucket_later),
                            count = grouped.later.size,
                            accentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(grouped.later, key = { "later_${it.id}" }) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            language = uiState.settings.language,
                            isOverdue = false,
                            isToday = false,
                            onCardClick = { onEditClick(reminder.id) },
                            onDoneClick = { onMarkDoneClick(reminder.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationPermissionBanner(
    onAllowClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.NotificationsOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.banner_notifications_disabled_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = stringResource(R.string.banner_notifications_disabled_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            TextButton(onClick = onAllowClick) {
                Text(
                    text = stringResource(R.string.action_allow),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SummaryOverviewCard(
    summary: ReminderSummary,
    defaultCurrencyCode: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SummaryColumn(
                    label = stringResource(R.string.summary_next_7_days),
                    totals = summary.next7DaysTotals,
                    defaultCurrencyCode = defaultCurrencyCode,
                    modifier = Modifier.weight(1f)
                )
                SummaryColumn(
                    label = stringResource(R.string.summary_end_of_month),
                    totals = summary.endOfMonthTotals,
                    defaultCurrencyCode = defaultCurrencyCode,
                    modifier = Modifier.weight(1f)
                )
            }

            if (summary.overdueCount > 0) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                )
                Text(
                    text = stringResource(R.string.summary_includes_overdue, summary.overdueCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SummaryColumn(
    label: String,
    totals: List<CurrencyTotal>,
    defaultCurrencyCode: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (totals.isEmpty()) {
            Text(
                text = MoneyFormat.formatMinorAmount(0L, defaultCurrencyCode),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        } else {
            totals.forEachIndexed { index, item ->
                Text(
                    text = MoneyFormat.formatMinorAmount(item.totalMinor, item.currencyCode),
                    style = if (index == 0) {
                        MaterialTheme.typography.titleLarge
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun BucketSectionHeader(
    title: String,
    count: Int,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = accentColor
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReminderCard(
    reminder: PaymentReminder,
    language: AppLanguage,
    isOverdue: Boolean,
    isToday: Boolean,
    onCardClick: () -> Unit,
    onDoneClick: () -> Unit
) {
    val containerColor = when {
        isOverdue -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
        isToday -> MaterialTheme.colorScheme.surfaceContainerHigh
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    Card(
        onClick = onCardClick,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (reminder.note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.CreditCard,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = reminder.note,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = MoneyFormat.formatMinorAmount(reminder.amountMinor, reminder.currencyCode),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isOverdue) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        MetaBadge(
                            icon = Icons.Rounded.Event,
                            text = formatLocalDate(reminder.dueDate, language),
                            tint = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MetaBadge(
                            icon = Icons.Rounded.Schedule,
                            text = MoneyFormat.formatTime(reminder.notifyHour, reminder.notifyMinute),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (reminder.recurrence != RecurrenceType.ONCE) {
                            MetaBadge(
                                icon = Icons.Rounded.EventRepeat,
                                text = recurrenceLabel(reminder.recurrence),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        reminder.remindDaysBefore.forEach { daysBefore ->
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surfaceContainerHighest
                            ) {
                                Text(
                                    text = remindDayChipLabel(daysBefore),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                FilledTonalButton(
                    onClick = onDoneClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.action_mark_done),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun MetaBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    tint: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = tint
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = tint
        )
    }
}

@Composable
private fun EmptyRemindersState(onAddClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Payments,
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.empty_reminders_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.empty_reminders_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(onClick = onAddClick) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = stringResource(R.string.action_add_reminder))
            }
        }
    }
}
