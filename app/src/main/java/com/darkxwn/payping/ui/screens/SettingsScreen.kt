package com.darkxwn.payping.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.darkxwn.payping.R
import com.darkxwn.payping.domain.backup.ImportMode
import com.darkxwn.payping.domain.model.AppLanguage
import com.darkxwn.payping.domain.model.AppSettings
import com.darkxwn.payping.domain.model.CurrencyOption
import com.darkxwn.payping.ui.components.resolveSystemLanguageTag

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    hasNotificationPermission: Boolean,
    canScheduleExactAlarms: Boolean,
    pendingImportJson: String?,
    onLanguageChange: (AppLanguage) -> Unit,
    onOpenThemeSettings: () -> Unit,
    onDefaultCurrencyChange: (String) -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onOpenExactAlarmSettings: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onConfirmImport: (ImportMode) -> Unit,
    onDismissImportDialog: () -> Unit,
    onOpenDisclaimer: () -> Unit,
    onOpenAbout: () -> Unit,
    bottomBarClearance: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    var languageMenuExpanded by remember { mutableStateOf(false) }
    val systemLanguageTag = remember { resolveSystemLanguageTag() }
    val systemLanguageName = if (AppLanguage.SYSTEM.resolvedLocaleTag(systemLanguageTag) == "ru") {
        stringResource(R.string.language_ru)
    } else {
        stringResource(R.string.language_en)
    }
    val selectedLanguageShortLabel = when (settings.language) {
        AppLanguage.SYSTEM -> stringResource(R.string.language_system_short)
        AppLanguage.RU -> stringResource(R.string.language_ru)
        AppLanguage.EN -> stringResource(R.string.language_en)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.nav_settings),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 16.dp + bottomBarClearance
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionTitle(stringResource(R.string.settings_section_general))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(role = Role.DropdownList) {
                                    languageMenuExpanded = true
                                }
                                .padding(horizontal = 16.dp, vertical = 15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = stringResource(R.string.settings_language),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Box {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = selectedLanguageShortLabel,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = languageMenuExpanded,
                                    onDismissRequest = { languageMenuExpanded = false }
                                ) {
                                    val languageChoices = listOf(
                                        AppLanguage.SYSTEM to stringResource(R.string.language_system_option, systemLanguageName),
                                        AppLanguage.RU to stringResource(R.string.language_ru),
                                        AppLanguage.EN to stringResource(R.string.language_en)
                                    )
                                    languageChoices.forEach { (option, label) ->
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            trailingIcon = if (settings.language == option) {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            } else {
                                                null
                                            },
                                            onClick = {
                                                languageMenuExpanded = false
                                                onLanguageChange(option)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    SettingsNavigationRow(
                        icon = Icons.Rounded.Palette,
                        title = stringResource(R.string.settings_theme),
                        onClick = onOpenThemeSettings
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_default_currency),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CurrencyOption.entries.forEach { option ->
                                FilterChip(
                                    selected = settings.defaultCurrencyCode == option.code,
                                    onClick = { onDefaultCurrencyChange(option.code) },
                                    label = { Text("${option.symbol} ${option.code}") }
                                )
                            }
                        }
                    }
                }
            }

            SectionTitle(stringResource(R.string.settings_section_notifications))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PermissionStatusRow(
                        icon = Icons.Rounded.Notifications,
                        title = stringResource(R.string.settings_notifications_permission),
                        statusText = stringResource(
                            if (hasNotificationPermission) R.string.status_granted else R.string.status_not_granted
                        ),
                        isOk = hasNotificationPermission,
                        actionLabel = if (!hasNotificationPermission) stringResource(R.string.action_grant) else null,
                        onAction = onRequestNotificationPermission
                    )

                    PermissionStatusRow(
                        icon = Icons.Rounded.Schedule,
                        title = stringResource(R.string.settings_exact_alarms),
                        statusText = stringResource(
                            if (canScheduleExactAlarms) R.string.status_granted else R.string.status_exact_fallback
                        ),
                        isOk = canScheduleExactAlarms,
                        actionLabel = if (!canScheduleExactAlarms) stringResource(R.string.action_open_settings) else null,
                        onAction = onOpenExactAlarmSettings
                    )
                }
            }

            SectionTitle(stringResource(R.string.settings_section_backup))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                val density = LocalDensity.current
                var maxBackupButtonHeightPx by remember(settings.language) { mutableIntStateOf(0) }
                val sharedBackupButtonHeight = max(
                    72.dp,
                    with(density) { maxBackupButtonHeightPx.toDp() }
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BackupActionButton(
                        icon = Icons.Rounded.Upload,
                        title = stringResource(R.string.settings_export_json),
                        subtitle = stringResource(R.string.settings_export_desc),
                        minHeight = sharedBackupButtonHeight,
                        onMeasuredHeightPx = { height ->
                            if (height > maxBackupButtonHeightPx) {
                                maxBackupButtonHeightPx = height
                            }
                        },
                        onClick = onExportClick
                    )

                    BackupActionButton(
                        icon = Icons.Rounded.Download,
                        title = stringResource(R.string.settings_import_json),
                        subtitle = stringResource(R.string.settings_import_desc),
                        minHeight = sharedBackupButtonHeight,
                        onMeasuredHeightPx = { height ->
                            if (height > maxBackupButtonHeightPx) {
                                maxBackupButtonHeightPx = height
                            }
                        },
                        onClick = onImportClick
                    )
                }
            }

            SectionTitle(stringResource(R.string.settings_section_other))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsNavigationRow(
                        icon = Icons.Rounded.Description,
                        title = stringResource(R.string.settings_disclaimer),
                        onClick = onOpenDisclaimer
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    SettingsNavigationRow(
                        icon = Icons.Rounded.Info,
                        title = stringResource(R.string.settings_about),
                        onClick = onOpenAbout
                    )
                }
            }
        }
    }

    if (pendingImportJson != null) {
        AlertDialog(
            onDismissRequest = onDismissImportDialog,
            title = { Text(stringResource(R.string.dialog_import_mode_title)) },
            text = { Text(stringResource(R.string.dialog_import_mode_body)) },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { onConfirmImport(ImportMode.MERGE) }) {
                        Text(stringResource(R.string.action_import_merge))
                    }
                    TextButton(onClick = { onConfirmImport(ImportMode.REPLACE) }) {
                        Text(
                            text = stringResource(R.string.action_import_replace),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissImportDialog) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun SettingsNavigationRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BackupActionButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    minHeight: Dp,
    onMeasuredHeightPx: (Int) -> Unit,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = minHeight)
            .onSizeChanged { size -> onMeasuredHeightPx(size.height) }
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                minLines = 2
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun PermissionStatusRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    statusText: String,
    isOk: Boolean,
    actionLabel: String?,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isOk) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = if (isOk) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (actionLabel != null) {
            TextButton(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}
