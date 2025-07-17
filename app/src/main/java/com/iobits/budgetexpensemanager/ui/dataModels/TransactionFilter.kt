package com.iobits.budgetexpensemanager.ui.dataModels

data class TransactionFilter (
    var amount:Float,
    var type :String,
    var date :String,
    var description :String,
    var category :String,
    var itemDate : String
    )