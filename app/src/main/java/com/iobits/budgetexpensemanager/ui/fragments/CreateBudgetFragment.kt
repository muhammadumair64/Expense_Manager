package com.iobits.budgetexpensemanager.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.ads.AdSize
import com.google.android.material.datepicker.MaterialDatePicker
import com.iobits.budgetexpensemanager.R
import com.iobits.budgetexpensemanager.databinding.FragmentCreateBudgetBinding
import com.iobits.budgetexpensemanager.localDatabase.entities.Account
import com.iobits.budgetexpensemanager.localDatabase.entities.Budget
import com.iobits.budgetexpensemanager.localDatabase.entities.Categories
import com.iobits.budgetexpensemanager.managers.AnalyticsManager
import com.iobits.budgetexpensemanager.myApplication.MyApplication
import com.iobits.budgetexpensemanager.ui.adapters.TransactionCategoriesAdapter
import com.iobits.budgetexpensemanager.ui.fragments.bottom_sheets.CalculatorBottomSheetFragment
import com.iobits.budgetexpensemanager.ui.fragments.bottom_sheets.CategoryBottomSheetFragment
import com.iobits.budgetexpensemanager.ui.viewModels.MainViewModel
import com.iobits.budgetexpensemanager.utils.IconHelper
import com.iobits.budgetexpensemanager.utils.ItemSpacingDecoration
import com.iobits.budgetexpensemanager.utils.K
import com.iobits.budgetexpensemanager.utils.clearBackStack
import com.iobits.budgetexpensemanager.utils.disableMultipleClicking
import com.iobits.budgetexpensemanager.utils.getCurrentDateTime
import com.iobits.budgetexpensemanager.utils.getCurrentTime
import com.iobits.budgetexpensemanager.utils.handleBackPress
import com.iobits.budgetexpensemanager.utils.safeNavigate
import com.iobits.budgetexpensemanager.utils.visible
import com.iobits.budgetexpensemanager.utils.formatToCustomString
import kotlinx.coroutines.launch

class CreateBudgetFragment : Fragment() {
    val TAG = "CreateBudgetFragmentTAG"
    private val binding by lazy {
        FragmentCreateBudgetBinding.inflate(layoutInflater)
    }
    private var transactionAmount = ""
    var type = ""
    var account : Account? = null
    private var transactionAdapter: TransactionCategoriesAdapter? = null
    private val mainViewModel: MainViewModel by activityViewModels()

