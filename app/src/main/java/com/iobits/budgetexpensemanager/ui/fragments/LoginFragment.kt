package com.iobits.budgetexpensemanager.ui.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.firebase.auth.FirebaseAuth
import com.iobits.budgetexpensemanager.R
import com.iobits.budgetexpensemanager.databinding.FragmentLoginBinding
import com.iobits.budgetexpensemanager.managers.PreferenceManager
import com.iobits.budgetexpensemanager.myApplication.MyApplication
import com.iobits.budgetexpensemanager.ui.viewModels.AuthViewModel
import com.iobits.budgetexpensemanager.utils.GoogleSignInHelper
import com.iobits.budgetexpensemanager.utils.K
import com.iobits.budgetexpensemanager.utils.TinyDB
import com.iobits.budgetexpensemanager.utils.disableMultipleClicking
import com.iobits.budgetexpensemanager.utils.gone
import com.iobits.budgetexpensemanager.utils.handleBackPress
import com.iobits.budgetexpensemanager.utils.safeNavigate
import com.iobits.budgetexpensemanager.utils.visible


class LoginFragment : Fragment() {
    private var mAuth: FirebaseAuth? = null
    private val binding by lazy {
        FragmentLoginBinding.inflate(layoutInflater)
    }
    private val authViewModel : AuthViewModel by activityViewModels()
    private var tinyDB : TinyDB? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
         mAuth = FirebaseAuth.getInstance();
         tinyDB = TinyDB(requireContext())
         initViews()
         authViewModel.startLoading?.invoke(false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
       handleBackPress {
            requireActivity().moveTaskToBack(true)
        }
    }
    private fun initViews(){
        val gLoginCheck =  MyApplication.mInstance.preferenceManager.getBoolean(
            PreferenceManager.Key.REMOTE_CONFIG_IS_GL_ENABLE,
            false
        )

        binding.apply {
            if(!gLoginCheck){
                textView7.gone()
                googleSignIn.gone()
            }else{
                textView7.visible()
                googleSignIn.visible()
            }
            signIn.setOnClickListener{
            if(authViewModel.isValidEmail(binding.email.text)){
                mAuth.let {
                    if (it != null) {
                        authViewModel.startLoading?.invoke(true)
                        authViewModel.signIn(it,binding.email.text.toString(),binding.email.text.toString(),requireContext(),requireActivity())
                    }
                }
            }
            else{
                binding.email.error = getString(R.string.invalid_email)
                binding.password.error = getString(R.string.invalid_password)
            }
            }
            forgotPassword.setOnClickListener {
                safeNavigate(R.id.action_loginFragment_to_forgotPasswordFragment,R.id.loginFragment)
            }
            googleSignIn.setOnClickListener {
                disableMultipleClicking(it)
               GoogleSignInHelper.onStart?.invoke()
            }
            signUp.setOnClickListener {
                safeNavigate(R.id.action_loginFragment_to_signUpFragment,R.id.loginFragment)
            }
            skip.setOnClickListener {
                tinyDB?.putBoolean(K.IsLoginSkipped,true)
                safeNavigate(R.id.action_loginFragment_to_createAccountFragment,R.id.loginFragment)
            }
            /** Google Login Response */
            GoogleSignInHelper.result = { it , email ->
                if(it){
                    authViewModel.email = email
                    Toast.makeText(activity, "Google sign in successful!", Toast.LENGTH_SHORT) .show()
                  //  safeNavigate(R.id.action_loginFragment_to_dashboardFragment,R.id.loginFragment)
                    mAuth?.uid?.let { it1 -> authViewModel.getProfileFromCloud(it1) }
                } else {
                    Toast.makeText(activity, "Google sign in failed!", Toast.LENGTH_SHORT) .show()
                }
            }
        }
        authViewModel.apply {
            result = {
                if(it){
                    safeNavigate(R.id.action_loginFragment_to_premiumFragment,R.id.loginFragment)
                }
                authViewModel.startLoading?.invoke(false)
            }
            createAccount = {
                if(it){
                    safeNavigate(R.id.action_loginFragment_to_createAccountFragment,R.id.loginFragment)
                }
                authViewModel.startLoading?.invoke(false)
            }
        }
    }
}
