package com.iobits.budgetexpensemanager.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.iobits.budgetexpensemanager.R
import com.iobits.budgetexpensemanager.databinding.ItemTransactionTabSelectorBinding


class TransactionTabAdapter(
    private val context: Context,
) : RecyclerView.Adapter<TransactionTabAdapter.MyHolder>() {

    var onClick : ((position: Int) -> Unit)? = null
    private var tabList = ArrayList<String>()
    val TAG = "RecentTransactionTag"

    var selectorValue = 0

    class MyHolder(val binding : ItemTransactionTabSelectorBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(ItemTransactionTabSelectorBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.binding.Title.text = tabList[position]
        if(selectorValue == position){
            holder.binding.apply {
            selector.visibility = View.VISIBLE
            Title.setTextColor(ContextCompat.getColor(context,R.color.gradient_start))
            }
        } else {
            holder.binding.apply {
            selector.visibility = View.INVISIBLE
            Title.setTextColor(ContextCompat.getColor(context,R.color.light_Text))
            }
        }

        holder.binding.root.setOnClickListener {
            selectorValue = position
            notifyDataSetChanged()

            onClick?.invoke(position)
        }
    }

    fun updateList(mlist : ArrayList<String>?) {
        tabList.clear()
        if (mlist != null) {
            tabList.addAll(mlist)
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return tabList.size
    }
}
