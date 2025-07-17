package com.iobits.budgetexpensemanager.localDatabase

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.iobits.budgetexpensemanager.localDatabase.dao.AccountDao
import com.iobits.budgetexpensemanager.localDatabase.dao.BudgetDao
import com.iobits.budgetexpensemanager.localDatabase.dao.CategoriesDao
import com.iobits.budgetexpensemanager.localDatabase.dao.ProfileDao
import com.iobits.budgetexpensemanager.localDatabase.entities.Account
import com.iobits.budgetexpensemanager.localDatabase.entities.Categories
import com.iobits.budgetexpensemanager.localDatabase.entities.Profile
import com.iobits.budgetexpensemanager.localDatabase.entities.Budget


@Database(entities = [Profile::class,Account::class,Categories::class, Budget::class], version = 1)
@TypeConverters(Converters::class)
abstract  class LocalDataBase: RoomDatabase() {

    abstract fun profileDao():  ProfileDao
    abstract fun accountDao():  AccountDao
    abstract fun budgetDao():  BudgetDao
    abstract fun categoriesDao():  CategoriesDao

}