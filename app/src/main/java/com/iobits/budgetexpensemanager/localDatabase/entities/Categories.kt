package com.iobits.budgetexpensemanager.localDatabase.entities

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity
data class Categories(
    @PrimaryKey
    var id:Int,var expenseList: ArrayList<String>, var incomeList: ArrayList<String>)