package com.example.falldetection.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.falldetection.R
import com.google.android.material.chip.Chip

class ChipAdapter (private val dataList: MutableList<DataFall>): RecyclerView.Adapter<ChipAdapter.ChipViewHolder>(){

    inner class ChipViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val chip: Chip = itemView.findViewById(R.id.chip)
        //val deleteButton: ShapeableImageView = itemView.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chip_template, parent, false)
        return ChipViewHolder(view)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
        val dataFall = dataList[position]
        holder.chip.text = dataFall.fecha

        /*
        holder.deleteButton.setOnClickListener {
            dataList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemChanged(position, dataList.size)
        }
         */
    }
}