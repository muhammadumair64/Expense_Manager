package com.iobits.budgetexpensemanager.localDatabase.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.iobits.budgetexpensemanager.localDatabase.entities.Categories

@Dao
interface CategoriesDao {
    @Upsert
    fun insertCategories(categories: Categories)

    @Query("SELECT * FROM Categories")
    fun getCategories(): LiveData<Categories>
    @Query("DELETE FROM Categories")
    fun deleteAllCategories()
}
