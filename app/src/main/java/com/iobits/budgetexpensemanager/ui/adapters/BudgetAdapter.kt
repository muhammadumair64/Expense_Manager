package com.iobits.budgetexpensemanager.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iobits.budgetexpensemanager.databinding.ItemBudgetLayoutBinding
import com.iobits.budgetexpensemanager.managers.PreferenceManager
import com.iobits.budgetexpensemanager.myApplication.MyApplication
import com.iobits.budgetexpensemanager.ui.dataModels.BudgetCategory
import com.iobits.budgetexpensemanager.utils.IconHelper
import com.iobits.budgetexpensemanager.utils.K
import com.iobits.budgetexpensemanager.utils.gone
import com.iobits.budgetexpensemanager.utils.invisible
import com.iobits.budgetexpensemanager.utils.visible
import org.mariuszgromada.math.mxparser.Expression


class BudgetAdapter(
    private val context: Context,
) : RecyclerView.Adapter<BudgetAdapter.MyHolder>() {

    var onClick: ((item: BudgetCategory) -> Unit)? = null
    var onClickAddMore: (() -> Unit)? = null
    private var budgetCategoryArrayList = ArrayList<BudgetCategory>()
    val TAG = "BudgetAdapterT"

    class MyHolder(val binding: ItemBudgetLayoutBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(
            ItemBudgetLayoutBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyHolder, @SuppressLint("RecyclerView") position: Int) {

        if (position == budgetCategoryArrayList.size - 1) {
            holder.binding.addMore.visible()
            holder.binding.addMore.setOnClickListener {
                onClickAddMore?.invoke()
            }
        } else {
            holder.binding.addMore.gone()
        }
        if(budgetCategoryArrayList.size > 1) {
            if(!MyApplication.mInstance.preferenceManager.getBoolean(
                    PreferenceManager.Key.IS_APP_PREMIUM,
                    false
                )
            ){
                holder.binding.crown.visible()
            }
        }
        holder.binding.apply {
            title.text = budgetCategoryArrayList[position].category
//          itemCard.setCardBackgroundColor(android.graphics.Color.parseColor(K.categoryColors[transactionList[position].category]))
            usedAmount.text = "${K.SYMBOL} ${budgetCategoryArrayList[position].currentAmount}"
            totalAmount.text = "${K.SYMBOL} ${budgetCategoryArrayList[position].totalAmount}"
            icon.setImageResource(IconHelper.iconChooser3d(budgetCategoryArrayList[position].category))
            val budgetDate = budgetCategoryArrayList[position].date.split(" ")
            date.text = budgetDate[0]
            Log.d(
                TAG,
                "onBindViewHolder: amounts ${budgetCategoryArrayList[position].currentAmount} === ${budgetCategoryArrayList[position].totalAmount}"
            )
            val total = budgetCategoryArrayList[position].totalAmount
            val current = budgetCategoryArrayList[position].currentAmount
            val value = current.toFloat() / total.toFloat()
            val avg = value * 100
            Log.d(TAG, "onBindViewHolder: avg $avg")
            if (avg > 100) {
                circularProgressBar.progress = 100f
            } else {
                circularProgressBar.progress = avg.toFloat()
            }
        }
        holder.itemView.setOnClickListener {
            onClick?.invoke(budgetCategoryArrayList[position])
        }
    }

    fun updateList(mlist: ArrayList<BudgetCategory>) {
        budgetCategoryArrayList.clear()
        budgetCategoryArrayList.addAll(mlist)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return budgetCategoryArrayList.size
    }
}