    var date = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            initViews()
            initListeners()
            loadAds()
        }catch (e:Exception){
            Log.d(TAG, "onCreateView: Error ${e.localizedMessage}")}
        // Inflate the layout for this fragment
        return binding.root
    }
    private fun loadAds(){
        MyApplication.mInstance.adsManager.showBanner(requireContext(), AdSize.BANNER,binding.adView,this.getString(R.string.ADMOB_BANNER_V2),binding.shimmerLayout);
    }
    private fun initViews() {
        mainViewModel.getAccount().observe(viewLifecycleOwner, Observer {
            if(it != null){
                account = it
            }
        })
        val datePicker =     MaterialDatePicker.Builder.datePicker().build()
        transactionAdapter = TransactionCategoriesAdapter(requireContext())
        binding.apply {
            binding.time.text = getCurrentTime()
            /** recycler view */
            recyclerView.apply {
                layoutManager = GridLayoutManager(requireContext(), 4)
                val spacingInPixels = resources.getDimensionPixelSize(R.dimen.spacing_between_items)
                val decor = ItemSpacingDecoration(spacingInPixels)
//                recyclerView.addItemDecoration(decor)
                adapter = transactionAdapter
            }
            calender.setOnClickListener {
                disableMultipleClicking(it)
                lifecycleScope.launch {
                datePicker.setStyle(R.style.Theme_BudgetExpenseManager,R.style.Theme_BudgetExpenseManager)
                datePicker.show(requireActivity().supportFragmentManager ,"Date_Picker")
                }
            }
            back.setOnClickListener {
                moveBack()
            }
            datePicker.apply {
                addOnPositiveButtonClickListener {
                    date = it.formatToCustomString()
                    dateTv.text = date
                }
            }

            transactionAdapter?.onClick = {
                if (it == K.AddNew) {
                    openCategoryBottomSheet()
                } else {
                    binding.apply {
                        category.text = it
                        categoryIcon.setImageResource(IconHelper.iconChooser(it))
                        iconCard.visible()
                        if (K.categoryColors.contains(it)) {
                            iconCard.setCardBackgroundColor(android.graphics.Color.parseColor(K.categoryColors[it]))
                        } else {
                            iconCard.setCardBackgroundColor(android.graphics.Color.parseColor(K.categoryColors[K.CUSTOM]))
                        }
                        val emptyArrayList = ArrayList<String>()
                        transactionAdapter!!.updateList(emptyArrayList)
                    }
                }
            }
        }
    }

    private fun initListeners() {
        mainViewModel.getCategories().observe(viewLifecycleOwner, Observer {
            if (it != null) {
                mainViewModel.apply {
                    incomeNameList = it.incomeList
                    expenseNameList = it.expenseList
                }
                if (type != "") {
                    if (type == K.INCOME) {
                        transactionAdapter!!.updateList(mainViewModel.incomeNameList)
                    } else {
                        transactionAdapter!!.updateList(mainViewModel.expenseNameList)
                    }
                }
            }
        })
        binding.apply {
            add.setOnClickListener {
                disableMultipleClicking(it)
                if (binding.amount.text != "") {
                    MyApplication.mInstance.adsManager.loadInterstitialAd(requireActivity()){
                    createBudget(transactionAmount)
                    }
                } else {
                    openBottomSheet()
                }
            }
            amount.setOnClickListener {
                openBottomSheet()
            }
            expenseCardData()
            incomeCard.setOnClickListener {
                type = K.INCOME
                income.setBackgroundResource(R.drawable.light_orange_gradient)
                expense.setBackgroundResource(R.drawable.button_bg)
                expense.alpha = 0.1f
                income.alpha = 1f
                transactionAdapter!!.updateList(mainViewModel.incomeNameList)
            }
            expenseCard.setOnClickListener {
             expenseCardData()
            }
        }
    }

    private fun expenseCardData(){
        binding.apply {
            type = K.EXPENSE
            expense.setBackgroundResource(R.drawable.light_orange_gradient)
            income.setBackgroundResource(R.drawable.button_bg)
            income.alpha = 0.1f
            expense.alpha = 1f
            transactionAdapter!!.updateList(mainViewModel.expenseNameList)
        }
    }
    override fun onResume() {
        super.onResume()
        handleBackPress {
            moveBack()
        }
    }

    private fun moveBack() {
        safeNavigate(
            R.id.action_addTransactionFragment_to_dashboardFragment,
            R.id.addTransactionFragment
        )
        clearBackStack(R.id.dashboardFragment,    false)
    }

    @SuppressLint("SetTextI18n")
    private fun openBottomSheet() {
        val bottomSheetFragment = CalculatorBottomSheetFragment()
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        bottomSheetFragment.value = {
            binding.amount.text = "${K.SYMBOL} $it"
            binding.addTv.text = "Create Budget"
            transactionAmount = it
        }
    }

    private fun openCategoryBottomSheet() {
        val bottomSheetFragment = CategoryBottomSheetFragment()
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        bottomSheetFragment.name = {
            if (type == K.INCOME) {
                val position = mainViewModel.incomeNameList.size - 1
                mainViewModel.incomeNameList.add(position, it)
                mainViewModel.insertCategories(
                    Categories(
                        0,
                        mainViewModel.expenseNameList,
                        mainViewModel.incomeNameList
                    )
                )
            } else {
                val position = mainViewModel.expenseNameList.size - 1
                mainViewModel.expenseNameList.add(position, it)
                mainViewModel.insertCategories(
                    Categories(
                        0,
                        mainViewModel.expenseNameList,
                        mainViewModel.incomeNameList
                    )
                )
            }
        }
    }

    private fun createBudget(amount: String) {
        val mDate = if(date != ""){
            date+binding.time.text
        }else{
            getCurrentDateTime()
        }
        if(amount != "" && binding.category.text != "") {
            try {
                val input : Double = amount.toDouble()
                account?.budgets?.add( Budget(0,binding.category.text.toString(),input.toInt(),mDate,binding.note.text.toString()))
                if(account != null){
                    account.let { it?.let { it1 -> mainViewModel.updateAccount(it1,requireContext()) } }
                }

                mainViewModel.updateResult = {
                    if(it){
                        AnalyticsManager.logEvent("User_Create_Budget_Successfully",null)
                        safeNavigate(R.id.action_createBudgetFragment_to_dashboardFragment,R.id.createBudgetFragment)
                    }
                }
            }catch (e:Exception){
                e.localizedMessage
            }

//         mainViewModel.insertBudgets()
        }else{
            Toast.makeText(requireContext(), "Please Select Category", Toast.LENGTH_SHORT).show()
        }
    }
}
