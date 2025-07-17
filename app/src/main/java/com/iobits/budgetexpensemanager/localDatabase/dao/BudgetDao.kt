package com.iobits.budgetexpensemanager.localDatabase.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.iobits.budgetexpensemanager.localDatabase.entities.Budget

@Dao
interface BudgetDao {
    @Upsert
    fun insertBudget(budget: Budget)
    @Query("SELECT * FROM budget")
    fun getBudgets(): LiveData<List<Budget>>

    @Query("DELETE FROM budget")
    fun deleteAllBudgets()
}