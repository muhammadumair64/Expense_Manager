package com.iobits.budgetexpensemanager.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.iobits.budgetexpensemanager.R
import com.iobits.budgetexpensemanager.databinding.FragmentTransactionsBinding
import com.iobits.budgetexpensemanager.managers.AdsManager
import com.iobits.budgetexpensemanager.managers.AnalyticsManager
import com.iobits.budgetexpensemanager.myApplication.MyApplication
import com.iobits.budgetexpensemanager.ui.adapters.TransactionFilterAdapter
import com.iobits.budgetexpensemanager.ui.adapters.TransactionTabAdapter
import com.iobits.budgetexpensemanager.ui.dataModels.Transaction
import com.iobits.budgetexpensemanager.ui.viewModels.DataShareViewModel
import com.iobits.budgetexpensemanager.ui.viewModels.MainViewModel
import com.iobits.budgetexpensemanager.utils.gone
import com.iobits.budgetexpensemanager.utils.handleBackPress
import com.iobits.budgetexpensemanager.utils.safeNavigate
import com.iobits.budgetexpensemanager.utils.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class TransactionsFragment : Fragment() {
    val TAG = "TransactionsFragmentTAG"
    val binding by lazy {
        FragmentTransactionsBinding.inflate(layoutInflater)
    }
    private var tabAdapter: TransactionTabAdapter? = null
    private val mainViewModel: MainViewModel by activityViewModels()
    private val dataShareViewModel: DataShareViewModel by activityViewModels()
    private var transactionList = ArrayList<Transaction>()
    private var transactionAdapter: TransactionFilterAdapter? = null

    var startDate :Date? = null
    var endDate :Date? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        AnalyticsManager.logEvent("User_Is_In_Transactions_Screen",null)

        initView()
        initListeners()
        performNavigation()
        transactionSorter()
         loadAds()

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

    private fun loadAds(){
        MyApplication.mInstance.adsManager.loadNativeAd(
            requireActivity(),
            binding.adView,
            AdsManager.NativeAdType.NOMEDIA_MEDIUM,
            getString(R.string.ADMOB_NATIVE_WITHOUT_MEDIA_V2),
            binding.shimmerLayout
        )    }

    private fun datePicker() {
        val builder = MaterialDatePicker.Builder.dateRangePicker()
        builder.setTitleText("Select Starting and Ending Dates")
        val constraintsBuilder = CalendarConstraints.Builder()

        val currentMillis = System.currentTimeMillis()
        constraintsBuilder.setStart(currentMillis - TimeUnit.DAYS.toMillis(365 * 3)) // Example: Set minimum year to 5 years ago
        constraintsBuilder.setEnd(currentMillis + TimeUnit.DAYS.toMillis(365 * 3))   // Example: Set maximum year to 5 years in the future

        builder.setCalendarConstraints(constraintsBuilder.build())

        val materialDatePicker = builder.build()
        materialDatePicker.show(requireActivity().supportFragmentManager, "tagone")
        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            startDate = Date(selection.first)
            endDate = Date(selection.second)

            val dateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
            val formattedStartDate = dateFormat.format(startDate)
            val formattedEndDate = dateFormat.format(endDate)

            lifecycleScope.launch {
                mainViewModel.calculateSpendingDaily(transactionList)
                val myList =  mainViewModel.filterTransactionsByDate(mainViewModel.categoryTotalTransactions,formattedStartDate,formattedEndDate)
                if(myList.isEmpty()){
                    binding.noItemLayout.visible()
                }
                binding.progress.gone()
                transactionAdapter?.updateList(myList as ArrayList,1)
                Log.d(TAG, "datePicker SORTED LIST:$myList ")
            }
        }
        materialDatePicker.addOnDismissListener {
            binding.progress.gone()
        }
    }

    private fun initListeners() {
        tabAdapter?.onClick ={
            mainViewModel.apply {
             categoryTotalTransactions.clear()
            when(it){
                0 ->{
                    lifecycleScope.launch {
                     calculateSpendingDaily(transactionList)
                     transactionAdapter?.updateList(categoryTotalTransactions,1)
                    }
                }
                1 ->{        lifecycleScope.launch {
                    calculateSpendingWeekly(transactionList)
                    transactionAdapter?.updateList(categoryTotalTransactions,2)
                }}
                2 ->{        lifecycleScope.launch {
                    calculateSpendingMonthly(transactionList)
                    transactionAdapter?.updateList(categoryTotalTransactions,3)
                }}
                3 ->{        lifecycleScope.launch {
                    calculateSpendingYearly(transactionList)
                    transactionAdapter?.updateList(categoryTotalTransactions,4)
                }}
                4-> {
                    binding.progress.visible()
                    datePicker()
                  }
               }
            }
        }
    }
    private fun initView() {
        tabAdapter = TransactionTabAdapter(requireContext())
        transactionAdapter = TransactionFilterAdapter(requireContext())
        initListeners()
        binding.topSelectionRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = tabAdapter
        }
        binding.transactionRv.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = transactionAdapter
        }
        tabAdapter?.updateList(dataShareViewModel.tabList)

    }

    @SuppressLint("SetTextI18n")
    private fun transactionSorter() {
        mainViewModel.getAccount().observe(viewLifecycleOwner, Observer { account ->
            if(account != null){
                binding.apply {
                    incomeTv.text = "+"+account.income
                    expenseTv.text = "-"+account.expense
                }
                lifecycleScope.launch {
                    if (account.transactions.isNotEmpty()) {
                        account.transactions.sortBy { it.date }
                        transactionList = account.transactions
                    }
                        Log.d(TAG, "transactionSorter: ${account.transactions} ")
                        lifecycleScope.launch(Dispatchers.IO) {
                            mainViewModel.apply {
                            categoryTotalTransactions.clear()
                            calculateSpendingDaily(account.transactions)
                            withContext(Dispatchers.Main){
                                if(categoryTotalTransactions.isNotEmpty()){
                                    binding.noItemLayout.gone()
                                }
                                    transactionAdapter?.updateList(categoryTotalTransactions,1)
                            }
                            }
                        }
                }
            }
        })
    }
    private fun performNavigation() {
        mainViewModel.onNavItemSelected = {
            when (it) {
                1 -> {
                    safeNavigate(
                        R.id.action_transactionsFragment_to_homeFragment,
                        R.id.transactionsFragment
                    )
                }
                2 -> {}
                3 -> {
                    safeNavigate(
                        R.id.action_transactionsFragment_to_analysisFragment,
                        R.id.transactionsFragment
                    )
                }
                4 -> {
                    safeNavigate(
                        R.id.action_transactionsFragment_to_budgetFragment,
                        R.id.transactionsFragment
                    )
                }
            }
        }
    }
}
