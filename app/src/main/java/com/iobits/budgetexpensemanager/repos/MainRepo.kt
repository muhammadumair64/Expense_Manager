package com.iobits.budgetexpensemanager.repos

import androidx.lifecycle.LiveData
import com.iobits.budgetexpensemanager.localDatabase.dao.AccountDao
import com.iobits.budgetexpensemanager.localDatabase.dao.BudgetDao
import com.iobits.budgetexpensemanager.localDatabase.dao.CategoriesDao
import com.iobits.budgetexpensemanager.localDatabase.dao.ProfileDao
import com.iobits.budgetexpensemanager.localDatabase.entities.Account
import com.iobits.budgetexpensemanager.localDatabase.entities.Budget
import com.iobits.budgetexpensemanager.localDatabase.entities.Categories
import com.iobits.budgetexpensemanager.localDatabase.entities.Profile
import javax.inject.Inject

class MainRepo @Inject constructor(private val profileDao: ProfileDao, private val accountDao: AccountDao , private  val categoriesDao: CategoriesDao ,private val budgetDao: BudgetDao ){

    suspend fun insertProfile(profile: Profile) {
        return profileDao.insertProfile(profile)
    }

    suspend fun getProfile(): Profile {
        return profileDao.getProfile()
    }
    suspend fun insertAccount(account: Account) {
        return accountDao.insertAccount(account)
    }

        fun getAccount(): LiveData<Account> {
        return accountDao.getAccount()
    }

    suspend fun insertCategories(categories: Categories) {
        return categoriesDao.insertCategories(categories)
    }

    fun getCategories(): LiveData<Categories> {
        return categoriesDao.getCategories()
    }
    suspend fun insertBudget(budget: Budget) {
        return budgetDao.insertBudget(budget)
    }

    fun getBudgets(): LiveData<List<Budget>> {
        return budgetDao.getBudgets()
    }


    fun deleteDataBase(){
        accountDao.deleteAllAccounts()
        categoriesDao.deleteAllCategories()
        budgetDao.deleteAllBudgets()
        profileDao.deleteAllProfiles()
    }

}