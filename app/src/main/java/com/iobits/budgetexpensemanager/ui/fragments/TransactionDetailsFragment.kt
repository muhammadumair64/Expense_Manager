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
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.iobits.budgetexpensemanager.R
import com.iobits.budgetexpensemanager.databinding.FragmentTransactionDetailsBinding
import com.iobits.budgetexpensemanager.managers.AdsManager
import com.iobits.budgetexpensemanager.myApplication.MyApplication
import com.iobits.budgetexpensemanager.ui.dataModels.Transaction
import com.iobits.budgetexpensemanager.ui.fragments.bottom_sheets.CalculatorBottomSheetFragment
import com.iobits.budgetexpensemanager.ui.viewModels.MainViewModel
import com.iobits.budgetexpensemanager.utils.IconHelper
import com.iobits.budgetexpensemanager.utils.K
import com.iobits.budgetexpensemanager.utils.disableMultipleClicking
import com.iobits.budgetexpensemanager.utils.formatToCustomString
import com.iobits.budgetexpensemanager.utils.getCurrentDateTime
import com.iobits.budgetexpensemanager.utils.gone
import com.iobits.budgetexpensemanager.utils.observeOnce
import com.iobits.budgetexpensemanager.utils.popBackStack
import java.text.SimpleDateFormat
import java.util.Calendar


class TransactionDetailsFragment : Fragment() {

    val binding by lazy {
        FragmentTransactionDetailsBinding.inflate(layoutInflater)
    }
    val TAG = "DetailsFragmentTAG"
    private val mainViewModel: MainViewModel by activityViewModels()
    var transaction: Transaction? = null
    var amount = 0f
    var isChangingApply = false
    var date = ""
    var time = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initViews()
        loadAds()
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    fun initViews() {
        val datePicker =   MaterialDatePicker.Builder.datePicker().build()
        val timePicker =   MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(10)
            .build()

        transaction = mainViewModel.selectedTransaction
        binding.apply {
            amount.text = K.SYMBOL + transaction?.amount.toString()
            note.text = transaction?.description.toString()
            val myDateTime = transaction?.date
            val splitDateTime = myDateTime?.split(" ")
            dateTv.text = splitDateTime?.get(0) ?: ""
            time.text = splitDateTime?.get(1) ?: ""
            transaction?.let { IconHelper.iconChooser(it.category) }
                ?.let { categoryIcon.setImageResource(it) }
            if (K.categoryColors.contains(transaction?.category)) {
                iconCard.setCardBackgroundColor(android.graphics.Color.parseColor(K.categoryColors[transaction?.category]))
            } else {
                iconCard.setCardBackgroundColor(android.graphics.Color.parseColor(K.categoryColors[K.CUSTOM]))
            }
            category.text = transaction?.category
            currency.text = K.SYMBOL

            edit.setOnClickListener {
                disableMultipleClicking(it)
                openBottomSheet()
            }
            save.setOnClickListener {
                if(isChangingApply){
                    updateTransaction()
                }else{
                    findNavController().popBackStack()
                }
            }
            delete.setOnClickListener {
                deleteTransaction()
            }
            if((transaction?.description?.length ?: 0) == 0){
                binding.constraintLayout9.gone()
            }
            calender.setOnClickListener {
                disableMultipleClicking(it)
                datePicker.setStyle(R.style.Theme_BudgetExpenseManager,R.style.Theme_BudgetExpenseManager)
                datePicker.show(requireActivity().supportFragmentManager ,"Date_Picker")
            }
            time.setOnClickListener {
                disableMultipleClicking(it)
                timePicker.setStyle(R.style.Theme_BudgetExpenseManager,R.style.Theme_BudgetExpenseManager)
                timePicker.show(requireActivity().supportFragmentManager,"Time_Picker")
            }
            back.setOnClickListener {
                popBackStack()
            }
        }
        datePicker.apply {
            addOnPositiveButtonClickListener {
                isChangingApply = true
                date = it.formatToCustomString()
                binding.dateTv.text = date
            }
        }
        timePicker.addOnPositiveButtonClickListener {
            isChangingApply = true
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
            binding.time.text =  time
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

    private fun deleteTransaction() {
        mainViewModel.getAccount().observeOnce(viewLifecycleOwner, Observer { account ->
            var transactions = ArrayList<Transaction>()
               transactions = account.transactions
               transactions.remove(transaction)
               account.transactions = transactions
               mainViewModel.updateAccount(account, requireContext())
               Toast.makeText(requireContext(), "Transaction Delete successfully", Toast.LENGTH_SHORT).show()
               findNavController().popBackStack()
        })
    }

    private fun openBottomSheet() {
        val bottomSheetFragment = CalculatorBottomSheetFragment()
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        bottomSheetFragment.value = {
            amount = it.toFloat()
            binding.amount.text = "${K.SYMBOL} $it"
            binding.addTv.text = "Update"
            isChangingApply = true
        }
    }
    private fun updateTransaction() {

        var previousAmount  = 0f
        val mDate = if(date != "" ){
            if(time != ""){
                date+" "+time
            } else {
                date+" "+binding.time.text
            }
        }else{
            getCurrentDateTime()
        }
        mainViewModel.getAccount().observeOnce(viewLifecycleOwner, Observer { account ->
            Log.d(TAG, "updateTransaction: $account ===== date is $mDate")
            if (account.transactions.isNotEmpty()){
                val transactions = ArrayList<Transaction>()
                transactions.addAll(account.transactions)
                val index = transactions.indexOf(transaction)
                previousAmount = transactions[index].amount
                if (amount == 0f) {
                    amount = previousAmount
                }
               val newEntry  =  Transaction(
                        amount.toFloat(),
                        transaction!!.type,
                        mDate,
                        binding.note.text.toString(),
                        binding.category.text.toString()
                    )

                transactions.add(index,newEntry)
                transactions.remove(transaction)

                if ( transaction!!.type == K.INCOME) {
                    account.currentBalance -= previousAmount
                    account.income -= previousAmount
                    account.currentBalance += amount.toFloat()
                    account.income += amount.toFloat()
                } else {
                    account.currentBalance += previousAmount
                    account.expense -= previousAmount
                    account.currentBalance -= amount.toFloat()
                    account.expense += amount.toFloat()
                }
                account.transactions = transactions
                mainViewModel.updateAccount(account, requireContext())
                Log.d(TAG, "updateTransaction: $transactions")
               // Do Next move here
                Toast.makeText(requireContext(), "Transaction update successfully", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        })
    }
}