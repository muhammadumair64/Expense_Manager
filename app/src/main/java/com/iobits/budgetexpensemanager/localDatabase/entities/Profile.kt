package com.iobits.budgetexpensemanager.localDatabase.entities

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
@Keep
@Entity
data class Profile (
    @PrimaryKey
    var id:Int,
    var name :String,
    var pic :String,
    var email :String,
    var currency :String,
    )