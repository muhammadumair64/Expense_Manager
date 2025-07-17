package com.iobits.budgetexpensemanager.ui.fragments

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.iobits.budgetexpensemanager.R
import com.iobits.budgetexpensemanager.databinding.FragmentCreateAccountBinding
import com.iobits.budgetexpensemanager.localDatabase.entities.Profile
import com.iobits.budgetexpensemanager.managers.AnalyticsManager
import com.iobits.budgetexpensemanager.ui.adapters.CurrencyAdapter
import com.iobits.budgetexpensemanager.ui.viewModels.AuthViewModel
import com.iobits.budgetexpensemanager.ui.viewModels.DataShareViewModel
import com.iobits.budgetexpensemanager.ui.viewModels.MainViewModel
import com.iobits.budgetexpensemanager.utils.CountrySymbols
import com.iobits.budgetexpensemanager.utils.K
import com.iobits.budgetexpensemanager.utils.TinyDB
import com.iobits.budgetexpensemanager.utils.disableMultipleClicking
import com.iobits.budgetexpensemanager.utils.hideKeyboard
import com.iobits.budgetexpensemanager.utils.safeNavigate

class CreateAccountFragment : Fragment() {
    val TAG = "CreateAccountFragment"
    private val binding by lazy {
        FragmentCreateAccountBinding.inflate(layoutInflater)
    }
    private val authViewModel: AuthViewModel by activityViewModels()
    private val dataShareViewModel: DataShareViewModel by activityViewModels()
    private var tinyDB : TinyDB? = null
    private var currencySymbol = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        tinyDB = TinyDB(requireContext())
        initViews()
        return binding.root
    }

    private fun initViews() {
        binding.apply {
            currencySelector.setOnClickListener {
                hideKeyboard(binding.root)
                disableMultipleClicking(it)
                currency()
            }

            login.setOnClickListener {
                safeNavigate(
                    R.id.action_createAccountFragment_to_loginFragment,
                    R.id.createAccountFragment
                )
            }

            start.setOnClickListener {
                if (accountName.text.length >= 3 && currencySymbol != "") {
                    if(tinyDB?.getBoolean(K.IsLoginSkipped) == true){
                        skippedUser()
                    }else{
                        registeredUser()
                    }

                } else {
                    if (accountName.text.length < 3) {
                        accountName.error = "Invalid Name"
                    }
                    if (currencySymbol == "") {
                        currency.error = "Invalid Currency"
                    }
                }
            }
        }
    }

    private fun registeredUser(){
        val profile = Profile(
            0,
            binding.accountName.text.toString(),
            "",
            authViewModel.email,
            currencySymbol
        )
        authViewModel.apply {
            authViewModel.startLoading?.invoke(true)
            uploadProfile(profile,"0")
            dataShareViewModel.uploadCategories()
        }
        authViewModel.result = {
            if (it) {
                AnalyticsManager.logEvent("User_Create_Account_Successfully",null)
                authViewModel.startLoading?.invoke(false)
                safeNavigate(
                    R.id.action_createAccountFragment_to_premiumFragment,
                    R.id.createAccountFragment
                )
            } else {
                authViewModel.startLoading?.invoke(false)
                Toast.makeText(requireContext(), "Account not created. Please try again", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun skippedUser(){
        val profile = Profile(
            0,
            binding.accountName.text.toString(),
            "",
            "",
            currencySymbol
        )
        authViewModel.apply {
            authViewModel.startLoading?.invoke(true)
            insertAccount(0f,false)
            insertProfile(profile)
            dataShareViewModel.insertCategories()
        }
        authViewModel.result = {
            if (it) {
                AnalyticsManager.logEvent("User_Create_Account_Successfully",null)
                authViewModel.startLoading?.invoke(false)
                safeNavigate(
                    R.id.action_createAccountFragment_to_premiumFragment,
                    R.id.createAccountFragment
                )
            } else {
                authViewModel.startLoading?.invoke(false)
                Toast.makeText(requireContext(), "Account not created. Please try again", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun currency() {
        val dialogBuilder = AlertDialog.Builder(context)
        val dialog: View = layoutInflater.inflate(R.layout.layout_currency_dialog, null)
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.80).toInt()
        val alertDialog = dialogBuilder.create()
        alertDialog.setView(dialog)
        try {
            alertDialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        alertDialog.window?.setLayout(width, height);
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val mList = CountrySymbols.countryList
        Log.d(TAG, "currency: $mList")
        val mAdapter = CurrencyAdapter(requireContext(), mList)
        dialog.findViewById<RecyclerView>(R.id.currency_rv).apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
        mAdapter.onClick = {
            /** selected currency */
            currencySymbol = it.symbols
            binding.currency.setText(currencySymbol)
            alertDialog.dismiss()
        }
        addFiltrationToRecyclerView(dialog, mAdapter)
    }

    private fun addFiltrationToRecyclerView(
        dialog: View,
        mAdapter: CurrencyAdapter
    ) {
        dialog.findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.et_all_search)
            .addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    charSequence: CharSequence, i: Int, i1: Int,
                    i2: Int
                ) {

                }

                override fun onTextChanged(
                    charSequence: CharSequence, i: Int, i1: Int,
                    i2: Int
                ) {

                }

                override fun afterTextChanged(editable: Editable) {
                    mAdapter.getFilter()
                        .filter(dialog.findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.et_all_search).text.toString())
                }
            })
    }
}
