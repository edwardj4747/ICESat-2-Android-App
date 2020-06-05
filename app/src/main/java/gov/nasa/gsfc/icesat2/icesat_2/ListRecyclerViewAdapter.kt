package gov.nasa.gsfc.icesat2.icesat_2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView

//ViewHolder - storing references to the views involved to make this more efficient
class ListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var textViewDateTime: TextView = view.findViewById(R.id.textViewDateTime)
    var textViewLatLng: TextView = view.findViewById(R.id.textViewLatLng)
}

class ListRecyclerViewAdapter(private val allPoints: ArrayList<Point>) : RecyclerView.Adapter<ListViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.location_list_ticket, parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.textViewDateTime.text = allPoints[position].dateString
        holder.textViewLatLng.text = "${allPoints[position].latitude} N ${allPoints[position].longitude} E"
    }

    override fun getItemCount(): Int {
       return allPoints.size
    }
}