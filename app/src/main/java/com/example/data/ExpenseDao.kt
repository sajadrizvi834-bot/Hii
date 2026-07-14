package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Int)

    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalSpent(): Flow<Double?>
}

@Dao
interface BlockedItemDao {
    @Query("SELECT * FROM blocked_items")
    fun getAllBlockedItems(): Flow<List<BlockedItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedItem(item: BlockedItem)

    @Update
    suspend fun updateBlockedItem(item: BlockedItem)

    @Delete
    suspend fun deleteBlockedItem(item: BlockedItem)

    @Query("SELECT * FROM blocked_items WHERE name = :name LIMIT 1")
    suspend fun getBlockedItemByName(name: String): BlockedItem?
}
