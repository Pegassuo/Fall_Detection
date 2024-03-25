package com.example.falldetection

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ListView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class FallDataAdapter(private var fallList: List<DataFall>): RecyclerView.Adapter<FallDataAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val fallDate: TextView = itemView.findViewById(R.id.fall_date)
        val fallTime: TextView = itemView.findViewById(R.id.fall_time)
        val gpsText: TextView = itemView.findViewById(R.id.gps_text)
        val gpsFrame: FrameLayout = itemView.findViewById(R.id.gps_frame)
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

        if(fall.latitude == 0.0 && fall.longitude == 0.0){
            holder.gpsText.visibility = View.VISIBLE
            holder.gpsFrame.visibility = View.GONE
        }else{
            holder.fallLocationMap.onCreate(null)
            holder.fallLocationMap.getMapAsync{ googleMap ->
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(fall.latitude, fall.longitude), 15f))
                val marker = MarkerOptions().position(LatLng(fall.latitude, fall.longitude))
                googleMap.addMarker(marker)
            }
        }

        val contactAdapter = fall.contacts?.let {
            ArrayAdapter<String>(
                mContext,
                android.R.layout.simple_list_item_1,
                it.map { it.name + " - " + it.number}
            )
        }
        holder.contactedNumbersList.adapter = contactAdapter

        contactAdapter?.let { adapter ->
            val totalHeight = calculateListViewHeight(holder.contactedNumbersList, adapter)
            val params = holder.contactedNumbersList.layoutParams
            params.height = totalHeight
            holder.contactedNumbersList.layoutParams = params
        }

    }
    private fun calculateListViewHeight(listView: ListView, adapter: ArrayAdapter<String>): Int {
        var totalHeight = 0
        for (i in 0 until adapter.count) {
            val listItem = adapter.getView(i, null, listView)
            listItem.measure(0, 0)
            totalHeight += listItem.measuredHeight
        }
        val dividerHeight = listView.dividerHeight * (adapter.count - 1)
        return totalHeight + dividerHeight
    }
    fun updateData(newFallList: List<DataFall>){
        fallList = newFallList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = fallList.size
}