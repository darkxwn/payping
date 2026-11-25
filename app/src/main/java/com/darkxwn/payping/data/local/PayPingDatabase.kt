package com.darkxwn.payping.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PaymentReminderEntity::class,
        PaymentHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PayPingDatabase : RoomDatabase() {
    abstract fun reminderDao(): PaymentReminderDao
    abstract fun historyDao(): PaymentHistoryDao

    companion object {
        private const val DB_NAME = "payping.db"

        @Volatile
        private var instance: PayPingDatabase? = null

        fun getInstance(context: Context): PayPingDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PayPingDatabase::class.java,
                    DB_NAME
                ).build().also { instance = it }
            }
        }
    }
}
