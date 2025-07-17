package com.iobits.budgetexpensemanager.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.iobits.budgetexpensemanager.R
import com.iobits.budgetexpensemanager.databinding.FragmentCategoryAnalysisBinding
import com.iobits.budgetexpensemanager.ui.viewModels.MainViewModel

class CategoryAnalysisFragment : Fragment() {

    val binding by lazy {
        FragmentCategoryAnalysisBinding.inflate(layoutInflater)
    }

    val mainViewModel : MainViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {



        return binding.root
    }

    fun initViews(){


    }
}
