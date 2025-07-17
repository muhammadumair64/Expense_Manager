package com.iobits.budgetexpensemanager.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.firebase.auth.FirebaseAuth
import com.iobits.budgetexpensemanager.R
import com.iobits.budgetexpensemanager.databinding.FragmentForgotPasswordBinding
import com.iobits.budgetexpensemanager.ui.viewModels.AuthViewModel
import com.iobits.budgetexpensemanager.utils.safeNavigate

class ForgotPasswordFragment : Fragment() {
    private var mAuth: FirebaseAuth? = null
    val binding by lazy {
        FragmentForgotPasswordBinding.inflate(layoutInflater)
    }
    private val authViewModel : AuthViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mAuth = FirebaseAuth.getInstance();
        initViews()
        // Inflate the layout for this fragment
        return binding.root
    }
    private fun initViews(){
        binding.apply {
            confirm.setOnClickListener{

                if(authViewModel.isValidEmail(binding.email.text)){
                    mAuth.let {
                        if (it != null) {
                            authViewModel.startLoading?.invoke(true)
                            mAuth?.let { it1 -> authViewModel.forgotPassword(it1,binding.email.text.toString(),requireContext(),requireActivity()) }
                        }
                    }
                }
                else{
                    binding.email.error = getString(R.string.invalid_email)
                }
            }
            signIn.setOnClickListener {
                safeNavigate(R.id.action_forgotPasswordFragment_to_loginFragment,R.id.forgotPasswordFragment)
            }
        }
        authViewModel.result = {
            if(it){
              safeNavigate(R.id.action_forgotPasswordFragment_to_loginFragment,R.id.forgotPasswordFragment)
            }
        }
    }
}