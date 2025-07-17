package com.iobits.budgetexpensemanager.utils

import com.iobits.budgetexpensemanager.R

object IconHelper {
    fun iconChooser(category: String): Int {
        when (category) {
            K.INCOME -> {
                return R.drawable.income_ic
            }

            K.FOOD -> {
                return R.drawable.food_ic
            }

            K.BEAUTY -> {
                return R.drawable.beauty_ic
            }

            K.GROCERIES -> {
                return R.drawable.groceries_ic
            }

            K.RENT -> {
                return R.drawable.rent_ic

            }

            K.GIFTs -> {
                return R.drawable.gift_ic

            }

            K.WORK -> {
                return R.drawable.work_ic

            }

            K.TRAVEL -> {
                return R.drawable.travel_ic

            }

            K.ENTERTAINMENT -> {
                return R.drawable.entertainment_ic

            }

            K.MEDICINE -> {
                return R.drawable.medicine_ic

            }

            K.SHOPPING -> {
                return R.drawable.shopping_ic

            }

            K.INVESTMENT -> {
                return R.drawable.investment
            }
            K.AddNew->{
                return R.drawable.add_black
            }
        }
        return R.drawable.custom_ic
    }
    fun iconChooser3d(category: String): Int {
        when (category) {
            K.INCOME -> {
                return R.drawable.income_3d
            }

            K.FOOD -> {
                return R.drawable.food_3d

            }

            K.BEAUTY -> {
                return R.drawable.beauty_3d

            }

            K.GROCERIES -> {
                return R.drawable.groceries_3d


            }

            K.RENT -> {
                return R.drawable.rent_3d


            }

            K.GIFTs -> {
                return R.drawable.gift_3d


            }

            K.WORK -> {
                return R.drawable.work_3d


            }

            K.TRAVEL -> {
                return R.drawable.travel_3d


            }

            K.ENTERTAINMENT -> {
                return R.drawable.entertainment_3d


            }

            K.MEDICINE -> {
                return R.drawable.medicine_3d


            }

            K.SHOPPING -> {
                return R.drawable.shopping_3d


            }

            K.INVESTMENT -> {
                return R.drawable.investment_3d
            }
            K.AddNew->{
                return R.drawable.add_black
            }
        }
        return R.drawable.custom_3d
    }
}