package com.iobits.budgetexpensemanager.localDatabase.entities

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity
data class Budget(
    @PrimaryKey(autoGenerate = true)
    var id:Int,
    var category: String,
    var amount: Int,
    var date: String,
    var description: String
)