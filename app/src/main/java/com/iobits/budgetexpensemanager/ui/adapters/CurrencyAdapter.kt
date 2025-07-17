package com.iobits.budgetexpensemanager.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import androidx.recyclerview.widget.RecyclerView
import com.iobits.budgetexpensemanager.databinding.ItemCurrencyBinding
import com.iobits.budgetexpensemanager.ui.dataModels.MoneySymbol



class CurrencyAdapter(
    private val context: Context,private var currencyList: ArrayList<MoneySymbol>
) : RecyclerView.Adapter<CurrencyAdapter.MyHolder>() {

    var onClick: ((item: MoneySymbol) -> Unit)? = null
    private var mCurrencyList : MutableList<MoneySymbol> = currencyList
    private var mCurrencyListFiltered : MutableList<MoneySymbol> = currencyList

    class MyHolder(val binding: ItemCurrencyBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(ItemCurrencyBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.binding.apply {
            countryName.text = mCurrencyListFiltered[position].name
            countrySymbol.text = mCurrencyListFiltered[position].symbols
            Log.d("CurrencyAdapterTag", "onBindViewHolder:${position}")
            root.setOnClickListener {
                onClick?.invoke(mCurrencyListFiltered[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return mCurrencyListFiltered.size
    }

    fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    mCurrencyList.sortBy {
                        it.name.toLowerCase()
                    }
                    mCurrencyListFiltered = mCurrencyList
                } else {
                    val filteredList: MutableList<MoneySymbol> = ArrayList()
                    for (row in mCurrencyList) {
                        if (row.name.toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row)
                        }

                        mCurrencyListFiltered = filteredList
                    }
                }

                val filterResults = FilterResults()

                mCurrencyListFiltered.sortBy {
                    it.name
                }

                filterResults.values = mCurrencyListFiltered


                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {

                @Suppress("UNCHECKED_CAST")
                try {
                    mCurrencyListFiltered = filterResults.values as MutableList<MoneySymbol>
                    // refresh the list with filtered data
                    notifyDataSetChanged()
                }catch (e:Exception){
                    e.localizedMessage
                }
            }
        }
    }
}
