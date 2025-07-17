package com.iobits.budgetexpensemanager.ui.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.MobileAds
import com.iobits.budgetexpensemanager.R
import com.iobits.budgetexpensemanager.databinding.FragmentDashboardBinding
import com.iobits.budgetexpensemanager.managers.GoogleMobileAdsConsentManager
import com.iobits.budgetexpensemanager.managers.PreferenceManager
import com.iobits.budgetexpensemanager.myApplication.MyApplication
import com.iobits.budgetexpensemanager.ui.viewModels.AuthViewModel
import com.iobits.budgetexpensemanager.ui.viewModels.DataShareViewModel
import com.iobits.budgetexpensemanager.ui.viewModels.MainViewModel
import com.iobits.budgetexpensemanager.utils.AdsCounter
import com.iobits.budgetexpensemanager.utils.gone
import com.iobits.budgetexpensemanager.utils.handleBackPress
import com.iobits.budgetexpensemanager.utils.safeNavigate
import com.iobits.budgetexpensemanager.utils.showEmailChooser
import java.util.concurrent.atomic.AtomicBoolean


class DashboardFragment : Fragment() {
    val TAG = "DashboardFragmentTag"
    private val binding by lazy {
        FragmentDashboardBinding.inflate(layoutInflater)
    }
    private val mainViewModel: MainViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    private val dataShareViewModel: DataShareViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//      mainViewModel.getAccount()
        authViewModel.getProfileFromLocalDb()
        initViews()

