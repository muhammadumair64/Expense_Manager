package com.iobits.budgetexpensemanager.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iobits.budgetexpensemanager.databinding.ItemRecentHomeBinding
import com.iobits.budgetexpensemanager.ui.dataModels.Transaction
import com.iobits.budgetexpensemanager.utils.IconHelper
import com.iobits.budgetexpensemanager.utils.K


class RecentAdapter(
    private val context: Context,
) : RecyclerView.Adapter<RecentAdapter.MyHolder>() {

    var onClick: ((position: Transaction) -> Unit)? = null
    private var transactionList = ArrayList<Transaction>()


    class MyHolder(val binding: ItemRecentHomeBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(ItemRecentHomeBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyHolder, @SuppressLint("RecyclerView") position: Int) {

        holder.binding.apply {
            title.text = transactionList[position].category
            totalAmount.text = "${K.SYMBOL} ${transactionList[position].amount}"
            icon.setImageResource(IconHelper.iconChooser3d(transactionList[position].category))
            val transactionDate = transactionList[position].date.split(" ")
            date.text = transactionDate[0]
        }
        holder.itemView.setOnClickListener {
            onClick?.invoke(transactionList[position])
        }
    }

    fun updateList(mlist: ArrayList<Transaction>?) {
        mlist?.reverse()
        transactionList.clear()
        if (mlist != null) {
            transactionList.addAll(mlist)
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }
}
