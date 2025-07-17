package com.iobits.budgetexpensemanager.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.MobileAds
import com.iobits.budgetexpensemanager.R
import com.iobits.budgetexpensemanager.databinding.FragmentHomeBinding
import com.iobits.budgetexpensemanager.managers.GoogleMobileAdsConsentManager
import com.iobits.budgetexpensemanager.managers.PreferenceManager
import com.iobits.budgetexpensemanager.myApplication.MyApplication
import com.iobits.budgetexpensemanager.ui.adapters.RecentAdapter
import com.iobits.budgetexpensemanager.ui.adapters.TotalSpendingAdapter
import com.iobits.budgetexpensemanager.ui.dataModels.Transaction
import com.iobits.budgetexpensemanager.ui.fragments.bottom_sheets.RatingBottomSheetFragment
import com.iobits.budgetexpensemanager.ui.viewModels.AuthViewModel
import com.iobits.budgetexpensemanager.ui.viewModels.DataShareViewModel
import com.iobits.budgetexpensemanager.ui.viewModels.MainViewModel
import com.iobits.budgetexpensemanager.utils.AdsCounter
import com.iobits.budgetexpensemanager.utils.K
import com.iobits.budgetexpensemanager.utils.gone
import com.iobits.budgetexpensemanager.utils.handleBackPress
import com.iobits.budgetexpensemanager.utils.safeNavigate
import com.iobits.budgetexpensemanager.utils.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class HomeFragment : Fragment() {
    val TAG = "HomeFragmentTag"
    private val binding by lazy {
        FragmentHomeBinding.inflate(layoutInflater)
    }
    private var totalSpendingsAdapter: TotalSpendingAdapter? = null
    private var recentAdapter: RecentAdapter? = null
    private val mainViewModel: MainViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    private val dataShareViewModel: DataShareViewModel by activityViewModels()

    private val isMobileAdsInitializeCalled = AtomicBoolean(false)
    lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: HOME CREATED ")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        showConsent()
        initViews()
        performNavigation()
        return binding.root
    }
    override fun onResume() {
        super.onResume()
        showRatingDialog()
            handleBackPress {
                requireActivity().moveTaskToBack(true)
            }
        if(AdsCounter.showPro()){
          dataShareViewModel.showPremium?.invoke()
        }
            mainViewModel.getAccount().observe(viewLifecycleOwner, Observer {
                try {
                    if (it != null) {
                        Log.d(TAG, "initViews : Transactions data = $it ")
                        binding.apply {
                            totalBalance.text =
                                "${authViewModel.profileData.value?.currency} " + it.currentBalance.toString()
                            income.text = "+ ${K.SYMBOL} " + it.income.toString()
                            expense.text = "- ${K.SYMBOL} " + it.expense.toString()
                        }
                        recentAdapter?.updateList(it.transactions)
                        if(it.transactions.isNotEmpty()) {
                            binding.totalSpendingRv.visible()
                            calculateSpending(it.transactions)
                        } else {
                            binding.spendingsLayout.gone()
                        }
                    }else{
                        binding.spendingsLayout.gone()
                        Log.d(TAG, "Transactions Null")
                    }
                } catch (e: Exception){
                    binding.spendingsLayout.gone()
                    Log.d(TAG, "onResume: ERROR ${e.localizedMessage}")
                }
     })
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        totalSpendingsAdapter = TotalSpendingAdapter(requireContext())
        recentAdapter = RecentAdapter(requireContext())
        binding.apply {
            totalSpendingRv.apply {
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                    requireContext(),
                    RecyclerView.HORIZONTAL,
                    false
                )
                adapter = totalSpendingsAdapter
            }
            recentRv.apply {
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                    requireContext(),
                    RecyclerView.VERTICAL,
                    false
                )
                adapter = recentAdapter
            }
        }
        recentAdapter?.onClick = {
            if(AdsCounter.showAd()) {
                MyApplication.mInstance.adsManager.loadInterstitialAd(requireActivity()){
                    mainViewModel.clickOnRecentTransaction?.invoke(it)
                }
            } else {
            mainViewModel.clickOnRecentTransaction?.invoke(it)
            }
        }
    }
    private fun loadAds() {
    MyApplication.mInstance.adsManager.showBanner(requireContext(), AdSize.LARGE_BANNER,binding.adView,this.getString(R.string.ADMOB_BANNER_V2),binding.shimmerLayout);
    }
    private fun performNavigation() {
        mainViewModel.onNavItemSelected = {
            when (it) {
                1 -> {}
                2 -> {
                    safeNavigate(R.id.action_homeFragment_to_transactionsFragment, R.id.homeFragment)
                }
                3 -> {
                    safeNavigate(R.id.action_homeFragment_to_analysisFragment, R.id.homeFragment)
                }
                4 -> {
                    safeNavigate(R.id.action_homeFragment_to_budgetFragment, R.id.homeFragment)
                }
            }
        }
    }

    private fun calculateSpending(transactions: ArrayList<Transaction>) {
        lifecycleScope.launch(Dispatchers.IO) {
            transactions.reverse()
            val categoryTotalTransactions = ArrayList<Transaction>()
            val categoryTotals = mutableMapOf<String, Pair<Float, String>>()
            for (transaction in transactions) {
                val category = transaction.category
                val amount = transaction.amount
                val date = transaction.date

                // Update the total amount and date for the category
                if (categoryTotals.containsKey(category)) {
                    val (totalAmount, earliestDate) = categoryTotals[category]!!
                    categoryTotals[category] = Pair(totalAmount + amount, minOf(earliestDate, date))
                } else {
                    categoryTotals[category] = Pair(amount, date)
                }
            }

            for ((category, pair) in categoryTotals){
                val (totalAmount, earliestDate) = pair

                // Create a new Transaction object with the category, total amount, and earliest date
                val categoryTotalTransaction = Transaction(totalAmount, "Total", earliestDate, "", category)

                // Add the new Transaction object to the list
                categoryTotalTransactions.add(categoryTotalTransaction)
            }
            println("Category Total Transactions:")
            for (transaction in categoryTotalTransactions) {
                println("${transaction.category}: ${transaction.amount}, Date: ${transaction.date}")
            }
            if(categoryTotalTransactions.isNotEmpty()){
                withContext(Dispatchers.Main){
                    binding.noItemLayout.gone()
                }
            }
            withContext(Dispatchers.Main){
            totalSpendingsAdapter?.updateList(categoryTotalTransactions)
            }
        }
    }
    private fun showRatingDialog() {
        if (MyApplication.mInstance.preferenceManager.getBoolean(
                PreferenceManager.Key.SHOW_RATING_DIALOG,
                true
            )
        ) {
            if(AdsCounter.isShowRatting()){
                openRatingBottomSheet()
            }
        }
    }
    private fun openRatingBottomSheet() {
        val bottomSheetFragment = RatingBottomSheetFragment()
        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }

    private fun showConsent() {
        Log.d(TAG, "Google Mobile Ads SDK Version: " + MobileAds.getVersion())

        googleMobileAdsConsentManager =
            GoogleMobileAdsConsentManager.getInstance(requireContext())
        googleMobileAdsConsentManager.gatherConsent(
            requireActivity()
        ) { consentError ->
            if (consentError != null) {
                // Consent not obtained in current session.
                Log.w(
                    TAG,
                    String.format(
                        "%s: %s",
                        consentError.errorCode,
                        consentError.message
                    )
                )
            }
            if (googleMobileAdsConsentManager.canRequestAds()) {
                initializeMobileAdsSdk()
            }
            if (googleMobileAdsConsentManager.isPrivacyOptionsRequired) {
                // Regenerate the options menu to include a privacy setting.
                requireActivity().invalidateOptionsMenu()
            }
        }

        if (googleMobileAdsConsentManager.canRequestAds()) {
            initializeMobileAdsSdk()
        }
    }

    private fun initializeMobileAdsSdk() {
        Log.d(TAG, "initializeMobileAdsSdk: call ")
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }
        Log.d(TAG, "initializeMobileAdsSdk: App instance")
        // Initialize the Mobile Ads SDK.
        MyApplication.mInstance.adsManager.initSDK(requireContext()) {
            Log.d(TAG, "initializeMobileAdsSdk: successful")
            // call here
            loadAds()
        }
    }
}
