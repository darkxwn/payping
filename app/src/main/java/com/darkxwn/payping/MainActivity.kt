package com.darkxwn.payping

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.darkxwn.payping.notifications.NotificationHelper
import com.darkxwn.payping.ui.PayPingAppScreen
import com.darkxwn.payping.ui.PayPingViewModel
import com.darkxwn.payping.ui.theme.PayPingTheme
import com.darkxwn.payping.ui.theme.isPayPingDarkTheme

class MainActivity : AppCompatActivity() {

    private val viewModel: PayPingViewModel by viewModels {
        PayPingViewModel.Factory(application as PayPingApp)
    }

    private var hasNotificationPermission by mutableStateOf(true)
    private var canScheduleExactAlarms by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        refreshSystemStates()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val settings = uiState.settings
            val useDark = isPayPingDarkTheme(settings.themeMode)

            DisposableEffect(useDark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { useDark },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { useDark }
                )
                onDispose {}
            }

            PayPingTheme(
                themeMode = settings.themeMode,
                pureBlack = settings.pureBlack,
                useDynamicColor = settings.useDynamicColor,
                accentColorArgb = settings.accentColorArgb
            ) {
                PayPingAppScreen(
                    viewModel = viewModel,
                    hasNotificationPermission = hasNotificationPermission,
                    canScheduleExactAlarms = canScheduleExactAlarms,
                    onPermissionResult = { refreshSystemStates() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshToday()
        refreshSystemStates()
    }

    private fun refreshSystemStates() {
        hasNotificationPermission = NotificationHelper.hasNotificationPermission(this)
        canScheduleExactAlarms = viewModel.canScheduleExactAlarms()
    }
}
