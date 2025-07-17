package com.iobits.budgetexpensemanager.ui.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.ads.AdSize
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.iobits.budgetexpensemanager.R
import com.iobits.budgetexpensemanager.databinding.FragmentAddTransactionBinding
import com.iobits.budgetexpensemanager.localDatabase.entities.Categories
import com.iobits.budgetexpensemanager.managers.AnalyticsManager
import com.iobits.budgetexpensemanager.managers.PreferenceManager
import com.iobits.budgetexpensemanager.myApplication.MyApplication
import com.iobits.budgetexpensemanager.ui.adapters.TransactionCategoriesAdapter
import com.iobits.budgetexpensemanager.ui.dataModels.Transaction
import com.iobits.budgetexpensemanager.ui.fragments.bottom_sheets.CalculatorBottomSheetFragment
import com.iobits.budgetexpensemanager.ui.fragments.bottom_sheets.CategoryBottomSheetFragment
import com.iobits.budgetexpensemanager.ui.viewModels.DataShareViewModel
import com.iobits.budgetexpensemanager.ui.viewModels.MainViewModel
import com.iobits.budgetexpensemanager.utils.IconHelper
import com.iobits.budgetexpensemanager.utils.K
import com.iobits.budgetexpensemanager.utils.clearBackStack
import com.iobits.budgetexpensemanager.utils.disableMultipleClicking
import com.iobits.budgetexpensemanager.utils.getCurrentDateTime
import com.iobits.budgetexpensemanager.utils.getCurrentTime
import com.iobits.budgetexpensemanager.utils.handleBackPress
import com.iobits.budgetexpensemanager.utils.safeNavigate
import com.iobits.budgetexpensemanager.utils.visible
import com.iobits.budgetexpensemanager.utils.formatToCustomString
import com.iobits.budgetexpensemanager.utils.observeOnce
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlin.math.exp


class AddTransactionFragment : Fragment() {
    val TAG = "AddTransactionTAG"
    private val binding by lazy {
        FragmentAddTransactionBinding.inflate(layoutInflater)
    }
    private var transactionAmount = ""
    var type = ""
    private var transactionAdapter: TransactionCategoriesAdapter? = null
    private val mainViewModel: MainViewModel by activityViewModels()
    private val shareViewModel: DataShareViewModel by activityViewModels()

    var  dialog: AlertDialog? = null