        initListeners()
        return binding.root
    }

    fun initViews() {
        binding.animationView2.setOnClickListener {
           safeNavigate(R.id.action_dashboardFragment_to_premiumFragment,R.id.dashboardFragment)
        }

        dataShareViewModel.showPremium = {
          safeNavigate(R.id.action_dashboardFragment_to_premiumFragment,R.id.dashboardFragment)
        }


        binding.apply {
            drawerLayout.apply {
                setViewScale(Gravity.START, 0.9f)
                setViewElevation(Gravity.START, 20f)
                setRadius(Gravity.START, 20f)
            }
            menu.setOnClickListener {
                if (drawerLayout.isOpen) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    drawerLayout.openDrawer(GravityCompat.START)
                }
            }
            mainViewModel.clickOnRecentTransaction = {
                mainViewModel.selectedTransaction = it
                safeNavigate(R.id.action_dashboardFragment_to_transactionDetailsFragment,R.id.dashboardFragment)
            }
            navigationLayout.apply {

                premium.setOnClickListener {
                    MyApplication.mInstance.isOutForRating = true
                    safeNavigate(R.id.action_dashboardFragment_to_premiumFragment,R.id.dashboardFragment)
                }
                profile.setOnClickListener {
                    MyApplication.mInstance.isOutForRating = true
                safeNavigate(
                    R.id.action_dashboardFragment_to_profileFragment,
                    R.id.dashboardFragment
                )
            }
                privacyPolicy.setOnClickListener {
                    MyApplication.mInstance.isOutForRating = true
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                 showPrivacyPolicy(requireContext())
             }
                support.setOnClickListener {
                    MyApplication.mInstance.isOutForRating = true
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    if (MyApplication.mInstance.preferenceManager.getBoolean(
                            PreferenceManager.Key.IS_APP_PREMIUM,
                            false
                        )
                    ){
                        customerSupport()
                    }else{
                        safeNavigate(R.id.action_dashboardFragment_to_premiumFragment,R.id.dashboardFragment)
                    }
                }
                rateus.setOnClickListener {
                    MyApplication.mInstance.isOutForRating = true
                    val url = "https://play.google.com/store/apps/details?id=com.budgetplanner.expensetracker.managermoney"
                    val i = Intent(Intent.ACTION_VIEW)
                    i.setData(Uri.parse(url))
                    startActivity(i)
                    MyApplication.mInstance.preferenceManager.put(PreferenceManager.Key.SHOW_RATING_DIALOG,false)
                }
            }
        }
    }
    
    private fun customerSupport() {
        val supportEmail = "arbax8031@gmail.com" // Replace with your support email address
        val subject = "Support"
        val feedback = getString(R.string.app_name)
        requireContext().showEmailChooser(supportEmail, subject, feedback)
    }

    private fun showPrivacyPolicy(context: Context) {
        if (URLUtil.isValidUrl("https://igniteapps.blogspot.com/2024/03/privacy-policy-expense-tracker.html")) {
            val i = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://igniteapps.blogspot.com/2024/03/privacy-policy-expense-tracker.html")
            )
            context.startActivity(i)
        }
    }
    override fun onResume() {
        super.onResume()
        handleBackPress {
            requireActivity().finish()
        }

    }

    private fun initListeners() {
        mainViewModel.addMoreBudget = {
            safeNavigate(
                R.id.action_dashboardFragment_to_createBudgetFragment,
                R.id.dashboardFragment
            )
        }
        /** Bottom Nav Listeners */
        mainViewModel.navBarSetter = {
            Log.d(TAG, "initListeners: Invoke  ")
            when (it) {
                1 -> {
                    bottomNavSetter(1)
                }

                2 -> {
                    bottomNavSetter(2)
                }

                3 -> {
                    bottomNavSetter(3)
                }

                4 -> {
                    bottomNavSetter(4)
                }
            }
        }
        mainViewModel.createBudgetClick = {
            safeNavigate(
                R.id.action_dashboardFragment_to_createBudgetFragment,
                R.id.dashboardFragment
            )
        }
        binding.apply {
            home.setOnClickListener {
                bottomNavSetter(1)
                mainViewModel.onNavItemSelected?.invoke(1)
            }
            transaction.setOnClickListener {
                bottomNavSetter(2)
                mainViewModel.onNavItemSelected?.invoke(2)
            }
            analysis.setOnClickListener {
                bottomNavSetter(3)
                mainViewModel.onNavItemSelected?.invoke(3)
            }
            budget.setOnClickListener {
                bottomNavSetter(4)
                mainViewModel.onNavItemSelected?.invoke(4)
            }
            addTransaction.setOnClickListener {
                safeNavigate(
                    R.id.action_dashboardFragment_to_addTransactionFragment,
                    R.id.dashboardFragment
                )
            }
        }
    }

    private fun bottomNavSetter(item: Int) {
        binding.apply {
            homeTxt.setTextColor(resources.getColor(R.color.light_Text))
            transactionTxt.setTextColor(resources.getColor(R.color.light_Text))
            analysisTxt.setTextColor(resources.getColor(R.color.light_Text))
            budgetTxt.setTextColor(resources.getColor(R.color.light_Text))

            homeIc.setImageResource(R.drawable.gray_home)
            transactionIc.setImageResource(R.drawable.gray_transaction)
            analysisIc.setImageResource(R.drawable.gray_analysis)
            budgetIc.setImageResource(R.drawable.gray_budget)

            when (item) {
                1 -> {
                    title.text = "Expense Manager"
                    homeTxt.setTextColor(resources.getColor(R.color.textColor))
                    homeIc.setImageResource(R.drawable.grad_home)
                }

                2 -> {
                    title.text = "Transactions"
                    transactionTxt.setTextColor(resources.getColor(R.color.textColor))
                    transactionIc.setImageResource(R.drawable.grad_transcation)
                }

                3 -> {
                    title.text = "Analysis"
                    analysisTxt.setTextColor(resources.getColor(R.color.textColor))
                    analysisIc.setImageResource(R.drawable.grad_analysis)
                }

                4 -> {
                    title.text = "Budget"
                    budgetTxt.setTextColor(resources.getColor(R.color.textColor))
                    budgetIc.setImageResource(R.drawable.grad_budget)
                }
            }
        }
    }


}
