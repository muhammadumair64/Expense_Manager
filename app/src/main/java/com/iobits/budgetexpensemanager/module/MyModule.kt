package com.iobits.budgetexpensemanager.module

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.iobits.budgetexpensemanager.localDatabase.LocalDataBase
import com.iobits.budgetexpensemanager.localDatabase.dao.AccountDao
import com.iobits.budgetexpensemanager.localDatabase.dao.BudgetDao
import com.iobits.budgetexpensemanager.localDatabase.dao.CategoriesDao
import com.iobits.budgetexpensemanager.localDatabase.dao.ProfileDao
import com.iobits.budgetexpensemanager.utils.TinyDB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MyModule {

    var context: Context? = null
    @Singleton
    @Provides
    fun provideContext(application: Application): Context {
        context = application.applicationContext

        return context!!
    }

    @Provides
    @Singleton
    fun provideTinyDB(@ApplicationContext appContext: Context): TinyDB {
        return TinyDB(appContext)
    }
    @Singleton
    @Provides
    fun provideDataBase(application: Application): LocalDataBase {

        return Room.databaseBuilder(
            application,
            LocalDataBase::class.java,
            "localDatabase"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideProfileDao(localDataBase: LocalDataBase): ProfileDao {
        return localDataBase.profileDao()
    }

    @Provides
    @Singleton
    fun provideAccountDao(localDataBase: LocalDataBase): AccountDao {
        return localDataBase.accountDao()
    }

    @Provides
    @Singleton
    fun provideCategoriesDao(localDataBase: LocalDataBase): CategoriesDao {
        return localDataBase.categoriesDao()
    }
    @Provides
    @Singleton
    fun provideBudgetDao(localDataBase: LocalDataBase): BudgetDao {
        return localDataBase.budgetDao()
    }
}
