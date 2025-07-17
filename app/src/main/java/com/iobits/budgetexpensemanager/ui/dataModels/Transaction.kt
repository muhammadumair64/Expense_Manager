package com.iobits.budgetexpensemanager.ui.dataModels

import androidx.annotation.Keep

@Keep
data class Transaction (
    var amount:Float,
    var type :String,
    var date :String,
    var description :String,
    var category :String,
    )