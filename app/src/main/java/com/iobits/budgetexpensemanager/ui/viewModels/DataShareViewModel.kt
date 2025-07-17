package com.iobits.budgetexpensemanager.ui.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iobits.budgetexpensemanager.localDatabase.entities.Categories
import com.iobits.budgetexpensemanager.repos.MainRepo
import com.iobits.budgetexpensemanager.utils.K
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataShareViewModel @Inject constructor(private val repository: MainRepo) : ViewModel() {
    val TAG = "DataViewModelTag"
    private val fireStore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    var drawerSetter: ((Int) -> Unit)? = null
    var showPremium : (() -> Unit)? = null
    var addTopSpacing : ((Boolean) -> Unit)? = null


    /** Lists */
    private var expenseNameList: ArrayList<String> = ArrayList()
    private var incomeNameList: ArrayList<String> = ArrayList()
    var tabList: ArrayList<String> = ArrayList()
    var budgetTabList : ArrayList<String> = ArrayList()

    init {
        initTransactionsCategoryDummy()
        initIncomeDummy()
        initRvData()
        initBudgetTabData()
    }
    private fun initRvData() {
        tabList.apply {
            add("Daily")
            add("Weekly")
            add("Monthly")
            add("Yearly")
            add("Custom")
        }

    }
    private fun initBudgetTabData() {
        budgetTabList.apply {
            add("January")
            add("February")
            add("March")
            add("April")
            add("May")
            add("June")
            add("July")
            add("August")
            add("September")
            add("October")
            add("November")
            add("December")
        }
    }

    private fun initTransactionsCategoryDummy() {
        expenseNameList.apply {
            add(K.FOOD)
            add(K.SHOPPING)
            add(K.MEDICINE)
            add(K.INVESTMENT)
            add(K.BEAUTY)
            add(K.GROCERIES)
            add(K.RENT)
            add(K.GIFTs)
            add(K.WORK)
            add(K.TRAVEL)
            add(K.ENTERTAINMENT)
            add(K.AddNew)
        }
    }

    private fun initIncomeDummy() {
        incomeNameList.apply {
            add(K.INCOME)
            add(K.AddNew)
        }
    }

    fun uploadCategories() {
        val categories = Categories(0, expenseNameList, incomeNameList)
        val categoryData = hashMapOf(
            "id" to categories.id,
            "expenseList" to categories.expenseList,
            "incomeList" to categories.incomeList
        )
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val uid = currentUser?.uid

        // FireStore collection name where you want to store profiles
        val profilesCollection = fireStore.collection(K.FIRE_STORE_NAME)
        if (uid != null) {
            profilesCollection.document("Users")
                .collection(uid)
                .document("Categories")
                .set(categoryData)
                .addOnSuccessListener {
                    Log.d(TAG, "uploadAccount: Successful ")
                    insertCategories()
                }
                .addOnFailureListener { e ->
                    // Handle any errors
                    Log.d(TAG, "uploadAccount: ERROR ${e.localizedMessage} ")
                }
        }
    }

    fun insertCategories() {
        val categories = Categories(0, expenseNameList, incomeNameList)
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertCategories(categories)
        }
    }
}
