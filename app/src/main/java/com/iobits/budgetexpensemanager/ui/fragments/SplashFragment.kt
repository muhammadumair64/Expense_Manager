package com.iobits.budgetexpensemanager.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.iobits.budgetexpensemanager.R
import com.iobits.budgetexpensemanager.databinding.FragmentSplashBinding
import com.iobits.budgetexpensemanager.managers.PreferenceManager
import com.iobits.budgetexpensemanager.myApplication.MyApplication
import com.iobits.budgetexpensemanager.ui.viewModels.AuthViewModel
import com.iobits.budgetexpensemanager.ui.viewModels.DataShareViewModel
import com.iobits.budgetexpensemanager.ui.viewModels.MainViewModel
import com.iobits.budgetexpensemanager.utils.K
import com.iobits.budgetexpensemanager.utils.TinyDB
import com.iobits.budgetexpensemanager.utils.clearBackStack
import com.iobits.budgetexpensemanager.utils.safeNavigate
import com.iobits.budgetexpensemanager.utils.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashFragment : Fragment() {
    private val binding by lazy {
        FragmentSplashBinding.inflate(layoutInflater)
    }
    private val authViewModel: AuthViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val dataShareViewModel :DataShareViewModel by activityViewModels()
    private var isProfileExist = false
    var tinyDB : TinyDB? = null
    val TAG ="SplashFragmentTAG"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        tinyDB =  TinyDB(requireContext())
        initViews()
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun initViews() {

        authViewModel.apply {
            getProfileFromLocalDb()
            profileData.observe(viewLifecycleOwner, Observer { it ->
                if (it != null) {
                    isProfileExist = true
                    /**  sync data */
                    if(tinyDB?.getBoolean(K.IsLoginSkipped) == false){
                    mainViewModel.getAccount().observe(viewLifecycleOwner, Observer { account ->
                        if (account != null && account.transactions.isNotEmpty()) {
                            mainViewModel.updateAccountOnFirebase(account,requireContext())
                        }
                    })
                    mainViewModel.getCategories().observe(viewLifecycleOwner, Observer {
                        if (it!= null) {
                            mainViewModel.uploadCategories(it)
                        }
                    })
                    }
                }
            })
        }
        initOpenAd()
    }


    private fun initOpenAd() {
        lifecycleScope.launch() {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                delay(5000)
                 openMainActivity()
            }
        }
    }

    private fun showNextBtn(){
        binding.getStart.visible()
        binding.nextBtn.setOnClickListener {
            MyApplication.mInstance.adsManager.showInterstitialAdWithoutLoading(requireActivity()){
                openMainActivity()
            }
        }
    }

    private fun openMainActivity() {
        if (isProfileExist) {
            safeNavigate(
                R.id.action_splashFragment_to_premiumFragment,
                R.id.splashFragment
            )
//            clearBackStack(R.id.dashboardFragment)
        } else {
            safeNavigate(R.id.action_splashFragment_to_loginFragment, R.id.splashFragment)
//            clearBackStack(R.id.loginFragment)
        }
    }
}
