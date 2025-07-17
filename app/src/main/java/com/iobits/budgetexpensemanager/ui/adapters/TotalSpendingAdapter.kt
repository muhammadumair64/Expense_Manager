package com.iobits.budgetexpensemanager.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iobits.budgetexpensemanager.databinding.ItemsTotalSpendingTransactionsBinding
import com.iobits.budgetexpensemanager.ui.dataModels.Transaction
import com.iobits.budgetexpensemanager.utils.IconHelper
import com.iobits.budgetexpensemanager.utils.K


class TotalSpendingAdapter(
    private val context: Context,
) : RecyclerView.Adapter<TotalSpendingAdapter.MyHolder>() {

    var onClick: ((transaction: Transaction) -> Unit)? = null
    private var transactionList = ArrayList<Transaction>()
    val TAG = "RecentTransactionTag"

    class MyHolder(val binding: ItemsTotalSpendingTransactionsBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(ItemsTotalSpendingTransactionsBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyHolder, @SuppressLint("RecyclerView") position: Int) {

        /**
         * Use glide to load Thumbnails in Rv
         */
       try {
//            Glide.with(context)
//                .asBitmap()
//                .load(differ.currentList[position].artUri)
//                .into(holder.binding.thumbNail)
        }
       catch (e: Exception) {
            e.localizedMessage
        }
        if(K.categoryColors.contains(transactionList[position].category)){
            holder.binding.itemCard.setCardBackgroundColor(android.graphics.Color.parseColor(K.categoryColors[transactionList[position].category]))
        }else{
            holder.binding.itemCard.setCardBackgroundColor(android.graphics.Color.parseColor(K.categoryColors[K.CUSTOM]))
        }
        holder.binding.apply {
            category.text = transactionList[position].category
//          itemCard.setCardBackgroundColor(android.graphics.Color.parseColor(K.categoryColors[transactionList[position].category]))
            amount.text = "${K.SYMBOL} ${transactionList[position].amount}"
            icon.setImageResource(IconHelper.iconChooser(transactionList[position].category))
        }

        holder.itemView.setOnClickListener {
            onClick?.invoke(transactionList[position])
        }
    }

    fun updateList(mlist: ArrayList<Transaction>?) {
        transactionList.clear()
        if (mlist != null) {
            mlist.reverse()
            transactionList.addAll(mlist)
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }
}
