package com.iobits.budgetexpensemanager.ui.fragments.bottom_sheets

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iobits.budgetexpensemanager.R
import com.iobits.budgetexpensemanager.databinding.ItemCalculatorBottomSheetBinding
import com.iobits.budgetexpensemanager.databinding.ItemCreateCategoryBottomSheetBinding
import com.iobits.budgetexpensemanager.ui.dataModels.Transaction
import com.iobits.budgetexpensemanager.utils.K
import com.iobits.budgetexpensemanager.utils.getCurrentDateTime
import org.mariuszgromada.math.mxparser.Expression
import java.text.DecimalFormat

class CategoryBottomSheetFragment : BottomSheetDialogFragment() {
    val binding by lazy {
        ItemCreateCategoryBottomSheetBinding.inflate(layoutInflater)
    }
    var name : ((String)->Unit)? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View  {
         initListeners()
        return binding.root
    }

    private fun initListeners() {
        binding.apply {
            addCategory.setOnClickListener{
                if(nameEt.text.length >= 3){
                    name?.invoke(nameEt.text.toString())
                    dismiss()
                }else{
                    Toast.makeText(requireContext(), "Enter valid name", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
