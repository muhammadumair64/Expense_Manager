package com.iobits.budgetexpensemanager.localDatabase

import androidx.room.TypeConverter
import com.google.errorprone.annotations.Keep
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iobits.budgetexpensemanager.localDatabase.entities.Budget
import com.iobits.budgetexpensemanager.ui.dataModels.Transaction
class Converters {
    @TypeConverter
    fun fromTransactionsList(value: ArrayList<Transaction>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toTransactionsList(value: String): ArrayList<Transaction>? {
        val listType = object : TypeToken<ArrayList<Transaction>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromBudgetsList(value: ArrayList<Budget>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toBudgetsList(value: String): ArrayList<Budget>? {
        val listType = object : TypeToken<ArrayList<Budget>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromCategoriesList(value: ArrayList<String>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toCategoriesList(value: String): ArrayList<String>? {
        val listType = object : TypeToken<ArrayList<String>>() {}.type
        return Gson().fromJson(value, listType)
    }
}