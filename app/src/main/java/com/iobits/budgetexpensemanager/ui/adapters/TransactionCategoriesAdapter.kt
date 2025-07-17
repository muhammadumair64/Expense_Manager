package com.iobits.budgetexpensemanager.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iobits.budgetexpensemanager.databinding.ItemsAddTransactionsBinding
import com.iobits.budgetexpensemanager.managers.PreferenceManager
import com.iobits.budgetexpensemanager.myApplication.MyApplication
import com.iobits.budgetexpensemanager.ui.dataModels.AddTransactionCategory
import com.iobits.budgetexpensemanager.utils.IconHelper
import com.iobits.budgetexpensemanager.utils.K
import com.iobits.budgetexpensemanager.utils.gone
import com.iobits.budgetexpensemanager.utils.invisible
import com.iobits.budgetexpensemanager.utils.visible


class TransactionCategoriesAdapter(
    private val context: Context,
) : RecyclerView.Adapter<TransactionCategoriesAdapter.MyHolder>() {

    var onClick: ((item: String) -> Unit)? = null
    private var transactionList = ArrayList<String>()
    val TAG = "TransactionCategoriesAdapter"

    class MyHolder(val binding: ItemsAddTransactionsBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(
            ItemsAddTransactionsBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyHolder, @SuppressLint("RecyclerView") position: Int) {
        /**
         * Use glide to load Thumbnails in Rv
         */
        if (transactionList.isNotEmpty() && position >= 0) {
            try {
                Glide.with(context)
                    .asBitmap()
                    .load(IconHelper.iconChooser(transactionList[position]))
                    .into(holder.binding.imageView2)
            } catch (e: Exception) {
                e.localizedMessage
            }
            if (K.categoryColors.contains(transactionList[position])) {
                holder.binding.itemCard.setCardBackgroundColor(android.graphics.Color.parseColor(K.categoryColors[transactionList[position]]))
            } else {
                holder.binding.itemCard.setCardBackgroundColor(android.graphics.Color.parseColor(K.categoryColors[K.CUSTOM]))
            }

            holder.binding.title.text = transactionList[position]

            if (transactionList[position] == K.AddNew) {
                holder.binding.pro.visible()
                if (MyApplication.mInstance.preferenceManager.getBoolean(
                        PreferenceManager.Key.IS_APP_PREMIUM,
                        false
                    )
                ){
                    holder.binding.pro.gone()
                }
                holder.binding.title.invisible()

                holder.binding.itemCard.cardElevation = 0f
                val paddingInDp = 8 // 5dp
                val density = context.resources.displayMetrics.density
                val paddingInPx = (paddingInDp * density).toInt()
                holder.binding.imageView2.setPadding(
                    paddingInPx,
                    paddingInPx,
                    paddingInPx,
                    paddingInPx
                )
                holder.binding.addNewItem.visible()
            } else {
                holder.binding.pro.gone()
                holder.binding.title.visible()
                val paddingInDp = 0 // 5dp
                val density = context.resources.displayMetrics.density
                val paddingInPx = (paddingInDp * density).toInt()
                holder.binding.imageView2.setPadding(
                    paddingInPx,
                    paddingInPx,
                    paddingInPx,
                    paddingInPx
                )
                holder.binding.addNewItem.invisible()

            }

            holder.itemView.setOnClickListener {
                try {
                    onClick?.invoke(transactionList[position])
                } catch (e: Exception){
                    e.localizedMessage
                }
            }
        }
    }

    fun updateList(mlist: ArrayList<String>) {
        transactionList.clear()
        transactionList.addAll(mlist)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }
}
