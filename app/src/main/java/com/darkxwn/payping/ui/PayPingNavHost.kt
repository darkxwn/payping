package com.darkxwn.payping.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.darkxwn.payping.R
import com.darkxwn.payping.ui.components.formatLocalDate
import com.darkxwn.payping.ui.screens.HistoryScreen
import com.darkxwn.payping.ui.screens.ReminderEditScreen
import com.darkxwn.payping.ui.screens.RemindersScreen
import com.darkxwn.payping.ui.screens.SettingsScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

private sealed class TopTab(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector
) {
    data object Reminders : TopTab("reminders", R.string.nav_reminders, Icons.Rounded.Payments)
    data object History : TopTab("history", R.string.nav_history, Icons.Rounded.History)
    data object Settings : TopTab("settings", R.string.nav_settings, Icons.Rounded.Settings)
}

private object EditorRoute {
    const val ARG_ID = "reminderId"
    const val PATTERN = "editor/{$ARG_ID}"
    fun create(reminderId: Long = -1L): String = "editor/$reminderId"
}

@Composable
fun PayPingAppScreen(
    viewModel: PayPingViewModel,
    hasNotificationPermission: Boolean,
    canScheduleExactAlarms: Boolean,
    onPermissionResult: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pendingImportJson by remember { mutableStateOf<String?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        onPermissionResult()
    }

    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                val json = viewModel.buildExportBackupJson()
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(json.toByteArray(Charsets.UTF_8))
                    } ?: error("Cannot open output stream")
                }
            }.onSuccess {
                viewModel.notifyExportSuccess()
            }.onFailure {
                viewModel.notifyExportError()
            }
        }
    }

    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        stream.readBytes().toString(Charsets.UTF_8)
                    } ?: error("Cannot open input stream")
                }
            }.onSuccess { rawJson ->
                pendingImportJson = rawJson
            }.onFailure {
                snackbarHostState.showSnackbar(context.getString(R.string.snackbar_import_error))
            }
        }
    }

    LaunchedEffect(viewModel, uiState.settings.language) {
        viewModel.events.collect { event ->
            val message = when (event) {
                is UiFeedbackEvent.MarkedDone -> {
                    val nextDate = event.result.advancedToDate
                    if (nextDate != null) {
                        context.getString(
                            R.string.snackbar_marked_done_recurring,
                            event.result.title,
                            formatLocalDate(nextDate, uiState.settings.language)
                        )
                    } else {
                        context.getString(
                            R.string.snackbar_marked_done_once,
                            event.result.title
                        )
                    }
                }
                UiFeedbackEvent.ExportSuccess -> context.getString(R.string.snackbar_export_success)
                UiFeedbackEvent.ExportError -> context.getString(R.string.snackbar_export_error)
                is UiFeedbackEvent.ImportSuccess -> context.getString(
                    R.string.snackbar_import_success,
                    event.summary.importedRemindersCount,
                    event.summary.importedHistoryCount
                )
                UiFeedbackEvent.ImportError -> context.getString(R.string.snackbar_import_error)
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    val requestNotifications: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            context.startActivity(intent)
        }
    }

    val openExactAlarmSettings: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        }
    }

    val tabs = listOf(TopTab.Reminders, TopTab.History, TopTab.Settings)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = tabs.any { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.route } == true
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = stringResource(tab.labelRes)
                                )
                            },
                            label = { Text(stringResource(tab.labelRes)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopTab.Reminders.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(TopTab.Reminders.route) {
                RemindersScreen(
                    uiState = uiState,
                    hasNotificationPermission = hasNotificationPermission,
                    onRequestNotificationPermission = requestNotifications,
                    onAddClick = {
                        navController.navigate(EditorRoute.create(-1L))
                    },
                    onEditClick = { id ->
                        navController.navigate(EditorRoute.create(id))
                    },
                    onMarkDoneClick = { id ->
                        viewModel.markDone(id)
                    }
                )
            }

            composable(TopTab.History.route) {
                HistoryScreen(
                    history = uiState.history,
                    language = uiState.settings.language,
                    onDeleteItem = { id -> viewModel.deleteHistoryItem(id) },
                    onClearAll = { viewModel.clearHistory() }
                )
            }

            composable(TopTab.Settings.route) {
                SettingsScreen(
                    settings = uiState.settings,
                    hasNotificationPermission = hasNotificationPermission,
                    canScheduleExactAlarms = canScheduleExactAlarms,
                    pendingImportJson = pendingImportJson,
                    onLanguageChange = { viewModel.setLanguage(it) },
                    onThemeChange = { viewModel.setThemeMode(it) },
                    onDefaultCurrencyChange = { viewModel.setDefaultCurrency(it) },
                    onRequestNotificationPermission = requestNotifications,
                    onOpenExactAlarmSettings = openExactAlarmSettings,
                    onExportClick = {
                        val fileName = "payping-backup-${LocalDate.now()}.json"
                        exportBackupLauncher.launch(fileName)
                    },
                    onImportClick = {
                        importBackupLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                    },
                    onConfirmImport = { mode ->
                        val json = pendingImportJson
                        pendingImportJson = null
                        if (json != null) {
                            viewModel.importBackupJson(json, mode)
                        }
                    },
                    onDismissImportDialog = { pendingImportJson = null }
                )
            }

            composable(
                route = EditorRoute.PATTERN,
                arguments = listOf(navArgument(EditorRoute.ARG_ID) { type = NavType.LongType })
            ) { entry ->
                val reminderId = entry.arguments?.getLong(EditorRoute.ARG_ID) ?: -1L
                ReminderEditScreen(
                    reminderId = reminderId,
                    defaultCurrencyCode = uiState.settings.defaultCurrencyCode,
                    language = uiState.settings.language,
                    loadReminder = { id -> viewModel.loadReminder(id) },
                    onSave = { reminder ->
                        viewModel.saveReminder(reminder) {
                            navController.popBackStack()
                        }
                    },
                    onDelete = { id ->
                        viewModel.deleteReminder(id) {
                            navController.popBackStack()
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
