package com.example.falldetection

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip

class ChipAdapter<T> (private val dataList: MutableList<T>): RecyclerView.Adapter<ChipAdapter<T>.ChipViewHolder>() where T: Any{
    val storeData = StoreData()
    private lateinit var context : Context
    inner class ChipViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val chip: Chip = itemView.findViewById(R.id.chip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chip_template, parent, false)
        return ChipViewHolder(view)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
        val item = dataList[position]

        Log.d(TAG, "Binding item at position $position: $item")

        when(item){
            is DataFall -> {
                holder.chip.text = item.fecha
            }
            is DataContact -> {
                holder.chip.text = item.name
            }
            else -> {
                Log.e(TAG, "Unexpected item type: ${item.javaClass}")
            }
        }

        holder.chip.setOnClickListener {
            dataList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, dataList.size)

            try{
                storeData.saveJson(context, dataList as MutableList<out Any>, when(item){
                    is DataFall -> "falls"
                    is DataContact -> "contacts"
                    else -> ""
                })
            }catch(e: Exception){
                Log.e(TAG, "Error saving data: ${e.message}")
            }
        }
    }

    fun updateData(newDataList: List<T>) {
        dataList.clear()
        dataList.addAll(newDataList)
        notifyDataSetChanged()
    }
}