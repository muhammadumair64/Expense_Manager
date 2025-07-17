package com.iobits.budgetexpensemanager.ui.viewModels

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iobits.budgetexpensemanager.localDatabase.entities.Account
import com.iobits.budgetexpensemanager.localDatabase.entities.Budget
import com.iobits.budgetexpensemanager.localDatabase.entities.Categories
import com.iobits.budgetexpensemanager.repos.MainRepo
import com.iobits.budgetexpensemanager.ui.dataModels.GraphDataModel
import com.iobits.budgetexpensemanager.ui.dataModels.Transaction
import com.iobits.budgetexpensemanager.ui.dataModels.TransactionFilter
import com.iobits.budgetexpensemanager.utils.K
import com.iobits.budgetexpensemanager.utils.TinyDB
import com.iobits.budgetexpensemanager.utils.isInternetAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: MainRepo, private val tinyDB: TinyDB) : ViewModel() {
    private val TAG = "MainViewModelTag"

    var currentIncome = ""
    var currentExpense = ""

    /** Lists */
    var expenseNameList: ArrayList<String> = ArrayList()
    var incomeNameList: ArrayList<String> = ArrayList()
    val categoryTotalTransactions = ArrayList<TransactionFilter>()
    val graphDataList = ArrayList<GraphDataModel>()
    val pieDataList = ArrayList<TransactionFilter>()

    /** Fire store  init */
    private val fireStore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    /** navigation */
    var onNavItemSelected: ((Int) -> Unit)? = null
    var navBarSetter: ((Int) -> Unit)? = null
    var createBudgetClick : (() -> Unit)? = null
    var addMoreBudget : (() -> Unit)? = null
    var deleteFirebaseAccount: ((Boolean)->Unit)? = null
    var updateResult : ((Boolean)->Unit)? = null

    var startLoading:((Boolean)-> Unit)? = null
    var clickOnRecentTransaction:((Transaction)-> Unit)? = null


    var selectedTransaction : Transaction? = null
    var enteredAmount  = 0

    fun updateAccount(account: Account, requireContext: Context) {

        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAccount(account)
//          accountInfo.postValue(account)
            if(!tinyDB.getBoolean(K.IsLoginSkipped)){
                Log.d(TAG, "update Account on Firebase")
                updateAccountOnFirebase(account, requireContext)
            }else{
                withContext(Dispatchers.Main){
                    updateResult?.invoke(true)
                }
            }
        }
    }

    fun getAccount(): LiveData<Account> {
        return repository.getAccount()
    }

    fun deleteDataBase(){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDataBase()
        }
    }
    fun updateAccountOnFirebase(account: Account, context: Context) {
        if (isInternetAvailable(context)) {
            val accountData = hashMapOf(
                "id" to account.id,
                "totalAmount" to account.totalAmount,
                "currentBalance" to account.currentBalance,
                "transactions" to account.transactions,
                "budgets" to account.budgets,
                "income" to account.income,
                "expense" to account.expense,
                "date" to account.date
            )
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            val uid = currentUser?.uid

            //  FireStore collection name where you want to store profiles
            val profilesCollection = fireStore.collection(K.FIRE_STORE_NAME)

            if (uid != null) {
                profilesCollection.document("Users")
                    .collection(uid)
                    .document("Account")
                    .set(accountData)
                    .addOnSuccessListener {
                        Log.d(TAG, "update Account: Successful ")
                        updateResult?.invoke(true)
                    }
                    .addOnFailureListener { e ->
                        // Handle any errors
                        Log.d(TAG, "update Account: ERROR ${e.localizedMessage} ")
                        updateResult?.invoke(true)
                }
            }
        }
    }

    fun deleteUserFromFirebase(){
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val uid = currentUser?.uid

// Firestore collection name where you want to delete the collection
        val profilesCollection = fireStore.collection(K.FIRE_STORE_NAME)
        if (uid != null) {
            val userCollectionReference = profilesCollection.document("Users").collection(uid)

            // Query for all documents within the collection
            userCollectionReference.get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        // Delete each document in the collection
                        document.reference.delete()
                            .addOnSuccessListener {
                                // Handle success
                                deleteFirebaseAccount?.invoke(true)
                                Log.d(TAG, "Document deleted successfully: ${document.id}")
                            }
                            .addOnFailureListener { e ->
                                // Handle failure
                                deleteFirebaseAccount?.invoke(false)
                                Log.e(TAG, "Error deleting document ${document.id}: $e")
                            }
                    }
                }
                .addOnFailureListener { e ->
                    // Handle failure to fetch documents
                    deleteFirebaseAccount?.invoke(false)
                    Log.e(TAG, "Error fetching documents from collection: $e")
                }
        }

    }

    //----------------------------------------- Budgets ------------------------------------------//
    fun insertBudgets(budget: Budget){
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertBudget(budget)
        }
    }
    fun getBudgets(): LiveData<List<Budget>> {
        return repository.getBudgets()
    }
    fun uploadBudgets(budgets: List<Budget>) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val uid = currentUser?.uid

        // Firestore collection name where you want to store budgets
        val budgetsCollection = FirebaseFirestore.getInstance().collection(K.FIRE_STORE_NAME)

        if (uid != null) {
            // Document reference for the user's budgets
            val userBudgetsRef = budgetsCollection.document("Users")
                .collection(uid)
                .document("Budgets")

            // Convert the list of budgets to a map for Firestore
            val budgetsMap = hashMapOf<String, Any>()
            for ((index, budget) in budgets.withIndex()) {
                val budgetData = hashMapOf(
                    "id" to budget.id,
                    "amount" to budget.amount,
                    "category" to budget.category,
                    // Add more fields as necessary
                )
                budgetsMap["budget$index"] = budgetData
            }

            // Upload the list of budgets to Firestore
            userBudgetsRef.set(budgetsMap)
                .addOnSuccessListener {
                    Log.d(TAG, "uploadBudgets: Successful")
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "uploadBudgets: ERROR ${e.localizedMessage}")
                }
        }
    }

    //--------------------------------------- Categories -----------------------------------------//
    fun uploadCategories(categories: Categories) {
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
                }
                .addOnFailureListener { e ->
                    // Handle any errors
                    Log.d(TAG, "uploadAccount: ERROR ${e.localizedMessage} ")
                }
        }
    }

    fun insertCategories(categories: Categories) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertCategories(categories)
            if(!tinyDB.getBoolean(K.IsLoginSkipped)){
              uploadCategories(categories)
            }
        }
    }

    fun getCategories(): LiveData<Categories> {
        return repository.getCategories()
    }
    //---------------------------------- Transaction Sorter --------------------------------------//
    fun calculateSpendingDaily(transactions: ArrayList<Transaction>) {
        transactions.reverse()

        // Map to store category-wise and date-wise transactions
        val categoryDateMap = mutableMapOf<String, MutableMap<String, Float>>()
        val categoryType = mutableMapOf<String, String>()

        for (transaction in transactions) {
            val category = transaction.category
            val amount = transaction.amount
            val dateTime = transaction.date
            val splitter = dateTime.split(" ")
            val date = splitter[0]

            // Initialize or get the category map if it exists
            val categoryMap = categoryDateMap.getOrPut(category) { mutableMapOf() }

            // Update the total amount for the corresponding category and date
            val existingValue = categoryMap[date]
            if (existingValue != null) {
                categoryMap[date] = existingValue + amount
            } else {
                categoryMap[date] = amount
            }
            categoryType[category] = transaction.type
        }

        // Iterate through the category and date map to create total transactions
        for ((category, dateMap) in categoryDateMap) {
            for ((date, totalAmount) in dateMap) {
                // Create a new Transaction object with the category, total amount, and date
                val categoryTotalTransaction = TransactionFilter(
                    totalAmount,
                    categoryType[category]!!,
                    date,
                    "",
                    category,
                    date
                )

                // Add the new Transaction object to the list
                categoryTotalTransactions.add(categoryTotalTransaction)
            }
        }

        println("Category Total Transactions:")
        for (transaction in categoryTotalTransactions) {
            println("SORTER : ${transaction.category}: ${transaction.amount}, Date: ${transaction.date}")
        }
    }

    fun filterTransactionsByDate(transactions: ArrayList<TransactionFilter>, startDateStr: String, endDateStr: String): List<TransactionFilter> {
        val dateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())

        val startDate = dateFormat.parse(startDateStr)
        val endDate = dateFormat.parse(endDateStr)

        return transactions.filter { transaction ->
            val transactionDate = dateFormat.parse(transaction.date)
            !transactionDate.before(startDate) && !transactionDate.after(endDate)
        }
    }

    fun calculateSpendingWeekly(transactions: ArrayList<Transaction>) {
        transactions.reverse()

        // Map to store category-wise and week-wise transactions
        val categoryWeekMap = mutableMapOf<String, MutableMap<String, Float>>()
        val categoryLastTransactionDateMap = mutableMapOf<String, MutableMap<String, String>>()
        val categoryType = mutableMapOf<String, String>()

        for (transaction in transactions) {
            try {
                val category = transaction.category
                val amount = transaction.amount
                val dateTime = transaction.date
                val dateString = dateTime.split(" ")
                val formatter = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
                val date = formatter.parse(dateString[0])
                val calendar = Calendar.getInstance()
                calendar.time = date

                // Get the week number and year for the transaction
                val weekNumber = calendar[Calendar.WEEK_OF_YEAR]
                val year = calendar[Calendar.YEAR]

                // Construct key for the week (combining week number and year)
                val weekKey = "$weekNumber-$year"

                // Calculate the starting date of the week
                val weekStart = getStartOfWeek(calendar)

                // Initialize or get the category map if it exists
                val categoryMap = categoryWeekMap.getOrPut(category) { mutableMapOf() }

                // Update the total amount for the corresponding category and week
                val existingValue = categoryMap[weekStart]
                if (existingValue != null) {
                    categoryMap[weekStart] = existingValue + amount
                } else {
                    categoryMap[weekStart] = amount
                }

                // Update the last transaction date for the category for the current week
                val categoryDateMap = categoryLastTransactionDateMap.getOrPut(category) { mutableMapOf() }
                categoryDateMap[weekKey] = dateTime
                categoryType[category] = transaction.type
            }catch (e:Exception){
                Log.d(TAG, "calculateSpendingWeekly: ${e.localizedMessage}")
            }
        }

        // Iterate through the category and week map to create total transactions
        for ((category, weekMap) in categoryWeekMap) {
            for ((weekStart, totalAmount) in weekMap) {
                val lastTransactionDate = categoryLastTransactionDateMap[category]?.get(weekStart) ?: ""
                val categoryTotalTransaction = TransactionFilter(
                    totalAmount,
                    categoryType[category]!!,
                    weekStart,
                    "",
                    category,
                    lastTransactionDate
                )
                categoryTotalTransactions.add(categoryTotalTransaction)
                // Print or use this transaction object as needed
                println("SORTER : ${categoryTotalTransaction.category}: ${categoryTotalTransaction.amount}, Week Start: ${categoryTotalTransaction.date}, Last Transaction Date: ${categoryTotalTransaction.itemDate}")
            }
        }
    }

    private fun getStartOfWeek(calendar: Calendar): String {
        // Find the starting day of the week (Sunday) and reset time to 00:00:00
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // Get the starting date of the week
        return SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(calendar.time)
    }

    fun calculateSpendingMonthly(transactions: ArrayList<Transaction>) {
        transactions.reverse()
        transactions.sortBy {
            it.date
        }

        // Map to store category-wise and month-wise transactions
        val categoryMonthMap = mutableMapOf<String, MutableMap<String, Float>>()
        val categoryLastTransactionDateMap = mutableMapOf<String, MutableMap<String, String>>() // Updated data structure
        val categoryType = mutableMapOf<String, String>()

        for (transaction in transactions) {
            try {
                val category = transaction.category
                val amount = transaction.amount
                val dateTime = transaction.date
                val dateString = dateTime.split(" ")
                val formatter = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
                val date = formatter.parse(dateString[0].trim())

                // Get the month name for the transaction
                val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(date)

                // Initialize or get the category map if it exists
                val categoryMap = categoryMonthMap.getOrPut(category) { mutableMapOf() }

                // Update the total amount for the corresponding category and month
                val existingValue = categoryMap[monthName]
                if (existingValue != null) {
                    categoryMap[monthName] = existingValue + amount
                } else {
                    categoryMap[monthName] = amount
                }
                categoryType[category] = transaction.type

                // Update the last transaction date for the category for the current month
                val categoryDateMap = categoryLastTransactionDateMap.getOrPut(category) { mutableMapOf() }
                categoryDateMap[monthName] = dateTime
            }catch (e:Exception){
                Log.d(TAG, "calculateSpendingMonthly: ERROR ${e.localizedMessage}")
            }
        }

        // Iterate through the category and month map to create total transactions
        for ((category, monthMap) in categoryMonthMap) {
            for ((monthName, totalAmount) in monthMap) {
                val lastTransactionDate = categoryLastTransactionDateMap[category]?.get(monthName) ?: ""
                val categoryTotalTransaction = TransactionFilter(
                    totalAmount,
                    categoryType[category]!!,
                    monthName,
                    "",
                    category,
                    lastTransactionDate
                )
                categoryTotalTransactions.add(categoryTotalTransaction)
                // Print or use this transaction object as needed
                println("SORTER Month : ${categoryTotalTransaction.category}: ${categoryTotalTransaction.amount}, Month: ${categoryTotalTransaction.date}, Last Transaction Date: ${categoryTotalTransaction.itemDate}")
            }
        }
    }

    fun calculateSpendingYearly(transactions: ArrayList<Transaction>) {
        transactions.reverse()

        // Map to store category-wise and year-wise transactions
        val categoryYearMap = mutableMapOf<String, MutableMap<Int, Float>>()
        val categoryLastTransactionDateMap = mutableMapOf<String, String>()

        for (transaction in transactions) {
            val category = transaction.category
            val amount = transaction.amount
            val dateTime = transaction.date
            val dateString = dateTime.split(" ")
            val formatter = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
            val date = formatter.parse(dateString[0])
            val calendar = Calendar.getInstance()
            calendar.time = date

            // Get the year for the transaction
            val year = calendar[Calendar.YEAR]

            // Initialize or get the category map if it exists
            val categoryMap = categoryYearMap.getOrPut(category) { mutableMapOf() }

            // Update the total amount for the corresponding category and year
            val existingValue = categoryMap[year]
            if (existingValue != null) {
                categoryMap[year] = existingValue + amount
            } else {
                categoryMap[year] = amount
            }

            // Update the last transaction date for the category
            categoryLastTransactionDateMap[category] = dateTime
        }

        // Iterate through the category and year map to create total transactions
        for ((category, yearMap) in categoryYearMap) {
            for ((year, totalAmount) in yearMap) {
                val lastTransactionDate = categoryLastTransactionDateMap[category]
                val categoryTotalTransaction = TransactionFilter(
                    totalAmount,
                    "Total",
                    year.toString(),
                    "",
                    category,
                    lastTransactionDate ?: ""
                )
                categoryTotalTransactions.add(categoryTotalTransaction)
                // Print or use this transaction object as needed
                println("SORTER : ${categoryTotalTransaction.category}: ${categoryTotalTransaction.amount}, Year: ${categoryTotalTransaction.date}, Last Transaction Date: ${categoryTotalTransaction.itemDate}")
            }
        }
    }

    fun calculateGraphData(transactions: ArrayList<TransactionFilter>) {
        if(transactions.isNotEmpty()) {
            graphDataList.clear()
            transactions.sortBy {
                it.date
            }
            var referenceDate = ""
            var income = 0f
            var expense = 0f

            transactions.forEachIndexed { index, transaction ->
                if (referenceDate == "") {
                    referenceDate = transaction.date
                }

                if (transaction.date == referenceDate) {
                    if (transaction.type == K.INCOME) {
                        income += transaction.amount
                    } else {
                        expense += transaction.amount
                    }
                    if (index == transactions.lastIndex) {
                        graphDataList.add(GraphDataModel(income, expense, referenceDate))
                    }
                } else {
                    graphDataList.add(GraphDataModel(income, expense, referenceDate))
                    referenceDate = transaction.date
                    income = if (transaction.type == K.INCOME) transaction.amount else 0f
                    expense = if (transaction.type != K.INCOME) transaction.amount else 0f

                    if (index == transactions.lastIndex) {
                        graphDataList.add(GraphDataModel(income, expense, referenceDate))
                    }

                }
                Log.d(TAG, "calculateGraphData: $income === $expense === $referenceDate")
            }
        }
    }

    fun calculatePieGraphData(transactions: ArrayList<TransactionFilter>) {
        if(transactions.isNotEmpty()){
            pieDataList.clear()
            var date = ""
            transactions.reverse()
            date = transactions[0].date

            // Filter transactions for the current date
            val filteredTransactions = transactions.filter { it.date == date }

            // Sort the filtered transactions by amount in descending order
            val sortedTransactions = filteredTransactions.sortedByDescending { it.amount }

            val otherAmount = sortedTransactions.drop(5).sumByDouble { it.amount.toDouble() }

            // Add top 5 transactions to pieDataList
            for (i in 0 until minOf(sortedTransactions.size, 5)) {
                if(sortedTransactions[i].type != K.INCOME ){
                    pieDataList.add(sortedTransactions[i])
                }
            }

            // Add 'Other' category if there are more than 5 transactions
            if (sortedTransactions.size > 5) {
                pieDataList.add(
                    TransactionFilter(
                        otherAmount.toFloat(),
                        "Other",
                        date,
                        "",
                        "Others",
                        date
                    )
                )
            }
        }
    }


}
