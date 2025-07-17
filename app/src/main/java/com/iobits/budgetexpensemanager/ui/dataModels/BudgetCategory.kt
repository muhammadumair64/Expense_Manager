package com.iobits.budgetexpensemanager.ui.dataModels

data class BudgetCategory(
    var category: String,
    var totalAmount: Int,
    var currentAmount :Int,
    var date: String,
    var description: String
)