package com.darkxwn.payping.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentCategoryDao {
    @Query("SELECT * FROM payment_categories ORDER BY sortOrder ASC, createdAtEpochMillis ASC")
    fun observeAll(): Flow<List<PaymentCategoryEntity>>

    @Query("SELECT * FROM payment_categories ORDER BY sortOrder ASC, createdAtEpochMillis ASC")
    suspend fun getAll(): List<PaymentCategoryEntity>

    @Query("SELECT * FROM payment_categories WHERE uid = :uid LIMIT 1")
    suspend fun getByUid(uid: String): PaymentCategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PaymentCategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<PaymentCategoryEntity>)

    @Query("DELETE FROM payment_categories WHERE uid = :uid")
    suspend fun deleteByUid(uid: String)

    @Query("DELETE FROM payment_categories")
    suspend fun deleteAll()
}

@Dao
interface PaymentReminderDao {
    @Query("SELECT * FROM payment_reminders WHERE isArchived = 0 ORDER BY dueDateEpochDay ASC, notifyHour ASC, notifyMinute ASC")
    fun observeAll(): Flow<List<PaymentReminderEntity>>

    @Query("SELECT * FROM payment_reminders WHERE isArchived = 0 ORDER BY dueDateEpochDay ASC, notifyHour ASC, notifyMinute ASC")
    suspend fun getAll(): List<PaymentReminderEntity>

    @Query("SELECT * FROM payment_reminders ORDER BY dueDateEpochDay ASC, notifyHour ASC, notifyMinute ASC")
    suspend fun getAllIncludingArchived(): List<PaymentReminderEntity>

    @Query("SELECT * FROM payment_reminders WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PaymentReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PaymentReminderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<PaymentReminderEntity>): List<Long>

    @Update
    suspend fun update(entity: PaymentReminderEntity)

    @Query("UPDATE payment_reminders SET categoryUid = :targetCategoryUid WHERE categoryUid = :oldCategoryUid")
    suspend fun reassignCategory(oldCategoryUid: String, targetCategoryUid: String)

    @Query("DELETE FROM payment_reminders WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM payment_reminders")
    suspend fun deleteAll()
}

@Dao
interface PaymentHistoryDao {
    @Query("SELECT * FROM payment_history ORDER BY paidAtEpochMillis DESC, id DESC")
    fun observeAll(): Flow<List<PaymentHistoryEntity>>

    @Query("SELECT * FROM payment_history ORDER BY paidAtEpochMillis DESC, id DESC")
    suspend fun getAll(): List<PaymentHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PaymentHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<PaymentHistoryEntity>): List<Long>

    @Query("UPDATE payment_history SET categoryUid = :targetCategoryUid WHERE categoryUid = :oldCategoryUid")
    suspend fun reassignCategory(oldCategoryUid: String, targetCategoryUid: String)

    @Query("DELETE FROM payment_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM payment_history")
    suspend fun deleteAll()
}
