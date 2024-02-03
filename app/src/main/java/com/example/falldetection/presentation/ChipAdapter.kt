package com.example.falldetection.presentation

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.falldetection.R
import com.google.android.material.chip.Chip

class ChipAdapter (private val dataList: MutableList<DataFall>): RecyclerView.Adapter<ChipAdapter.ChipViewHolder>(){
    val storeFall = StoreFall()
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
        val dataFall = dataList[position]
        holder.chip.text = dataFall.fecha

        holder.chip.setOnClickListener {
            dataList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, dataList.size)

            try{
                storeFall.saveJson(context, dataList)
            }catch(e: Exception){
                Log.e(TAG, "Error saving data: ${e.message}")
            }
        }
    }
}