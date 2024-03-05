package com.example.falldetectionphone

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class FallDataAdapter(private val fallList: List<DataFall>): RecyclerView.Adapter<FallDataAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val fallDate: TextView = itemView.findViewById(R.id.fall_date)
        val fallTime: TextView = itemView.findViewById(R.id.fall_time)
        val fallLocationMap: MapView = itemView.findViewById(R.id.fall_location_map)
        val contactedNumbersList: ListView = itemView.findViewById(R.id.contacted_numbers_list)
    }

    private lateinit var mContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fall_item, parent, false)
        mContext = view.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fall = fallList[position]
        holder.fallDate.text = fall.fecha
        holder.fallTime.text = fall.hora

        holder.fallLocationMap.onCreate(null)
        holder.fallLocationMap.getMapAsync{ googleMap ->
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(fall.latitude, fall.longitude), 15f))
            val marker = MarkerOptions().position(LatLng(fall.latitude, fall.longitude))
            googleMap.addMarker(marker)
        }

        val contactAdapter = fall.contacts?.let {
            ArrayAdapter<String>(
                mContext,
                android.R.layout.simple_list_item_1,
                it.map { it.name + " - " + it.number}
            )
        }
        holder.contactedNumbersList.adapter = contactAdapter

    }

    override fun getItemCount(): Int = fallList.size
}