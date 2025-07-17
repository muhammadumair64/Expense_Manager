package com.iobits.budgetexpensemanager.utils

object K {
    var SYMBOL = ""

    val FIRE_STORE_NAME ="ExpenseManagerDatabase"
    val INCOME = "Income"
    val EXPENSE = "Expense"
    val IsLoginSkipped = "IsLoginSkipped"

    /** Categories */
    val FOOD = "Food"
    val SHOPPING = "Shopping"
    val MEDICINE = "Medicine"
    val INVESTMENT = "Investment"
    val BEAUTY = "Beauty"
    val GROCERIES = "Groceries"
    val RENT = "Rent"
    val GIFTs = "Gifts"
    val WORK = "Work"
    val TRAVEL = "Travel"
    val ENTERTAINMENT = "Entertainment"
    val AddNew = "AddNew"
    val CUSTOM = "CustomCategory"

    var categoryColors: MutableMap<String, String> = mutableMapOf(
        FOOD to "#FFA964",
        SHOPPING to "#AD5CFF",
        MEDICINE to "#FFA9C6",
        INVESTMENT to "#6971FF",
        BEAUTY to "#F55E5E",
        GROCERIES to "#95ACFF",
        RENT to "#FFA59F",
        GIFTs to "#EE94FF",
        WORK to "#FF9470",
        TRAVEL to "#1AC6C6",
        ENTERTAINMENT to "#BF9FF3",
        INCOME to "#EE94FF",
        AddNew to "#FFFFFF",
        CUSTOM to "#52A1FF"
    )
}