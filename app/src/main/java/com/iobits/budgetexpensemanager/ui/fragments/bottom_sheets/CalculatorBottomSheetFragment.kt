package com.iobits.budgetexpensemanager.ui.fragments.bottom_sheets

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iobits.budgetexpensemanager.R
import com.iobits.budgetexpensemanager.databinding.ItemCalculatorBottomSheetBinding
import com.iobits.budgetexpensemanager.ui.dataModels.Transaction
import com.iobits.budgetexpensemanager.ui.viewModels.MainViewModel
import com.iobits.budgetexpensemanager.utils.K
import com.iobits.budgetexpensemanager.utils.getCurrentDateTime
import org.mariuszgromada.math.mxparser.Expression
import java.text.DecimalFormat

class CalculatorBottomSheetFragment : BottomSheetDialogFragment() {
    val binding by lazy {
        ItemCalculatorBottomSheetBinding.inflate(layoutInflater)
    }
    private val mainViewModel: MainViewModel by activityViewModels()
    var value : ((String)->Unit)? = null
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
          if(mainViewModel.enteredAmount != 0){
              binding.input.text = mainViewModel.enteredAmount.toString()
          }
            btn0.setOnClickListener {
                  inputNumber("0")
            }
            btn1.setOnClickListener {
                  inputNumber("1")
            }
            btn2.setOnClickListener {
                  inputNumber("2")
            }
            btn3.setOnClickListener {
                  inputNumber("3")
            }
            btn4.setOnClickListener {
                  inputNumber("4")
            }
            btn5.setOnClickListener {
                  inputNumber("5")
            }
            btn6.setOnClickListener {
                  inputNumber("6")
            }
            btn7.setOnClickListener {
                  inputNumber("7")
            }
            btn8.setOnClickListener {
                inputNumber("8")
            }
            btn9.setOnClickListener {
                inputNumber("9")
            }
            btnDot.setOnClickListener {
                inputNumber(".")
            }
            btnPlus.setOnClickListener {
                inputNumber("+")
            }
            btnMin.setOnClickListener {
                inputNumber("-")
            }
            btnMulti.setOnClickListener {
                inputNumber("*")
            }
            btnDiv.setOnClickListener {
                inputNumber("/")
            }
            btnClear.setOnClickListener {
                binding.input.text = ""
            }
            btnEqual.setOnClickListener {
                showResult()
            }
            addAmount.setOnClickListener {
                showResult()
                if(input.text != "ERROR" && input.length() <= 9){

                    value?.invoke(input.text.trim().toString())
                    dismiss()
                }else{
                    binding.input.text = "ERROR"
                }
            }
            close.setOnClickListener {
                dismiss()
            }
        }
    }

    private fun inputNumber(value :String){
        if( binding.input.text =="ERROR") {
            binding.input.text = ""
        }
        binding.input.text = binding.input.text.toString() + value
    }

    private fun getInputExpression(): String {
        var expression = binding.input.text.replace(Regex("÷"), "/")
        expression = expression.replace(Regex("×"), "*")
        return expression
    }

    private fun showResult() {
        binding.apply {
            try {
                val expression = getInputExpression()
                val result = Expression(expression).calculate()
                if (result.isNaN()) {
                    // Show Error Message
                    input.text = "ERROR"

                } else {
                    // Show Result
                    input.text = DecimalFormat("0.######").format(result).toString()
                }
            } catch (e: Exception) {
                // Show Error Message
                input.text = "ERROR"
            }
        }
    }
}
