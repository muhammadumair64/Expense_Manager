package com.iobits.budgetexpensemanager.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.iobits.budgetexpensemanager.R
import com.iobits.budgetexpensemanager.databinding.FragmentBudgetBinding
import com.iobits.budgetexpensemanager.managers.AnalyticsManager
import com.iobits.budgetexpensemanager.managers.PreferenceManager
import com.iobits.budgetexpensemanager.myApplication.MyApplication
import com.iobits.budgetexpensemanager.ui.adapters.BudgetAdapter
import com.iobits.budgetexpensemanager.ui.adapters.TransactionTabAdapter
import com.iobits.budgetexpensemanager.ui.dataModels.BudgetCategory
import com.iobits.budgetexpensemanager.ui.dataModels.Transaction
import com.iobits.budgetexpensemanager.ui.dataModels.TransactionFilter
import com.iobits.budgetexpensemanager.ui.viewModels.DataShareViewModel
import com.iobits.budgetexpensemanager.ui.viewModels.MainViewModel
import com.iobits.budgetexpensemanager.utils.getCurrentDate
import com.iobits.budgetexpensemanager.utils.getCurrentDateTime
import com.iobits.budgetexpensemanager.utils.getCurrentMonthNumber
import com.iobits.budgetexpensemanager.utils.gone
import com.iobits.budgetexpensemanager.utils.handleBackPress
import com.iobits.budgetexpensemanager.utils.safeNavigate
import com.iobits.budgetexpensemanager.utils.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BudgetFragment : Fragment() {

    val TAG  = "BudgetFragmentTAG"

    private val mainViewModel: MainViewModel by activityViewModels()
    private val dataShareViewModel: DataShareViewModel by activityViewModels()
    val binding by lazy {
        FragmentBudgetBinding.inflate(layoutInflater)
    }
    private var tabAdapter: TransactionTabAdapter? = null
    private var budgetAdapter: BudgetAdapter? = null
    private var transactionList = ArrayList<Transaction>()
    var budgetList = ArrayList<BudgetCategory>()
    var newList =  ArrayList<TransactionFilter>()
    var month = ""
    var year = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        AnalyticsManager.logEvent("User_Is_In_Budget_Screen",null)
        performNavigation()
        initRv()
        initViews()
        transactionSorter()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        handleBackPress {
            mainViewModel.apply {
                onNavItemSelected?.invoke(1)
                navBarSetter?.invoke(1)
            }
        }
    }
    private fun initViews(){
        val savedDate = getCurrentDate().split("-")
         month = savedDate[1]
         year = savedDate[2]
        binding.createBudget.setOnClickListener {
            if(budgetList.size>1){
                if(MyApplication.mInstance.preferenceManager.getBoolean(
                        PreferenceManager.Key.IS_APP_PREMIUM,
                        false
                    )
                ){
                    mainViewModel.createBudgetClick?.invoke()
                }else{
                    dataShareViewModel.showPremium?.invoke()
                }
            }else{
                mainViewModel.createBudgetClick?.invoke()
            }
        }
    }

    private fun initRv() {
        budgetAdapter = BudgetAdapter(requireContext())
        tabAdapter = TransactionTabAdapter(requireContext())

        binding.topSelectionRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = tabAdapter
        }
        binding.budgetRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = budgetAdapter
        }
        tabAdapter?.apply {
            updateList(dataShareViewModel.budgetTabList)
            selectorValue = getCurrentMonthNumber()
        }
        binding.topSelectionRv.smoothScrollToPosition(getCurrentMonthNumber())
        budgetAdapter?.onClickAddMore={
            if(budgetList.size>1){
                if(MyApplication.mInstance.preferenceManager.getBoolean(
                        PreferenceManager.Key.IS_APP_PREMIUM,
                        false
                    )
                ){
                    mainViewModel.addMoreBudget?.invoke()
                }else{
                    dataShareViewModel.showPremium?.invoke()
                }
            }else{
                mainViewModel.addMoreBudget?.invoke()
            }
        }
        tabAdapter?.onClick={
           val item =  dataShareViewModel.budgetTabList[it]
            month =   item.substring(0, 3)
          lifecycleScope.launch {
             transactionSorter()
          }
        }
    }
    private fun transactionSorter() {
        mainViewModel.getAccount().observe(viewLifecycleOwner, Observer { account ->
            if(account != null) {
                lifecycleScope.launch {
                    if (account.transactions.isNotEmpty()) {
                        account.transactions.sortBy { it.date }
                        transactionList = account.transactions
                    }
                    Log.d(TAG, "transactionSorter: ${account.transactions} ")
                    lifecycleScope.launch(Dispatchers.IO) {
                        mainViewModel.apply {
                            categoryTotalTransactions.clear()
                            calculateSpendingMonthly(account.transactions)
                            calculateRemainingBudget()
                        }
                    }
                }
            }
        })
    }
    private suspend fun  calculateRemainingBudget(){
        newList.clear()
        budgetList.clear()
        Log.d(TAG, "calculateRemainingBudget: Month $month -- Year $year")
        mainViewModel.categoryTotalTransactions.forEach {
            Log.d(TAG, "calculateRemainingBudget: item date ${it.itemDate}")
            if(it.itemDate.contains(month) && it.itemDate.contains(year)){
                newList.add(it)
                Log.d(TAG, "calculateRemainingBudget: New Item $it")
            }
        }
        withContext(Dispatchers.Main){
            try{
                mainViewModel.getAccount().observe(viewLifecycleOwner, Observer {
                    if(it.budgets.isNotEmpty()){
                        lifecycleScope.launch {
                            it.budgets.forEach{ budget ->
                                budget.apply {
                                    val items = newList.filter { it.category == budget.category }
                                    Log.d(TAG, "calculateRemainingBudget: items is $items  Month $month")
                                    var budgetMonth = date.split("-")
                                    if(budgetMonth[1] ==  month){
                                        if(items.isNotEmpty()){
                                            budgetList.add(BudgetCategory(category,budget.amount,items[0].amount.toInt(),date,description))
                                        }else{
                                            budgetList.add(BudgetCategory(category,budget.amount,0,date,description))
                                        }
                                    }
                                }
                            }
                            if(budgetList.isEmpty()){
                                binding.noItemLayout.visible()
                            }else{
                                binding.noItemLayout.gone()
                            }
                            budgetAdapter?.updateList(budgetList)
                        }
                    }else{
                        binding.noItemLayout.visible()
                    }
                })
            }catch (e:Exception){
                Log.d(TAG, "calculateRemainingBudget: ${e.localizedMessage}")
            }
        }
    }
    private fun performNavigation() {
        mainViewModel.onNavItemSelected = {
            when (it) {
                1 -> {
                    safeNavigate(R.id.action_budgetFragment_to_homeFragment, R.id.budgetFragment)
                }

                2 -> {
                    safeNavigate(
                        R.id.action_budgetFragment_to_transactionsFragment,
                        R.id.budgetFragment
                    )
                }

                3 -> {
                    safeNavigate(
                        R.id.action_budgetFragment_to_analysisFragment,
                        R.id.budgetFragment
                    )
                }

                4 -> {}
            }
        }
    }
}
