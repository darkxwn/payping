package com.darkxwn.payping.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.darkxwn.payping.R
import com.darkxwn.payping.domain.model.BottomBarStyle
import com.darkxwn.payping.domain.model.LiquidGlassLevel
import com.darkxwn.payping.ui.components.LiquidGlassContainer
import com.darkxwn.payping.ui.components.LocalHazeState
import com.darkxwn.payping.ui.components.formatLocalDate
import com.darkxwn.payping.ui.screens.AboutScreen
import com.darkxwn.payping.ui.screens.CategoriesScreen
import com.darkxwn.payping.ui.screens.DisclaimerScreen
import com.darkxwn.payping.ui.screens.HistoryScreen
import com.darkxwn.payping.ui.screens.ReminderEditScreen
import com.darkxwn.payping.ui.screens.RemindersScreen
import com.darkxwn.payping.ui.screens.SettingsScreen
import com.darkxwn.payping.ui.screens.ThemeSettingsScreen
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
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

private object SettingsRoutes {
    const val THEME = "settings/theme"
    const val CATEGORIES = "settings/categories"
    const val DISCLAIMER = "settings/disclaimer"
    const val ABOUT = "settings/about"
}

@Composable
fun PayPingAppScreen(
    viewModel: PayPingViewModel,
    hasNotificationPermission: Boolean,
    canScheduleExactAlarms: Boolean,
    onPermissionResult: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = uiState.settings
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hazeState = rememberHazeState()

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

    LaunchedEffect(viewModel, settings.language) {
        viewModel.events.collect { event ->
            val message = when (event) {
                is UiFeedbackEvent.MarkedDone -> {
                    val nextDate = event.result.advancedToDate
                    if (nextDate != null) {
                        context.getString(
                            R.string.snackbar_marked_done_recurring,
                            event.result.title,
                            formatLocalDate(nextDate, settings.language)
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

    val tabs = remember { listOf(TopTab.Reminders, TopTab.History, TopTab.Settings) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = tabs.any { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.route } == true
    }

    val isFloatingBottomBar = settings.bottomBarStyle == BottomBarStyle.FLOATING
    // Liquid Glass never applies when the bottom bar is pinned.
    val bottomBarGlassLevel = if (isFloatingBottomBar) settings.liquidGlassLevel else LiquidGlassLevel.NONE
    val navBottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val floatingClearance = if (isFloatingBottomBar) 92.dp + navBottomInset else 0.dp

    val navigateToTab: (TopTab) -> Unit = { tab ->
        navController.navigate(tab.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    CompositionLocalProvider(LocalHazeState provides hazeState) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(state = hazeState)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = TopTab.Reminders.route,
                            modifier = Modifier.fillMaxSize()
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
                                    },
                                    bottomBarClearance = floatingClearance
                                )
                            }

                            composable(TopTab.History.route) {
                                HistoryScreen(
                                    history = uiState.history,
                                    language = settings.language,
                                    onDeleteItem = { id -> viewModel.deleteHistoryItem(id) },
                                    onClearAll = { viewModel.clearHistory() },
                                    customCategories = uiState.customCategories,
                                    bottomBarClearance = floatingClearance
                                )
                            }

                            composable(TopTab.Settings.route) {
                                SettingsScreen(
                                    settings = settings,
                                    hasNotificationPermission = hasNotificationPermission,
                                    canScheduleExactAlarms = canScheduleExactAlarms,
                                    pendingImportJson = pendingImportJson,
                                    onLanguageChange = { viewModel.setLanguage(it) },
                                    onOpenThemeSettings = {
                                        navController.navigate(SettingsRoutes.THEME)
                                    },
                                    onOpenCategories = {
                                        navController.navigate(SettingsRoutes.CATEGORIES)
                                    },
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
                                    onDismissImportDialog = { pendingImportJson = null },
                                    onOpenDisclaimer = {
                                        navController.navigate(SettingsRoutes.DISCLAIMER)
                                    },
                                    onOpenAbout = {
                                        navController.navigate(SettingsRoutes.ABOUT)
                                    },
                                    bottomBarClearance = floatingClearance
                                )
                            }

                            composable(SettingsRoutes.CATEGORIES) {
                                CategoriesScreen(
                                    customCategories = uiState.customCategories,
                                    language = settings.language,
                                    liquidGlassLevel = settings.liquidGlassLevel,
                                    onSaveCategory = { viewModel.saveCategory(it) },
                                    onDeleteCategory = { viewModel.deleteCategory(it) },
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            composable(SettingsRoutes.THEME) {
                                ThemeSettingsScreen(
                                    settings = settings,
                                    onThemeModeChange = { viewModel.setThemeMode(it) },
                                    onPureBlackChange = { viewModel.setPureBlack(it) },
                                    onSelectAccentColor = { viewModel.selectAccentColor(it) },
                                    onSelectDynamicColor = { viewModel.selectDynamicColor() },
                                    onAddCustomAccentColor = { viewModel.addCustomAccentColor(it) },
                                    onRemoveCustomAccentColor = { viewModel.removeCustomAccentColor(it) },
                                    onAppFontChange = { viewModel.setAppFont(it) },
                                    onUseSystemFontScaleChange = { viewModel.setUseSystemFontScale(it) },
                                    onFontScalePercentChange = { viewModel.setFontScalePercent(it) },
                                    onBottomBarStyleChange = { viewModel.setBottomBarStyle(it) },
                                    onLiquidGlassLevelChange = { viewModel.setLiquidGlassLevel(it) },
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            composable(SettingsRoutes.DISCLAIMER) {
                                DisclaimerScreen(
                                    liquidGlassLevel = settings.liquidGlassLevel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            composable(SettingsRoutes.ABOUT) {
                                AboutScreen(
                                    liquidGlassLevel = settings.liquidGlassLevel,
                                    onNavigateBack = { navController.popBackStack() },
                                    onShowMessage = { message ->
                                        scope.launch { snackbarHostState.showSnackbar(message) }
                                    }
                                )
                            }

                            composable(
                                route = EditorRoute.PATTERN,
                                arguments = listOf(navArgument(EditorRoute.ARG_ID) { type = NavType.LongType })
                            ) { entry ->
                                val reminderId = entry.arguments?.getLong(EditorRoute.ARG_ID) ?: -1L
                                ReminderEditScreen(
                                    reminderId = reminderId,
                                    defaultCurrencyCode = settings.defaultCurrencyCode,
                                    language = settings.language,
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
                                    onNavigateBack = { navController.popBackStack() },
                                    liquidGlassLevel = settings.liquidGlassLevel,
                                    customCategories = uiState.customCategories,
                                    recentCurrencyCodes = settings.recentCurrencies,
                                    onSaveCategory = { viewModel.saveCategory(it) }
                                )
                            }
                        }
                    }

                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .let { mod ->
                                if (showBottomBar && isFloatingBottomBar) {
                                    mod.padding(bottom = floatingClearance)
                                } else if (!showBottomBar) {
                                    mod.navigationBarsPadding().padding(bottom = 8.dp)
                                } else {
                                    mod.padding(bottom = 8.dp)
                                }
                            }
                    )
                }

                if (showBottomBar && !isFloatingBottomBar) {
                    PinnedNavigationBar(
                        tabs = tabs,
                        currentDestination = currentDestination,
                        onSelectTab = navigateToTab
                    )
                }
            }

            if (showBottomBar && isFloatingBottomBar) {
                FloatingLiquidBottomBar(
                    tabs = tabs,
                    currentDestination = currentDestination,
                    liquidGlassLevel = bottomBarGlassLevel,
                    hazeState = hazeState,
                    onSelectTab = navigateToTab,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun PinnedNavigationBar(
    tabs: List<TopTab>,
    currentDestination: NavDestination?,
    onSelectTab: (TopTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        tabs.forEach { tab ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectTab(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = stringResource(tab.labelRes)
                    )
                },
                label = {
                    Text(
                        text = stringResource(tab.labelRes),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}

@Composable
private fun FloatingLiquidBottomBar(
    tabs: List<TopTab>,
    currentDestination: NavDestination?,
    liquidGlassLevel: LiquidGlassLevel,
    hazeState: HazeState,
    onSelectTab: (TopTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val pillShape = RoundedCornerShape(28.dp)
    val activeIndicatorAlpha = when (liquidGlassLevel) {
        LiquidGlassLevel.NONE -> 1f
        LiquidGlassLevel.STANDARD -> 0.72f
        LiquidGlassLevel.MAXIMUM -> 0.30f
    }

    LiquidGlassContainer(
        liquidGlassLevel = liquidGlassLevel,
        shape = pillShape,
        cornerRadiusDp = 28.dp,
        hazeState = hazeState,
        solidColor = MaterialTheme.colorScheme.surfaceContainer,
        refractionDp = 12.dp,
        dispersionDp = 2.2.dp,
        modifier = modifier
            .widthIn(max = 420.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val isSelected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                val indicatorBg = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = activeIndicatorAlpha)
                } else {
                    Color.Transparent
                }
                val contentTint = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(indicatorBg)
                        .semantics { selected = isSelected }
                        .clickable(role = Role.Tab) { onSelectTab(tab) }
                        .padding(horizontal = 6.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = stringResource(tab.labelRes),
                            tint = contentTint,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(tab.labelRes),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = contentTint,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
