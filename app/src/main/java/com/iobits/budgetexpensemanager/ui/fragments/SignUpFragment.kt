package com.iobits.budgetexpensemanager.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.firebase.auth.FirebaseAuth
import com.iobits.budgetexpensemanager.R
import com.iobits.budgetexpensemanager.databinding.FragmentSignUpBinding
import com.iobits.budgetexpensemanager.ui.viewModels.AuthViewModel
import com.iobits.budgetexpensemanager.utils.K
import com.iobits.budgetexpensemanager.utils.TinyDB
import com.iobits.budgetexpensemanager.utils.safeNavigate

class SignUpFragment : Fragment() {
    private var mAuth: FirebaseAuth? = null
    private val binding by lazy {
        FragmentSignUpBinding.inflate(layoutInflater)
    }
    var tinyDB : TinyDB ? = null
    private val authViewModel : AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
         tinyDB = TinyDB(requireContext())
        mAuth = FirebaseAuth.getInstance();

        initViews()
        return binding.root
    }

    private fun initViews(){
        binding.apply {
            signUp.setOnClickListener{

                if(authViewModel.isValidEmail(binding.email.text)){
                    mAuth.let {
                        if (it != null) {
                            authViewModel.startLoading?.invoke(true)
                            authViewModel.signUp(it,binding.email.text.toString(),binding.email.text.toString(),requireContext(),requireActivity())
                        }
                    }
                }
                else{
                    binding.email.error =    getString(R.string.invalid_email)
                    binding.password.error = getString(R.string.invalid_password)
                }
            }
            signIn.setOnClickListener {

                safeNavigate(R.id.action_signUpFragment_to_loginFragment,R.id.signUpFragment)
            }
            skip.setOnClickListener {
                tinyDB?.putBoolean(K.IsLoginSkipped,true)
                safeNavigate(R.id.action_signUpFragment_to_createAccountFragment,R.id.signUpFragment)
            }
        }
        authViewModel.result = {
            if(it){
                safeNavigate(R.id.action_signUpFragment_to_createAccountFragment,R.id.signUpFragment)
            }
            authViewModel.startLoading?.invoke(false)
        }
    }
}
