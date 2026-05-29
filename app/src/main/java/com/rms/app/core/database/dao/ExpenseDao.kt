package com.rms.app.core.database.dao

import androidx.room.*
import com.rms.app.core.model.entities.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE tenantId = :tenantId ORDER BY expenseDate DESC")
    fun getExpensesByTenant(tenantId: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses ORDER BY expenseDate DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): Expense?

    @Query("SELECT SUM(amount) FROM expenses WHERE tenantId = :tenantId")
    fun getTotalExpensesByTenant(tenantId: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)
}
