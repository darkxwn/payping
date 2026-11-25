package com.darkxwn.payping

import android.app.Application
import android.content.Context
import com.darkxwn.payping.data.local.PayPingDatabase
import com.darkxwn.payping.data.repository.PaymentRepository
import com.darkxwn.payping.data.settings.SettingsRepository
import com.darkxwn.payping.notifications.AndroidAlarmScheduler
import com.darkxwn.payping.notifications.NotificationHelper

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: PayPingDatabase by lazy {
        PayPingDatabase.getInstance(appContext)
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(appContext)
    }

    val alarmScheduler: AndroidAlarmScheduler by lazy {
        AndroidAlarmScheduler(appContext)
    }

    val paymentRepository: PaymentRepository by lazy {
        PaymentRepository(
            database = database,
            settingsRepository = settingsRepository,
            alarmScheduler = alarmScheduler
        )
    }
}

class PayPingApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.ensureChannel(this)
    }
}
