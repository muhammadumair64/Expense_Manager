package com.iobits.budgetexpensemanager.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iobits.budgetexpensemanager.databinding.ItemTransactionBinding
import com.iobits.budgetexpensemanager.ui.dataModels.TransactionFilter
import com.iobits.budgetexpensemanager.utils.IconHelper
import com.iobits.budgetexpensemanager.utils.K
import com.iobits.budgetexpensemanager.utils.gone
import com.iobits.budgetexpensemanager.utils.invisible
import com.iobits.budgetexpensemanager.utils.visible
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale


class TransactionFilterAdapter(
    private val context: Context,
) : RecyclerView.Adapter<TransactionFilterAdapter.MyHolder>() {

    var onClick: ((position: Int) -> Unit)? = null
    private var transactionList = ArrayList<TransactionFilter>()
    val TAG = "RecentTransactionTag"


    class MyHolder(val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(ItemTransactionBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyHolder, @SuppressLint("RecyclerView") position: Int) {

        if(position == 0){
            holder.binding.sortDate.apply {
                text = transactionList[position].date
                visible()
                Log.d(TAG, "onBindViewHolder: $position === point 1")
            }
        }else{
            if(transactionList[position].date == transactionList[position-1].date){
                holder.binding.sortDate.gone()
            }else{
                holder.binding.sortDate.apply {
                    text = transactionList[position].date
                    visible()
                    Log.d(TAG, "onBindViewHolder: $position === point 2")
                }
            }
        }

        Log.d(TAG, "onBindViewHolder: Transaction Date ${transactionList[position].date}")
        holder.binding.apply {
            title.text = transactionList[position].category
            totalAmount.text = "${K.SYMBOL} ${transactionList[position].amount}"
            icon.setImageResource(IconHelper.iconChooser3d(transactionList[position].category))
            val transactionDate = transactionList[position].itemDate.split(" ")
            date.text = transactionDate[0]
        }
    }

    fun updateList(mlist: ArrayList<TransactionFilter>?,sortType:Int) {
        transactionList.clear()
        try {
            when (sortType){
                1->{if (mlist != null) {
                    // Sort the list by date
                    mlist.sortWith(compareByDescending<TransactionFilter> { it.date.substring(7, 11).toInt() }
                        .thenByDescending { getMonthIndex(it.date.substring(3, 6)) }
                        .thenByDescending { it.date.substring(0, 2).toInt() })
                    // Update transactionList
                    transactionList.addAll(mlist)
                }}
                2->{if (mlist != null) {
                    // Sort the list by date
                    mlist.sortWith(compareByDescending<TransactionFilter> { it.date.substring(7, 11).toInt() }
                        .thenByDescending { getMonthIndex(it.date.substring(3, 6)) }
                        .thenByDescending { it.date.substring(0, 2).toInt() })
                    // Update transactionList
                    transactionList.addAll(mlist)
                }}
                3->{if (mlist != null) {
                    mlist.sortBy { it.date }
                    mlist.reverse()
                    transactionList.addAll(mlist)
                }}
                4->{if (mlist != null) {
                    mlist.sortBy { it.date }
                    mlist.reverse()
                    transactionList.addAll(mlist)
                }}
            }
            notifyDataSetChanged()
        }catch (e:Exception){
            e.localizedMessage
        }

    }

    override fun getItemCount(): Int {
        return transactionList.size
    }
    private fun getMonthIndex(month: String): Int {
        return when (month) {
            "Jan" -> 1
            "Feb" -> 2
            "Mar" -> 3
            "Apr" -> 4
            "May" -> 5
            "Jun" -> 6
            "Jul" -> 7
            "Aug" -> 8
            "Sep" -> 9
            "Oct" -> 10
            "Nov" -> 11
            "Dec" -> 12
            else -> 0 // Default case, should never happen with valid date inputs
        }
    }
}
