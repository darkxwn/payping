package com.darkxwn.payping.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PaymentReminderEntity::class,
        PaymentHistoryEntity::class,
        PaymentCategoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class PayPingDatabase : RoomDatabase() {
    abstract fun reminderDao(): PaymentReminderDao
    abstract fun historyDao(): PaymentHistoryDao
    abstract fun categoryDao(): PaymentCategoryDao

    companion object {
        private const val DB_NAME = "payping.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS payment_categories (
                        uid TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        iconKey TEXT NOT NULL,
                        colorArgb INTEGER NOT NULL,
                        sortOrder INTEGER NOT NULL DEFAULT 0,
                        createdAtEpochMillis INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )

                db.execSQL("ALTER TABLE payment_reminders ADD COLUMN uid TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE payment_reminders SET uid = 'rem-' || id WHERE uid = ''")
                db.execSQL("ALTER TABLE payment_reminders ADD COLUMN paymentType TEXT NOT NULL DEFAULT 'MANUAL'")
                db.execSQL("ALTER TABLE payment_reminders ADD COLUMN categoryUid TEXT NOT NULL DEFAULT 'cat_other'")
                db.execSQL("ALTER TABLE payment_reminders ADD COLUMN badgeIconKey TEXT")
                db.execSQL("ALTER TABLE payment_reminders ADD COLUMN badgeText TEXT")
                db.execSQL("ALTER TABLE payment_reminders ADD COLUMN badgeColorArgb INTEGER NOT NULL DEFAULT 4279139430")
                db.execSQL("ALTER TABLE payment_reminders ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE payment_reminders ADD COLUMN isVariableAmount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE payment_reminders ADD COLUMN serviceLink TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE payment_reminders ADD COLUMN installmentPaid INTEGER")
                db.execSQL("ALTER TABLE payment_reminders ADD COLUMN installmentTotal INTEGER")

                db.execSQL("ALTER TABLE payment_history ADD COLUMN uid TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE payment_history SET uid = 'hist-' || id WHERE uid = ''")
                db.execSQL("ALTER TABLE payment_history ADD COLUMN reminderUid TEXT")
                db.execSQL("ALTER TABLE payment_history ADD COLUMN paymentType TEXT NOT NULL DEFAULT 'MANUAL'")
                db.execSQL("ALTER TABLE payment_history ADD COLUMN categoryUid TEXT NOT NULL DEFAULT 'cat_other'")
                db.execSQL("ALTER TABLE payment_history ADD COLUMN badgeIconKey TEXT")
                db.execSQL("ALTER TABLE payment_history ADD COLUMN badgeText TEXT")
                db.execSQL("ALTER TABLE payment_history ADD COLUMN badgeColorArgb INTEGER NOT NULL DEFAULT 4279139430")
                db.execSQL("ALTER TABLE payment_history ADD COLUMN previousDueDateEpochDay INTEGER")
                db.execSQL("ALTER TABLE payment_history ADD COLUMN nextDueDateAfterPaymentEpochDay INTEGER")
                db.execSQL("ALTER TABLE payment_history ADD COLUMN rateToUsd REAL")
            }
        }

        @Volatile
        private var instance: PayPingDatabase? = null

        fun getInstance(context: Context): PayPingDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PayPingDatabase::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
