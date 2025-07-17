package com.iobits.budgetexpensemanager.localDatabase.entities

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.iobits.budgetexpensemanager.ui.dataModels.Transaction

@Keep
@Entity
data class Account (
    @PrimaryKey
    var id:Int,
    var totalAmount : Float,
    var currentBalance :Float,
    var transactions : ArrayList<Transaction>,
    var  budgets : ArrayList<Budget>,
    var income :Float,
    var expense :Float,
    var date :String,
)