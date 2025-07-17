package com.iobits.budgetexpensemanager.localDatabase.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.iobits.budgetexpensemanager.localDatabase.entities.Account
import com.iobits.budgetexpensemanager.localDatabase.entities.Profile

@Dao
interface AccountDao {
    @Upsert
    fun insertAccount(account: Account)

    @Query("SELECT * FROM account")
    fun getAccount(): LiveData<Account>

    @Query("DELETE FROM account")
    fun deleteAllAccounts()
}