    var isGetData = false
    var date = ""
    var time = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initViews()
        loadAds()
        initListeners()
        expenseType()
        return binding.root
    }

    private fun loadAds() {
        MyApplication.mInstance.adsManager.showBanner(
            requireContext(),
            AdSize.LARGE_BANNER,
            binding.adView,
            this.getString(R.string.ADMOB_BANNER_V2),
            binding.shimmerLayout
        );
    }

    private fun initViews() {
        mainViewModel.enteredAmount = 0

        val datePicker = MaterialDatePicker.Builder.datePicker().build()
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(10)
            .build()
        transactionAdapter = TransactionCategoriesAdapter(requireContext())
        binding.apply {
            binding.time.text = getCurrentTime()
            /** recycler view */
            recyclerView.apply {
                layoutManager = GridLayoutManager(requireContext(), 4)
                adapter = transactionAdapter
            }
            back.setOnClickListener {
                safeNavigate(
                    R.id.action_addTransactionFragment_to_dashboardFragment,
                    R.id.addTransactionFragment
                )
            }
            calender.setOnClickListener {
                disableMultipleClicking(it)
                datePicker.setStyle(
                    R.style.Theme_BudgetExpenseManager,
                    R.style.Theme_BudgetExpenseManager
                )
                datePicker.show(requireActivity().supportFragmentManager, "Date_Picker")
            }
            time.setOnClickListener {
                disableMultipleClicking(it)
                timePicker.setStyle(
                    R.style.Theme_BudgetExpenseManager,
                    R.style.Theme_BudgetExpenseManager
                )
                timePicker.show(requireActivity().supportFragmentManager, "Time_Picker")
            }
            datePicker.apply {
                addOnPositiveButtonClickListener {
                    date = it.formatToCustomString()
                    dateTv.text = date
                }
            }

            transactionAdapter?.onClick = {
                if (it == K.AddNew) {
                    if (MyApplication.mInstance.preferenceManager.getBoolean(
                            PreferenceManager.Key.IS_APP_PREMIUM,
                            false
                        )
                    ) {
                        openCategoryBottomSheet()
                    }else{
                       safeNavigate(R.id.action_addTransactionFragment_to_premiumFragment,R.id.addTransactionFragment)
                    }

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
        timePicker.addOnPositiveButtonClickListener {
            val selectedHour = timePicker.hour
            val selectedMinute = timePicker.minute

            // Use Calendar to set the selected time
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
            }

            // Format the time as hh:mm a (AM/PM)
            val simpleDateFormat = SimpleDateFormat("hh:mm a")
            val formattedTime = simpleDateFormat.format(calendar.time)
            time = formattedTime
            binding.time.text = time
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
            amountLayout.setOnClickListener {
                disableMultipleClicking(it)
                openBottomSheet()
            }
            edit.setOnClickListener {
                disableMultipleClicking(it)
                openBottomSheet()
            }
            add.setOnClickListener {
                disableMultipleClicking(it)
                if (binding.amount.text != "") {
                    if(category.text == ""){
                    showTransactionOptions()
                    }else{
                        MyApplication.mInstance.adsManager.loadInterstitialAd(requireActivity()) {
                            saveTransaction(transactionAmount)
                        }

                    }
                } else {
                    openBottomSheet()
                }
            }
            incomeCard.setOnClickListener {
                incomeType()
            }
            expenseCard.setOnClickListener {
                expenseType()
            }
        }
    }

    private fun expenseType() {
        binding.apply {
            type = K.EXPENSE
            expense.setBackgroundResource(R.drawable.light_orange_gradient)
            income.setBackgroundResource(R.drawable.button_bg)
            income.alpha = 0.1f
            expense.alpha = 1f
            transactionAdapter!!.updateList(mainViewModel.expenseNameList)
        }
    }

    private  fun incomeType(){
        binding.apply {
            type = K.INCOME
            income.setBackgroundResource(R.drawable.light_orange_gradient)
            expense.setBackgroundResource(R.drawable.button_bg)
            expense.alpha = 0.1f
            income.alpha = 1f
            transactionAdapter!!.updateList(mainViewModel.incomeNameList)
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
        clearBackStack(R.id.dashboardFragment, false)
    }

    @SuppressLint("SetTextI18n")
    private fun openBottomSheet() {
        try {
            val bottomSheetFragment = CalculatorBottomSheetFragment()
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
            bottomSheetFragment.value = {
                binding.amount.text = "${K.SYMBOL} $it"
                binding.addTv.text = "Add Transaction"
                transactionAmount = it
                mainViewModel.enteredAmount = it.toInt()
            }
        }catch (e:Exception){
            Log.d(TAG, "openBottomSheet: ${e.localizedMessage}")}
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

    private fun saveTransaction(amount: String) {
        val mDate = if (date != "") {
            if (time != "") {
                "$date $time"
            } else {
                date + " " + binding.time.text
            }
        } else {
            getCurrentDateTime()
        }
        if (amount != "" && binding.category.text != "") {
            mainViewModel.getAccount().observeOnce(viewLifecycleOwner, Observer { account ->
                try {
                    if (!isGetData) {
                        isGetData = true
                        if (account != null) {
                            if (account.transactions.isEmpty()) {
                                val transaction = ArrayList<Transaction>()
                                transaction.add(
                                    Transaction(
                                        amount.toFloat(),
                                        type,
                                        mDate,
                                        binding.note.text.toString(),
                                        binding.category.text.toString()
                                    )
                                )
                                if (type == K.INCOME) {
                                    account.currentBalance += amount.toFloat()
                                    account.income += amount.toFloat()
                                } else {
                                    account.currentBalance -= amount.toFloat()
                                    account.expense += amount.toFloat()
                                }
                                account.transactions = transaction
                                mainViewModel.updateAccount(account, requireContext())
                                moveBack()
                            }
                            else {
                                if (type == K.INCOME) {
                                    account.currentBalance += amount.toFloat()
                                    account.income += amount.toFloat()
                                } else {
                                    account.currentBalance -= amount.toFloat()
                                    account.expense += amount.toFloat()
                                }
                                account.transactions.add(
                                    Transaction(
                                        amount.toFloat(),
                                        type,
                                        mDate,
                                        binding.note.text.toString(),
                                        binding.category.text.toString()
                                    )
                                )
                                mainViewModel.updateAccount(account, requireContext())
                                Log.d(TAG, "ADD_AMOUNT: else Block")
                                AnalyticsManager.logEvent("User_Add_Transaction_Successfully", null)
                                moveBack()
                            }
                        }
                    }
                }catch (e:Exception){
                    Log.d(TAG, "saveTransaction: ERROR ${e.localizedMessage}")
                }
            })
        } else {
            Toast.makeText(requireContext(), "Please Enter amount & category", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun showTransactionOptions(){
        try {

            val alertCustomdialog: View = LayoutInflater.from(requireContext()).inflate(R.layout.select_transaction_dialog, null)

            //initialize alert builder.
            val alert: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            alert.setView(alertCustomdialog);
            val income = alertCustomdialog.findViewById<AppCompatButton>(R.id.income_btn)
            val expense = alertCustomdialog.findViewById<AppCompatButton>(R.id.expense_btn)

            dialog = alert.create()
            //this line removed app bar from dialog and make it transperent and you see the image is like floating outside dialog box.
            dialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            //finally show the dialog box in android all

            dialog?.show()

            expense.setOnClickListener {
                expenseType()
                Toast.makeText(requireContext(), "Please Select Category", Toast.LENGTH_SHORT).show()
            dialog?.dismiss()
            }

            income.setOnClickListener {
                incomeType()
                Toast.makeText(requireContext(), "Please Select Category", Toast.LENGTH_SHORT).show()
                dialog?.dismiss()
            }

        } catch (e: Exception) {
            e.localizedMessage
        }
    }
}
